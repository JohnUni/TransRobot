package au.innovation.network;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

import au.innovation.protocol.IDataMessage;
import au.innovation.utility.LogOutput;

/*
 * Copyright (c) 2015-2015, John Wong <john dot innovation dot au at gmail dot com>, all rights reserved.
 * http://www.bitranslator.com
 * 
 * AsyncClient.java
 * TransRobot
 * Created by John Wong on 23/09/2015.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *   * Redistributions of source code must retain the above copyright notice,
 *     this list of conditions and the following disclaimer.
 *   * Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 * Apple, the Apple logo, iPad, iPhone and iPod touch are trademarks of Apple Inc.
 * 
 * LEGO, the LEGO logo and MINDSTORMS are trademarks and/or copyrights of the LEGO Group.
 * 
 * All other trademarks, logos and copyrights are the property of their respective owners 
 * and are hereby acknowledged.
 * 
 */

public class AsyncClient implements ISendMessage {
	// raw data processor, with message handler in the processor
	protected IRawNetworkDataProcessor m_processor = null;

	// server ip_address, or domain
	protected String m_strServerIp = "127.0.0.1";
	// server port
	protected Integer m_nServerPort = 8090;

	// message buffer, might be from other thread
	protected ArrayList<IDataMessage> m_vecWaitingMessage = null;
	// serial id of message sent, not yet used currently
	protected int m_nLocalLamportClock = 1;

	// states of client
	protected final static int CLIENT_SOCKET_INIT = 0;
	protected final static int CLIENT_SOCKET_CONNECTING = 1;
	protected final static int CLIENT_SOCKET_CONNECTED = 2;
	protected final static int CLIENT_SOCKET_ERROR = 3;

	// current state of client socket
	protected int m_nClientStatus = CLIENT_SOCKET_INIT;

	// socket channel of client
	protected SocketChannel m_socketChannel = null;
	// selector for asynchronization
	protected Selector m_selector = null;

	// flag for some network error.
	protected boolean m_bWithNetworkError = false;

	// send & receive buffer
	//protected ByteBuffer m_byteSendBuffer = null;
	protected ByteBuffer m_byteRecvBuffer = null;
	protected Integer m_nBufferSize = 16 * 1024;

	// time record and some parameters for reconnection
	protected long m_nConnectBeginTime = -1;
	protected long m_nErrorOccurTime = -1;

	protected Integer m_nMaxWaitMilliSecond = 20;

	protected long m_nMaxWaitTimeAfterConnectBegin = 3 * 1000;
	protected long m_nMaxWaitTimeAfterErrorOccur = 5 * 1000;

	protected Integer m_nMaxWaitIdleMilliSecond = 20;
	
	protected boolean syncRemoteLamportClock( int nRemoteLamportClock )
	{
		boolean bRet = false;
		if( nRemoteLamportClock > m_nLocalLamportClock )
		{
			LogOutput.print("client sync with remote: remote="+nRemoteLamportClock+", local="+m_nLocalLamportClock+", final="+(nRemoteLamportClock+1)+"\n");
			m_nLocalLamportClock = nRemoteLamportClock; 
		}
		else if( nRemoteLamportClock < 0 )
		{
			LogOutput.print("client sync remote < 0? \n");
		}
		m_nLocalLamportClock++;
		bRet = true;
		return bRet;
	}
	
	public int getLocalLamportClock()
	{
		return m_nLocalLamportClock;
	}

	// this interface does not startup a real thread, the function stepNetwork
	// need to be called periodically.
	public boolean start(String strServerIp, Integer nServerPort, IMessageHandler handler, String strMode) {
		boolean bRet = false;

		m_strServerIp = strServerIp;
		m_nServerPort = nServerPort;
		
		try {
			m_byteRecvBuffer = ByteBuffer.allocate(m_nBufferSize);
		} catch (IllegalArgumentException e) {
			LogOutput.debug("not enough memory for recv buffer?\n");
		}
		
		m_vecWaitingMessage = new ArrayList<IDataMessage>();
		m_nClientStatus = CLIENT_SOCKET_INIT;

		if (strMode.equalsIgnoreCase("Robot")) {
			m_processor = new RawDataProcessorRobot(handler);
			LogOutput.debug("robot client handler based on client logic.\n");
		} else if (strMode.equalsIgnoreCase("RemoteController")) {
			m_processor = new RawDataProcessorRemoteController(handler);
			LogOutput.debug("remote controller handler based on client logic.\n");
		}

		if (m_processor != null) {
			// LogOutput.debug("remote handler on client.\n");
			bRet = true;
		}

		return bRet;
	}

	private boolean stepNetwork() {
		boolean bRet = false;

		LogOutput.debug("client step network!\n");

		switch (m_nClientStatus) {
		case CLIENT_SOCKET_INIT:
			LogOutput.debug("client init.\n");
			boolean bStartConnect = connectToServer();
			if (bStartConnect) {
				m_nConnectBeginTime = System.currentTimeMillis();
				bRet = true;
				m_nClientStatus = CLIENT_SOCKET_CONNECTING;
			} else {
				LogOutput.debug("client start connect error.\n");
				m_nConnectBeginTime = -1;
				m_nErrorOccurTime = System.currentTimeMillis();
				m_nClientStatus = CLIENT_SOCKET_ERROR;
			}
			break;
		case CLIENT_SOCKET_CONNECTING:
			// LogOutput.debug("client connecting.\n");
			boolean bWorking = CheckNetworkStillWorking();
			if (bWorking) {
				// LogOutput.debug("client connecting2.\n");
				if (m_nConnectBeginTime > 0) {
					long nCurrnetTime = System.currentTimeMillis();

					long nDiff = nCurrnetTime - m_nConnectBeginTime;
					if (nDiff >= m_nMaxWaitTimeAfterErrorOccur) {
						LogOutput.debug("client timeout1.\n");
						m_nConnectBeginTime = -1;
						m_nErrorOccurTime = System.currentTimeMillis();
						m_nClientStatus = CLIENT_SOCKET_ERROR;
						bRet = true;
					} else {
						// waiting
						// LogOutput.debug("client checking connection.\n");
						boolean bCheck = CheckConnecting();
						if (bCheck) {
							LogOutput.debug("client connected!\n");
							m_nClientStatus = CLIENT_SOCKET_CONNECTED;
							m_nConnectBeginTime = -1;
							bRet = true;
						} else {
							if (!CheckNetworkStillWorking()) {
								LogOutput.debug("client not work1.\n");
								m_nConnectBeginTime = -1;
								m_nErrorOccurTime = System.currentTimeMillis();
								m_nClientStatus = CLIENT_SOCKET_ERROR;
								bRet = true;
							}
						}
					}
				}
			} else {
				LogOutput.debug("client not work2.\n");
				m_nConnectBeginTime = -1;
				m_nErrorOccurTime = System.currentTimeMillis();
				m_nClientStatus = CLIENT_SOCKET_ERROR;
				bRet = true;
			}
			break;
		case CLIENT_SOCKET_CONNECTED:
			// LogOutput.debug("client connected.\n");

			boolean bSend = sendAndRecv();

			if (bSend) {
				bRet = true;
			} else {
				if (!CheckNetworkStillWorking()) {
					LogOutput.debug("client send error.\n");
					m_nConnectBeginTime = -1;
					m_nErrorOccurTime = System.currentTimeMillis();
					m_nClientStatus = CLIENT_SOCKET_ERROR;
					bRet = true;
				}
			}
			break;
		case CLIENT_SOCKET_ERROR:
			LogOutput.debug("client error.\n");
			if (m_nErrorOccurTime > 0) {
				long nCurrnetTime = System.currentTimeMillis();
				long nDiff = nCurrnetTime - m_nErrorOccurTime;
				if (nDiff >= m_nMaxWaitTimeAfterErrorOccur) {
					m_nErrorOccurTime = -1;
					m_nClientStatus = CLIENT_SOCKET_INIT;
					bRet = true;
				}
			} else {
				m_nClientStatus = CLIENT_SOCKET_INIT;
			}
			break;
		default:
			break;
		}

		return bRet;
	}

	public boolean stop() {
		boolean bRet = false;

		return bRet;
	}

	@Override
	public boolean sendMessage(IDataMessage message) {
		boolean bRet = false;
		// synchronized (this) {
		m_vecWaitingMessage.add(message);
		bRet = true;
		// }
		return bRet;
	}

	public boolean sendAndRecv() {
		boolean bRet = true;
		
		// synchronized(this) {
		if (m_vecWaitingMessage.size() > 0) {
			for (int i = 0; i < m_vecWaitingMessage.size(); ++i) {
				IDataMessage oneMessage = m_vecWaitingMessage.get(i);

				boolean bSentOne = this.sendMessageToServer(oneMessage);
				if (!bSentOne) {
					bRet = false;
					LogOutput.debug("sendMessageToServer fail!\n");
				}
			}
			m_vecWaitingMessage.clear();
		}
		// }

		boolean bTryRecv = tryRecvData(m_nMaxWaitMilliSecond);
		
		if (!bTryRecv) {
			bRet = false;
		}

		return bRet;
	}

	private boolean tryRecvData(int nMaxWaitMilliSecond) {

		boolean bRet = true;
		if (CLIENT_SOCKET_CONNECTED == m_nClientStatus) {
			// boolean bProcessRecv = false;

			try {
				// LogOutput.debug("tryAccept before select.\n");
				if (m_selector.select(1) > 0) {
					Set<SelectionKey> readySet = m_selector.selectedKeys();
					Iterator<SelectionKey> iterator = readySet.iterator();
					while (iterator.hasNext()) {
						SelectionKey key = (SelectionKey) iterator.next();

						// int nOperation = key.readyOps();
						// LogOutput.debug("recv2 nOperation:" + nOperation +
						// "\n");

						if (key.isReadable()) {
							SocketChannel sChannel = (SocketChannel) key.channel();

							m_byteRecvBuffer.clear();
							int nReadBytesCount = sChannel.read(m_byteRecvBuffer);
							if (nReadBytesCount > 0) {
								// m_byteBuffer.flip();
								// String strData =
								// String(m_byteBuffer.array());

								// LogOutput.debug("read[" +
								// String.format("%3d", nReadBytesCount) + "]:
								// ");
								// for (int i = 0; i < nReadBytesCount; ++i) {
								// byte ch = m_byteRecvBuffer.get(i);
								// LogOutput.printHex(ch);
								// LogOutput.print(" ");
								// }
								// LogOutput.print("\n");

								// byte[] byteBuffer = m_byteBuffer.array();
								if (m_processor != null) {
									boolean bProcess = m_processor.receiveData(sChannel, m_byteRecvBuffer.array(),
											nReadBytesCount);
									if (!bProcess) {
										LogOutput.debug("process recv data fail!\n");
									}
								}

								// m_byteRecvBuffer.clear();

								bRet = true;
							} else if (nReadBytesCount == 0) {
								m_bWithNetworkError = false;
								bRet = false;
								LogOutput.debug(nReadBytesCount + ":server.socket close by client side.\n");
								key.cancel();
							} else if (nReadBytesCount < 0) {
								bRet = false;
								m_bWithNetworkError = true;
								LogOutput.debug(nReadBytesCount + ":server.socket read exception.\n");
								key.cancel();
							} else {
								// bRet = false;
								LogOutput.debug("no else\n");
							}
						}
					}
				}
			} catch (Exception e) {
				LogOutput.debug("client tryRecvData exception!\n");
				e.printStackTrace();
				m_bWithNetworkError = true;
				bRet = false;
			}
		}

		return bRet;
	}

	protected boolean connectToServer() {
		boolean bRet = false;

		try {
			InetAddress serverIPAddress = InetAddress.getByName(m_strServerIp);
			int port = m_nServerPort;
			InetSocketAddress serverAddress = new InetSocketAddress(serverIPAddress, port);
			SocketChannel socketChannel = SocketChannel.open();
			Selector selector = Selector.open();
			socketChannel.configureBlocking(false);
			// int operations = SelectionKey.OP_CONNECT |
			// SelectionKey.OP_READ | SelectionKey.OP_WRITE;
			int operations = SelectionKey.OP_CONNECT | SelectionKey.OP_READ;
			socketChannel.register(selector, operations);
			socketChannel.connect(serverAddress);

			m_socketChannel = socketChannel;
			m_selector = selector;
			m_bWithNetworkError = false;

			bRet = true;
		} catch (UnknownHostException e) {
			m_bWithNetworkError = true;
			LogOutput.debug("connection UnknownHostException!\n");
		} catch (ClosedChannelException e) {
			m_bWithNetworkError = true;
			LogOutput.debug("connection ClosedChannelException!\n");
		} catch (IOException e) {
			m_bWithNetworkError = true;
			LogOutput.debug("connection IOException!\n");
		} catch (Exception e) {
			m_bWithNetworkError = true;
			LogOutput.debug("connection exception!\n");
		}
		return bRet;
	}

	protected boolean CheckNetworkStillWorking() {
		boolean bRet = true;
		if (m_bWithNetworkError) {
			bRet = false;
		}
		return bRet;
	}

	protected boolean CheckConnecting() {
		boolean bRet = false;

		try {
			if (m_selector.select(m_nMaxWaitMilliSecond) > 0) {
				Set<SelectionKey> readySet = m_selector.selectedKeys();

				Iterator<SelectionKey> iterator = readySet.iterator();
				while (iterator.hasNext()) {
					SelectionKey key = (SelectionKey) iterator.next();
					iterator.remove();

					// int nOperation = key.readyOps();
					// LogOutput.debug("nOperation:" + nOperation + "\n");

					if (key.isConnectable()) {
						SocketChannel channel = (SocketChannel) key.channel();
						while (channel.isConnectionPending()) {
							LogOutput.debug("pending for connected signal!\n");
							channel.finishConnect();
						}

						// LogOutput.debug("connected!\n");
						bRet = true;
					}
				}
			}
		} catch (ConnectException e) {
			LogOutput.debug("connect exception! " + e.getMessage() + "\n");
			m_bWithNetworkError = true;
		} catch (Exception e) {
			LogOutput.debug("CheckConnecting exception!\n");
			e.printStackTrace();
			m_bWithNetworkError = true;
		}
		return bRet;
	}
	
	// Integer m_nSendCounter = 0;
	// Integer m_nMaxWaitCounter = 50;

	// private boolean sendDataLogic() {
	// boolean bRet = false;
	//
	// synchronized (this) {
	// for (int i = 0; i < m_vecWaitingMessage.size(); i++) {
	// IDataMessage message = m_vecWaitingMessage.get(i);
	// if (message != null) {
	// boolean bSent = this.sendMessageToServer(message);
	// if (bSent) {
	// bRet = true;
	// }
	// }
	// }
	// m_vecWaitingMessage.clear();
	// }
	//
	// return bRet;
	// }

	protected boolean sendMessageToServer(IDataMessage message) {
		boolean bRet = false;

		this.m_nLocalLamportClock++;
		message.updateLamportClock( this.m_nLocalLamportClock );
		
		int nLamp = message.getLamportClock();
		LogOutput.debug("client message: nLamp = "+nLamp+"\n");
		
		byte[] buffer = message.getMessageBuffer();
		if (buffer != null && buffer.length > 0) {
			ByteBuffer writeBuffer = ByteBuffer.wrap(buffer);
			int nWrite = -1;
			try {
				nWrite = m_socketChannel.write(writeBuffer);
			} catch (IOException e) {
				LogOutput.debug("client sendMessageToServer exception!\n");
				m_bWithNetworkError = true;
			}
			if (nWrite == buffer.length) {
				//LogOutput.debug("all data sent:" + nWrite + "\n");
				LogOutput.debug("message sent to server, len:" + nWrite + "\n");
				bRet = true;
			} else {
				LogOutput.debug("not all data sent: data:" + buffer.length + ", sent:" + nWrite + "\n");
			}
		}
		return bRet;
	}

}

package au.innovation.network;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import au.innovation.protocol.IDataMessage;
import au.innovation.utility.LogOutput;

/*
 * Copyright (c) 2015-2015, John Wong <john dot innovation dot au at gmail dot com>, all rights reserved.
 * http://www.bitranslator.com
 * 
 * AsyncServer.java
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

public class AsyncServer implements ISendMessage {

	protected AsynchronousServerThread 	serverThread = null;
	protected IRawNetworkDataProcessor	m_processor = null;

	protected String 	m_strServerIp;
	protected Integer 	m_nServerPort;
	
	protected boolean syncRemoteLamportClock( int nRemoteLamportClock )
	{
		if( serverThread != null)
		{
			return serverThread.syncRemoteLamportClock(nRemoteLamportClock);
		}
		else
		{
			return false;
		}
	}
	
	public int getLocalLamportClock()
	{
		if( serverThread != null )
		{
			return serverThread.getLocalLamportClock();
		}
		else
		{
			return -1;
		}
	}
	
	// this interface does not startup a real thread, the function stepNetwork need to be called periodically.
	public boolean start(String strServerIp, Integer nServerPort, IMessageHandler handler, String strMode) {
		boolean bRet = false;

		if (handler != null) {
			// IRawNetworkDataProcessor processor
			if( strMode.equalsIgnoreCase("Robot") )
			{
				m_processor = new RawDataProcessorRobot( handler );
				LogOutput.debug("robot handler based on server.\n");
			}
			
			if( m_processor != null )
			{
				m_strServerIp = strServerIp;
				m_nServerPort = nServerPort;

				serverThread = new AsynchronousServerThread();
				bRet = serverThread.startListen(m_strServerIp, m_nServerPort);
			}
			else
			{
				LogOutput.debug("cannot create robot handler on server!\n");
			}
		}

		return bRet;
	}
	
	public boolean stepNetwork()
	{
		boolean bRet = false;
		if( serverThread != null )
		{
			bRet = serverThread.stepNetwork();
		}
		return bRet;
	}
	
	public boolean stop()
	{
		boolean bRet = false;
		if( serverThread != null )
		{
			bRet = serverThread.stopNetwork();
		}
		return bRet;
	}
	
	@Override
	public boolean sendMessage( IDataMessage message )
	{
		boolean bRet = false;
		if( serverThread != null )
		{
			bRet = serverThread.sendMessage( message );
		}
		return bRet;
	}

	protected class AsynchronousServerThread /*extends Thread */ {
		
		boolean	m_bThreadInterrupted = false;

		protected int m_nLocalLamportClock = 1;
		
		protected ServerSocketChannel 	m_ssChannel = null;
		protected Set<SocketChannel>	m_setConnectedClients = null;
		protected Selector 				m_selectorAccept = null;
		protected Selector 				m_selectorSendRecv = null;
		protected boolean 				m_bListenReady = false;

		protected ByteBuffer 			m_byteSendBuffer = null;
		protected ByteBuffer 			m_byteRecvBuffer = null;
		protected Integer				m_nBufferSize = 32*1024;
		protected Integer 				m_nMaxWaitMilliSecond = 20;
		
		protected ArrayList<IDataMessage>	m_vecWaitingMessage = null;
		
		boolean syncRemoteLamportClock( int nRemoteLamportClock )
		{
			boolean bRet = false;
			if( nRemoteLamportClock > m_nLocalLamportClock )
			{
				//LogOutput.debug("server sync with remote: remote="+nRemoteLamportClock+", local="+m_nLocalLamportClock+", final="+(nRemoteLamportClock+1)+"\n");				
				m_nLocalLamportClock = nRemoteLamportClock; 
			}
			else if( nRemoteLamportClock < 0 )
			{
				LogOutput.debug("server sync remote < 0? \n");
			}
			m_nLocalLamportClock++;
			bRet = true;
			return bRet;
		}
		
		public int getLocalLamportClock()
		{
			return m_nLocalLamportClock;
		}

		/*
		public void run() {
			
			m_bThreadInterrupted = false;
			
			boolean bStartup = startListen();
			
			if (bStartup) {
				LogOutput.print("server thread start!\n");

				while ( !m_bThreadInterrupted ) {

					boolean bStep = stepNetwork();
					
					if (m_bThreadInterrupted || interrupted()) {
						break;
					}
					
					if (!bStep) {
						try {
							doIdleTask();
							sleep(m_nMaxWaitMilliSecond);
							// LogOutput.print("server sleeping.\n");
						} catch (InterruptedException e) {
							m_bThreadInterrupted = true;
							LogOutput.print("server sleep interrupted!\n");
						}
					}
				}
			} else {
				LogOutput.print("server startup fail!\n");
			}
			LogOutput.print("server thread end!\n");
		}
		*/
		
		public boolean stepNetwork()
		{
			boolean bRet = false;
			
			boolean bSent = false;
			//synchronized(this) {
				if( m_vecWaitingMessage.size() > 0 )
				{
					//LogOutput.print("before m_vecWaitingMessage.size:"+m_vecWaitingMessage.size()+"\n");
					for( int i=0; i<m_vecWaitingMessage.size(); ++i )
					{
						IDataMessage oneMessage = m_vecWaitingMessage.get(i);
						
						bSent = this.sendMessage( oneMessage );
						if( bSent )
						{
							bRet = true;
						}
						else
						{
							LogOutput.debug("broadcastMessage fail!\n");							
						}
					}
					m_vecWaitingMessage.clear();
					//LogOutput.print("after m_vecWaitingMessage.size:"+m_vecWaitingMessage.size()+"\n");
				}
			//}
			
			boolean bAccept = tryAccept( m_nMaxWaitMilliSecond );
			boolean bTryRead = tryRead(m_nMaxWaitMilliSecond);
			
			if( bAccept || bTryRead )
			{
				bRet = true;				
			}
			
			return bRet;
		}
		
		public boolean stopNetwork()
		{
			boolean bRet = false;
			
			this.m_bThreadInterrupted = true;
			bRet = true;
			
			return bRet;
		}
		
		public boolean sendMessage( IDataMessage message )
		{
			boolean bRet = false;
			
			SocketChannel toChannel = message.getChannel();
			if (toChannel != null && toChannel.isConnected()) {
				
				this.m_nLocalLamportClock++;
				message.updateLamportClock( this.m_nLocalLamportClock );

				//int nLamp = message.getLamportClock();
				// LogOutput.debug("server message: nLamp = "+nLamp+"\n");

				byte[] writeByteBuffer = message.getMessageBuffer();
				ByteBuffer buffer = ByteBuffer.wrap(writeByteBuffer);
				int nMessageLength = message.getMessageLength();
				int nWriteLength = 0;
				try {
					do {
						int nWriteOnce = toChannel.write(buffer);
						nWriteLength += nWriteOnce;
						if (nWriteLength == nMessageLength) {
							bRet = true;
							// LogOutput.debug("==== write success!=====\n");
						} else {
							LogOutput.debug("write again\n");
						}
					} while (nWriteLength != nMessageLength);

				} catch (IOException e) {
					LogOutput.debug("==== socket fail! remove!=====\n");
					try {
						toChannel.close();
					} catch (IOException e1) {
					}
					m_setConnectedClients.remove(toChannel);
				}
			} else {
				LogOutput.debug("==== cannot write!=====\n");
			}

			return bRet;
		}
		
		boolean startListen(String strServerIp, Integer nServerPort) {
			boolean bRet = false;
			
			try {
				m_byteSendBuffer = ByteBuffer.allocate( m_nBufferSize );
				m_byteRecvBuffer = ByteBuffer.allocate( m_nBufferSize );
				m_vecWaitingMessage = new ArrayList<IDataMessage>(); 
				
				m_bListenReady = false;
				
				InetAddress hostIPAddress = InetAddress.getByName(strServerIp);
				InetSocketAddress socketAddress = new InetSocketAddress(hostIPAddress, nServerPort);
				
				ServerSocketChannel ssChannel = ServerSocketChannel.open();
				ssChannel.configureBlocking(false);
				ssChannel.socket().bind( socketAddress );
				
				Selector selectorAccept = Selector.open();
				ssChannel.register(selectorAccept, SelectionKey.OP_ACCEPT);
				
				Selector selectorSendRecv = Selector.open();
				
				m_ssChannel = ssChannel;
				m_selectorAccept = selectorAccept;
				m_selectorSendRecv = selectorSendRecv;
				m_bListenReady = true;
				bRet = true;
			}
			catch( UnknownHostException e ) {
				LogOutput.debug("server thread UnknownHostException!\n");
				e.printStackTrace();
			}
			catch( ClosedChannelException e ) {
				LogOutput.debug("server thread ClosedChannelException!\n");
				e.printStackTrace();
			}
			catch( IOException e ) {
				LogOutput.debug("server thread IOException! strServerIp:"+strServerIp+", nServerPort:"+nServerPort+"\n");
				e.printStackTrace();
			}
			catch (Exception e) {
				LogOutput.debug("server thread Exception!\n");
				e.printStackTrace();
			}

			return bRet;
		}

		boolean tryAccept(Integer nMaxWaitMilliSecond)
		{
			boolean bRet = false;
			
			if( m_bListenReady )
			{
				try {
					boolean bProcessAcceptOrRecv = false;
					
					//LogOutput.debug("tryAccept before select.\n");
					if (m_selectorAccept.select( 1 ) > 0)
					{
						bProcessAcceptOrRecv = processAcceptReadySet(m_selectorAccept.selectedKeys());
					}
					
					if( bProcessAcceptOrRecv )
					{
						bRet = true;
					}
					else
					{
						//LogOutput.debug("tryAccept sleeping.\n");
						Thread.sleep(nMaxWaitMilliSecond);
					}
				}
				catch (IOException e) 
				{
					LogOutput.debug("tryAccept IOException!\n");
				}
				catch( InterruptedException e )
				{
					m_bThreadInterrupted = true;
					LogOutput.debug("tryAccept interrupted!\n");
					//if( interrupted() )
					//{
					//	LogOutput.debug("tryAccept interrupted() is true!\n");
					//}
					//else
					//{
					//	LogOutput.debug("tryAccept interrupted() is false!\n");
					//}
				}
			}
			
			return bRet;
		}
		
		boolean tryRead(Integer nMaxWaitMilliSecond)
		{
			boolean bRet = false;
			
			if( m_bListenReady )
			{
				try {
					boolean bProcessAcceptOrRecv = false;
					
					//LogOutput.debug("tryRead before select.\n");
					if (m_selectorSendRecv.select( nMaxWaitMilliSecond ) > 0)
					{
						bProcessAcceptOrRecv = processSendRecvReadySet(m_selectorSendRecv.selectedKeys());
					}
					
					if( bProcessAcceptOrRecv )
					{
						bRet = true;
					}
					else
					{
						//LogOutput.debug("tryRead sleeping.\n");
						Thread.sleep(nMaxWaitMilliSecond);
					}
				}
				catch (IOException e) 
				{
					LogOutput.debug("tryRead IOException!\n");
				}
				catch( InterruptedException e )
				{
					m_bThreadInterrupted = true;
					LogOutput.debug("tryRead interrupted!\n");
					//if( interrupted() )
					//{
					//	LogOutput.debug("tryRead interrupted() is true!\n");
					//}
					//else
					//{
					//	LogOutput.debug("tryRead interrupted() is false!\n");
					//}
				}
			}
			
			return bRet;
		}

		public boolean processAcceptReadySet(Set<SelectionKey> readySet) 
		{
			boolean bRet = false;
			
			Iterator<SelectionKey> iterator = readySet.iterator();
			while (iterator.hasNext()) {
				SelectionKey key = (SelectionKey) iterator.next();
				iterator.remove();
				
				//int nOperation = key.readyOps();
				//LogOutput.debug("accept nOperation:"+nOperation+"\n");
				
				if (key.isAcceptable())
				{
					try {
						ServerSocketChannel ssChannel = (ServerSocketChannel) key.channel();
						SocketChannel sChannel = (SocketChannel) ssChannel.accept();
						sChannel.configureBlocking(false);
						//sChannel.register(m_selectorSendRecv, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
						sChannel.register(m_selectorSendRecv, SelectionKey.OP_READ);
						
						if( m_setConnectedClients == null )
						{
							m_setConnectedClients = new HashSet<SocketChannel>();	
						}
						m_setConnectedClients.add( sChannel );
						
						SocketAddress socketAddress = sChannel.getRemoteAddress();
						LogOutput.debug("add connection! "+socketAddress.toString()+"\n");
						bRet = true;
					}
					catch( ClosedChannelException e ) {
						LogOutput.debug("processReadySet ClosedChannelException!\n");
					}
					catch( IOException e ) {
						LogOutput.debug("processReadySet IOException!\n");
					}
				}
			}
			
			return bRet;			
		}
		
		public boolean processSendRecvReadySet(Set<SelectionKey> readySet) 
		{
			boolean bRet = false;
			
			Iterator<SelectionKey> iterator = readySet.iterator();
			while (iterator.hasNext()) {
				SelectionKey key = (SelectionKey) iterator.next();
				iterator.remove();
				
				int nOperation = key.readyOps();
				//LogOutput.debug("recv, ready nOperation:"+nOperation+"\n");
				
				if (key.isReadable())
				{
					//LogOutput.debug("key.isReadable. \n");
					SocketChannel sChannel = (SocketChannel) key.channel();
					
					try {
						
						m_byteRecvBuffer.clear();
						int nReadBytesCount = sChannel.read( m_byteRecvBuffer );
						if (nReadBytesCount > 0) {
							
							//LogOutput.debug("read["+String.format("%3d", nReadBytesCount)+"]: ");
							//for( int i=0; i<nReadBytesCount; ++i )
							//{
							//	byte ch = m_byteRecvBuffer.get( i );
							//	LogOutput.printHex( ch );
							//	LogOutput.print(" ");
							//}
							//LogOutput.print("\n");
							
							if( m_processor != null )
							{
								// byte[] byteBuffer = m_byteBuffer.array();
								boolean bHandle = m_processor.receiveData(sChannel, m_byteRecvBuffer.array(), nReadBytesCount);
								if( !bHandle )
								{
									//LogOutput.debug("handle server socket fail!\n");
								}
								bRet = true;
							}
							else
							{
								LogOutput.debug("WHY NO handle?\n");
							}
							
							//m_byteRecvBuffer.clear();
						}
						else if(nReadBytesCount == 0)
						{
							LogOutput.debug(nReadBytesCount+":server.socket read exception.\n");
							key.cancel();
						}
						else if(nReadBytesCount < 0)
						{
							LogOutput.debug(nReadBytesCount+":server.socket close by client side.\n");
							key.cancel();
						}
						else
						{
							LogOutput.debug("no else\n");
						}
					} catch (IOException e) {
						LogOutput.debug("processReadySet2 IOException!\n");
					}
					
					//String msg = processRead(key);
					//if (msg.length() > 0) {
					//	SocketChannel sChannel = (SocketChannel) key.channel();
					//	ByteBuffer buffer = ByteBuffer.wrap(msg.getBytes());
					//	sChannel.write(buffer);
					//}
				}
			}
			
			return bRet;
		}
	}
}

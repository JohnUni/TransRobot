package au.innovation.network;

import java.nio.channels.SocketChannel;

import au.innovation.protocol.BaseMessage;
import au.innovation.protocol.IDataMessage;
import au.innovation.protocol.RemoteCommandMessage;
import au.innovation.protocol.SensorColorNotificationMessage;
import au.innovation.protocol.SensorNotificationMessage;
import au.innovation.utility.LogOutput;

/*
 * Copyright (c) 2015-2015, John Wong <john dot innovation dot au at gmail dot com>, all rights reserved.
 * http://www.bitranslator.com
 * 
 * RawDataProcessorRobot.java
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

public class RawDataProcessorRobot extends BaseRawNetworkDataProcessor {

	// byte1:size, byte2:command, byte34:messageid, (optional byte5:subtype)
	// size: only 0x00-0x70, size>=0x80 is for larger size usage.

	// private ByteBuffer m_bufferRecv = null;
	private byte[] m_byteDataBuffer = null;
	private int m_nDataInBufferLength = 0;

	// protected IMessageHandler m_messageHandler = null;

	RawDataProcessorRobot(IMessageHandler handler) {
		super(handler);
	}

	@Override
	public boolean receiveData(SocketChannel fromChannel, byte[] buffer, int nDataLength) {
		boolean bRet = false;

		if (buffer.length >= nDataLength) {
			if (m_byteDataBuffer == null) {
				m_byteDataBuffer = new byte[nDataLength];
				System.arraycopy(buffer, 0, m_byteDataBuffer, 0, nDataLength);
				m_nDataInBufferLength = nDataLength;
				LogOutput.print("copy data! len=" + nDataLength);
				if (nDataLength >= 4) {
					LogOutput.print(String.format(" %02X", m_byteDataBuffer[0]));
					LogOutput.print(String.format(" %02X", m_byteDataBuffer[1]));
					LogOutput.print(String.format(" %02X", m_byteDataBuffer[2]));
					LogOutput.print(String.format(" %02X", m_byteDataBuffer[3]));
				}
				LogOutput.print("\n");
			} else {
				LogOutput.print("append data!\n");
				byte[] newBuffer = new byte[m_byteDataBuffer.length + nDataLength];
				System.arraycopy(m_byteDataBuffer, 0, newBuffer, 0, m_nDataInBufferLength);
				System.arraycopy(buffer, 0, newBuffer, m_byteDataBuffer.length, nDataLength);

				m_byteDataBuffer = newBuffer;
				m_nDataInBufferLength += nDataLength;
			}
		} else {
			LogOutput.print("CANNOT COPY BYTE!\n");
		}

		bRet = processParseBuffer(fromChannel);

		return bRet;
	}

	private boolean processParseBuffer(SocketChannel fromChannel) {
		boolean bRet = true;

		int nProcessingOffset = 0;

		if (m_nDataInBufferLength < BaseMessage.MESSAGE_HEADER_LENGTH) {
			LogOutput.print("less than header:");
			LogOutput.print(" m_nDataInBufferLength=" + m_nDataInBufferLength);
			LogOutput.print("\n");
		}

		while (m_nDataInBufferLength >= (nProcessingOffset + BaseMessage.MESSAGE_HEADER_LENGTH)) {

			int nSize = m_byteDataBuffer[nProcessingOffset];
			int nMessageType = m_byteDataBuffer[nProcessingOffset + 1];
			byte chMesageIdLow = m_byteDataBuffer[nProcessingOffset + 2];
			byte chMesageIdHigh = m_byteDataBuffer[nProcessingOffset + 3];
			int nMesageIdLow = chMesageIdLow;
			int nMesageIdHigh = chMesageIdHigh;
			int nMessageId = nMesageIdLow | (nMesageIdHigh << 8);

			LogOutput.print("BaseRemote Handler:");
			// LogOutput.print(" offset=" + nProcessingOffset);
			// LogOutput.print(" size=" + nSize);
			// LogOutput.print(" type=" + nMessageType);
			// LogOutput.print(" MessageId=" + nMessageId);
			// LogOutput.print("\n");

			LogOutput.print(" nSize:" + nSize);
			LogOutput.print(" nMessageType:" + nMessageType);
			LogOutput.print(" chMesageIdLow:" + chMesageIdLow);
			LogOutput.print(" chMesageIdHigh:" + chMesageIdHigh);
			LogOutput.print("\n");

			boolean bIsValidType = BaseMessage.isValidType(nMessageType);

			if (!bIsValidType) {
				LogOutput.print("NOT VALID! clear, type:" + nMessageType + "\n");
				m_byteDataBuffer = null;
				m_nDataInBufferLength = 0;
				nProcessingOffset = 0;
				bRet = false;
			} else if (0 == (BaseMessage.MESSAGE_EXTEND_SIZE_MASK & nSize)) {
				if (m_nDataInBufferLength >= (nProcessingOffset + nSize)) {
					boolean bErrorOccur = false;

					IDataMessage objMessage = null;

					switch (nMessageType) {
					case BaseMessage.MESSAGE_REMOTE_COMMAND:
						objMessage = new RemoteCommandMessage(false);
						break;
					case BaseMessage.MESSAGE_REMOTE_COMMAND_ACK:
						objMessage = new RemoteCommandMessage(true);
						break;

					case BaseMessage.MESSAGE_SENSOR_COLOR:
						objMessage = new SensorColorNotificationMessage(false);
						break;
					case BaseMessage.MESSAGE_SENSOR_GYRO:
					case BaseMessage.MESSAGE_SENSOR_SONIC:
					case BaseMessage.MESSAGE_SENSOR_TOUCH:
						objMessage = new SensorNotificationMessage(nMessageType, false);
						break;
					case BaseMessage.MESSAGE_SENSOR_COLOR_ACK:
						objMessage = new SensorColorNotificationMessage(true);
						break;
					case BaseMessage.MESSAGE_SENSOR_GYRO_ACK:
					case BaseMessage.MESSAGE_SENSOR_SONIC_ACK:
					case BaseMessage.MESSAGE_SENSOR_TOUCH_ACK:
						objMessage = new SensorNotificationMessage(nMessageType, true);
						break;

					case BaseMessage.MESSAGE_UNKNOWN:
					case BaseMessage.MESSAGE_UNKNOWN_ACK:
						LogOutput.print("process unknown message type2!\n");
						bErrorOccur = true;
						break;

					default:
						LogOutput.print("process unknown message type!\n");
						bErrorOccur = true;
						break;
					}

					if (objMessage != null && !bErrorOccur) {
						boolean bBuild = objMessage.buildRemoteMessage(nMessageType, nMessageId, m_byteDataBuffer,
								nProcessingOffset + BaseMessage.MESSAGE_HEADER_LENGTH,
								nSize - BaseMessage.MESSAGE_HEADER_LENGTH);
						if (bBuild) {
							LogOutput.print("build success!\n");
							boolean bHandle = this.handleMessage(fromChannel, objMessage);
							if (bHandle) {
								LogOutput.print("handle success!\n");
							} else {
								LogOutput.print("handle fail!\n");
							}
						} else {
							LogOutput.print("build remote message fail!\n");
						}
					}

					if (bErrorOccur) {
						LogOutput.print("process error, remove all buffer!\n");
						m_byteDataBuffer = null;
						m_nDataInBufferLength = 0;
						nProcessingOffset = 0;
						bRet = false;
						// break;
					} else {
						nProcessingOffset += nSize;
					}
				} else {
					LogOutput.print("no enough data:");
					LogOutput.print(" chSize=" + nSize);
					LogOutput.print(" m_nDataInBufferLength=" + m_nDataInBufferLength);
					LogOutput.print("\n");
				}
			} else {
				LogOutput.print("unknown message, remove all buffer!\n");
				m_byteDataBuffer = null;
				m_nDataInBufferLength = 0;
				nProcessingOffset = 0;
				bRet = false;
			}
		}

		if (m_byteDataBuffer != null && m_nDataInBufferLength > 0) {
			if (m_nDataInBufferLength == nProcessingOffset) {
				m_byteDataBuffer = null;
				m_nDataInBufferLength = 0;
			}
			if (m_nDataInBufferLength > nProcessingOffset) {
				int newDataBufferLength = m_nDataInBufferLength - nProcessingOffset;
				byte[] newDataBuffer = new byte[newDataBufferLength];
				System.arraycopy(m_byteDataBuffer, nProcessingOffset, newDataBuffer, 0, newDataBufferLength);
				m_byteDataBuffer = newDataBuffer;
				m_nDataInBufferLength = newDataBufferLength;
			}
		}

		return bRet;
	}

	protected boolean handleMessage(SocketChannel fromChannel, IDataMessage message) {
		{
			boolean bRet = false;

			if (m_handler != null) {
				bRet = m_handler.handle(fromChannel, message);
			}

			return bRet;
		}
	}
}

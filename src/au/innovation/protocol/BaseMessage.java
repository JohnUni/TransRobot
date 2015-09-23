package au.innovation.protocol;

import java.nio.channels.SocketChannel;

/*
 * Copyright (c) 2015-2015, John Wong <john dot innovation dot au at gmail dot com>, all rights reserved.
 * http://www.bitranslator.com
 * 
 * BaseMessage.java
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


public abstract class BaseMessage implements IDataMessage {
	// byte1:size, byte2:command, byte34:messageid, (optional byte5:subtype)
	// size: only 0x00-0x70, size>=0x80 is for larger size usage.
	
	protected SocketChannel m_channel = null;
	protected boolean m_bIsAck = false;
	
	public final static int  MESSAGE_HEADER_LENGTH = 4;
	public final static byte MESSAGE_EXTEND_SIZE_MASK = (byte) 0x80;
	
	protected int m_nMessageType = MESSAGE_UNKNOWN;
	protected int m_nMessageSubType = MESSAGE_SUBTYPE_UNKNOWN;
	
	protected int m_nLamportClock = -1;
	
	protected byte[] 	m_byteBodyBuffer = null;
	protected int 		m_nBodyLength = -1;
	
	protected boolean	m_bValid = false;	
	

	BaseMessage()
	{
		//m_byteFullMessageBuffer = null;
		//m_nFullMessageLength = -1;
		
		m_nBodyLength = 0;
		m_byteBodyBuffer = null;
		m_bValid = false;
	}
	
	//BaseMessage( int nBodyLength )
	//{
	//	m_nBodyLength = nBodyLength;
	//	m_byteBodyBuffer = new byte[m_nBodyLength];
	//}

	public static boolean isValidType( int nMessageType )
	{
		boolean bRet = false;
		if( (nMessageType == MESSAGE_REMOTE_COMMAND) ||
			(nMessageType == MESSAGE_REMOTE_COMMAND_ACK) ||
			(nMessageType == MESSAGE_SENSOR_COLOR) ||
			(nMessageType == MESSAGE_SENSOR_GYRO) ||
			(nMessageType == MESSAGE_SENSOR_SONIC) ||
			(nMessageType == MESSAGE_SENSOR_TOUCH) ||
			(nMessageType == MESSAGE_SENSOR_RAWCOLOR) ||
			(nMessageType == MESSAGE_SENSOR_COLOR_ACK) ||
			(nMessageType == MESSAGE_SENSOR_GYRO_ACK) ||
			(nMessageType == MESSAGE_SENSOR_SONIC_ACK) ||
			(nMessageType == MESSAGE_SENSOR_TOUCH_ACK) ||
			(nMessageType == MESSAGE_SENSOR_RAWCOLOR_ACK)||
			(nMessageType == MESSAGE_POSITION_DIRECTION_UPDATE)||
			(nMessageType == MESSAGE_POSITION_DIRECTION_UPDATE_ACK) )
		{
			bRet = true;
		}
		return bRet;
	}
	
	@Override
	public SocketChannel getChannel() {
		return m_channel;
	}
	
	@Override
	public void updateLamportClock( int nNewLamportClock )
	{
		m_nLamportClock = nNewLamportClock;
	}

	protected final boolean updateMessageBody( byte[] bufferBody, int nBodyLength )
	{
		boolean bRet = false;
		
		if( bufferBody != null && nBodyLength > 0 && nBodyLength <= bufferBody.length  )
		{
			m_nBodyLength = nBodyLength;
			m_byteBodyBuffer = new byte[nBodyLength];
			System.arraycopy(bufferBody, 0, m_byteBodyBuffer, 0, nBodyLength);
			
			m_bValid = true;
			bRet = true;
		}
		return bRet;
	}
	
	protected abstract boolean assembleRemoteMessage();

	@Override
	public int getMessageType() {
		return m_nMessageType;
	}

	@Override
	public int getMessageSubType() {
		return m_nMessageSubType;
	}

	@Override
	public int getLamportClock() {
		return m_nLamportClock;
	}

	@Override
	public int getMessageLength() {
		if( m_bValid )
		{
			int nFullMessageLength = MESSAGE_HEADER_LENGTH + m_nBodyLength;
			return nFullMessageLength;
		}
		else
		{
			return -1;
		}
	}

	@Override
	public byte[] getMessageBuffer() {
		if( m_bValid && m_byteBodyBuffer != null )
		{
			int nFullMessageLength = MESSAGE_HEADER_LENGTH + m_nBodyLength;
			byte[] byteFullMessageBuffer = new byte[nFullMessageLength];
			
			int nHighId = (( m_nLamportClock >> 8) & 0x00FF );
			int nLowId = m_nLamportClock & 0x00FF;
			
			byteFullMessageBuffer[0] = (byte)(nFullMessageLength & 0xFF);
			byteFullMessageBuffer[1] = (byte)(m_nMessageType & 0xFF);
			byteFullMessageBuffer[2] = (byte)nLowId;
			byteFullMessageBuffer[3] = (byte)nHighId;
			
			System.arraycopy(m_byteBodyBuffer, 0, byteFullMessageBuffer, MESSAGE_HEADER_LENGTH, m_nBodyLength);

			return byteFullMessageBuffer;
		}
		else
		{
			return null;
		}
	}

}

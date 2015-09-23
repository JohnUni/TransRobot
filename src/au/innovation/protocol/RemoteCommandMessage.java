package au.innovation.protocol;

import au.innovation.utility.LogOutput;

/*
 * Copyright (c) 2015-2015, John Wong <john dot innovation dot au at gmail dot com>, all rights reserved.
 * http://www.bitranslator.com
 * 
 * RemoteCommandMessage.java
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

public class RemoteCommandMessage extends BaseMessage {
	
	static final int REMOTE_COMMAND_DATA_LENGTH = 8;
	
	protected boolean m_bIsAck = false;
	
	//protected int m_nSubCommand = 0;	// 1 byte  ===> to 1st byte of m_nTag
	protected int m_nData = 0;			// 4 bytes
	protected int m_nTag = 0;			// 4 bytes
	
	public RemoteCommandMessage(boolean bIsAck) {
		super();
		if( !bIsAck )
		{
			m_nMessageType = MESSAGE_REMOTE_COMMAND;
		}
		else
		{
			m_nMessageType = MESSAGE_REMOTE_COMMAND_ACK;
		}
		m_bIsAck = bIsAck;
	}
	
	public RemoteCommandMessage( int nMessageSubType, int nData, int nTag, boolean bIsAck ) {
		super();
		if( !bIsAck )
		{
			m_nMessageType = MESSAGE_REMOTE_COMMAND;
		}
		else
		{
			m_nMessageType = MESSAGE_REMOTE_COMMAND_ACK;
		}
		m_bIsAck = bIsAck;
		
		m_nData = nData;
		m_nMessageSubType = nMessageSubType;
		m_nTag = nTag;
		this.assembleRemoteMessage();
	}
	
	@Override
	public boolean buildRemoteMessage( int nMessageType, int nLamportClock, byte[] buffer, int nOffset, int nDataSize) {
		boolean bRet = false;
		if( ( buffer.length >= (nOffset+nDataSize) ) &&  ( nDataSize == REMOTE_COMMAND_DATA_LENGTH ) )
		{
			m_nMessageType = nMessageType;
			m_nLamportClock = nLamportClock;
		
			byte nDataLL = buffer[nOffset];
			byte nDataL = buffer[nOffset+1];
			byte nDataH = buffer[nOffset+2];
			byte nDataHH = buffer[nOffset+3];
			
			byte nTagLL = buffer[nOffset+4];
			byte nTagL = buffer[nOffset+5];
			byte nTagH = buffer[nOffset+6];
			byte nTagHH = buffer[nOffset+7];
			
			m_nData = ( nDataHH << 24 ) | ( nDataH << 16 ) | ( nDataL << 8 ) | nDataLL; 
			//m_nTag = ( nTagHH << 24 ) | ( nTagH << 16 ) | ( nTagL << 8 ) | nTagLL; 
			m_nTag = ( nTagH << 16 ) | ( nTagL << 8 ) | nTagLL;
			
			m_nMessageSubType = (nTagHH & 0xFF);
			
			//LogOutput.print("cmd:" );
			//LogOutput.print( nDataLL + ",");
			//LogOutput.print( nDataL + ",");
			//LogOutput.print( nDataH + ",");
			//LogOutput.print( nDataHH + "|");
			//LogOutput.print( nTagLL + ",");
			//LogOutput.print( nTagL + ",");
			//LogOutput.print( nTagH + ",");
			//LogOutput.print( nTagHH + "");
			//LogOutput.print( "\n" );
			
			bRet = this.assembleRemoteMessage();
		}
		else
		{
			LogOutput.print("NOT SAME!" + nDataSize + ":"+ REMOTE_COMMAND_DATA_LENGTH + " " + nOffset + "" + buffer.length + "\n");
		}
		return bRet;
	}
	
	@Override
	protected boolean assembleRemoteMessage() {
		boolean bRet = false;
		
		byte[] bufferBody = new byte[REMOTE_COMMAND_DATA_LENGTH];
		int nBodyLength = REMOTE_COMMAND_DATA_LENGTH;
		
		bufferBody[0] = (byte)(m_nData & 0xFF );
		bufferBody[1] = (byte)((m_nData >> 8) & 0xFF );
		bufferBody[2] = (byte)((m_nData >> 16) & 0xFF );
		bufferBody[3] = (byte)((m_nData >> 24) & 0xFF );
		
		bufferBody[4] = (byte)(m_nTag & 0xFF );
		bufferBody[5] = (byte)((m_nTag >> 8) & 0xFF );
		bufferBody[6] = (byte)((m_nTag >> 16) & 0xFF );
		//bufferBody[7] = (byte)((m_nTag >> 24) & 0xFF );
		
		bufferBody[7] = (byte)(m_nMessageSubType & 0xFF );
		
		bRet = super.updateMessageBody(bufferBody, nBodyLength);
		
		return bRet;
	}
	
}

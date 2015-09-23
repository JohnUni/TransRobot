package au.innovation.protocol;

import au.innovation.utility.StringUtility;

/*
 * Copyright (c) 2015-2015, John Wong <john dot innovation dot au at gmail dot com>, all rights reserved.
 * http://www.bitranslator.com
 * 
 * SensorRawColorNotificationMessage.java
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

public class SensorRawColorNotificationMessage extends BaseMessage {

	static final int SENSOR_NOTIFICATION_DATA_LENGTH = 24;
	protected boolean m_bIsAck = false;

	protected int		m_nRawValueR = 0;	// 4 bytes
	protected int		m_nRawValueG = 0;	// 4 bytes
	protected int		m_nRawValueB = 0;	// 4 bytes

	protected long		m_nBeginTime = 0;	// 4 bytes
	protected long		m_nEndTime = 0;		// 4 bytes
	
	protected int m_nTag = 0;			// 4 bytes
	
	public SensorRawColorNotificationMessage(boolean bIsAck) {
		super();
		if( !bIsAck )
		{
			m_nMessageType = MESSAGE_SENSOR_RAWCOLOR;
		}
		else
		{
			m_nMessageType = MESSAGE_SENSOR_RAWCOLOR_ACK;
		}
		m_bIsAck = bIsAck;
	}
	
	public SensorRawColorNotificationMessage(int nRawValueR, int nRawValueG, int nRawValueB, long nBegin, long nEnd, int nTag, boolean bIsAck) {
		super();
		if( !bIsAck )
		{
			m_nMessageType = MESSAGE_SENSOR_RAWCOLOR;
		}
		else
		{
			m_nMessageType = MESSAGE_SENSOR_RAWCOLOR_ACK;
		}
		m_bIsAck = bIsAck;
		
		m_nRawValueR = nRawValueR;	// 4 bytes
		m_nRawValueG = nRawValueG;	// 4 bytes
		m_nRawValueB = nRawValueB;	// 4 bytes

		m_nBeginTime = nBegin;	// 4 bytes
		m_nEndTime = nEnd;		// 4 bytes
		m_nTag = nTag;			// 4 bytes

		this.assembleRemoteMessage();
	}
	
	public int getRawValueR() { return m_nRawValueR; }
	public int getRawValueG() { return m_nRawValueG; }
	public int getRawValueB() { return m_nRawValueB; }
	
	@Override
	public boolean buildRemoteMessage(int nMessageType, int nLamportClock, byte[] buffer, int nOffset, int nDataSize) {
		boolean bRet = false;
		
		m_nMessageType = nMessageType;
		m_nLamportClock = nLamportClock;
		
//		int nDataLL = buffer[nOffset];
//		int nDataL = buffer[nOffset+1];
//		int nDataH = buffer[nOffset+2];
//		int nDataHH = buffer[nOffset+3];
//		
//		int nTagLL = buffer[nOffset+4];
//		int nTagL = buffer[nOffset+5];
//		int nTagH = buffer[nOffset+6];
//		int nTagHH = buffer[nOffset+7];
//		
//		m_nARGBData = ( nDataHH << 24 ) | ( nDataH << 16 ) | ( nDataL << 8 ) | nDataLL; 
//		m_nTag = ( nTagHH << 24 ) | ( nTagH << 16 ) | ( nTagL << 8 ) | nTagLL; 
		
		m_nRawValueR = StringUtility.getIntFromByteArray(buffer, 4*0);
		m_nRawValueG = StringUtility.getIntFromByteArray(buffer, 4*1);
		m_nRawValueB = StringUtility.getIntFromByteArray(buffer, 4*2);
		
		m_nBeginTime = StringUtility.getIntFromByteArray(buffer, 4*3);
		m_nEndTime = StringUtility.getIntFromByteArray(buffer, 4*4);
		
		m_nTag = StringUtility.getIntFromByteArray(buffer, 4*5);

		bRet = this.assembleRemoteMessage();
		
		return bRet;
	}

	@Override
	protected boolean assembleRemoteMessage() {
		boolean bRet = false;
		
		byte[] bufferBody = new byte[SENSOR_NOTIFICATION_DATA_LENGTH];
		int nBodyLength = SENSOR_NOTIFICATION_DATA_LENGTH;
		
//		bufferBody[0] = (byte)(m_nARGBData & 0xFF );
//		bufferBody[1] = (byte)((m_nARGBData >> 8) & 0xFF );
//		bufferBody[2] = (byte)((m_nARGBData >> 16) & 0xFF );
//		bufferBody[3] = (byte)((m_nARGBData >> 24) & 0xFF );
//		
//		bufferBody[4] = (byte)(m_nTag & 0xFF );
//		bufferBody[5] = (byte)((m_nTag >> 8) & 0xFF );
//		bufferBody[6] = (byte)((m_nTag >> 16) & 0xFF );
//		bufferBody[7] = (byte)((m_nTag >> 24) & 0xFF );

		StringUtility.writeIntToByteArray(m_nRawValueR, bufferBody, 4*0);
		StringUtility.writeIntToByteArray(m_nRawValueG, bufferBody, 4*1);
		StringUtility.writeIntToByteArray(m_nRawValueB, bufferBody, 4*2);
		StringUtility.writeIntToByteArray((int) m_nBeginTime, bufferBody, 4*3);
		StringUtility.writeIntToByteArray((int) m_nEndTime, bufferBody, 4*4);
		StringUtility.writeIntToByteArray(m_nTag, bufferBody, 4*5);
		
		bRet = super.updateMessageBody(bufferBody, nBodyLength);
		return bRet;
	}
}
package au.innovation.utility;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/*
 * Copyright (c) 2015-2015, John Wong <john dot innovation dot au at gmail dot com>, all rights reserved.
 * http://www.bitranslator.com
 * 
 * StringUtility.java
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

public class StringUtility {

	public static String byte2Hex(byte ch) {
		return String.format("%02X", ch);
	}

	public static byte Hex2byte(String str) {
		byte ch = 0;
		try {
			ch = Byte.parseByte(str);
		} catch (NumberFormatException e) {
			//print("byte2Hex NumberFormatException: " + str);
		}
		return ch;
	}
	
	public static int getIntFromByteArray(byte[] buffer, int offset) {
		int nRet = 0;

		int nDataLL = buffer[offset];
		int nDataL = buffer[offset + 1];
		int nDataH = buffer[offset + 2];
		int nDataHH = buffer[offset + 3];
		nRet = (nDataHH << 24) | (nDataH << 16) | (nDataL << 8) | nDataLL;

		return nRet;
	}

	public static void writeIntToByteArray(int value, byte[] buffer, int offset) {
		buffer[0 + offset] = (byte) (value & 0xFF);
		buffer[1 + offset] = (byte) ((value >> 8) & 0xFF);
		buffer[2 + offset] = (byte) ((value >> 16) & 0xFF);
		buffer[3 + offset] = (byte) ((value >> 24) & 0xFF);
	}

	// "http://servername.domain.domain:portnumber"
	// "http://servername:portnumber" (with implicit domain information)
	// "servername:portnumber" (with implicit domain and protocol information).
	public static String parseUrlServer(String strUrl) {
		String strRet = null;

		String strCut = strUrl;
		String strPrefix = "http://";
		if (strUrl.toLowerCase().startsWith(strPrefix)) {
			strCut = strUrl.substring(strPrefix.length());
		}

		int nLastIndex = strCut.lastIndexOf(":");
		if (nLastIndex > 0 && nLastIndex < strCut.length()) {
			strRet = strCut.substring(0, nLastIndex);
		} else {
			strRet = strCut;
		}

		return strRet;
	}

	public static int parseUrlServerPort(String strUrl) {
		int nRet = 0;

		int nLastIndex = strUrl.lastIndexOf(":");
		if (nLastIndex >= 0 && nLastIndex < strUrl.length()) {
			String strPort = strUrl.substring(nLastIndex + 1);
			try {
				int nPort = Integer.parseInt(strPort);
				nRet = nPort;
			} catch (NumberFormatException e) {
				// nothing to do
			}
		}

		return nRet;
	}

	public static Date getDateFromString(String strDate) {
		Date date = null;
		// 2015-08-07T18:29:02Z
		// debug( "format: "+strDate+" \n" );
		if (strDate != null) {
			SimpleDateFormat formater = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
			try {
				date = formater.parse(strDate);
			} catch (ParseException e) {
			}
		}
		return date;
	}

	public static String getStringFromDate(Date date) {
		String strDate = null;
		if (date != null) {
			SimpleDateFormat formater = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
			strDate = formater.format(date);
		}
		return strDate;
	}
}

package au.innovation.protocol;

import java.nio.channels.SocketChannel;

/*
 * Copyright (c) 2015-2015, John Wong <john dot innovation dot au at gmail dot com>, all rights reserved.
 * http://www.bitranslator.com
 * 
 * IDataMessage.java
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

public interface IDataMessage {

	public SocketChannel getChannel();
	
	public int getMessageType();
	public int getMessageSubType();
	
	public int getMessageLength();
	public byte[] getMessageBuffer();
	
	public boolean buildRemoteMessage(int nMessageType, int nLamportClock, byte[] buffer, int nOffset, int nDataSize);

	public void updateLamportClock(int nNewLamportClock);
	public int getLamportClock();

	public final int MESSAGE_UNKNOWN = 0x00;
	public final int MESSAGE_UNKNOWN_ACK = 0x80;
	public final int MESSAGE_ACK_BASE = MESSAGE_UNKNOWN_ACK;

	public final int MESSAGE_REMOTE_COMMAND = 0x01;
	public final int MESSAGE_REMOTE_COMMAND_ACK = 0x81;

	public final int MESSAGE_SENSOR_COLOR = 0x10;
	public final int MESSAGE_SENSOR_GYRO = 0x11;
	public final int MESSAGE_SENSOR_SONIC = 0x12;
	public final int MESSAGE_SENSOR_TOUCH = 0x13;
	public final int MESSAGE_SENSOR_RAWCOLOR = 0x14;

	public final int MESSAGE_SENSOR_COLOR_ACK = 0x90;
	public final int MESSAGE_SENSOR_GYRO_ACK = 0x91;
	public final int MESSAGE_SENSOR_SONIC_ACK = 0x92;
	public final int MESSAGE_SENSOR_TOUCH_ACK = 0x93;
	public final int MESSAGE_SENSOR_RAWCOLOR_ACK = 0x94;

	public final int MESSAGE_POSITION_DIRECTION_UPDATE = 0x20;
	public final int MESSAGE_POSITION_DIRECTION_UPDATE_ACK = 0xA0;

	public final int MESSAGE_SUBTYPE_UNKNOWN = 0x00;

	public final int MESSAGE_SUBTYPE_REMOTE_COMMAND_RESET = 0x01;
	public final int MESSAGE_SUBTYPE_REMOTE_COMMAND_TURN_LEFT = 0x02;
	public final int MESSAGE_SUBTYPE_REMOTE_COMMAND_TURN_RIGHT = 0x03;
	public final int MESSAGE_SUBTYPE_REMOTE_COMMAND_FORWARD = 0x04;
	public final int MESSAGE_SUBTYPE_REMOTE_COMMAND_BACKWARD = 0x05;
	public final int MESSAGE_SUBTYPE_REMOTE_COMMAND_BRAKE = 0x06;
	public final int MESSAGE_SUBTYPE_REMOTE_COMMAND_STOP = MESSAGE_SUBTYPE_REMOTE_COMMAND_BRAKE;

	public final int MESSAGE_SUBTYPE_REMOTE_COMMAND_TURN_LEFT_ANGLE = 0x11;
	public final int MESSAGE_SUBTYPE_REMOTE_COMMAND_TURN_RIGHT_ANGLE = 0x12;
	public final int MESSAGE_SUBTYPE_REMOTE_COMMAND_FORWARD_DISTANCE = 0x13;
	public final int MESSAGE_SUBTYPE_REMOTE_COMMAND_BACKWARD_DISTANCE = 0x14;
	
	public final int MESSAGE_SUBTYPE_REMOTE_COMMAND_CRUISE_SPEEDUP = 0x20;
	public final int MESSAGE_SUBTYPE_REMOTE_COMMAND_CRUISE_SPEEDDOWN = 0x21;
	public final int MESSAGE_SUBTYPE_REMOTE_COMMAND_CRUISE_SPEED_RESET = 0x22;
	public final int MESSAGE_SUBTYPE_REMOTE_COMMAND_TURNNING_SPEEDUP = 0x23;
	public final int MESSAGE_SUBTYPE_REMOTE_COMMAND_TURNNING_SPEEDDOWN = 0x24;
	public final int MESSAGE_SUBTYPE_REMOTE_COMMAND_TURNNING_SPEED_RESET = 0x25;
	
}

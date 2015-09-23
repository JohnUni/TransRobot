import java.nio.channels.SocketChannel;
import java.util.Vector;

import lejos.hardware.Brick;
import lejos.hardware.BrickFinder;
import lejos.hardware.Key;
import lejos.hardware.port.MotorPort;
import au.innovation.hardware.ColorSensor;
import au.innovation.hardware.GyroSensor;
import au.innovation.hardware.ISensor;
import au.innovation.hardware.SmartMotor;
import au.innovation.hardware.TouchSensor;
import au.innovation.hardware.UltraSonicSensor;
import au.innovation.network.AsyncServer;
import au.innovation.network.IMessageHandler;
import au.innovation.protocol.IDataMessage;
import au.innovation.protocol.RemoteCommandMessage;
import au.innovation.utility.LogOutput;

/*
 * Copyright (c) 2015-2015, John Wong <john dot innovation dot au at gmail dot com>, all rights reserved.
 * http://www.bitranslator.com
 * 
 * RobotHost.java
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

public class RobotHost extends AsyncServer implements IMessageHandler {

	public static void main(String[] args) 
	{
		RobotHost robot = new RobotHost();
		robot.instance_main( args );
	}

	private SmartMotor m_motor = null;
	private Vector<ISensor> m_vecSensor = null;
    
	private void instance_main(String[] args)
	{
		LogOutput.enable(true);
		LogOutput.setDebug(true);
		
        m_vecSensor = new Vector<ISensor>();
        
        ColorSensor color = new ColorSensor();
        GyroSensor gyro = new GyroSensor();
        TouchSensor touch = new TouchSensor();
        UltraSonicSensor sonic = new UltraSonicSensor();
        
        color.open( "S1" );
        gyro.open( "S2" );
        touch.open( "S3" );
        sonic.open( "S4" );
        
        m_vecSensor.add(color);
        m_vecSensor.add(touch);
        m_vecSensor.add(sonic);
        m_vecSensor.add(gyro);
        
		m_motor = new SmartMotor(this, MotorPort.A, MotorPort.D, gyro, color); 
		
		// "10.0.1.1" "127.0.0.1"
		String strServerIp = "10.0.1.1";
		int nServerPort = 8090;
		
	    Brick brick = BrickFinder.getDefault();
		Key escape = brick.getKey("Escape");
		
		this.start(strServerIp, nServerPort, this, "Robot");
	    
		while (!escape.isDown()) {
			
        	for( ISensor sensor : m_vecSensor )
        	{
        		if( sensor.isAvailable() )
        		{
        			sensor.fetchSample();
        		}
        	}
        	
        	m_motor.stepMotor();
        	
			boolean bStep = this.stepNetwork();
			if (bStep) {
				// LogOutput.debug("step ok. \n");
			} else {
				try {
					// LogOutput.debug("sleep(20). \n");
					Thread.sleep(20);
				} catch (InterruptedException e) {
				}
			}
		}
		
		this.stop();
		
	}

	@Override
	public boolean handle(SocketChannel fromChannel, IDataMessage message) {
		boolean bRet = false;

		int nRemoteLamportClock = message.getLamportClock();
		this.syncRemoteLamportClock(nRemoteLamportClock);
		
		int nLocalLamportClock = this.getLocalLamportClock();
		message.updateLamportClock(nLocalLamportClock);
		
		int nMessageType = message.getMessageType();
		switch( nMessageType )
		{
		case IDataMessage.MESSAGE_REMOTE_COMMAND:
			bRet = handleRemoteCommand(fromChannel, (RemoteCommandMessage) message);
			break;
		case IDataMessage.MESSAGE_REMOTE_COMMAND_ACK:
			break;
		default:
			// WHY HERE?
			LogOutput.debug(" WHY HERE? nMessageType:"+nMessageType+"\n");
			break;
		}
		
		return bRet;
	}
	
	protected boolean handleRemoteCommand(SocketChannel fromChannel, RemoteCommandMessage message) {
		boolean bRet = false;

		int nSubType = message.getMessageSubType();
		
		switch (nSubType) {
		case IDataMessage.MESSAGE_SUBTYPE_REMOTE_COMMAND_RESET:
			// to deal with reset command
			break;
		case IDataMessage.MESSAGE_SUBTYPE_REMOTE_COMMAND_STOP: // MESSAGE_SUBTYPE_REMOTE_COMMAND_BRAKE 
			bRet = this.m_motor.stop();
			break;
		case IDataMessage.MESSAGE_SUBTYPE_REMOTE_COMMAND_TURN_LEFT:
			bRet = this.m_motor.turnLeft();
			break;
		case IDataMessage.MESSAGE_SUBTYPE_REMOTE_COMMAND_TURN_RIGHT:
			bRet = this.m_motor.turnRight();
			break;
		case IDataMessage.MESSAGE_SUBTYPE_REMOTE_COMMAND_FORWARD:
			bRet = this.m_motor.goForward();
			break;
		case IDataMessage.MESSAGE_SUBTYPE_REMOTE_COMMAND_BACKWARD:
			bRet = this.m_motor.goBackward();
			break;

		case IDataMessage.MESSAGE_SUBTYPE_REMOTE_COMMAND_TURN_LEFT_ANGLE:
			//bRet = this.m_motor.turnAngleDegree( nDataValue );
			break;
		case IDataMessage.MESSAGE_SUBTYPE_REMOTE_COMMAND_TURN_RIGHT_ANGLE:
			//bRet = this.m_motor.turnAngleDegree( -nDataValue );
			break;
		case IDataMessage.MESSAGE_SUBTYPE_REMOTE_COMMAND_FORWARD_DISTANCE:
			//bRet = this.m_motor.goForwardDistance( nDataValue );
			break;
		case IDataMessage.MESSAGE_SUBTYPE_REMOTE_COMMAND_BACKWARD_DISTANCE:
			//bRet = this.m_motor.goBackwardDistance( nDataValue );
			break;
		default:
			break;
		}
		
		RemoteCommandMessage respMessage = new RemoteCommandMessage( true ); 
		this.sendMessage(respMessage);
		
		return bRet;
	}
}

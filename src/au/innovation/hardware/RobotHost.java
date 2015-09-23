package au.innovation.hardware;

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


public class RobotHost {

	public static void main(String[] args) 
	{
		//RobotHost robot = new RobotHost();
		///robot.instance_main( args );
	}
}
/*
	private BaseMotor m_motor = null;
	private Vector<IBaseSensor> m_vecSensor = null;
    
	private void instance_main(String[] args)
	{
		LogOutput.enable(true);
		LogOutput.setDebug(true);
		
        Vector<IBaseSensor> vecSensor = new Vector<IBaseSensor>();
        
        ColorSensor color = new ColorSensor();
        GyroSensor gyro = new GyroSensor();
        TouchSensor touch = new TouchSensor();
        UltraSonicSensor sonic = new UltraSonicSensor();
        
        color.open( "S4" );
        gyro.open( "S2" );
        touch.open( "S3" );
        sonic.open( "S1" );
        
        vecSensor.add(color);
        vecSensor.add(touch);
        vecSensor.add(sonic);
        vecSensor.add(gyro);
        
		motor = new BaseMotorMove(MotorPort.A, MotorPort.D, gyro); 
		
		// "10.0.1.1" "127.0.0.1"
		String strServerIp = "10.0.1.1";
		int nServerPort = 8090;
		
	    Brick brick = BrickFinder.getDefault();
		Key escape = brick.getKey("Escape");
		
		robot.start(strServerIp, nServerPort, robot, "Robot");
	    
		while (!escape.isDown()) {
			boolean bStep = robot.stepNetwork();
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
		
		robot.stop();
		
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
	
}
*/

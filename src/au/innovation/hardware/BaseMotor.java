package au.innovation.hardware;

import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.Port;
import lejos.robotics.RegulatedMotor;
import lejos.robotics.RegulatedMotorListener;

import au.innovation.network.IMessageHandler;
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

public class BaseMotor implements RegulatedMotorListener {

	// handler to deal with the notification of motor Started and Stopped  
	protected IMessageHandler m_handler;
	
	// dual-motor objects
	protected EV3LargeRegulatedMotor motorLeft = null; // new EV3LargeRegulatedMotor(MotorPort.A);
	protected EV3LargeRegulatedMotor motorRight = null;// new EV3LargeRegulatedMotor(MotorPort.D);
	
	// The maximum reliably sustainable velocity is 100 x battery voltage
	protected float m_nMovingSpeed = 180;
	protected float m_nTurningSpeed = 30;
	
	protected int m_nTachoBeginMove = 0;
	protected int m_nTachoEndMove = 0;
	
	// status of motors
	public final int STATUS_STOP = 0;
	public final int STATUS_MOVING = 1;
	public final int STATUS_TURNING = 2;
	public final int STATUS_TURNING_TESTING = 3;
	//public final int STATUS_MOVING_TURNING = 4;
	
	protected int m_nStatus = STATUS_STOP;
	
	
	public BaseMotor(IMessageHandler handler, Port left, Port right) {

		m_handler = handler;
		
		motorLeft = new EV3LargeRegulatedMotor(left);
		motorRight = new EV3LargeRegulatedMotor(right);
		motorLeft.setSpeed(m_nMovingSpeed);
		motorRight.setSpeed(m_nMovingSpeed);
		
		// listen to left wheel only, not the right one
		motorLeft.addListener(this);
		//motorRight.addListener(this);
		
		// synchronize the right wheel to the left one
		RegulatedMotor[] listMotors = { motorRight };
		motorLeft.synchronizeWith(listMotors);
	}

	public int getMotorStatus() {
		return m_nStatus;
	}
	
	public boolean stop() {
		LogOutput.print(" motors stop. \n");
		boolean bRet = false;
		motorLeft.startSynchronization();
		motorLeft.stop(false);
		motorRight.stop(false);
		motorLeft.endSynchronization();
		m_nStatus = STATUS_STOP;
		bRet = true;
		return bRet;
	}
	
	public boolean goForward() {
		boolean bRet = false;
		if (STATUS_STOP == m_nStatus) {
			LogOutput.print(" motors go forward. \n");
			motorLeft.startSynchronization();
			motorLeft.setSpeed(m_nMovingSpeed);
			motorRight.setSpeed(m_nMovingSpeed);
			motorLeft.forward();
			motorRight.forward();
			motorLeft.endSynchronization();
			m_nStatus = STATUS_MOVING;
			bRet = true;
		} else {
			LogOutput.print(" motor cannot forward since is not in STOP status!\n");
		}
		return bRet;
	}

	public boolean goBackward() {
		boolean bRet = false;
		if (STATUS_STOP == m_nStatus) {
			LogOutput.print(" motors go backward. \n");
			motorLeft.startSynchronization();
			motorLeft.setSpeed(m_nMovingSpeed);
			motorRight.setSpeed(m_nMovingSpeed);
			motorLeft.backward();
			motorRight.backward();
			motorLeft.endSynchronization();
			m_nStatus = STATUS_MOVING;
			bRet = true;
		} else {
			LogOutput.print(" motor cannot backward since is not in STOP status!\n");
		}
		return bRet;
	}

	// speed: unit in meters per second
	public boolean goForwardSpeed(float nSpeed) {
		boolean bRet = false;
		if (STATUS_STOP == m_nStatus) {
			//LogOutput.print(" motors go forward with speed"+nSpeed+". \n");
			m_nMovingSpeed = nSpeed;
			bRet = this.goForward();
		} else {
			LogOutput.print(" motor cannot goforward since is not in STOP status!\n");
		}

		return bRet;
	}
	
	public boolean goBackward(float nSpeed) {
		boolean bRet = false;
		if (STATUS_STOP == m_nStatus) {
			//LogOutput.print(" motors go backward with speed"+nSpeed+". \n");
			m_nMovingSpeed = nSpeed;
			bRet = this.goBackward();
		} else {
			LogOutput.print(" motor cannot gobackward since is not in STOP status!\n");
		}

		return bRet;
	}

	public boolean turnLeft() {
		boolean bRet = false;
		if (STATUS_STOP == m_nStatus) {
			LogOutput.print(" motors turn left. \n");
			motorLeft.startSynchronization();
			motorLeft.setSpeed(m_nTurningSpeed);
			motorRight.setSpeed(m_nTurningSpeed);
			motorLeft.backward();
			motorRight.forward();
			motorLeft.endSynchronization();
			m_nStatus = STATUS_TURNING;
			bRet = true;
		} else {
			LogOutput.print(" motor cannot turn left since is not in STOP status!\n");
		}
		return bRet;
	}

	public boolean turnRight() {
		boolean bRet = false;
		if (STATUS_STOP == m_nStatus) {
			LogOutput.print(" motors turn left. \n");
			motorLeft.startSynchronization();
			motorLeft.setSpeed(m_nTurningSpeed);
			motorRight.setSpeed(m_nTurningSpeed);
			motorLeft.forward();
			motorRight.backward();
			motorLeft.endSynchronization();
			m_nStatus = STATUS_TURNING;
			bRet = true;
		} else {
			LogOutput.print(" motor cannot turn right since is not in STOP status!\n");
		}
		return bRet;
	}
	
	public boolean turnLeft(float nSpeed) {
		boolean bRet = false;
		if (STATUS_STOP == m_nStatus) {
			m_nTurningSpeed = nSpeed;
			bRet = this.turnLeft();
		}
		return bRet;
	}
	
	public boolean turnRight(float nSpeed) {
		boolean bRet = false;
		if (STATUS_STOP == m_nStatus) {
			m_nTurningSpeed = nSpeed;
			bRet = this.turnRight();
		}
		return bRet;
	}
	
	public int getCurrentTachoCount()
	{
		int nRet = 0;
		nRet = this.motorLeft.getTachoCount();
		return nRet;
	}
	
	@Override
	public void rotationStarted(RegulatedMotor motor, int tachoCount, boolean stalled, long timeStamp) {
		if (motor.equals(this.motorLeft)) {
			m_nTachoBeginMove = motor.getTachoCount();
			if( m_handler != null )
			{
				//SomeMessage message = new SomeMessage(); 
				//m_handler.postMessage( message );
			}
		}
		// LogOutput.print("R Started["+m_nCallbackCount+"] c:" +tachoCount+"\n" );
	}
	
	@Override
	public void rotationStopped(RegulatedMotor motor, int tachoCount, boolean stalled, long timeStamp) {
		if (motor.equals(this.motorLeft)) {
			m_nTachoEndMove = motor.getTachoCount();
			if( m_handler != null )
			{
				//SomeMessage message = new SomeMessage(); 
				//m_handler.postMessage( message );
			}
		}
		// LogOutput.print("R Stopped["+m_nCallbackCount+"] c:" +tachoCount+"\n" );
	}
}

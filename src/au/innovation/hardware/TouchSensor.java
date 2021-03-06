package au.innovation.hardware;

import au.innovation.protocol.IDataMessage;
import au.innovation.protocol.SensorNotificationMessage;
import lejos.hardware.Brick;
import lejos.hardware.BrickFinder;
import lejos.hardware.port.Port;
import lejos.hardware.sensor.EV3TouchSensor;

/*
 * Copyright (c) 2015-2015, John Wong <john dot innovation dot au at gmail dot com>, all rights reserved.
 * http://www.bitranslator.com
 * 
 * TouchSensor.java
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

public class TouchSensor extends BaseSensorImpl {

	EV3TouchSensor ev3TouchSensor;
	
	private int  	m_nMultiplyFactor = 10000;
	private int 	m_nValue;
	
	@Override
	public boolean open(String strPortName) {

		boolean bRet = false;

		if (m_bAvailable) {
			System.out.print("touch already start\n");
			bRet = true;
		} else {
			try {
				Brick brick = BrickFinder.getDefault();
				Port port = brick.getPort(strPortName);
				ev3TouchSensor = new EV3TouchSensor(port);

				sensorMode = ev3TouchSensor.getTouchMode();
				sampleSize = sensorMode.sampleSize();
				sample = new float[sampleSize];

				System.out.print("touch start\n");
				m_bAvailable = true;
				bRet = true;
			} catch (Exception e) {
				System.out.print("touch exception!\n");
			}
		}
		return bRet;
	}

	@Override
	public boolean close() {
		boolean bRet = false;
		
		if (m_bAvailable) {
			m_bAvailable = false;

			try {
				ev3TouchSensor.close();
			} catch (Exception e) {
				System.out.print("touch close exception!\n");
			}

			System.out.print("touch stop\n");
			bRet = true;
		}
		else
		{
			bRet = true;
		}
		return bRet;
	}
	
	@Override
	protected boolean processSample() 
	{
		boolean bRet = false;

		System.out.print("process touch...\n");
		for (int i = 0; i < sampleSize; i++) {
			System.out.print("T[" + sampleSize + ":" + i + "] " + sample[i] + "\n");
		}

		if( 1 == sampleSize )
		{
			m_nValue = (int)(sample[0] * m_nMultiplyFactor);
			bRet = true;
		}

		return bRet;
	}
	
	@Override
	public IDataMessage getDataMessage() {
		if (m_bAvailable) {
			return new SensorNotificationMessage(IDataMessage.MESSAGE_SENSOR_TOUCH, m_nValue, false);
		}
		else
		{
			return null;
		}
	}
}

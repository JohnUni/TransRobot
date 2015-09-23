package au.innovation.hardware;

import au.innovation.protocol.IDataMessage;
import au.innovation.protocol.SensorRawColorNotificationMessage;
import lejos.hardware.Brick;
import lejos.hardware.BrickFinder;
import lejos.hardware.port.Port;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.robotics.Color;

/*
 * Copyright (c) 2015-2015, John Wong <john dot innovation dot au at gmail dot com>, all rights reserved.
 * http://www.bitranslator.com

 * ColorSensor.java
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

public class ColorSensor extends BaseSensorImpl {

	private EV3ColorSensor ev3ColorSensor;
	
	private int		m_nARGBValue = 0;
	// {NONE,RED,GREEN,BLUE,YELLOW,MAGENTA,ORANGE,WHITE,BLACK,PINK,GRAY,LIGHT_GRAY,DARK_GRAY,CYAN,BROWN} -> [-1,13]
	private int		m_nColorId = Color.NONE;
	
	private int  	m_nMultiplyFactor = 10000;
	private int		m_nMultiValueR = 0;
	private int		m_nMultiValueG = 0;
	private int		m_nMultiValueB = 0;
	
	private int		m_nTag = 0;
	

	public int getARGB() { return m_nARGBValue; }
	public int getColorId() { return m_nColorId; }
	
	public int getMultiplyFactor() { return m_nMultiplyFactor; }
	public int getValueRed() { return m_nMultiValueR; }
	public int getValueGreen() { return m_nMultiValueG; }
	public int getValueBlue() { return m_nMultiValueB; }
	
	void updateCalibration( double up, double down, double nThreshold )
	{
		// formator.updateCalibration( up, down, nThreshold );
	}
	
	@Override
	public boolean open(String strPortName) {

		boolean bRet = false;

		if (m_bAvailable) {
			System.out.print("the color already start\n");
			bRet = true;
		} else {
			try {
				Brick brick = BrickFinder.getDefault();
				Port colorPort = brick.getPort(strPortName);
				ev3ColorSensor = new EV3ColorSensor(colorPort);
				ev3ColorSensor.setFloodlight( Color.NONE );
				
				sensorMode = ev3ColorSensor.getRGBMode();
				//// {NONE,RED,GREEN,BLUE,YELLOW,MAGENTA,ORANGE,WHITE,BLACK,PINK,GRAY,LIGHT_GRAY,DARK_GRAY,CYAN,BROWN} -> [-1,13]
				//sensorMode = ev3ColorSensor.getColorIDMode();
				sampleSize = sensorMode.sampleSize();
				sample = new float[sampleSize];

				System.out.print("color sensor start\n");
				m_bAvailable = true;
				bRet = true;
			} catch (Exception e) {
				System.out.print("color sensor exception!\n");
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
				ev3ColorSensor.close();
			} catch (Exception e) {
				System.out.print("color sensor close exception!\n");
			}
			sampleSize = 0;

			System.out.print("color sensor stop\n");
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

		//	EV3 color sensor, RGB mode
		//			Measures the level of red, green and blue light when illuminated by a white light source..
		//
		//	Size and content of the sample
		//	The sample contains 3 elements containing the intensity level (Normalized between 0 and 1)
		//			of red, green and blue light respectivily.

		//System.out.print("process color...\n");
		//for (int i = 0; i < sampleSize; i++) {
		//	System.out.print("C[" + sampleSize + ":" + i + "] " + sample[i] + "\n");
		//}
		
		if( 1 == sampleSize )
		{
			float nFloatId = sample[0];
			int nColorId = (int) nFloatId;
			m_nColorId = nColorId;
			System.out.print("C[id=" + nColorId + "]\n");
		}
		else if( 3 == sampleSize )
		{
			float nRed = sample[0]; 
			float nGreen = sample[1]; 
			float nBlue = sample[2];
			//System.out.print("C[R" + nRed+ " G"+ nGreen + " B" + nBlue + "]\n");
			
			m_nMultiValueR = (int) (nRed * m_nMultiplyFactor);
			m_nMultiValueG = (int) (nGreen * m_nMultiplyFactor);
			m_nMultiValueB = (int) (nBlue * m_nMultiplyFactor);
			
			//System.out.print("C[R" + m_nMultiValueR+ " G"+ m_nMultiValueG + " B" + m_nMultiValueB + "]");
			//long nDiff = m_nEndTime - m_nBeginTime;
			//System.out.print( nDiff+"\n");
			
			//nRed = (float) (nRed * 255.0);
			//nGreen = (float) (nGreen * 255.0);
			//nBlue = (float) (nBlue * 255.0);
			
			//int nFixedRed = (int)nRed; 
			//int nFixedGreen = (int)nGreen; 
			//int nFixedBlue = (int)nBlue; 
			//nFixedRed = nFixedRed &0xFF;
			//nFixedGreen = nFixedGreen &0xFF;
			//nFixedBlue = nFixedBlue &0xFF;
			//m_nARGBValue = ( (nFixedRed << 16) | (nFixedGreen << 8) | (nFixedBlue) );
			
			//m_nARGBValue = formator.formatARGB(nRed, nGreen, nBlue);
			//m_nColorId = formator.formatColor(nRed, nGreen, nBlue);
			
			//System.out.print("C[R" + m_nMultiValueR+ " G"+ m_nMultiValueG + " B" + m_nMultiValueB + "]\n");
			
			bRet = true;
		}
		return bRet;
	}
	
	@Override
	public IDataMessage getDataMessage() {
		if (m_bAvailable) {
			//return new SensorColorNotificationMessage(m_nARGBValue, false);
			return new SensorRawColorNotificationMessage(m_nMultiValueR, m_nMultiValueG, m_nMultiValueB, m_nBeginSamplingTime, m_nEndSamplingTime, m_nTag, false);
		}
		else
		{
			return null;
		}
	}
}

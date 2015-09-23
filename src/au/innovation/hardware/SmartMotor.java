package au.innovation.hardware;

import lejos.hardware.port.Port;

import au.innovation.network.IMessageHandler;
import au.innovation.utility.LogOutput;
import au.innovation.utility.Vector2d;

public class SmartMotor extends BaseMotor {

	GyroSensor m_gyroSensor = null;
	ColorSensor m_colorSensor = null;
	
	// direction for accurate movement, or accurate angle rotation
	private double m_nStartDirection = 0.0;

	protected boolean m_bAutoCalibration = false;
	protected double m_nMovingCalibrationThresholdDegree = 2;
	protected double m_nMaxRatioInCalibrationAdjust = 0.05;

	protected double m_nDesireDirection = 0.0;
	protected boolean m_bIsTurningLeft = true;
	protected double m_nTurningThresholdDegree = 0.1;
	
	// for recording linear movement distance and direction from the last movement action.
	protected Vector2d m_vecLinearMovement = null;
	protected float m_nOrigDirectionDegree = 0;
	
	protected float m_nAngularMovement = 0;

	public SmartMotor(IMessageHandler handler, Port left, Port right, GyroSensor gyro, ColorSensor color ) {
		super(handler, left, right);
		m_colorSensor = color;
		m_gyroSensor = gyro;
	}

	public void stepMotor() {
		if (STATUS_STOP != m_nStatus) {

			double nCurrentDirection = m_gyroSensor.getGyroRawValue();

			if (STATUS_TURNING == m_nStatus) {
				boolean bNeedStop = false;
				
				double nDiff = m_nDesireDirection - nCurrentDirection;
				// LogOutput.debug("---- " + nCurrentDirection +" "
				// +m_nDesireDirection + " " +nDiff);

				if (m_bIsTurningLeft && nDiff > 0) {
					bNeedStop = true;
				} else if (!m_bIsTurningLeft && nDiff < 0) {
					bNeedStop = true;
				}

				if (bNeedStop) {
					this.stop();
				}
			} else if (STATUS_TURNING_TESTING == m_nStatus) {
				double nDiff = m_nStartDirection - nCurrentDirection;
				if (nDiff > 360) {
					this.stop();
					// int nFinalTachoCount = motorLeft.getTachoCount();
					// m_nTurnAroundTachoCount = nFinalTachoCount - m_nTachoBeginMove;
				}
			} else if (STATUS_MOVING == m_nStatus && m_bAutoCalibration) {
				double nDiff = m_nStartDirection - nCurrentDirection;
				boolean bNeedCalibration = false;
				if (Math.abs(nDiff) > m_nMovingCalibrationThresholdDegree) {
					bNeedCalibration = true;
				}

				if (bNeedCalibration) {
					LogOutput.debug(" need auto calibrating.. \n");
				}
			}
		}
	}
}

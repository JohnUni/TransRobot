package au.innovation.utility;

public class Vector2d {
	
	private float m_x;
	private float m_y;
	
	public Vector2d(float x, float y)
	{
		m_x = x;
		m_y = y;
	}
	
	public Vector2d(Vector2d orig)
	{
		m_x = orig.m_x;
		m_y = orig.m_y;
	}
	
	public Vector2d add(Vector2d orig)
	{
		m_x += orig.m_x;
		m_y += orig.m_y;
		return this;
	}
	
	public Vector2d sub(Vector2d orig)
	{
		m_x -= orig.m_x;
		m_y -= orig.m_y;
		return this;
	}
	
	public void polar(float radius, float theta)
	{
		double nRadius = (double)radius;
		double nTheta = (double)theta;
		double nX = nRadius * Math.cos( nTheta );
		double nY = nRadius * Math.sin( nTheta );
		m_x = (float)nX;
		m_y = (float)nY;
	}

	public void rotateDegree(float theta) {
		if (0 != theta) {
			double nOrigRadius = this.getRadius();
			double nOrigTheta = this.getAngleDegree();
			double nNewTheta = nOrigTheta + theta;
			double nNewX = nOrigRadius * Math.cos(nNewTheta);
			double nNewY = nOrigRadius * Math.sin(nNewTheta);
			m_x = (float) nNewX;
			m_y = (float) nNewY;
		}
	}
	
	public float getX() { return m_x; }
	public float getY() { return m_y; }
	
	public float getRadius()
	{
		float nRadius = (float)Math.sqrt( m_x*m_x + m_y*m_y );
		return nRadius;
	}
	
	public float getAngleDegree() 
	{
		// atan2(double y, double x)
		// Returns the angle theta from the conversion of rectangular coordinates (x, y)
		// 		to polar coordinates (r, theta).
		double nAngle = Math.atan2(m_x, m_y);
		return (float) (nAngle * 360.0 / (2.0 * Math.PI));
	}
}

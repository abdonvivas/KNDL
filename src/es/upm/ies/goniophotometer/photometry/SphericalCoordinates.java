package es.upm.ies.goniophotometer.photometry;

/**
 * Container to store spherical coordinates.
 * 
 * @author Abd&oacute;n Alejandro Vivas Imparato
 *
 */
public class SphericalCoordinates {
	private float theta;
	private float phi;
	private double r;

	/**
	 * Creates a new <code>SphericalCoordinates</code> instance with null values
	 * as attributes.
	 */
	public SphericalCoordinates() {
		// Do nothing
	}

	/**
	 * Creates a new <code>SphericalCoordinates</code> instance with the
	 * specified values of its coordinates.
	 * 
	 * @param theta
	 *            The value of the polar angle.
	 * @param phi
	 *            The value azimuth angle.
	 * @param r
	 *            The value of the radial coordinate.
	 */
	public SphericalCoordinates(float theta, float phi, double r) {
		this.theta = theta;
		this.phi = phi;
		this.r = r;
	}

	/**
	 * Returns the value of the polar angle.
	 * 
	 * @return The value of the polar angle.
	 */
	public float getTheta() {
		return theta;
	}

	/**
	 * Sets the valu of the polar angle.
	 * 
	 * @param theta
	 *            The value of the polar angle.
	 */
	public void setTheta(int theta) {
		this.theta = theta;
	}

	/**
	 * Returns the value of the azimuth angle.
	 * 
	 * @return The value of the azimuth angle.
	 */
	public float getPhi() {
		return phi;
	}

	/**
	 * Sets the value of the azimuth angle.
	 * 
	 * @param phi
	 *            The value of the azimuth angle.
	 */
	public void setPhi(int phi) {
		this.phi = phi;
	}

	/**
	 * Returns the value of the radial coordinate.
	 * 
	 * @return The value of the radial coordinate.
	 */
	public double getR() {
		return r;
	}

	/**
	 * Sets the value of the radial coordinate.
	 * 
	 * @param r
	 *            The value of the radial coordinate.
	 */
	public void setR(int r) {
		this.r = r;
	}
}

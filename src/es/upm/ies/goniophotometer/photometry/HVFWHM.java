package es.upm.ies.goniophotometer.photometry;

/**
 * Container to store the horizontal and vertical Full Width at Half Maximum
 * (FWHM).
 * 
 * @author Abd&oacute;n Alejandro Vivas Imparato
 *
 */
public class HVFWHM {
	float hFWHM;
	float vFWHM;

	/**
	 * Returns the value of the horizontal FWHM.
	 * 
	 * @return Value of the horizontal FWHM.
	 */
	public float getHFWHM() {
		return hFWHM;
	}

	/**
	 * Sets the horizontal FWHM.
	 * 
	 * @param hFWHM
	 *            Horizontal FWHM.
	 */
	public void setHFWHM(float hFWHM) {
		this.hFWHM = hFWHM;
	}

	/**
	 * Returns the value of the vertical FWHM.
	 * 
	 * @return Value of the vertical FWHM.
	 */
	public float getVFWHM() {
		return vFWHM;
	}

	/**
	 * Sets the vertical FWHM.
	 * 
	 * @param vFWHM
	 *            Vertical FWHM.
	 */
	public void setVFWHM(float vFWHM) {
		this.vFWHM = vFWHM;
	}

}

package es.upm.ies.goniophotometer.exceptions;

/**
 * Thrown when the number of measures taken is not consistent with the
 * resolutions specified.
 * 
 * @author Abd&oacute;n Alejandro Vivas Imparato
 *
 */
public class InconsistentMeasureException extends Exception {
	private static final long serialVersionUID = 1L;

	/**
	 * Constructs an <code>InconsistentMeasureException</code> with a default
	 * message.
	 */
	public InconsistentMeasureException() {
		super("Data is inconsistent. \nMake sure the device is sending the right number "
				+ "of measues to the serial port. The value depends on the resolution value."
				+ "\n Also make sure you are using the correct data format.");
	}
}

package es.upm.ies.goniophotometer.exceptions;

/**
 * Thrown if something goes wrong when opening the serial port.
 * 
 * @author Abd&oacute;n Alejandro Vivas Imparato
 *
 */
public class CommPortException extends Exception {
	private static final long serialVersionUID = 1L;

	/**
	 * Constructs a <code>CommPortException</code> with a default message.
	 */
	public CommPortException() {
		super("An error has ocurred when opening the serial port. It is possible that it is busy due to another program.");
	}
}

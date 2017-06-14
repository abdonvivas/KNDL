package es.upm.ies.goniophotometer.exceptions;

/**
 * Thrown when no device is configured to be used or when the one configured is
 * not detected.
 * 
 * @author Abd&oacute;n Alejandro Vivas Imparato
 *
 */
public class DeviceNotFoundException extends Exception {
	private static final long serialVersionUID = 1L;

	/**
	 * Constructs a <code>CommPortException</code> with a default message.
	 */
	public DeviceNotFoundException() {
		super("The device is not connected to the serial port or there was a serial port failure.");
	}
}

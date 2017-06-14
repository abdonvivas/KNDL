package es.upm.ies.goniophotometer.threads;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.swing.JOptionPane;

import es.upm.ies.goniophotometer.KNDL;
import es.upm.ies.goniophotometer.gui.MainWindow;
import es.upm.ies.goniophotometer.serial.KNDLSerialComm;
import es.upm.ies.goniophotometer.serial.KNDLSerialCommImpl;
import es.upm.ies.goniophotometer.utils.PropsMngr;

/**
 * This thread does the automatic calibration while the main thread maintains
 * the GUI functionality active in case the user wants to stop the calibration.
 * More specifically, it starts the serial port communications, sends the
 * calibration command receives the result of the calibration and stores it in
 * the properties.<br>
 * <br>
 * Go to <a href=
 * "https://github.com/abdonvivas/KNDL">https://github.com/abdonvivas/KNDL</a>
 * for more information about the format of the data.<br>
 * <br>
 * 
 * @author Abd&oacute;n Alejandro Vivas Imparato
 *
 */
public class CalibrationThread extends Thread {

	private static final String TIME_FORMAT = "HH:mm:ss";
	private Thread thread;
	private String threadName;
	private MainWindow owner;
	private KNDLSerialComm serial;
	private boolean eom;// End Of Measure
	private boolean stopped;// Execution stopped by the user
	private boolean measureException;// Something went wrong during execution
	private boolean deadDevice;// If the device doesn't acknowledge a command
	private String logFileName;

	/**
	 * Creates a new <code>MeasureThread</code>.
	 * 
	 * @param name
	 *            Name of the thread (ID).
	 * @param owner
	 *            The owner <code>MainWindow</code> from where the thread is
	 *            started.
	 */
	public CalibrationThread(String name, MainWindow owner) {
		threadName = name;
		this.owner = owner;
		eom = false;
		stopped = false;
		measureException = false;
		deadDevice = false;
		serial = new KNDLSerialCommImpl();
	}

	/**
	 * Causes this thread to begin execution.
	 */
	public void start() {
		owner.clearMonitor();
		if (Boolean.valueOf(PropsMngr.getInstance().getProperty(KNDL.STORELOGS))) {
			logFileName = owner.logMonitor(threadName);
		} else {
			logFileName = "";
		}
		printDateOnMonitor();
		owner.printlnOnMonitor("Starting calibration process...", logFileName);
		if (thread == null) {
			thread = new Thread(this, threadName);
			thread.start();
		}
	}

	/**
	 * Method that activates the flag that stops the main task of the thread.
	 */
	public void setStopped() {
		stopped = true;
		owner.enableStop(false);
		printDateOnMonitor();
		owner.printlnOnMonitor("Calibration stopped... ", logFileName);
	}

	/**
	 * Main task of the thread.
	 */
	public void run() {
		// Stores each reading from the serial port
		StringBuilder response = new StringBuilder("");
		// Stores everything received from the serial port
		ArrayList<String> wholeResponse = new ArrayList<String>();

		/*--- ACK CHECKING RELATED CLASSES ---*/
		CheckACK checkACK = new CheckACK();
		ExecutorService checkACKExecutor = Executors.newSingleThreadExecutor();
		Future<Boolean> futureACK;
		/*--- ACK CHECKING RELATED CLASSES ---*/

		/*--- START DEVICE ---*/
		try {
			/*--- OPEN SERIAL PORT AND SEND CALIBRATION COMMAND AND CONFIGURATION ---*/
			serial.openSerialPort();
			serial.receiveData();// FLUSH
			printDateOnMonitor();
			owner.printlnOnMonitor("[KNDL] Starting device...", logFileName);
			serial.sendData(KNDLSerialComm.CALIBRATE);
			/*--- OPEN SERIAL PORT AND SEND START COMMAND AND CONFIGURATION ---*/

			/*--- CHECK FOR ACKNOWLEDGEMENT ---*/
			try {
				printDateOnMonitor();
				// owner.printlnOnMonitor("Waiting for Acknowledgement...");
				owner.printlnOnMonitor("Waiting acknowledgement...", logFileName);
				futureACK = checkACKExecutor.submit(checkACK);
				checkACKExecutor.shutdown(); // no new tasks will be accepted
				if (!futureACK.get(KNDLSerialComm.SERIAL_COMM_TIMEOUT, TimeUnit.MILLISECONDS)) {
					eom = true;
					measureException = true;
					deadDevice = true;
					printDateOnMonitor();
					owner.printlnOnMonitor("Bad response.", logFileName);
				} else {
					printDateOnMonitor();
					owner.printlnOnMonitor("Acknowledgement received.", logFileName);
				}
			} catch (TimeoutException e1) {
				eom = true;
				measureException = true;
				deadDevice = true;
				printDateOnMonitor();
				owner.printlnOnMonitor("The device doesn't answer.", logFileName);
			} finally {
				if (!checkACKExecutor.isTerminated()) {
					checkACKExecutor.shutdownNow();
				}
			}
			/*--- CHECK FOR ACKNOWLEDGEMENT ---*/
		} catch (Exception e2) {
			measureException = true;
			String msg = "An error ocurred starting the device: \n";
			JOptionPane.showMessageDialog(null, msg + e2.getMessage(), "Error", JOptionPane.WARNING_MESSAGE);
		}
		/*--- START DEVICE ---*/

		try {
			// Enables the stop button now that the serial port is open and
			// the start command was sent.
			owner.enableStop(true);

			/*--- GET RESPONSE WITHOUT ACK TO PRINT IT ON MONITOR(*) ---*/
			response.append(checkACK.getResponse());
			String aux[] = response.toString().split(KNDLSerialComm.SEPARATOR);
			response.setLength(0);
			for (int i = 0; i < aux.length; i++) {
				if (!KNDLSerialComm.ACK.equalsIgnoreCase(aux[i])) {
					response.append(aux[i]);
					response.append(KNDLSerialComm.SEPARATOR);
				}
			}
			checkACK.setResponse("");
			/*--- GET RESPONSE WITHOUT ACK TO PRINT IT ON MONITOR(*) ---*/

			/*
			 * (*) It is printed on monitor during the first iteration of the
			 * bellow loop
			 */

			/*--- STORE EVERY RESPONSE SPLITTED INTO AN ARRAY ---*/
			while (!eom & !stopped) {
				response.append(serial.receiveData());
				if (!"".equals(response.toString())) {
					// If the last character is not the serial port separator,
					// keep reading until it is
					while (!response.substring(response.length() - 1).equals(KNDLSerialComm.SEPARATOR) & !eom
							& !stopped) {
						response.append(serial.receiveData());
					}
					String splitted[] = response.toString().split(KNDLSerialComm.SEPARATOR);
					wholeResponse.addAll(Arrays.asList(splitted));
					printDateOnMonitor();
					owner.printlnOnMonitor("[DEVICE] --------------------> " + response, logFileName);
					if (KNDLSerialComm.END.equalsIgnoreCase(wholeResponse.get(wholeResponse.size() - 1))) {
						eom = true;
					}
					response.setLength(0);
				}
			}
			/*--- STORE EVERY RESPONSE SPLITTED INTO AN ARRAY ---*/
		} catch (Exception e1) {
			measureException = true;
			String msg = "An error ocurred during calibration:\n";
			JOptionPane.showMessageDialog(null, msg + e1.getMessage(), "Error", JOptionPane.WARNING_MESSAGE);
		} finally {

			if ((stopped || measureException) && !deadDevice) {
				/*--- STOP DEVICE ---*/
				try {
					try {
						printDateOnMonitor();
						owner.printlnOnMonitor("[KNDL] Stopping device...", logFileName);
						serial.sendData(KNDLSerialComm.STOP);
						checkACKExecutor = Executors.newSingleThreadExecutor();
						futureACK = checkACKExecutor.submit(checkACK);
						// no new tasks will be accepted
						checkACKExecutor.shutdown();
						if (!futureACK.get(KNDLSerialComm.SERIAL_COMM_TIMEOUT, TimeUnit.MILLISECONDS)) {
							printDateOnMonitor();
							owner.printlnOnMonitor(
									"Bad response. It is possible that you have to restart the device manually.",
									logFileName);
						}
					} catch (TimeoutException e1) {
						String msg = "The device doesn't answer.\n"
								+ "It is possible that you have to restart it manually.";
						JOptionPane.showMessageDialog(null, msg, "Error", JOptionPane.WARNING_MESSAGE);
					} finally {
						if (!checkACKExecutor.isTerminated()) {
							checkACKExecutor.shutdownNow();
						}
					}
				} catch (Exception e2) {
					String msgStop = "An error ocurred sending the stop command.\n"
							+ "The device must be restarted manually.";
					JOptionPane.showMessageDialog(null, msgStop, "Error", JOptionPane.WARNING_MESSAGE);
				}
				/*--- STOP DEVICE ---*/
			}

			/*--- CLOSE SERIAL PORT AND WRITE RESULTS ---*/
			// Disable the stop button now that the measure is over
			owner.enableStop(false);
			serial.closeSerialPort();

			if (!wholeResponse.isEmpty()) {
				printDateOnMonitor();
				owner.printlnOnMonitor("Storing calibration result...", logFileName);
				storeOffset(wholeResponse);
			}

			printDateOnMonitor();
			owner.printlnOnMonitor("Calibration complete.", logFileName);
			if (Boolean.valueOf(PropsMngr.getInstance().getProperty(KNDL.STORELOGS))) {
				owner.logMonitor(threadName);
			}
			printDateOnMonitor();
			owner.printlnOnMonitor("KNDL at rest", logFileName);
			owner.setMeasureState(MainWindow.IDLE);
			/*--- CLOSE SERIAL PORT AND WRITE RESULTS ---*/
		}
	}

	/*
	 * Stores the calibration offset
	 */
	private void storeOffset(ArrayList<String> wholeResponse) {
		PropsMngr propsMngr = PropsMngr.getInstance();
		double offset = 0;
		try {
			/*--- PARSE RESPONSES FROM MICROPROCESSOR TO OBTAIN THE CALIBRATION OFFSET ---*/
			Iterator<String> itr = wholeResponse.iterator();
			while (itr.hasNext()) {
				String maxwell = itr.next();// AUX variable
				if (KNDLSerialComm.DATA.equalsIgnoreCase(maxwell)) {
					offset = Double.parseDouble(itr.next());
				}
			}
			/*--- PARSE RESPONSES FROM MICROPROCESSOR TO OBTAIN THE CALIBRATION OFFSET ---*/
		} catch (IndexOutOfBoundsException | NumberFormatException e1) {
			JOptionPane.showMessageDialog(null,
					"The results couldn't be generated ddue to errors in the measure process.\n"
							+ "It is possible that the device is using a wrong data format",
					"Error", JOptionPane.WARNING_MESSAGE);
			owner.setMeasureState(MainWindow.IDLE);
		} catch (NoSuchElementException e2) {
			/*
			 * Do nothing. This means you couldnt't recover the whole data after
			 * a measureException or a stopped
			 */
		} finally {
			try {
				propsMngr.setProperty(KNDL.CALOFFSET, String.valueOf(offset));
			} catch (IOException e) {
				/*
				 * Do nothing. It doesn't matter if the calibration offset
				 * property is not written into a file. It is a special property
				 * and never depends on the user.
				 */
			}
		}
	}

	/*
	 * Callable object whose task is to receive data from the serial port and
	 * check if there is any ACK within the data.
	 */
	private class CheckACK implements Callable<Boolean> {
		private String response = "";

		@Override
		public Boolean call() throws Exception {
			boolean ack = false;
			while (!ack) {
				response += serial.receiveData();
				// If the last character is not the serial port separator,
				// keep reading until it is
				if (!"".equals(response)) {
					while (!response.substring(response.length() - 1).equals(KNDLSerialComm.SEPARATOR)) {
						response += serial.receiveData();
					}
					String aux[] = response.split(KNDLSerialComm.SEPARATOR);
					for (int i = 0; i < aux.length; i++) {
						if (KNDLSerialComm.ACK.equalsIgnoreCase(aux[i])) {
							ack = true;
							break;
						}
					}
				}
			}
			return ack;
		}

		public void setResponse(String response) {
			this.response = response;
		}

		public String getResponse() {
			return response;
		}
	}

	/*
	 * Prints the date on the main window monitor.
	 */
	private void printDateOnMonitor() {
		SimpleDateFormat sdf = new SimpleDateFormat(TIME_FORMAT);
		Calendar calendar = new GregorianCalendar();
		owner.printOnMonitor("[KNDL] --- " + sdf.format(calendar.getTime()) + " --- ", logFileName);
	}
}

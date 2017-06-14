package es.upm.ies.goniophotometer.threads;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
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
import es.upm.ies.goniophotometer.exceptions.InconsistentMeasureException;
import es.upm.ies.goniophotometer.gui.MainWindow;
import es.upm.ies.goniophotometer.photometry.HVFWHM;
import es.upm.ies.goniophotometer.photometry.PhotometryCalculations;
import es.upm.ies.goniophotometer.photometry.SphericalCoordinates;
import es.upm.ies.goniophotometer.photometry.SphericalCoordinatesComparator;
import es.upm.ies.goniophotometer.serial.KNDLSerialComm;
import es.upm.ies.goniophotometer.serial.KNDLSerialCommImpl;
import es.upm.ies.goniophotometer.utils.CSVWriter;
import es.upm.ies.goniophotometer.utils.PropsMngr;

/**
 * This thread does the automatic measure while the main thread maintains the
 * GUI functionality active in case the user wants to stop the measure. More
 * specifically, it starts the serial port communications, sends necessary
 * commands, receives all the data, computes calculations and writes the results
 * in a CSV (Comma-Separated Values) file.<br>
 * <br>
 * Go to <a href=
 * "https://github.com/abdonvivas/KNDL">https://github.com/abdonvivas/KNDL</a>
 * for more information about the format of the data.<br>
 * <br>
 * The results are stored as luminous intensities (Candela) and if the user
 * marked the option of computing luminous flux and horiontal and vertical FWHM,
 * they are also stored in the CSV file.
 * 
 * @author Abd&oacute;n Alejandro Vivas Imparato
 *
 */
public class MeasureThread extends Thread {

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
	public MeasureThread(String name, MainWindow owner) {
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
		owner.printlnOnMonitor("Starting measure " + threadName + "... ", logFileName);
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
		owner.printlnOnMonitor("Finishing measure: " + threadName + "... ", logFileName);
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

		/*--- READ PROPERTIES AND COMPUTE SPS ---*/
		PropsMngr propsMngr = PropsMngr.getInstance();
		float resTheta = Float.parseFloat(propsMngr.getProperty(KNDL.RESTHETA));
		float dpsTheta = Float.parseFloat(propsMngr.getProperty(KNDL.DPSTHETA));
		// SPS = Steps Per Sample
		int spsTheta = (short) (resTheta / dpsTheta);
		float resPhi = Float.parseFloat(propsMngr.getProperty(KNDL.RESPHI));
		float dpsPhi = Float.parseFloat(propsMngr.getProperty(KNDL.DPSPHI));
		int spsPhi = (short) (resPhi / dpsPhi);
		/*--- READ PROPERTIES AND COMPUTE SPS ---*/

		/*--- START DEVICE ---*/
		try {
			/*--- OPEN SERIAL PORT AND SEND START COMMAND AND CONFIGURATION ---*/
			serial.openSerialPort();
			serial.receiveData();// FLUSH
			printDateOnMonitor();
			owner.printlnOnMonitor("[KNDL] Starting device...", logFileName);
			serial.sendData(KNDLSerialComm.START);
			owner.printlnOnMonitor("[KNDL] Sending configuration... (1/4)", logFileName);
			serial.sendData(String.valueOf(dpsTheta));
			owner.printlnOnMonitor("[KNDL] Sending configuration... (2/4)", logFileName);
			serial.sendData(String.valueOf(dpsPhi));
			owner.printlnOnMonitor("[KNDL] Sending configuration... (3/4)", logFileName);
			serial.sendData(String.valueOf(spsTheta));
			owner.printlnOnMonitor("[KNDL] Sending configuration... (4/4)", logFileName);
			serial.sendData(String.valueOf(spsPhi));
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
					// owner.printlnOnMonitor("Bad response.");
					owner.printlnOnMonitor("Bad response", logFileName);
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
			String msg = "An error ocurred during the measuring process:\n";
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
			printDateOnMonitor();
			owner.printlnOnMonitor("Stopped measure: " + threadName + ".", logFileName);

			if (!wholeResponse.isEmpty()) {
				printDateOnMonitor();
				owner.printlnOnMonitor("Storing results: " + threadName + "...", logFileName);
				writeResults(wholeResponse);
			}
			printDateOnMonitor();
			owner.printlnOnMonitor("KNDL at rest", logFileName);
			owner.setMeasureState(MainWindow.IDLE);
			/*--- CLOSE SERIAL PORT AND WRITE RESULTS ---*/
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
	 * Writes results into a CSV file. If applicable, it also computes the
	 * luminous flux and writes it in the CSV file as well.
	 */
	private void writeResults(ArrayList<String> wholeResponse) {
		PropsMngr propsMngr = PropsMngr.getInstance();

		ArrayList<SphericalCoordinates> lumints = new ArrayList<SphericalCoordinates>();

		// Square distance from the source to the sensor
		double sqd = Double.parseDouble(propsMngr.getProperty(KNDL.SENSORDISTANCE));
		sqd = sqd * sqd;

		double offset = Double.parseDouble(propsMngr.getProperty(KNDL.CALOFFSET));

		float resTheta = Float.parseFloat(PropsMngr.getInstance().getProperty(KNDL.RESTHETA));
		float resPhi = Float.parseFloat(PropsMngr.getInstance().getProperty(KNDL.RESPHI));

		try {
			owner.printlnOnMonitor("[KNDL] Analyzing data...", logFileName);
			/*--- PARSE RESPONSES FROM MICROPROCESSOR TO OBTAIN LUMINOUS INTENSITY CLASSES ---*/
			double v2lux = Double.parseDouble(propsMngr.getProperty(KNDL.VOLTS2LUX));
			Iterator<String> itr = wholeResponse.iterator();
			float theta;
			float phi;
			double value;
			while (itr.hasNext()) {
				String maxwell = itr.next();// AUX variable
				if (KNDLSerialComm.DATA.equalsIgnoreCase(maxwell)) {
					theta = Float.parseFloat(itr.next());
					phi = Float.parseFloat(itr.next());
					value = Double.parseDouble(itr.next()) - offset;
					value = value < 0 ? 0 : value;
					value *= v2lux;
					value *= sqd;
					lumints.add(new SphericalCoordinates(theta, phi, value));
				}
			}
			/*--- PARSE RESPONSES FROM MICROPROCESSOR TO OBTAIN LUMINOUS INTENSITY CLASSES ---*/
		} catch (IndexOutOfBoundsException | NumberFormatException e1) {
			JOptionPane.showMessageDialog(null,
					"The results couldn't be generated due to errors in the measure process.\n"
							+ "It is possible that the device is using a wrong data format",
					"Error", JOptionPane.WARNING_MESSAGE);
			owner.setMeasureState(MainWindow.IDLE);
		} catch (NoSuchElementException e2) {
			/*
			 * Do nothing. This means you couldnt't recover the whole data after
			 * a measureException or a stopped
			 */
		}

		// Number of Measures
		int nMeasuresSemiSphere = (int) ((360 / resPhi) * (90 / resTheta) + 1);
		int nMeasuresFullSphere = (int) ((360 / resPhi) * ((180 / resTheta) - 1) + 2);
		printDateOnMonitor();
		owner.printlnOnMonitor("Expected number of  measures (semisphere): " + nMeasuresSemiSphere, logFileName);
		printDateOnMonitor();
		owner.printlnOnMonitor("Expected number of  measures (full sphere, maximum): " + nMeasuresFullSphere,
				logFileName);
		printDateOnMonitor();
		owner.printlnOnMonitor("Actual number of measures: " + lumints.size(), logFileName);

		/*--- SORT VALUES ---*/
		Collections.sort(lumints, new SphericalCoordinatesComparator());
		/*--- SORT VALUES ---*/

		SimpleDateFormat sdf = new SimpleDateFormat(propsMngr.getProperty(KNDL.RESULTSPREFIX));
		Calendar calendar = new GregorianCalendar();
		String filename = sdf.format(calendar.getTime()) + "_" + threadName;
		CSVWriter csvWriter = new CSVWriter(propsMngr.getProperty(KNDL.RESULTSPATH) + "//" + filename,
				propsMngr.getProperty(KNDL.SEPARATOR));

		try {
			printDateOnMonitor();
			owner.printlnOnMonitor("Writing CSV file...", logFileName);
			/*--- WRITE CSV FILE ---*/
			csvWriter.createCSV();

			if (stopped) {
				printDateOnMonitor();
				owner.printlnOnMonitor("[KNDL] MEASURE STOPPED BY USER", logFileName);
				csvWriter.writeField("MEASURE STOPPED BY USER");
				csvWriter.nextLine();
				csvWriter.writeField("Results based on available data.");
			} else if (measureException) {
				owner.printlnOnMonitor("[KNDL] MEASURE STOPPED DUE TO ERRORS", logFileName);
				csvWriter.writeField("MEASURE STOPPED DUE TO ERRORS");
				csvWriter.nextLine();
				csvWriter.writeField("Results based on available data.");
			}

			/*--- WRITE LUMFLUX, H AND V FWHM AND DISTANCE ---*/
			if (Boolean.valueOf(propsMngr.getProperty(KNDL.PHOTOCALCS))) {
				printDateOnMonitor();
				owner.printlnOnMonitor("Computing luminous flux...", logFileName);
				double lumFlux = PhotometryCalculations.computeLumFlux(lumints, resTheta, resPhi);
				owner.printOnMonitor("Luminous flux (lm): ", logFileName);
				owner.printlnOnMonitor(String.format("%.2f", lumFlux), logFileName);

				printDateOnMonitor();
				owner.printlnOnMonitor("Computing horizontal and vertical FWHM...", logFileName);
				HVFWHM hvFWHM = PhotometryCalculations.computeHVFWHM(lumints, resTheta, resPhi);
				owner.printOnMonitor("Vertical FWHM (degrees): ", logFileName);
				owner.printlnOnMonitor(String.format("%.2f", hvFWHM.getVFWHM()), logFileName);
				owner.printOnMonitor("Horizontal FWHM (degrees): ", logFileName);
				owner.printlnOnMonitor(String.format("%.2f", hvFWHM.getHFWHM()), logFileName);

				csvWriter.nextLine();
				csvWriter.writeField("Luminous flux (lm): ");
				csvWriter.writeField(String.format("%.2f", lumFlux));
				csvWriter.nextLine();
				csvWriter.writeField("Horizontal FWHM (degrees): ");
				csvWriter.writeField(String.format("%.2f", hvFWHM.getHFWHM()));
				csvWriter.nextLine();
				csvWriter.writeField("Vertical FWHM (degrees): ");
				csvWriter.writeField(String.format("%.2f", hvFWHM.getVFWHM()));
			}

			csvWriter.nextLine();
			csvWriter.writeField("Source-to-sensor distance (m)");
			csvWriter.writeField(propsMngr.getProperty(KNDL.SENSORDISTANCE));
			csvWriter.nextLine();
			csvWriter.writeField("Calibration offset (V)");
			csvWriter.writeField(propsMngr.getProperty(KNDL.CALOFFSET));
			/*--- WRITE LUMFLUX, H AND V FWHM AND DISTANCE ---*/

			csvWriter.nextLine();
			csvWriter.nextLine();
			csvWriter.writeField("Polar angle (º)");
			csvWriter.writeField("Azimuth angle (º)");
			csvWriter.writeField("Luminous intensity (cd)");

			for (SphericalCoordinates lumint : lumints) {
				csvWriter.nextLine();
				csvWriter.writeField(String.valueOf(lumint.getTheta()));
				csvWriter.writeField(String.valueOf(lumint.getPhi()));
				csvWriter.writeField(String.valueOf(lumint.getR()));
			}

			csvWriter.closeCSV();
			/*--- WRITE CSV FILE ---*/
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "Error generating the CSV file.", "Error", JOptionPane.WARNING_MESSAGE);
			owner.setMeasureState(MainWindow.IDLE);
		} catch (InconsistentMeasureException e1) {
			JOptionPane.showMessageDialog(null, e1.getMessage(), "Error", JOptionPane.WARNING_MESSAGE);
			owner.setMeasureState(MainWindow.IDLE);
		}

		/*--- WRITE RAW FILE ---*/
		if (Boolean.valueOf(propsMngr.getProperty(KNDL.STORERAWS))) {
			if (!stopped && !measureException) {
				String rawName = filename + "_RAW";
				csvWriter.setFileName(propsMngr.getProperty(KNDL.RESULTSPATH) + KNDL.RAWSDIRECTORY + rawName);
				printDateOnMonitor();
				owner.printlnOnMonitor("Writing raw file...", logFileName);
				writeRawFile(csvWriter, lumints);
			} else {
				printDateOnMonitor();
				owner.printlnOnMonitor("The raw file wasn't stored because the measure didn't end normally.",
						logFileName);
			}
		}
		/*--- WRITE RAW FILE ---*/
	}

	/*
	 * Writes the raw file.
	 */
	private void writeRawFile(CSVWriter csvWriter, ArrayList<SphericalCoordinates> lumints) {
		try {
			File rawsDir = new File(PropsMngr.getInstance().getProperty(KNDL.RESULTSPATH) + KNDL.RAWSDIRECTORY);
			if (!rawsDir.exists()) {
				rawsDir.mkdir();
			}

			/*--- WRITE CSV FILE ---*/
			csvWriter.createCSV();

			for (SphericalCoordinates lumint : lumints) {
				csvWriter.writeField(String.valueOf(lumint.getTheta()));
				csvWriter.writeField(String.valueOf(lumint.getPhi()));
				csvWriter.writeField(String.valueOf(lumint.getR()));
				csvWriter.nextLine();
			}

			csvWriter.closeCSV();
			/*--- WRITE CSV FILE ---*/
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "Error generating raw files.", "Error", JOptionPane.WARNING_MESSAGE);
			owner.setMeasureState(MainWindow.IDLE);
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

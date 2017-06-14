package es.upm.ies.goniophotometer;

import java.awt.Font;
import java.io.IOException;
import java.util.Properties;
import java.util.regex.PatternSyntaxException;

import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;

import es.upm.ies.goniophotometer.gui.MainWindow;
import es.upm.ies.goniophotometer.serial.KNDLSerialComm;
import es.upm.ies.goniophotometer.serial.KNDLSerialCommImpl;
import es.upm.ies.goniophotometer.utils.CSVWriter;
import es.upm.ies.goniophotometer.utils.PropsMngr;

/**
 * KNDL, pronounced 'candle', is a software that runs on a computer as part of
 * an automated goniophotometer. The components of the goniophotometer must
 * implement KNDL Architecture. For more information about KNDL Architecture, go
 * to <a href=
 * "https://github.com/abdonvivas/KNDL">https://github.com/abdonvivas/KNDL</a><br>
 * <br>
 * This main class loads the default properties and the last-used properties
 * from the default_config.properties and config.properties files, respectively.
 * It also defines the developer default properties (see <code><a href=
 * "./utils/PropsMngr.html">PropsMngr</a></code> class for more information
 * about developer default properties). The developer default properties are as
 * follow:<br>
 * <br>
 * calculateLuminousFlux=true<br>
 * defaultDevice=.*[aA]rduino.*<br>
 * deviceInUse=<br>
 * degreesPerStepPhi=0.5<br>
 * degreesPerStepTheta=0.5<br>
 * degreesPerSamplePhi=0.5<br>
 * degreesPerSampleTheta=0.5<br>
 * resultsPath=[Directory from where KNDL's being executed]<br>
 * resultsPrefix=yy_MM_dd-HH'h'mm<br>
 * sensor2sourceDistance=1<br>
 * separator=,<br>
 * storeLogs=false<br>
 * storeRaws=false<br>
 * volts2luxRelation=1<br>
 * <br>
 * 
 * Copyright (C) 2017 Abdon Alejandro Vivas Imparato.<br>
 * <br>
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.<br>
 * <br>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.<br>
 * <br>
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see &#60;<a href=
 * "http://www.gnu.org/licenses/">http://www.gnu.org/licenses/&#62;</a>.
 * 
 * @author Abd&oacute;n Alejandro Vivas Imparato
 * @version 1.0
 *
 */
public class KNDL {
	private static MainWindow mainWindow;
	/**
	 * Name of the software.
	 */
	public static final String SOFTWARENAME = "KNDL";
	/**
	 * Version of the software.
	 */
	public static final String SOFTWAREVERSION = "1.0";

	private static final String LICENSE_NOTICE = SOFTWARENAME + " v" + SOFTWAREVERSION
			+ " Copyright © 2017 Abdon Alejandro Vivas Imparato\n" + "This program comes with ABSOLUTELY NO WARRANTY.\n"
			+ "This is free software, and you are welcome to redistribute it\n"
			+ "under certain conditions. Go to 'Help -> About KNDL' for details.";

	/**
	 * Font to be used by the GUI
	 */
	public static final FontUIResource GUIFONT = new FontUIResource("Arial", Font.BOLD, 14);
	/**
	 * Name of the property that indicates whether to do the photometry
	 * calculations (Luminous flux, and vertical and horizontal FWHM) or not.
	 */
	public static final String PHOTOCALCS = "photometryCalculations";
	/**
	 * Name of the property that indicates the offset in volts to be subtracted
	 * to each measure. This property is set by the calibration process.
	 */
	public static final String CALOFFSET = "calibrationOffset";
	/**
	 * Name of the property that indicates a regular expression to search for a
	 * default device in a list of the operating system-defined descriptions of
	 * the serial connected devices. This property can be changed in the
	 * advanced configuration window.
	 */
	public static final String DEFDEVREGEX = "defaultDeviceRegularExpresion";
	/**
	 * Name of the property that indicates the device to be used.
	 */
	public static final String DEVICE = "deviceInUse";
	/**
	 * Name of the property that indicates the degrees advanced per step on the
	 * azimuth angle. This property depends on the goniophotometer
	 * implementation.
	 */
	public static final String DPSPHI = "degreesPerStepPhi";
	/**
	 * Name of the property that indicates the degrees advanced per step on the
	 * polar angle. This property depends on the goniophotometer implementation.
	 */
	public static final String DPSTHETA = "degreesPerStepTheta";
	/**
	 * Name of the property that indicates the desired resolution for the
	 * measure, in degrees per sample (azimuth angle).
	 */
	public static final String RESPHI = "degreesPerSamplePhi";
	/**
	 * Name of the property that indicates the desired resolution for the
	 * measure, in degrees per sample (polar angle).
	 */
	public static final String RESTHETA = "degreesPerSampleTheta";
	/**
	 * Name of the property that indicates the path where the results are to be
	 * located.
	 */
	public static final String RESULTSPATH = "resultsPath";
	/**
	 * Name of the property that indicates the prefix to be used at the
	 * beginning of the results filename.
	 */
	public static final String RESULTSPREFIX = "resultsPrefix";
	/**
	 * Name of the property that indicates the distance between the light source
	 * and the lux meter. This property depends on the goniophotometer
	 * implementation.
	 */
	public static final String SENSORDISTANCE = "sensor2sourceDistance";
	/**
	 * Name of the property that indicates which separator to use when writing a
	 * CSV file.
	 */
	public static final String SEPARATOR = "separator";
	/**
	 * Name of the property that indicates whether to store log files or not.
	 */
	public static final String STORELOGS = "storeLogs";
	/**
	 * Name of the property that indicates whether to store raw files or not.
	 */
	public static final String STORERAWS = "storeRaws";
	/**
	 * Name of the property that indicates the relation between volts and lux
	 * ([volts2Lux]=[Lux/V]) for the specific lux meter used in the
	 * goniophotometer implementation.
	 */
	public static final String VOLTS2LUX = "volts2luxRelation";
	/**
	 * Prefix options available for the results filename.
	 */
	public static final String[] PREFIXDATEFORMAT = { "yyyy_MM_dd-HH'h'mm", "yy_MM_dd-HH'h'mm", "HH'h'mm" };
	/**
	 * Relative path to the directory where the log files will be stored.
	 */
	public static final String LOGSDIRECTORY = ".//logs//";
	/**
	 * Path to be appended to the results path and in which the raw files will
	 * be stored.
	 */
	public static final String RAWSDIRECTORY = "//raws//";

	/**
	 * Main function.
	 * 
	 * @param args
	 *            No arguments are needed to be passed to KNDL.
	 */
	public static void main(String[] args) {
		final PropsMngr propsMngr = PropsMngr.getInstance();
		final KNDLSerialComm serial = new KNDLSerialCommImpl();
		final Properties developerDefaults = new Properties();

		/*--- SET DEVELOPER DEFAULT PROPERTIES ---*/
		developerDefaults.setProperty(PHOTOCALCS, "true");
		developerDefaults.setProperty(CALOFFSET, "0");
		developerDefaults.setProperty(DEFDEVREGEX, ".*[aA]rduino.*");
		developerDefaults.setProperty(DEVICE, "");
		developerDefaults.setProperty(DPSPHI, String.valueOf(0.5));
		developerDefaults.setProperty(DPSTHETA, String.valueOf(0.5));
		developerDefaults.setProperty(RESPHI, String.valueOf(0.5));
		developerDefaults.setProperty(RESTHETA, String.valueOf(0.5));
		developerDefaults.setProperty(RESULTSPATH, System.getProperty("user.dir"));
		developerDefaults.setProperty(RESULTSPREFIX, PREFIXDATEFORMAT[1]);
		developerDefaults.setProperty(SENSORDISTANCE, "1");
		developerDefaults.setProperty(SEPARATOR, CSVWriter.COMMA);
		developerDefaults.setProperty(STORELOGS, "false");
		developerDefaults.setProperty(STORERAWS, "false");
		developerDefaults.setProperty(VOLTS2LUX, String.valueOf(1));
		propsMngr.setDeveloperDefaults(developerDefaults);
		/*--- SET PASSIVE DEFAULT PROPERTIES ---*/

		/*--- LOAD PROPERTIES ---*/
		propsMngr.loadProperties();
		if ("".equals(propsMngr.getProperty(DEVICE))) {
			try {
				propsMngr.setDefaultProperty(DEVICE, serial.matchRegEx(propsMngr.getProperty(DEFDEVREGEX)));
				propsMngr.setProperty(DEVICE, propsMngr.getDefaultProperty(DEVICE));
			} catch (PatternSyntaxException e1) {
				try {
					propsMngr.setProperty(DEFDEVREGEX, "");
				} catch (IOException e) {
					/*
					 * Do nothing. It doesn't matter if the device property is
					 * not written into a file. It is a special property and
					 * don't always depends on the user.
					 */
				}
			} catch (IOException e2) {
				/*
				 * Do nothing. It doesn't matter if the device property is not
				 * written into a file. It is a special property and don't
				 * always depends on the user.
				 */
			}
		}

		/*--- START GUI ---*/
		setUIFont(GUIFONT);
		mainWindow = new MainWindow();
		mainWindow.setVisible(true);
		mainWindow.printOnMonitor(LICENSE_NOTICE, "");
		// JOptionPane.showMessageDialog(null, "Remember to make the calibration
		// process before measuring.",
		// "Reminder", JOptionPane.WARNING_MESSAGE);
		/*--- START GUI ---*/
	}

	public static void setUIFont(FontUIResource font) {
		java.util.Enumeration<Object> keys = UIManager.getDefaults().keys();
		while (keys.hasMoreElements()) {
			Object key = keys.nextElement();
			Object value = UIManager.get(key);
			if (value instanceof FontUIResource) {
				UIManager.put(key, font);
			}
		}
	}
}

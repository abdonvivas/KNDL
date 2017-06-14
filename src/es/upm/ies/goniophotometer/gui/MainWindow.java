package es.upm.ies.goniophotometer.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

import javax.imageio.ImageIO;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import javax.swing.text.DefaultCaret;

import es.upm.ies.goniophotometer.KNDL;
import es.upm.ies.goniophotometer.serial.KNDLSerialComm;
import es.upm.ies.goniophotometer.serial.KNDLSerialCommImpl;
import es.upm.ies.goniophotometer.threads.CalibrationThread;
import es.upm.ies.goniophotometer.threads.MeasureThread;
import es.upm.ies.goniophotometer.utils.PropsMngr;

/**
 * Main window of KNDL's GUI.
 * 
 * @author Abd&oacute;n Alejandro Vivas Imparato
 *
 */
public class MainWindow extends JFrame {

	private static final long serialVersionUID = 1L;

	/**
	 * Indicates that KNDL is in measuring state.
	 */
	public static final boolean MEASURING = true;
	/**
	 * Indicates that KNDL is in idle state.
	 */
	public static final boolean IDLE = false;

	private static final String GREENLIGHTPATH = ".//img//Green_Light_24x24.png";
	private static final String REDLIGHTPATH = ".//img//Red_Light_24x24.png";
	private static final String LOGOPATH = ".//img//KNDLlogo.png";

	private static boolean measureState = IDLE;
	private MeasureThread t1;
	private CalibrationThread tcal;

	private ImageIcon greenLight;
	private ImageIcon redLight;
	// private ImageIcon logo;

	private JLabel availableDeviceImg;
	private JLabel measureStateImg;
	private JLabel logoImg;

	private JButton startButton;
	private JButton stopButton;
	private JButton calibrationButton;
	private JButton confDialogButton;
	private JButton exitButton;

	private JTextField measureNameTextField;

	private JTextArea monitor;

	private JMenuItem startItem;
	private JMenuItem stopItem;
	private JMenuItem calibrateItem;
	private JMenu configMenu;
	private JMenu helpMenu;
	private JCheckBoxMenuItem checkRaws;
	private JCheckBoxMenuItem checkLogs;

	/**
	 * Creates a new <code>MainWindow</code> instance.
	 */
	public MainWindow() {
		setTitle(KNDL.SOFTWARENAME);
		setLocationByPlatform(true);
		addWindowListener(new ExitProgram());
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		setLayout(new BorderLayout(5, 5));
		setMinimumSize(new Dimension(550, 400));

		// Image initialization
		imageInit();

		// Panels
		add(buildNorthPanel(), BorderLayout.NORTH);
		add(buildCenterPanel(), BorderLayout.CENTER);
		add(buildSouthPanel(), BorderLayout.SOUTH);
		// PADDING
		add(Box.createRigidArea(new Dimension(0, 0)), BorderLayout.WEST);
		add(Box.createRigidArea(new Dimension(0, 0)), BorderLayout.EAST);

		// Menus
		setJMenuBar(buildMenus());

		pack();
	}

	/*
	 * Sets the following constraints: gridy, gridx, weighty, weightx and fill.
	 * (See GridBagConstraints for more information).
	 */
	private void setBasicConstr(GridBagConstraints c, int gridy, int gridx, double weighty, double weightx, int fill) {
		c.gridy = gridy;
		c.gridx = gridx;
		c.weighty = weighty;
		c.weightx = weightx;
		c.fill = fill;
	}

	/*
	 * Builds KNDL's menus.
	 */
	private JMenuBar buildMenus() {
		JMenuBar menuBar = new JMenuBar();

		/*--- ACTIONS MENU ---*/
		JMenu actionsMenu = new JMenu("Actions");
		startItem = new JMenuItem("Start measure");
		startItem.addActionListener(new StartMeasureListener());
		stopItem = new JMenuItem("Stop measure");
		stopItem.addActionListener(new StopMeasureListener());
		stopItem.setEnabled(false);
		calibrateItem = new JMenuItem("Calibrate");
		calibrateItem.addActionListener(new CalibrateListener());
		JMenuItem exitItem = new JMenuItem("Exit");
		exitItem.addActionListener(new ExitListener());
		actionsMenu.add(startItem);
		actionsMenu.add(stopItem);
		actionsMenu.addSeparator();
		actionsMenu.add(calibrateItem);
		actionsMenu.addSeparator();
		actionsMenu.add(exitItem);
		menuBar.add(actionsMenu);
		/*--- ACTIONS MENU ---*/

		/*--- CONFIGURATION MENU ---*/
		configMenu = new JMenu("Configuration");
		JMenuItem configItem = new JMenuItem("Edit configuration");
		configItem.addActionListener(new GoToConfigDialog());
		JMenu advConfMenu = new JMenu("Advanced configuration");
		advConfMenu.addMenuListener(new AdvConfMenuListener());
		checkRaws = new JCheckBoxMenuItem("Store raw files");
		checkRaws.addItemListener(new CheckRawsItemListener());
		checkLogs = new JCheckBoxMenuItem("Store log files");
		checkLogs.addItemListener(new CheckLogsItemListener());
		JMenuItem resetOffsetItem = new JMenuItem("Reset offset");
		resetOffsetItem.addActionListener(new ResetOffsetListener());
		JMenuItem chngDefDevItem = new JMenuItem("Change default device");
		chngDefDevItem.addActionListener(new GoToChngDefDevDialog());
		JMenuItem writeDefsItem = new JMenuItem("Set current properties as default properties");
		writeDefsItem.addActionListener(new MkPropsDefault());

		configMenu.add(configItem);
		configMenu.add(advConfMenu);
		advConfMenu.add(checkRaws);
		advConfMenu.add(checkLogs);
		advConfMenu.addSeparator();
		advConfMenu.add(resetOffsetItem);
		advConfMenu.addSeparator();
		advConfMenu.add(chngDefDevItem);
		advConfMenu.add(writeDefsItem);
		menuBar.add(configMenu);
		/*--- CONFIGURATION MENU ---*/

		/*--- HELP MENU ---*/
		helpMenu = new JMenu("Help");
		JMenuItem aboutItem = new JMenuItem("About KNDL");
		aboutItem.addActionListener(new GoToAboutKNDLDialog());
		helpMenu.add(aboutItem);
		menuBar.add(helpMenu);
		/*--- HELP MENU ---*/

		return menuBar;
	}

	/*
	 * Builds north panel.
	 */
	private JPanel buildNorthPanel() {
		JPanel northPanel = new JPanel(new GridBagLayout());
		GridBagConstraints cNorth = new GridBagConstraints();

		/*--- BUTTONS ---*/
		JPanel buttonsPanel = new JPanel(new GridBagLayout());
		GridBagConstraints cButtons = new GridBagConstraints();
		cButtons.ipadx = 15;
		cButtons.ipady = 15;

		startButton = new JButton("Start measure");
		startButton.addActionListener(new StartMeasureListener());
		setBasicConstr(cButtons, 0, 0, 0., 0.0, GridBagConstraints.BOTH);
		cButtons.insets = new Insets(0, 0, 5, 0);
		buttonsPanel.add(startButton, cButtons);

		stopButton = new JButton("Stop measure");
		stopButton.addActionListener(new StopMeasureListener());
		stopButton.setEnabled(false);
		setBasicConstr(cButtons, 1, 0, 0.0, 0.0, GridBagConstraints.BOTH);
		cButtons.insets = new Insets(0, 0, 0, 0);
		buttonsPanel.add(stopButton, cButtons);

		cNorth.gridheight = 4;
		setBasicConstr(cNorth, 0, 0, 0.0, 0.3, GridBagConstraints.HORIZONTAL);
		northPanel.add(buttonsPanel, cNorth);
		/*--- BUTTONS ---*/

		/*--- LOGO ---*/
		cNorth.gridheight = 4;
		setBasicConstr(cNorth, 0, 1, 0.0, 0.4, GridBagConstraints.HORIZONTAL);
		northPanel.add(logoImg, cNorth);
		/*--- LOGO ---*/

		/*--- INDICATORS ---*/
		JLabel availableDeviceLabel = new JLabel("Device state: ", SwingConstants.CENTER);
		cNorth.gridheight = 1;
		setBasicConstr(cNorth, 0, 2, 0.0, 0.3, GridBagConstraints.HORIZONTAL);
		northPanel.add(availableDeviceLabel, cNorth);
		cNorth.gridheight = 1;
		setBasicConstr(cNorth, 1, 2, 0.0, 0.3, GridBagConstraints.HORIZONTAL);
		northPanel.add(availableDeviceImg, cNorth);

		JLabel measureStateLabel = new JLabel("Measure state: ", SwingConstants.CENTER);
		cNorth.gridheight = 1;
		setBasicConstr(cNorth, 2, 2, 0.0, 0.3, GridBagConstraints.HORIZONTAL);
		northPanel.add(measureStateLabel, cNorth);
		cNorth.gridheight = 1;
		setBasicConstr(cNorth, 3, 2, 0.0, 0.3, GridBagConstraints.HORIZONTAL);
		northPanel.add(measureStateImg, cNorth);
		/*--- INDICATORS ---*/

		return northPanel;
	}

	/*
	 * Builds center panel
	 */
	private JPanel buildCenterPanel() {
		JPanel centerPanel = new JPanel(new BorderLayout(0, 5));

		JPanel measureNamePanel = new JPanel();
		measureNamePanel.setLayout(new BoxLayout(measureNamePanel, BoxLayout.X_AXIS));
		JLabel measureNameLabel = new JLabel("Output file name:");
		measureNameTextField = new JTextField();
		measureNamePanel.add(measureNameLabel);
		measureNamePanel.add(measureNameTextField);

		monitor = new JTextArea();
		monitor.setEditable(false);
		// Next line configures autoscroll
		((DefaultCaret) monitor.getCaret()).setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		JScrollPane monitorPanel = new JScrollPane(monitor);
		monitor.setFont(monitor.getFont().deriveFont(Font.PLAIN));

		centerPanel.add(measureNamePanel, BorderLayout.NORTH);
		centerPanel.add(monitorPanel, BorderLayout.CENTER);
		centerPanel.add(new JLabel(), BorderLayout.EAST);

		return centerPanel;
	}

	/*
	 * Builds south panel.
	 */
	private JPanel buildSouthPanel() {
		JPanel southPanel = new JPanel(new GridBagLayout());
		GridBagConstraints cSouth = new GridBagConstraints();
		cSouth.ipadx = 15;
		cSouth.ipady = 15;
		cSouth.insets = new Insets(0, 0, 5, 0);

		calibrationButton = new JButton("Calibrate");
		calibrationButton.addActionListener(new CalibrateListener());
		confDialogButton = new JButton("Configuration");
		confDialogButton.addActionListener(new GoToConfigDialog());
		exitButton = new JButton("Exit");
		exitButton.addActionListener(new ExitListener());

		setBasicConstr(cSouth, 0, 0, 0.0, 0.45, GridBagConstraints.NONE);
		southPanel.add(calibrationButton, cSouth);
		setBasicConstr(cSouth, 0, 1, 1.0, 0.1, GridBagConstraints.BOTH);
		southPanel.add(new JLabel(), cSouth);
		setBasicConstr(cSouth, 0, 2, 0.0, 0.45, GridBagConstraints.NONE);
		southPanel.add(confDialogButton, cSouth);
		setBasicConstr(cSouth, 0, 3, 1.0, 0.1, GridBagConstraints.BOTH);
		southPanel.add(new JLabel(), cSouth);
		setBasicConstr(cSouth, 0, 4, 0.0, 0.45, GridBagConstraints.NONE);
		southPanel.add(exitButton, cSouth);

		return southPanel;
	}

	/*
	 * Image initialization. It actually initialize their respective labels.
	 */
	private void imageInit() {
		availableDeviceImg = new JLabel();
		availableDeviceImg.setFont(availableDeviceImg.getFont().deriveFont(Font.BOLD));
		availableDeviceImg.setHorizontalAlignment(JLabel.CENTER);

		measureStateImg = new JLabel();
		measureStateImg.setFont(measureStateImg.getFont().deriveFont(Font.BOLD));
		measureStateImg.setHorizontalAlignment(JLabel.CENTER);

		logoImg = new JLabel();
		logoImg.setFont(new Font("Arial", Font.BOLD, 50));
		logoImg.setHorizontalAlignment(JLabel.CENTER);

		loadImages();
		refreshIndicators();
	}

	/*
	 * Loads the images into the program.
	 */
	private void loadImages() {
		String renderKNDLlogo = "<html><font color=red>K</font>" + "<font color=green>N</font>"
				+ "<font color=blue>D</font>" + "<font color=orange>L</font>"
				+ "<font color=yellow>&equiv;</font></html>";
		redLight = null;
		greenLight = null;
		ImageIcon logo = null;
		try {
			redLight = new ImageIcon(ImageIO.read(new File(REDLIGHTPATH)));
			greenLight = new ImageIcon(ImageIO.read(new File(GREENLIGHTPATH)));
			logo = new ImageIcon(ImageIO.read(new File(LOGOPATH)));
		} catch (IOException e) {
			redLight = null;
			greenLight = null;
			logo = null;
		}
		assignImage(logo, logoImg, renderKNDLlogo, Color.BLACK);
	}

	/*
	 * Assigns an image to a label. In case the image is null, it assigns the
	 * specified auxiliary text.
	 */
	private void assignImage(ImageIcon img, JLabel label, String auxText, Color color) {
		if (img != null) {
			label.setIcon(img);
		} else {
			label.setText(auxText);
			label.setForeground(color);
		}
	}

	/*
	 * Refreshes the images of the measuring state and connected device
	 * indicators to a green light or a red light.
	 */
	private void refreshIndicators() {
		KNDLSerialComm serial = new KNDLSerialCommImpl();
		if (measureState) {
			assignImage(greenLight, measureStateImg, "MEASURING", Color.GREEN);
		} else {
			assignImage(redLight, measureStateImg, "IDLE", Color.RED);
		}

		if (serial.checkSerialPort(PropsMngr.getInstance().getProperty(KNDL.DEVICE))) {
			assignImage(greenLight, availableDeviceImg, "CONNECTED", Color.GREEN);
		} else {
			assignImage(redLight, availableDeviceImg, "DISCONNECTED", Color.RED);
		}
	}

	/**
	 * Sets the measuring state and enables or disables the controls
	 * accordingly. It also refreshes the indicators.
	 * 
	 * @param state
	 *            The state to be set.
	 */
	public void setMeasureState(boolean state) {
		startButton.setEnabled(!state);
		measureNameTextField.setEnabled(!state);
		confDialogButton.setEnabled(!state);
		calibrationButton.setEnabled(!state);
		exitButton.setEnabled(!state);
		configMenu.setEnabled(!state);
		helpMenu.setEnabled(!state);
		startItem.setEnabled(!state);
		calibrateItem.setEnabled(!state);
		measureState = state;
		refreshIndicators();
	}

	/**
	 * Enables or disables the stop button.
	 * 
	 * @param enabled
	 *            True to enable the stop button. False otherwise.
	 */
	public void enableStop(boolean enabled) {
		stopButton.setEnabled(enabled);
		stopItem.setEnabled(enabled);
	}

	/**
	 * Prints a string on KNDL's monitor and then terminates the line.
	 * 
	 * @param str
	 *            The string to be printed.
	 * @param logFileName
	 *            Relative path of the log file.
	 */
	public void printlnOnMonitor(String str, String logFileName) {
		monitor.append(str);
		monitor.append("\n");
		if (!"".equals(logFileName)) {
			appendToLog(str + "\r\n", logFileName);
		}
	}

	/**
	 * Prints a string on KNDL's monitor.
	 * 
	 * @param str
	 *            The string to be printed.
	 * @param logFileName
	 *            Relative path of the log file.
	 */
	public void printOnMonitor(String str, String logFileName) {
		monitor.append(str);
		if (!"".equals(logFileName)) {
			appendToLog(str, logFileName);
		}
	}

	/**
	 * Clears KNDL's monitor.
	 */
	public void clearMonitor() {
		monitor.setText("");
	}

	/**
	 * Appends <code>appendedstr</code> to the log file.
	 * 
	 * @param appendedstr
	 *            Name of the measure that is logging the monitor's text.
	 * @param logFileName
	 *            Relative path of the log file.
	 */
	public void appendToLog(String appendedstr, String logFileName) {
		File log;
		FileWriter writer;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH'h'mm");
		Calendar calendar = new GregorianCalendar();

		log = new File(logFileName);
		try {
			if (log.exists()) {
				writer = new FileWriter(log, true);
				writer.append(appendedstr);
				writer.flush();
				writer.close();
			}
		} catch (IOException e) {
			printlnOnMonitor("[KNDL]", "");
			printlnOnMonitor("[KNDL] --- " + sdf.format(calendar.getTime()) + " --- " + "Error appending to file.", "");
			printlnOnMonitor("[KNDL]", "");
		}
	}

	/**
	 * Logs KNDL's monitor text into a file located in the logs directory.
	 * 
	 * @param threadName
	 *            Name of the measure that is logging the monitor's text.
	 * @return The relative path of the log file.
	 */
	public String logMonitor(String threadName) {
		File logsDir = new File(KNDL.LOGSDIRECTORY);
		File log;
		FileWriter writer;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH'h'mm");
		Calendar calendar = new GregorianCalendar();
		String logName = KNDL.LOGSDIRECTORY + sdf.format(calendar.getTime()) + "_" + threadName + ".log";

		if (!logsDir.exists()) {
			logsDir.mkdir();
		}

		log = new File(logName);
		try {
			log.createNewFile();
			writer = new FileWriter(log);
			writer.write(monitor.getText());
			writer.flush();
			writer.close();
		} catch (IOException e) {
			printlnOnMonitor("[KNDL]", "");
			printlnOnMonitor("[KNDL] --- " + sdf.format(calendar.getTime()) + " --- " + "Error generating log file.",
					"");
			printlnOnMonitor("[KNDL]", "");
		}

		return logName;
	}

	/**
	 * Returns the state of the measure.
	 * 
	 * @return The state of the measure.
	 */
	public static boolean getMeasureState() {
		return measureState;
	}

	/*
	 * Configuration button Action Listener. It opens a configuration dialog.
	 */
	private class GoToConfigDialog implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			ConfigurationDialog configDialog = new ConfigurationDialog(MainWindow.this);
			configDialog.setVisible(true);
			refreshIndicators();
		}
	}

	/*
	 * Reset offset menu item Action Listener. It resets the offset.
	 */
	private class GoToChngDefDevDialog implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			ChangeDefDevRegExDialog chngDefDevRegExDialog = new ChangeDefDevRegExDialog(MainWindow.this);
			chngDefDevRegExDialog.setVisible(true);
		}
	}

	/*
	 * Change default device menu item Action Listener. It opens a Change
	 * Default Device Regular Expression Dialog.
	 */
	private class ResetOffsetListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			String[] options = { "Yes", "No" };
			int answer = 0;
			answer = JOptionPane.showOptionDialog(MainWindow.this, "Are you sure you want to reset the offset to zero?",
					"Warning", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[1]);
			if (answer == JOptionPane.YES_OPTION) {
				try {
					PropsMngr.getInstance().setProperty(KNDL.CALOFFSET, "0");
				} catch (IOException e1) {
					JOptionPane.showMessageDialog(null, "Sorry, the operation couldn't be done.", "Error",
							JOptionPane.WARNING_MESSAGE);
				}
			}
		}
	}

	/*
	 * About KNDL button Action Listener. It opens an About KNDL Dialog.
	 */
	private class GoToAboutKNDLDialog implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			AboutKNDLDialog aboutKNDLDialog = new AboutKNDLDialog(MainWindow.this);
			aboutKNDLDialog.setVisible(true);
			refreshIndicators();
		}
	}

	/*
	 * Start Button Action Listener. Starts a MeasureThread. With the specified
	 * name. If there is no name in the measure name text field, it is set to
	 * 'Untitled'.
	 */
	private class StartMeasureListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			String name = measureNameTextField.getText();
			if ("".equals(measureNameTextField.getText())) {
				name = "Untitled";
			}
			if (Files.exists(Paths.get(PropsMngr.getInstance().getProperty(KNDL.RESULTSPATH)))) {
				if (checkFileName(name)) {
					KNDLSerialComm serial = new KNDLSerialCommImpl();
					if (serial.checkSerialPort(PropsMngr.getInstance().getProperty(KNDL.DEVICE))) {
						// try {
						setMeasureState(MEASURING);
						update(getGraphics());
						t1 = new MeasureThread(name, MainWindow.this);
						t1.start();
					} else {
						JOptionPane.showMessageDialog(null, "Device not detected or disconnected.", "Error",
								JOptionPane.WARNING_MESSAGE);
						refreshIndicators();
					}
				} else {
					JOptionPane.showMessageDialog(null, "The asigned name is not valid.", "Error",
							JOptionPane.WARNING_MESSAGE);
				}
			} else {
				JOptionPane.showMessageDialog(null, "Path not valid. Set a correct path in the configuration window.",
						"Error", JOptionPane.WARNING_MESSAGE);
			}

		}

		/*
		 * Checks if the filename is a valid filename.
		 */
		private boolean checkFileName(String name) {
			boolean valido = true;
			File aux = new File(name);
			try {
				aux.getCanonicalPath();
			} catch (IOException e) {
				valido = false;
			}
			return valido;
		}
	}

	/*
	 * Calibrate Button Action Listener. Starts a CalibrationThread.
	 */
	private class CalibrateListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			String threadName = "calibration";
			KNDLSerialComm serial = new KNDLSerialCommImpl();

			if (serial.checkSerialPort(PropsMngr.getInstance().getProperty(KNDL.DEVICE))) {
				// try {
				setMeasureState(MEASURING);
				update(getGraphics());
				tcal = new CalibrationThread(threadName, MainWindow.this);
				tcal.start();
			} else {
				JOptionPane.showMessageDialog(null, "Device not detected or disconnected.", "Error",
						JOptionPane.WARNING_MESSAGE);
				refreshIndicators();
			}
		}
	}

	/*
	 * Stop Button Action Listener. Stops a MeasureThread.
	 */
	private class StopMeasureListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			if (t1 != null) {
				t1.setStopped();
			}
			if (tcal != null) {
				tcal.setStopped();
			}
		}
	}

	/*
	 * Advanced configuration menu Menu Listener. Whenever the menu is selected,
	 * it updates the value of the check box menu items.
	 */
	private class AdvConfMenuListener implements MenuListener {
		public void menuSelected(MenuEvent e) {
			checkRaws.setSelected(Boolean.valueOf(PropsMngr.getInstance().getProperty(KNDL.STORERAWS)));
			checkLogs.setSelected(Boolean.valueOf(PropsMngr.getInstance().getProperty(KNDL.STORELOGS)));
		}

		public void menuCanceled(MenuEvent arg0) {
			// DO NOTHING
		}

		public void menuDeselected(MenuEvent arg0) {
			// DO NOTHING
		}
	}

	/*
	 * Raw files check box menu item Item Listener. Change the STORERAWS
	 * property whenever the state of the check box changes.
	 */
	private class CheckRawsItemListener implements ItemListener {
		public void itemStateChanged(ItemEvent arg0) {
			try {
				PropsMngr.getInstance().setProperty(KNDL.STORERAWS, String.valueOf(checkRaws.isSelected()));
			} catch (IOException ex) {
				JOptionPane.showMessageDialog(null, ex.getMessage(), "Error", JOptionPane.WARNING_MESSAGE);
			}
		}
	}

	/*
	 * Log files check box menu item Item Listener. Change the STORELOGS
	 * property whenever the state of the check box changes.
	 */
	private class CheckLogsItemListener implements ItemListener {
		public void itemStateChanged(ItemEvent e) {
			try {
				PropsMngr.getInstance().setProperty(KNDL.STORELOGS, String.valueOf(checkLogs.isSelected()));
			} catch (IOException ex) {
				JOptionPane.showMessageDialog(null, ex.getMessage(), "Error", JOptionPane.WARNING_MESSAGE);
			}
		}
	}

	/*
	 * Alerts the user about what he/she is doing and then make the current
	 * properties the default properties if the user really wants to.
	 */
	private class MkPropsDefault implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			String[] options = { "Yes", "No" };
			int answer = 0;
			answer = JOptionPane.showOptionDialog(MainWindow.this,
					"Are you sure you wish to set the current properties as default properties?", "Warning",
					JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[1]);
			if (answer == JOptionPane.YES_OPTION) {
				PropsMngr.getInstance().mkPropsDefault();
			}
		}
	}

	/*
	 * Exit Action Listener. Closes KNDL. If there is a measure in progress,
	 * alerts the user and lets he/she chose whether to close it or not.
	 */
	private class ExitListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			String[] options = { "Yes", "No" };
			int answer = 0;
			if (measureState) {
				answer = JOptionPane.showOptionDialog(MainWindow.this,
						"There's a measure process ongoing. Are you sure you want to exit KNDL", "Exiting KNDL",
						JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[1]);
				if (answer == JOptionPane.YES_OPTION) {
					if (t1 != null) {
						t1.setStopped();
						try {
							t1.join();
						} catch (InterruptedException ex) {
							JOptionPane.showMessageDialog(null, ex.getMessage(), "Error", JOptionPane.WARNING_MESSAGE);
						}
					}
					System.exit(0);
				}
			} else {
				System.exit(0);
			}
		}
	}

	/*
	 * Window Adapter.
	 */
	private class ExitProgram extends WindowAdapter {
		public void windowClosing(WindowEvent e) {
			String[] options = { "Yes", "No" };
			int answer = 0;
			if (measureState) {
				answer = JOptionPane.showOptionDialog(MainWindow.this,
						"There's a measure process ongoing. Are you sure you want to exit KNDL", "Exiting KNDL",
						JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[1]);
			}
			if (answer == JOptionPane.YES_OPTION) {
				if (t1 != null) {
					t1.setStopped();
					try {
						t1.join();
					} catch (InterruptedException ex) {
						JOptionPane.showMessageDialog(null, ex.getMessage(), "Error", JOptionPane.WARNING_MESSAGE);
					}
				}
				System.exit(0);
			}
		}
	}
}

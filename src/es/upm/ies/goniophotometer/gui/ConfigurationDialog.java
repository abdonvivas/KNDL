package es.upm.ies.goniophotometer.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import es.upm.ies.goniophotometer.KNDL;
import es.upm.ies.goniophotometer.serial.KNDLSerialComm;
import es.upm.ies.goniophotometer.serial.KNDLSerialCommImpl;
import es.upm.ies.goniophotometer.utils.CSVWriter;
import es.upm.ies.goniophotometer.utils.PropsMngr;

/**
 * Dialog to change KNDL's properties.
 * 
 * @author Abd&oacute;n Alejandro Vivas Imparato
 * 
 */
public class ConfigurationDialog extends JDialog {

	private static final long serialVersionUID = 1L;

	private JRadioButton radioComma;
	private JRadioButton radioSemicolon;

	private JCheckBox calcLumFluxCheck;

	private JTextField textPath;

	private JComboBox<String> prefixCombo;

	// v2lux = Volts To Lux
	private JSpinner v2luxSpinner;

	// DPS = Degrees Per Step
	// Res = Resolution (Degrees Per Sample)
	private SpinnerNumberModel dpsThetaSM;
	private SpinnerNumberModel dpsPhiSM;
	private SpinnerNumberModel resThetaSM;
	private SpinnerNumberModel resPhiSM;
	private SpinnerNumberModel distanceSM;
	private SpinnerNumberModel v2luxSM;

	private JList<String> deviceList;
	private ArrayList<String> comPorts;

	/**
	 * Creates a new <code>ConfigurationDialog</code> instance with the
	 * specified JFrame as its owner.
	 * 
	 * @param owner
	 *            The <code>JFrame</code> from which the dialog is displayed.
	 */
	public ConfigurationDialog(JFrame owner) {
		super(owner, "Configuration", true);
		setLocationByPlatform(true);
		addWindowListener(new CloseConfigurationDialog());
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		setLayout(new BorderLayout());

		JPanel centerPanel = new JPanel(new GridBagLayout());
		GridBagConstraints cCenter = new GridBagConstraints();
		cCenter.insets = new Insets(5, 5, 5, 5);

		setBasicConstr(cCenter, 0, 0, 1.0, 1.0, GridBagConstraints.BOTH);
		centerPanel.add(buildUpperHalf(), cCenter);
		setBasicConstr(cCenter, 1, 0, 0.0, 1.0, GridBagConstraints.BOTH);
		centerPanel.add(buildLowerHalf(), cCenter);

		add(centerPanel, BorderLayout.CENTER);
		add(builSouthPanel(), BorderLayout.SOUTH);

		pack();
		setResizable(false);
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
	 * Builds the upper half of the center panel.
	 */
	private JPanel buildUpperHalf() {
		PropsMngr propsMngr = PropsMngr.getInstance();
		JPanel upperHalf = new JPanel(new BorderLayout());

		JPanel northPanel = new JPanel();
		northPanel.setLayout(new BoxLayout(northPanel, BoxLayout.Y_AXIS));

		/*--- PATH RELATED PANELS ---*/
		JLabel pathLabel = new JLabel("Output directory:");

		JPanel selectionPanel = new JPanel();
		selectionPanel.setLayout(new BoxLayout(selectionPanel, BoxLayout.X_AXIS));
		textPath = new JTextField();
		textPath.setText(propsMngr.getProperty(KNDL.RESULTSPATH));
		JButton selectPathButton = new JButton("...");
		selectPathButton.addActionListener(new SelectDirectoryListener());
		selectionPanel.add(textPath);
		selectionPanel.add(selectPathButton);

		pathLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		selectionPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		northPanel.add(pathLabel);
		northPanel.add(selectionPanel);
		/*--- PATH RELATED PANELSS ---*/

		/*--- PREFIX AND SEPARATOR PANNELS ---*/
		/*--- PREFIX PANEL ---*/
		JPanel prefixPanel = new JPanel(new GridBagLayout());
		GridBagConstraints cPrefix = new GridBagConstraints();

		JLabel prefixText = new JLabel("Name prefix:");

		prefixCombo = new JComboBox<String>(KNDL.PREFIXDATEFORMAT);
		prefixCombo.setSelectedItem(propsMngr.getProperty(KNDL.RESULTSPREFIX));

		setBasicConstr(cPrefix, 0, 0, 1.0, 1.0, GridBagConstraints.BOTH);
		prefixPanel.add(prefixText, cPrefix);
		setBasicConstr(cPrefix, 1, 0, 0.0, 1.0, GridBagConstraints.BOTH);
		cPrefix.insets = new Insets(0, 0, 10, 0);
		prefixPanel.add(prefixCombo, cPrefix);
		/*--- PREFIX PANEL ---*/

		/*--- SEPARATOR PANEL ---*/
		JPanel separatorPanel = new JPanel();
		separatorPanel.setLayout(new BoxLayout(separatorPanel, BoxLayout.Y_AXIS));
		separatorPanel.setBorder(BorderFactory.createTitledBorder("Separator (CSV)"));

		ButtonGroup separatorButtonGroup = new ButtonGroup();
		radioComma = new JRadioButton("Comma (,)");
		radioSemicolon = new JRadioButton("Semicolon (;)");
		if (CSVWriter.COMMA.equals(propsMngr.getProperty(KNDL.SEPARATOR))) {
			radioComma.setSelected(true);
		} else if (CSVWriter.SEMICOLON.equals(propsMngr.getProperty(KNDL.SEPARATOR))) {
			radioSemicolon.setSelected(true);
		}
		separatorButtonGroup.add(radioComma);
		separatorButtonGroup.add(radioSemicolon);

		separatorPanel.add(radioComma);
		separatorPanel.add(radioSemicolon);
		/*--- SEPARATOR PANEL ---*/

		JPanel prefixAndSeparatorPanel = new JPanel(new GridLayout(1, 2));
		prefixAndSeparatorPanel.add(prefixPanel);
		prefixAndSeparatorPanel.add(separatorPanel);
		prefixAndSeparatorPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		northPanel.add(prefixAndSeparatorPanel);
		/*--- PREFIX AND SEPARATOR PANNELS ---*/

		/*--- DEVICES PANEL ---*/
		JPanel devicesPanel = new JPanel();
		devicesPanel.setLayout(new BoxLayout(devicesPanel, BoxLayout.Y_AXIS));

		JLabel devicesLabel = new JLabel("Select your device's serial port: ");

		if ("".equals(propsMngr.getProperty(KNDL.DEVICE))) {
			try {
				propsMngr.setProperty(KNDL.DEVICE, propsMngr.getDefaultProperty(KNDL.DEVICE));
			} catch (IOException e) {
				/*
				 * Do nothing. It doesn't matter if the device property is not
				 * written into a file. It is a special property and don't
				 * always depends on the user.
				 */
			}
		}
		DefaultListModel<String> devices = new DefaultListModel<String>();
		deviceList = new JList<String>(devices);
		KNDLSerialComm serial = new KNDLSerialCommImpl();
		comPorts = serial.getComPorts();
		ArrayList<String> comPortDescriptions;
		comPortDescriptions = serial.getComPortDescriptions();
		for (int i = 0; i < comPorts.size(); i++) {
			devices.addElement(comPortDescriptions.get(i));
			if (comPorts.get(i).equals(propsMngr.getProperty(KNDL.DEVICE))) {
				deviceList.setSelectedIndex(i);
			}
		}

		JScrollPane devicesScrollPane = new JScrollPane(deviceList);

		devicesLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		devicesScrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);
		devicesPanel.add(devicesLabel);
		devicesPanel.add(devicesScrollPane);
		/*--- DEVICES PANEL ---*/

		upperHalf.add(northPanel, BorderLayout.NORTH);
		upperHalf.add(devicesPanel, BorderLayout.CENTER);

		return upperHalf;
	}

	/*
	 * Builds the lower half of the center panel.
	 */
	private JPanel buildLowerHalf() {
		PropsMngr propsMngr = PropsMngr.getInstance();

		JPanel lowerHalf = new JPanel(new GridBagLayout());
		GridBagConstraints cLower = new GridBagConstraints();

		/*--- DPS PANEL ---*/
		JPanel dpsPanel = new JPanel(new GridBagLayout());
		GridBagConstraints cDPS = new GridBagConstraints();
		cDPS.ipadx = 5;
		cDPS.ipady = 5;
		cDPS.insets = new Insets(2, 2, 2, 2);
		dpsPanel.setBorder(BorderFactory.createTitledBorder("Step angle (Degrees/Step)"));

		JLabel dpsThetaLabel = new JLabel("Polar angle: ", JLabel.RIGHT);
		dpsThetaSM = new SpinnerNumberModel(Double.parseDouble(propsMngr.getProperty(KNDL.DPSTHETA)), 0.01, 90, 0.01);
		dpsThetaSM.addChangeListener(new dpsListener());
		JSpinner dpsThetaSpinner = new JSpinner(dpsThetaSM);
		setBasicConstr(cDPS, 0, 0, 0.0, 0.0, GridBagConstraints.BOTH);
		cDPS.anchor = GridBagConstraints.LINE_END;
		dpsPanel.add(dpsThetaLabel, cDPS);
		setBasicConstr(cDPS, 0, 1, 0.0, 0.0, GridBagConstraints.BOTH);
		cDPS.anchor = GridBagConstraints.LINE_START;
		dpsPanel.add(dpsThetaSpinner, cDPS);

		JLabel dpsPhiLabel = new JLabel("Azimuth angle: ", JLabel.RIGHT);
		dpsPhiSM = new SpinnerNumberModel(Double.parseDouble(propsMngr.getProperty(KNDL.DPSPHI)), 0.01, 180, 0.01);
		dpsPhiSM.addChangeListener(new dpsListener());
		JSpinner dpsPhiSpinner = new JSpinner(dpsPhiSM);
		setBasicConstr(cDPS, 1, 0, 0.0, 0.0, GridBagConstraints.BOTH);
		cDPS.anchor = GridBagConstraints.LINE_END;
		dpsPanel.add(dpsPhiLabel, cDPS);
		setBasicConstr(cDPS, 1, 1, 0.0, 0.0, GridBagConstraints.BOTH);
		cDPS.anchor = GridBagConstraints.LINE_START;
		dpsPanel.add(dpsPhiSpinner, cDPS);

		setBasicConstr(cLower, 0, 0, 0.0, 0.5, GridBagConstraints.HORIZONTAL);
		lowerHalf.add(dpsPanel, cLower);
		/*--- DPS PANEL ---*/

		/*--- RESOLUTION PANEL ---*/
		JPanel resPanel = new JPanel(new GridBagLayout());
		GridBagConstraints cRes = new GridBagConstraints();
		cRes.ipadx = 5;
		cRes.ipady = 5;
		cRes.insets = new Insets(2, 2, 2, 2);
		resPanel.setBorder(BorderFactory.createTitledBorder("Resolution (degrees/sample)"));

		JLabel resThetaLabel = new JLabel("Polar angle: ", JLabel.RIGHT);
		resThetaSM = new SpinnerNumberModel(Double.parseDouble(propsMngr.getProperty(KNDL.RESTHETA)), 0.01, 90,
				(double) dpsThetaSM.getValue());
		JSpinner resThetaSpinner = new JSpinner(resThetaSM);
		setBasicConstr(cRes, 0, 0, 0.0, 0.0, GridBagConstraints.BOTH);
		cRes.anchor = GridBagConstraints.LINE_END;
		resPanel.add(resThetaLabel, cRes);
		setBasicConstr(cRes, 0, 1, 0.0, 0.0, GridBagConstraints.BOTH);
		cRes.anchor = GridBagConstraints.LINE_START;
		resPanel.add(resThetaSpinner, cRes);

		JLabel resPhiLabel = new JLabel("Azimuth angle: ", JLabel.RIGHT);
		resPhiSM = new SpinnerNumberModel(Double.parseDouble(propsMngr.getProperty(KNDL.RESPHI)), 0.01, 180,
				(double) dpsPhiSM.getValue());
		JSpinner resPhiSpinner = new JSpinner(resPhiSM);
		setBasicConstr(cRes, 1, 0, 0.0, 0.0, GridBagConstraints.BOTH);
		cRes.anchor = GridBagConstraints.LINE_END;
		resPanel.add(resPhiLabel, cRes);
		setBasicConstr(cRes, 1, 1, 0.0, 0.0, GridBagConstraints.BOTH);
		cRes.anchor = GridBagConstraints.LINE_START;
		resPanel.add(resPhiSpinner, cRes);

		setBasicConstr(cLower, 0, 1, 0.0, 0.5, GridBagConstraints.HORIZONTAL);
		lowerHalf.add(resPanel, cLower);
		/*--- RESOLUTION PANEL ---*/

		/*--- DIST PANEL ---*/
		JPanel distPanel = new JPanel(new GridBagLayout());
		GridBagConstraints cDist = new GridBagConstraints();
		cDist.ipadx = 5;
		cDist.ipady = 5;

		String auxStr = "Source-to-sensor distance (m):";
		JLabel distText = new JLabel(auxStr, JLabel.LEFT);

		distanceSM = new SpinnerNumberModel(Double.parseDouble(propsMngr.getProperty(KNDL.SENSORDISTANCE)), 0.01, 50,
				0.01);
		JSpinner distSpinner = new JSpinner(distanceSM);
		setBasicConstr(cDist, 0, 0, 0.0, 0.0, GridBagConstraints.BOTH);
		distPanel.add(distText, cDist);
		setBasicConstr(cDist, 0, 1, 0.0, 0.0, GridBagConstraints.BOTH);
		distPanel.add(distSpinner, cDist);

		setBasicConstr(cLower, 1, 0, 0.0, 1.0, GridBagConstraints.HORIZONTAL);
		cLower.gridheight = 2;
		cLower.insets = new Insets(0, 0, 0, 5);
		lowerHalf.add(distPanel, cLower);
		/*--- DIST PANEL ---*/

		/*--- LUFLUX PANEL ---*/
		calcLumFluxCheck = new JCheckBox("Calculate luminous flux and FWHM");
		calcLumFluxCheck.setSelected(Boolean.valueOf(propsMngr.getProperty(KNDL.PHOTOCALCS)));

		JPanel v2luxPanel = new JPanel(new GridBagLayout());
		GridBagConstraints cV2L = new GridBagConstraints();
		cV2L.ipadx = 5;
		cV2L.ipady = 5;

		JLabel v2luxText = new JLabel("Volts-lux relation [lux/V]:");
		v2luxSM = new SpinnerNumberModel(Double.parseDouble(propsMngr.getProperty(KNDL.VOLTS2LUX)), 0.01, 1000000.0,
				0.01);
		v2luxSpinner = new JSpinner(v2luxSM);

		setBasicConstr(cV2L, 0, 0, 0.0, 1.0, GridBagConstraints.BOTH);
		cV2L.insets = new Insets(0, 5, 0, 0);
		v2luxPanel.add(v2luxText, cV2L);
		setBasicConstr(cV2L, 0, 2, 0.0, 0.0, GridBagConstraints.BOTH);
		cV2L.insets = new Insets(0, 0, 0, 0);
		v2luxPanel.add(v2luxSpinner, cV2L);

		cLower.insets = new Insets(0, 0, 0, 0);
		setBasicConstr(cLower, 1, 1, 0.0, 0.5, GridBagConstraints.HORIZONTAL);
		cLower.gridheight = 1;
		lowerHalf.add(calcLumFluxCheck, cLower);
		setBasicConstr(cLower, 2, 1, 0.0, 0.5, GridBagConstraints.HORIZONTAL);
		cLower.gridheight = 1;
		lowerHalf.add(v2luxPanel, cLower);
		/*--- LUFLUX PANEL ---*/

		return lowerHalf;
	}

	/*
	 * Builds the south panel.
	 */
	private JPanel builSouthPanel() {
		JPanel southPanel = new JPanel(new GridBagLayout());
		GridBagConstraints cSouth = new GridBagConstraints();
		cSouth.ipadx = 10;
		cSouth.ipady = 10;
		cSouth.insets = new Insets(5, 5, 5, 5);

		setBasicConstr(cSouth, 0, 0, 1.0, 0.35, GridBagConstraints.BOTH);
		southPanel.add(new JLabel(), cSouth);

		JButton applyButton = new JButton("Apply");
		applyButton.addActionListener(new ApplyButtonListener());
		setBasicConstr(cSouth, 0, 1, 0.0, 0.1, GridBagConstraints.NONE);
		southPanel.add(applyButton, cSouth);

		JButton restoreButton = new JButton("Restore");
		restoreButton.addActionListener(new RestoreButtonListener());
		setBasicConstr(cSouth, 0, 2, 0.0, 0.1, GridBagConstraints.NONE);
		southPanel.add(restoreButton, cSouth);

		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new CancelButtonListener());
		setBasicConstr(cSouth, 0, 3, 0.0, 0.1, GridBagConstraints.NONE);
		southPanel.add(cancelButton, cSouth);

		setBasicConstr(cSouth, 0, 4, 1.0, 0.35, GridBagConstraints.BOTH);
		southPanel.add(new JLabel(), cSouth);

		return southPanel;
	}

	/*
	 * The closest multiple of y to x.
	 */
	private double closestMultiple(double x, double y) {
		double z = 0;
		double ytmp = (int) Math.round(y * 100);
		double xtmp = (int) Math.round(x * 100);
		if (xtmp % xtmp >= ytmp / 2) {
			z = (Math.floor(xtmp / ytmp) + 1) * ytmp;
		} else {
			z = Math.floor(xtmp / ytmp) * ytmp;
		}
		z = (Double) (z / 100);
		return z;
	}

	/*
	 * True if x is multiple of y.
	 */
	private boolean isMultiple(double x, double y) {
		boolean res = false;
		res = (int) Math.round(x * 100) % (int) Math.round(y * 100) == 0;
		return res;
	}

	/*
	 * Checks whether the fields are valid or not. For each non-valid field, it
	 * displays a warning message. If there is any non-valid field, it returns
	 * false (true otherwise).
	 */
	private boolean checkFields() {
		boolean camposValidos = true;
		StringBuilder notification = new StringBuilder("");

		boolean fieldChecks[] = { !("".equals(textPath.getText())), Files.exists(Paths.get(textPath.getText())),
				isMultiple(90, (Double) resThetaSM.getValue()), isMultiple(180, (Double) resPhiSM.getValue()),
				isMultiple((Double) resThetaSM.getValue(), (Double) dpsThetaSM.getValue()),
				isMultiple((Double) resPhiSM.getValue(), (Double) dpsPhiSM.getValue()) };

		String notifications[] = { "Output path is empty.", "Output path doesn't exists.",
				"Polar angle resolution must divide 90.",
				"Azimuth angle resolution must divide 180.",
				"Polar angle resolution must be a multiple of the the degrees per step.",
				"Azimuth angle resolution must be a multiple of the the degrees per step." };

		for (int i = 0; i < notifications.length; i++) {
			if (!fieldChecks[i]) {
				notification.append(notifications[i]);
				notification.append('\n');
				camposValidos = false;
			}
		}

		if (!camposValidos) {
			JOptionPane.showMessageDialog(ConfigurationDialog.this, notification.toString(), "Warning",
					JOptionPane.WARNING_MESSAGE);
		}

		return camposValidos;
	}

	/*
	 * Select directory Action Listener. It opens a file chooser.
	 */
	private class SelectDirectoryListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			JFileChooser fileChooser = new JFileChooser();
			fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			int returnVal = fileChooser.showOpenDialog(ConfigurationDialog.this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File directory = fileChooser.getSelectedFile();
				textPath.setText(directory.getAbsolutePath());
			}
		}
	}

	/*
	 * DPS spinners' Change Listener. If one of the DPS spinners change, the
	 * value of its corresponding resolution spinner changes to the closest
	 * multiple of the new DPS value, and the step size changes to the new DPS
	 * value itself.
	 */
	private class dpsListener implements ChangeListener {
		public void stateChanged(ChangeEvent chngevnt) {
			if (chngevnt.getSource().equals(dpsThetaSM)) {
				double aux = closestMultiple((double) resThetaSM.getValue(), (double) dpsThetaSM.getValue());
				resThetaSM.setValue(aux);
				resThetaSM.setStepSize((Double) dpsThetaSM.getValue());
			} else if (chngevnt.getSource().equals(dpsPhiSM)) {
				double aux = closestMultiple((double) resPhiSM.getValue(), (double) dpsPhiSM.getValue());
				resPhiSM.setValue(aux);
				resPhiSM.setStepSize((Double) dpsPhiSM.getValue());
			}

		}
	}

	/*
	 * Apply Button Action Listener. Apply changes only if the fields are valid.
	 */
	private class ApplyButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			if (checkFields()) {
				try {
					HashMap<String, String> propsAct = new HashMap<String, String>();
					propsAct.put(KNDL.RESULTSPATH, textPath.getText());
					propsAct.put(KNDL.RESULTSPREFIX, (String) prefixCombo.getSelectedItem());
					if (!comPorts.isEmpty() && deviceList.getSelectedIndex() >= 0) {
						propsAct.put(KNDL.DEVICE, comPorts.get(deviceList.getSelectedIndex()));
					} else {
						propsAct.put(KNDL.DEVICE, "");
					}
					propsAct.put(KNDL.DPSTHETA, String.valueOf(dpsThetaSM.getValue()));
					propsAct.put(KNDL.DPSPHI, String.valueOf(dpsPhiSM.getValue()));
					propsAct.put(KNDL.RESTHETA, String.valueOf(resThetaSM.getValue()));
					propsAct.put(KNDL.RESPHI, String.valueOf(resPhiSM.getValue()));
					propsAct.put(KNDL.SENSORDISTANCE, String.valueOf(distanceSM.getValue()));
					propsAct.put(KNDL.VOLTS2LUX, String.valueOf(v2luxSM.getValue()));
					if (radioComma.isSelected()) {
						propsAct.put(KNDL.SEPARATOR, CSVWriter.COMMA);
					} else if (radioSemicolon.isSelected()) {
						propsAct.put(KNDL.SEPARATOR, CSVWriter.SEMICOLON);
					}
					propsAct.put(KNDL.PHOTOCALCS, String.valueOf(calcLumFluxCheck.isSelected()));
					PropsMngr.getInstance().setProperties(propsAct);
					setVisible(false);
					dispose();
				} catch (IOException ex) {
					JOptionPane.showMessageDialog(null, ex.getMessage(), "Error", JOptionPane.WARNING_MESSAGE);
				}
			}
		}
	}

	/*
	 * Restore button Action Listener. Restores the fields to the default values
	 * without applying them.
	 */
	private class RestoreButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			KNDLSerialComm serial = new KNDLSerialCommImpl();
			PropsMngr propsMngr = PropsMngr.getInstance();
			propsMngr.loadDefaultProperties();

			textPath.setText(propsMngr.getDefaultProperty(KNDL.RESULTSPATH));
			prefixCombo.setSelectedItem(propsMngr.getDefaultProperty(KNDL.RESULTSPREFIX));
			propsMngr.setDefaultProperty(KNDL.DEVICE, serial.matchRegEx(propsMngr.getProperty(KNDL.DEFDEVREGEX)));
			for (int i = 0; i < comPorts.size(); i++) {
				if (comPorts.get(i).equals(propsMngr.getDefaultProperty(KNDL.DEVICE))) {
					deviceList.setSelectedIndex(i);
				}
			}
			dpsThetaSM.setValue(Double.parseDouble(propsMngr.getDefaultProperty(KNDL.DPSTHETA)));
			dpsPhiSM.setValue(Double.parseDouble(propsMngr.getDefaultProperty(KNDL.DPSPHI)));
			resThetaSM.setValue(Double.parseDouble(propsMngr.getDefaultProperty(KNDL.RESTHETA)));
			resPhiSM.setValue(Double.parseDouble(propsMngr.getDefaultProperty(KNDL.RESPHI)));
			distanceSM.setValue(Double.parseDouble(propsMngr.getDefaultProperty(KNDL.SENSORDISTANCE)));
			calcLumFluxCheck.setSelected(Boolean.valueOf(propsMngr.getDefaultProperty(KNDL.PHOTOCALCS)));
			if (Boolean.valueOf(propsMngr.getDefaultProperty(KNDL.PHOTOCALCS))) {
				v2luxSpinner.setEnabled(true);
			} else {
				v2luxSpinner.setEnabled(false);
			}
			v2luxSM.setValue(Double.parseDouble(propsMngr.getDefaultProperty(KNDL.VOLTS2LUX)));
			if (CSVWriter.COMMA.equals(propsMngr.getDefaultProperty(KNDL.SEPARATOR))) {
				radioComma.setSelected(true);
			} else if (CSVWriter.SEMICOLON.equals(propsMngr.getDefaultProperty(KNDL.SEPARATOR))) {
				radioSemicolon.setSelected(true);
			}
		}
	}

	/*
	 * Cancel button Action Listener. Closes the dialog.
	 */
	private class CancelButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			setVisible(false);
			dispose();
		}
	}

	/*
	 * Window Adapter.
	 */
	private class CloseConfigurationDialog extends WindowAdapter {
		public void windowClosing(WindowEvent e) {
			setVisible(false);
			dispose();
		}
	}
}

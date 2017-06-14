package es.upm.ies.goniophotometer.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.WindowConstants;
import javax.swing.border.LineBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;

import es.upm.ies.goniophotometer.KNDL;
import es.upm.ies.goniophotometer.serial.KNDLSerialComm;
import es.upm.ies.goniophotometer.serial.KNDLSerialCommImpl;
import es.upm.ies.goniophotometer.utils.PropsMngr;

/**
 * Dialog to change the regular expression to search for a default device. This
 * regular expression is matched against the operating system-defined
 * descriptions of the devices connected to the serial ports. Remember that this
 * descriptions may not be a good representation of the device.
 * 
 * @author Abd&oacute;n Alejandro Vivas Imparato
 *
 */
public class ChangeDefDevRegExDialog extends JDialog {

	private static final long serialVersionUID = 1L;

	private JTextField newDefDevRegEx;

	/**
	 * Creates a new <code>ChangeDefDevRegExDialog</code> instance with the
	 * specified JFrame as its owner.
	 * 
	 * @param owner
	 *            The <code>JFrame</code> from which the dialog is displayed.
	 */
	public ChangeDefDevRegExDialog(JFrame owner) {
		super(owner, "Advanced properties", true);
		addWindowListener(new CloseAdvConfigDialog());
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		setLayout(new BorderLayout(5, 5));
		setMinimumSize(new Dimension(460, 223));
		setLocationByPlatform(true);

		add(bulidNorthPanel(), BorderLayout.NORTH);
		add(buildCenterPanel(), BorderLayout.CENTER);
		add(buildSouthPanel(), BorderLayout.SOUTH);
		// PADDING
		add(Box.createRigidArea(new Dimension(0, 0)), BorderLayout.WEST);
		add(Box.createRigidArea(new Dimension(0, 0)), BorderLayout.EAST);

		pack();
		setResizable(false);
	}

	/*
	 * Builds the north panel of the dialog
	 */
	private JPanel bulidNorthPanel() {
		JPanel northPanel = new JPanel();
		northPanel.setLayout(new BoxLayout(northPanel, BoxLayout.Y_AXIS));
		northPanel.setBorder(new LineBorder(Color.RED, 2));
		northPanel.setBackground(Color.WHITE);

		// For your information
		String fyi = "KNDL uses a regular expression to find a default device among the available serial devices."
				+ " Each time KNDL starts or the properties are restored to default values, it selects the first"
				+ " device whose description matches the specified regular expression.";

		StyleContext sc = new StyleContext();
		final DefaultStyledDocument doc = new DefaultStyledDocument();
		JTextPane textPane = new JTextPane(doc);
		textPane.setEditable(false);
		textPane.setOpaque(false);
		textPane.setPreferredSize(new Dimension(500, 90));

		final Style style = sc.addStyle("IjustWantedCenterAlignedMultilineText", null);
		style.addAttribute(StyleConstants.Alignment, Integer.valueOf(StyleConstants.ALIGN_CENTER));

		try {
			doc.insertString(0, fyi, null);
			doc.setParagraphAttributes(0, 1, style, false);
		} catch (BadLocationException e) {
			/*
			 * Very unlikely to happen as my DefaultStyledDocument instance
			 * doesn't reference anything...
			 */
		}

		northPanel.add(textPane);

		return northPanel;
	}

	/*
	 * Builds the center panel of the dialog
	 */
	private JPanel buildCenterPanel() {
		JPanel centerPanel = new JPanel(new GridLayout(3, 2, 10, 10));
		PropsMngr propsMngr = PropsMngr.getInstance();

		JLabel currentDevDefRegExLabel = new JLabel("Current regular expression: ", JLabel.RIGHT);
		JLabel currentDefDevRegEx = new JLabel(propsMngr.getProperty(KNDL.DEFDEVREGEX));
		currentDefDevRegEx.setForeground(Color.BLUE);
		if ("".equals(propsMngr.getProperty(KNDL.DEFDEVREGEX))) {
			currentDefDevRegEx.setText("A regular expression hasn't been asigned yet");
			currentDefDevRegEx.setFont(currentDefDevRegEx.getFont().deriveFont(Font.ITALIC));
		}
		JLabel currentDevDescriptionLabel = new JLabel("Current device description: ", JLabel.RIGHT);
		JLabel currentDev;
		KNDLSerialComm serial = new KNDLSerialCommImpl();
		currentDev = new JLabel();
		if (!serial.checkSerialPort(propsMngr.getProperty(KNDL.DEVICE))) {
			currentDev.setText("A device hasn't been asigned yet");
			currentDev.setFont(currentDev.getFont().deriveFont(Font.ITALIC));
		} else if ("".equals(serial.getComPortDescription(propsMngr.getProperty(KNDL.DEVICE)))) {
			currentDev.setText("Description unavailable");
			currentDev.setFont(currentDev.getFont().deriveFont(Font.ITALIC));
		} else {
			currentDev = new JLabel(serial.getComPortDescription(propsMngr.getProperty(KNDL.DEVICE)));
		}
		currentDev.setForeground(Color.BLUE);
		JLabel newDefDevRegExLabel = new JLabel("New regular expression: ", JLabel.RIGHT);
		newDefDevRegEx = new JTextField();

		centerPanel.add(currentDevDefRegExLabel);
		centerPanel.add(currentDefDevRegEx);
		centerPanel.add(currentDevDescriptionLabel);
		centerPanel.add(currentDev);
		centerPanel.add(newDefDevRegExLabel);
		centerPanel.add(newDefDevRegEx);

		return centerPanel;
	}

	/*
	 * Builds the south panel of the dialog
	 */
	private JPanel buildSouthPanel() {
		JPanel southPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));

		JButton applyButton = new JButton("Apply");
		applyButton.addActionListener(new ApplyButtonListener());
		JButton restoreButton = new JButton("Set default regular exression");
		restoreButton.addActionListener(new RestoreButtonListener());
		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new CancelButtonListener());

		southPanel.add(applyButton, BorderLayout.SOUTH);
		southPanel.add(restoreButton, BorderLayout.SOUTH);
		southPanel.add(cancelButton, BorderLayout.SOUTH);
		return southPanel;
	}

	/*
	 * Apply Button Action Listener. Apply changes only if the fields are valid.
	 */
	private class ApplyButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			try {
				if (newDefDevRegEx.getText().length() <= 30) {
					try {
						PropsMngr propsMngr = PropsMngr.getInstance();
						propsMngr.setProperty(KNDL.DEFDEVREGEX, newDefDevRegEx.getText());
						propsMngr.setDefaultProperty(KNDL.DEVICE,
								(new KNDLSerialCommImpl()).matchRegEx(propsMngr.getProperty(KNDL.DEFDEVREGEX)));
					} catch (IOException e1) {
						JOptionPane.showMessageDialog(null, e1.getMessage(), "Error", JOptionPane.WARNING_MESSAGE);
					}
					setVisible(false);
					dispose();
				} else {
					String msg = "The value of the regular expression is limited to 30 characters";
					JOptionPane.showMessageDialog(null, msg, "Warning", JOptionPane.WARNING_MESSAGE);
				}
			} catch (PatternSyntaxException ex) {
				String msg = "That is not a correct regular expression! Try another.";
				JOptionPane.showMessageDialog(null, msg, "Warning", JOptionPane.WARNING_MESSAGE);
			}
		}
	}

	/*
	 * Restore button Action Listener. Restores the fields to the default values
	 * without applying them.
	 */
	private class RestoreButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			try {
				Pattern.compile(PropsMngr.getInstance().getDefaultProperty(KNDL.DEFDEVREGEX));
				newDefDevRegEx.setText(PropsMngr.getInstance().getDefaultProperty(KNDL.DEFDEVREGEX));
			} catch (PatternSyntaxException ex) {
				PropsMngr.getInstance().setDefaultProperty(KNDL.DEFDEVREGEX, "");
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
	private class CloseAdvConfigDialog extends WindowAdapter {
		public void windowClosing(WindowEvent e) {
			setVisible(false);
			dispose();
		}
	}
}

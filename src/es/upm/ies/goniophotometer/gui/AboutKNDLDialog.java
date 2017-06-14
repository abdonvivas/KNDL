package es.upm.ies.goniophotometer.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.WindowConstants;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import es.upm.ies.goniophotometer.KNDL;

/**
 * Shows the name and version of the software, as well as a brief description of
 * its functionality and license details.
 * 
 * @author Abd&oacute;n Alejandro Vivas Imparato
 *
 */
public class AboutKNDLDialog extends JDialog {
	private static final long serialVersionUID = 1L;
	private static final String LOGOPATH = ".//img//KNDLlogo.png";
	private static final String MOREABOUT = "<html><a href=\"\">https://github.com/abdonvivas/KNDL</a></html>";
	private static final String INFO = "KNDL, pronounced 'candle', is a software that runs on a computer as part of an automated\n"
			+ "goniophotometer. The components of the goniophotometer must implement KNDL Architecture.\n"
			+ "For more information about KNDL Architecture, go to the link at the end of this window.\n\n"
			+ "Copyright © 2017 Abdon Alejandro Vivas Imparato.\n\n"
			+ "This program is free software: you can redistribute it and/or modify\n"
			+ "it under the terms of the GNU General Public License as published by\n"
			+ "the Free Software Foundation, either version 3 of the License, or\n"
			+ "(at your option) any later version.\n\n"
			+ "This program is distributed in the hope that it will be useful,\n"
			+ "but WITHOUT ANY WARRANTY; without even the implied warranty of\n"
			+ "MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the\n"
			+ "GNU General Public License for more details.\n\n"
			+ "You should have received a copy of the GNU General Public License\n"
			+ "along with this program.  If not, see <http://www.gnu.org/licenses/>.";

	/**
	 * Creates a new <code>AboutKNDLDialog</code> instance with the specified
	 * JFrame as its owner.
	 * 
	 * @param owner
	 *            The <code>JFrame</code> from which the dialog is displayed.
	 */
	public AboutKNDLDialog(JFrame owner) {
		super(owner, "About KNDL", true);
		addWindowListener(new CloseAboutDialog());
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		setLayout(new BorderLayout());
		setLocationByPlatform(true);

		add(buildNorthPanel(), BorderLayout.NORTH);
		add(buildCenterPanel(), BorderLayout.CENTER);
		add(buildSouthPanel(), BorderLayout.SOUTH);

		pack();
		setResizable(false);
	}

	/*
	 * Builds the north panel of the dialog
	 */
	private JPanel buildNorthPanel() {
		JPanel northPanel = new JPanel();

		ImageIcon logo;
		JLabel logoImg = new JLabel();
		logoImg.setFont(new Font("Arial", Font.BOLD, 50));

		String renderKNDLlogo = "<html><font color=red>K</font>" + "<font color=green>N</font>"
				+ "<font color=blue>D</font>" + "<font color=orange>L</font>"
				+ "<font color=yellow>&equiv;</font></html>";
		logo = null;
		try {
			logo = new ImageIcon(ImageIO.read(new File(LOGOPATH)));
		} catch (IOException e) {
			logo = null;
		}
		if (logo != null) {
			logoImg.setIcon(logo);
		} else {
			logoImg.setText(renderKNDLlogo);
		}

		northPanel.add(logoImg);

		return northPanel;
	}

	/*
	 * Builds the center panel of the dialog
	 */
	private JPanel buildCenterPanel() {
		JPanel centerPanel = new JPanel();
		centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
		JLabel tittle = new JLabel(KNDL.SOFTWARENAME + " v" + KNDL.SOFTWAREVERSION, JLabel.CENTER);
		JLabel moreAboutPanel = new JLabel(MOREABOUT, JLabel.CENTER);

		StyleContext sc = new StyleContext();
		final DefaultStyledDocument doc = new DefaultStyledDocument();
		JTextPane textPane = new JTextPane(doc);
		textPane.setEditable(false);
		textPane.setOpaque(false);

		final Style style = sc.addStyle("IjustWantedCenterAlignedMultilineText", null);
		style.addAttribute(StyleConstants.FontSize, Integer.valueOf(12));
		style.addAttribute(StyleConstants.Alignment, Integer.valueOf(StyleConstants.ALIGN_CENTER));

		try {
			doc.insertString(0, INFO, null);
			doc.setParagraphAttributes(0, 2048, style, false);
		} catch (BadLocationException e) {
			/*
			 * Very unlikely to happen as my DefaultStyledDocument instance
			 * doesn't reference anything...
			 */
		}

		tittle.setAlignmentX(Component.CENTER_ALIGNMENT);
		textPane.setAlignmentX(Component.CENTER_ALIGNMENT);
		moreAboutPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

		centerPanel.add(tittle);
		centerPanel.add(textPane);
		centerPanel.add(moreAboutPanel);

		return centerPanel;
	}

	/*
	 * Builds the south panel of the dialog
	 */
	private JPanel buildSouthPanel() {
		JPanel southPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		JButton okButton = new JButton("OK");
		okButton.addActionListener(new OKButtonListener());
		southPanel.add(okButton);
		return southPanel;
	}

	/*
	 * OK button Action Listener. Closes the dialog.
	 */
	private class OKButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			setVisible(false);
			dispose();
		}
	}

	/*
	 * Window Adapter.
	 */
	private class CloseAboutDialog extends WindowAdapter {
		public void windowClosing(WindowEvent e) {
			setVisible(false);
			dispose();
		}
	}
}
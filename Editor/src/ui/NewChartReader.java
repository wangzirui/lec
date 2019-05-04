package ui;

import java.awt.Container;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JTextField;

import util.Util;

public class NewChartReader {
	JDialog dialog;
	JTextField name;
	int status;
	static int APPROVAL = 0, CANCEL = 1;
	JComboBox mode;
	String[] modes = {"universal", "existential"};
	
	int showDialog(JFrame parent) {
		status = CANCEL;
		dialog = new JDialog(parent, "New LSC", true);
		Container pane = dialog.getContentPane();
		pane.setLayout(new GridLayout(0, 2));
		pane.add(new Label("Name"));
		name = new JTextField();
		pane.add(name);
		pane.add(new Label("Mode"));
		mode = new JComboBox(modes);
		pane.add(mode);
		JButton ok = new JButton("OK");
		ok.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (Util.nonEmpty(name.getText())) {
					status = APPROVAL;
					dialog.setVisible(false);
				}
			}
		});
		pane.add(ok);
		dialog.pack();
		dialog.setLocationRelativeTo(parent);
		dialog.setVisible(true);
		return status;
	}
	
	String getText() {
		return "lsc " + mode.getSelectedItem() + " \"" + name.getText() + "\"\n" +
			(mode.getSelectedIndex() == 0 ? "prechart\nmain\nend" : "main\nend");
	}
}

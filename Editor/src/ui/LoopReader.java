package ui;

import java.awt.Container;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JTextField;

import util.Util;

public class LoopReader {
	JDialog dialog;
	JTextField times;
	int status;
	static int APPROVAL = 0, CANCEL = 1;
	
	int showDialog(JFrame parent) {
		status = CANCEL;
		dialog = new JDialog(parent, "New Loop", true);
		Container pane = dialog.getContentPane();
		pane.setLayout(new GridLayout(0, 2));
		pane.add(new Label("Times to Run"));
		times = new JTextField();
		pane.add(times);
		JButton ok = new JButton("OK");
		ok.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (Util.nonEmpty(times.getText())) {
					status = APPROVAL;
					dialog.setVisible(false);
				}
			}
		});
		dialog.add(ok);
		dialog.pack();
		dialog.setLocationRelativeTo(parent);
		dialog.setVisible(true);
		return status;
	}
	
	String getText() {
		return "loop " + times.getText() + "\nend";
	}
}

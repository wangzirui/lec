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

public class ActionReader {
	JDialog dialog;
	JTextField ins;
	JTextField code;
	int status;
	static int APPROVAL = 0, CANCEL = 1;
	
	int showDialog(JFrame parent) {
		status = CANCEL;
		dialog = new JDialog(parent, "New Local Action", true);
		Container pane = dialog.getContentPane();
		pane.setLayout(new GridLayout(0, 2));
		pane.add(new Label("Instance"));
		ins = new JTextField();
		pane.add(ins);
		pane.add(new Label("Method Call"));
		code = new JTextField();
		pane.add(code);
		JButton ok = new JButton("OK");
		ok.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (Util.nonEmpty(ins.getText())) {
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
		return "local_action " + ins.getText() + " \"" + code.getText().replace("\n", "\\n") + "\"";
	}
}

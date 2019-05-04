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

public class ConditionReader {
	JDialog dialog;
	JTextField content, objs;
	int status;
	static int APPROVAL = 0, CANCEL = 1;
	JComboBox temp;
	
	int showDialog(JFrame parent) {
		status = CANCEL;
		dialog = new JDialog(parent, "New Condition", true);
		Container pane = dialog.getContentPane();
		pane.setLayout(new GridLayout(0, 2));
		pane.add(new Label("Condition"));
		content = new JTextField();
		pane.add(content);
		pane.add(new Label("Instances"));
		objs = new JTextField();
		pane.add(objs);
		pane.add(new Label("Temperature"));
		temp = new JComboBox(new String[] {"cold", "hot"});
		pane.add(temp);
		JButton ok = new JButton("OK");
		ok.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (Util.nonEmpty(objs.getText())) {
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
		return "condition " + temp.getSelectedItem() + " \"" + content.getText() + "\" " + objs.getText();
	}
}

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

public class MessageReader {
	JDialog dialog;
	JTextField obj1, obj2, content;
	int status;
	static int APPROVAL = 0, CANCEL = 1;
	JComboBox temp;
	
	int showDialog(JFrame parent) {
		status = CANCEL;
		dialog = new JDialog(parent, "New Message", true);
		Container pane = dialog.getContentPane();
		pane.setLayout(new GridLayout(0, 2));
		pane.add(new Label("Sender"));
		obj1 = new JTextField();
		pane.add(obj1);
		pane.add(new Label("Receiver"));
		obj2 = new JTextField();
		pane.add(obj2);
		pane.add(new Label("Message"));
		content = new JTextField();
		pane.add(content);
		pane.add(new Label("Temperature"));
		temp = new JComboBox(new String[] {"cold", "hot"});
		pane.add(temp);
		JButton ok = new JButton("OK");
		ok.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (Util.nonEmpty(obj1.getText()) &&
						Util.nonEmpty(obj2.getText())) {
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
		return "message " + temp.getSelectedItem() + " " + obj1.getText() + " " + obj2.getText() +
			" \"" + content.getText() + "\"";
	}
}

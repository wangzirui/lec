package ui;

import java.awt.Container;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

public class IfReader {
	JDialog dialog;
	int status;
	static int APPROVAL = 0, CANCEL = 1;
	JRadioButton without, with;
	JTextField cond;
	
	int showDialog(JFrame parent) {
		status = CANCEL;
		dialog = new JDialog(parent, "New If Block", true);
		Container pane = dialog.getContentPane();
		pane.setLayout(new GridLayout(0, 2));
		
		pane.add(new Label("Condition"));
		cond = new JTextField();
		pane.add(cond);
		without = new JRadioButton("Without Else");
		without.setSelected(true);
		with = new JRadioButton("With Else");
		ButtonGroup g = new ButtonGroup();
		g.add(without);
		g.add(with);
		pane.add(without);
		pane.add(with);
		JButton ok = new JButton("OK");
		ok.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				status = APPROVAL;
				dialog.setVisible(false);
			}
		});
		pane.add(ok);
		dialog.pack();
		dialog.setLocationRelativeTo(parent);
		dialog.setVisible(true);
		return status;
	}

	String getText() {
		return "if \"" + cond.getText() + "\"\n" +
			(without.isSelected() ? "" : "else\n") + "end";
	}
}



import javax.swing.SwingUtilities;

import ui.UserInterface;

public class Editor {
	public static Editor editor;
	public UserInterface ui;
	
	public Editor() {
		ui = new UserInterface();
	}
	
	public static void main(String args[]) {
		editor = new Editor();
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				editor.ui.createAndShowGUI();
			}
		});
	}
}

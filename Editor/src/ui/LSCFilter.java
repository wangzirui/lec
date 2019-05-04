package ui;

import java.io.File;

import javax.swing.filechooser.FileFilter;

public class LSCFilter extends FileFilter {
	public boolean accept(File f) {
		if (f.isDirectory())
			return true;
		String name = f.getName();
		if (name.substring(name.lastIndexOf('.') + 1).equals("lsc"))
			return true;
		return false;
	}
	public String getDescription() {
		return "LSC Files (*.lsc)";
	}
}

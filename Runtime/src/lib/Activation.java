package lib;

import java.util.Stack;

public class Activation {
	String name;
	String[] code;
	int ptr;
	
	Stack<Integer> toRun;
	Stack<String> exitLabel;
	
	boolean ret;

	public Activation() {
		toRun = new Stack<Integer>();
		exitLabel = new Stack<String>();
	}
	
	public Activation(String name, String[] code) {
		this();
		this.name = name;
		this.code = code;
	}
}

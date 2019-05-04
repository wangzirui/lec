package lib;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.StringTokenizer;
import java.util.Vector;


public class Sys {
	public String[][] participants;
	public String[][][] code;
	public String[] actCond;
	public Vector<Instance> insts = new Vector<Instance>();
	int count;
	boolean stopped;
	
	public void run() {
		while (true) {
			System.out.println(">>Issue a command, e.g. " + actCond[0] + ", or press Ctrl-C to quit.");
			BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
			String line = "";
			try {
				line = in.readLine();
			} catch (IOException e) {
				System.err.println(e);
				return;
			}
			StringTokenizer t = new StringTokenizer(line);
			t.nextToken();
			String inst = t.nextToken();
			getInst(inst).exec(line, -1);
		}
	}
	
	void hotVio(String s) {
		System.out.println("Hot condition violation: " + s);
		stopped = true;
	}
	
	void activate(String line) {
//		System.out.println("activating " + line);
		for (int i = 0; i < actCond.length; i++) {
			if (actCond[i].equals(line)) {
				for (int j = 0; j < participants[i].length; j++) {
					Instance inst = getInst(participants[i][j]);
					inst.addChart(String.valueOf(count), code[i][j], line);
					if (!getThread(inst).isAlive()) {
						renewThread(inst);
						getThread(inst).start();
					}
				}
				count++;
			}
		}
		
	}

	void remove(String name) {
		for (int i = 0; i < insts.size(); i++) {
			Instance inst = insts.get(i);
			for (int j = 0; j < inst.charts.size(); j++)
				if (inst.charts.get(j).name.equals(name))
					inst.charts.remove(j);
		}
	}
	void exitChart(String actName, String label) {
		for (int i = 0; i < insts.size(); i++)
			for (int j = 0; j < insts.get(i).charts.size(); j++) {
				Activation chart = insts.get(i).charts.get(j);
				if (chart.name.equals(actName))
					if (chart.exitLabel.contains(label)) {
						while (!chart.code[chart.ptr].equals("label " + label)) {
							chart.ptr++;
						}
						while (!chart.exitLabel.peek().equals(label)) {
							chart.exitLabel.pop();
							chart.toRun.pop();
						}
					}
			}
	}
	
	public boolean eval(String s) {
		return true;
	}
	
	public Instance getInst(String s) {
		return null;
	}
	
	public Thread getThread(Instance i) {
		return null;
	}
	
	public Thread getThread(String s) {
		return getThread(getInst(s));
	}
	
	public void renewThread(Instance i) {
		
	}
}

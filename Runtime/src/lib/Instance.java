package lib;

import java.util.StringTokenizer;
import java.util.Vector;


public class Instance implements Runnable {
	Vector<Activation> charts = new Vector<Activation>();
	Sys sys;
	Vector<String> queue = new Vector<String>();
	int chartSelector;
	
	public Instance() {
		
	}
	
	public Instance(Sys sys) {
		this.sys = sys;
	}
	
	void addChart(String name, String[] s, String actCond) {
		Activation chart = new Activation(name, s);
		charts.add(chart);
		
		// Since initial event has occurred,
//		if (chart.code[0].startsWith("send")) {
//			chart.ptr = 1;
//		}
		String line = chart.code[chart.ptr];
		if (isStructural(line))
			exec(line, charts.size() - 1);
	}
	
	public void run() {
		while (charts.size() > 0 && !sys.stopped) {
			selectNRun();
		}
	}
	
	void selectNRun() {
		boolean run = false;
		for (int i = 0; i < charts.size(); i++) {
			int j = (i + chartSelector) % charts.size(); 
			String line = charts.get(j).code[charts.get(j).ptr];
			if (!violate(line, j)) {
				chartSelector = j;
				exec(line, chartSelector);
				run = true;
				break;
			}
		}
		if (!run) {
			rest();
		}
	}
	
	boolean violate(String line, int ch) {
		if (isStructural(line))
			return false;
		if (line.startsWith("ret_send") ||
				line.startsWith("ret_receive"))
			return false;

		for (int i = 0; i < charts.size(); i++) {
			Activation chart = charts.get(i);
			int m = findMain(chart);
			if (chart.ptr < m) {
				if (i == ch) {
					//return true;
					continue;
				} else {
					continue;
				}
			} else {
				if (i == ch) {
					continue;
				} else {
					int n = chart.code.length;
					int front = 0;
					
					while (front < n && !chart.code[front].equals(line))
						front++;
					if (front < n && front != chart.ptr)
						return true;
				}
			}
		}
		return false;
	}
	
	public void exec(String line, int ch) {
		StringTokenizer t = new StringTokenizer(line);
		String comm = t.nextToken();
		Activation chart = new Activation();
		if (0 <= ch && ch < charts.size())
			chart = charts.get(ch);

		if (comm.equals("jump")) {
			String lab = t.nextToken();
			int i = 0;
			while (!chart.code[i].equals("label " + lab))
				i++;
			chart.ptr = i;
			
		} else if (comm.equals("jon")) {
			if (!chart.ret) {
				String label = t.nextToken();
				int i = 0;
				while (!chart.code[i].equals("label " + label))
					i++;
				chart.ptr = i;
			} else {
				adv(ch);
			}
			
		} else if (comm.equals("anymore")) {
			chart.ret = chart.toRun.peek() > 0;
			adv(ch);

		} else if (comm.equals("subchart_start")) {
			String lab = t.nextToken();
			int times = Integer.parseInt(t.nextToken());
			
			if (chart.exitLabel.contains(lab)) {
				assert chart.exitLabel.peek().equals(lab);
				times = chart.toRun.peek() - 1;
				chart.toRun.pop();
				chart.toRun.push(times);
			} else {
				chart.exitLabel.push(lab);
				chart.toRun.push(times);
			}
			adv(ch);
		
		} else if (comm.equals("label")) {
			String label = t.nextToken();
			if (chart.exitLabel.contains(label)) {
				String retLab;
				do {
					retLab = chart.exitLabel.pop();
					chart.toRun.pop();
				} while (!retLab.equals(label));
			}
			adv(ch);

		} else if (comm.equals("condition")) {
			chart.ret = sys.eval(line.substring(line.indexOf('\"') + 1,
					line.lastIndexOf('\"')));
			String temp = t.nextToken();
			println(line);
			if (!chart.ret) {
				if (temp.equals("cold")) {
					sys.exitChart(chart.name, chart.exitLabel.peek());
				} else if (temp.equals("hot")) {
					sys.hotVio(line);
				}
			}
			adv(ch);
		} else if (comm.equals("ret_send")) {
			String sender = t.nextToken(), target = t.nextToken();
			String msg = sender + ":__ret=\"" + chart.ret + "\"";
			sys.getInst(target).queue.add(msg);
			sys.getThread(target).interrupt();
			adv(ch);
			
		} else if (comm.equals("ret_receive")) {
			String sender = t.nextToken();
			String msg = sender + ":__ret=\"";
			int p;
			while (true) {
				p = 0;
				while (p < queue.size() && !queue.get(p).startsWith(msg))
					p++;
				if (p >= queue.size())
					rest();
				else break;
			}
			msg = queue.get(p);
			queue.remove(p);
			msg = msg.substring(msg.indexOf('\"') + 1, msg.lastIndexOf('\"'));
			chart.ret = msg.equals("true");
			adv(ch);
			
		} else if (comm.equals("send")) {
			adv(line);
			String sender = t.nextToken();
			String target = t.nextToken();
			String msg = sender + ":" + 
				line.substring(line.indexOf('\"') + 1, line.lastIndexOf('\"'));
			println(line);
			sys.getInst(target).queue.add(msg);
			sys.getThread(target).interrupt();
		
		} else if (comm.equals("receive")) {
			String sender = t.nextToken();
			String msg = sender + ":" +
				line.substring(line.indexOf('\"') + 1, line.lastIndexOf('\"'));
			while (!queue.contains(msg)) {
				rest();
			}
			queue.remove(msg);
			println(line);
			adv(line);

		} else if (comm.equals("local_action")) {
			String call = line.substring(line.indexOf('\"') + 1, line.lastIndexOf('\"'));
			println(line);
			methodCall(call);
			adv(line);
			
		}
	}
	
	void adv(String line) {
		sys.activate(line);
		preVio(line);
		for (int i = 0; i < charts.size(); i++) {
			if (charts.get(i).code[charts.get(i).ptr].equals(line)) {
				charts.get(i).ptr++;
				chkDone(i);
			}
		}
	}
	
	void println(String s) {
		System.out.println(s);
	}
	void adv(int ch) {
		charts.get(ch).ptr++;
		if (charts.get(ch).ptr < charts.get(ch).code.length) {
			String line = charts.get(ch).code[charts.get(ch).ptr];
			if (isStructural(line))
				exec(line, ch);
		}
		chkDone(ch);
	}
	
	boolean isStructural(String line) {
		return !line.startsWith("send") &&
			!line.startsWith("receive") &&
			!line.startsWith("local_action") &&
			!line.startsWith("ret_send") &&
			!line.startsWith("ret_receive");
	}

	int findMain(Activation chart) {
		int m = 0;
		while (!chart.code[m].equals("label main"))
			m++;
		return m;
	}
	
	void preVio(String line) {
		for (int i = 0; i < charts.size(); i++) {
			Activation chart = charts.get(i);
			int m = findMain(chart);
			if (chart.ptr < m) {
				if (chart.code[chart.ptr].equals(line))
					return;
				int n = 0;
				while (n < chart.code.length && !chart.code[n].equals(line))
					n++;
				if (n < chart.code.length)
					sys.remove(chart.name);
			}
		}
	}
	
	void chkDone(int ch) {
		if (charts.get(ch).ptr >= charts.get(ch).code.length) {
			charts.remove(ch);
		}
	}

	void rest() {
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {	
		}
	}
	
	public void methodCall(String call) {
		
	}
}

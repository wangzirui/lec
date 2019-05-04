package translator;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Stack;
import java.util.StringTokenizer;
import java.util.Vector;


public class Translator {
	public static void main(String[] args) {
		try {
			System.out.print("Give me source file: ");
			BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
			String filename = in.readLine();
			String input = "";
			BufferedReader file = new BufferedReader(new FileReader(filename));
			for (String line; (line = file.readLine()) != null;)
				input += line + "\n";
			System.out.print("Give me output path: ");
			String path = in.readLine();
	
			Translator t = new Translator();
			t.translate(input, path);
		} catch (IOException e) {
			System.err.println(e);
		}
	}
	public void translate(String input, String path) {
		Vector<String> v = addInsts(input);
		System.out.println("after addInsts:");
		System.out.println(v);
		v = m2b(v);
		System.out.println("after m2b:");
		System.out.println(v);
		distribute(v, path);
	}
	
	Vector<String> addInsts(String source) {
		StringTokenizer lines = new StringTokenizer(source, "\n");
		Vector<String> out = new Vector<String>();
		Stack<Integer> toIns = new Stack<Integer>();
		Stack<Set<String> > insts = new Stack<Set<String> >();
		while (lines.hasMoreTokens()) {
			String line = lines.nextToken();
			out.add(line);
			if (line.startsWith("prechart") ||
					line.startsWith("subchart") ||
					line.startsWith("if") ||
					line.startsWith("loop")) {
				toIns.add(out.size() - 1);
				insts.add(new HashSet<String>());
			} else if (line.startsWith("end")) {
				String s = out.get(toIns.peek());
				for (Iterator<String> i = insts.peek().iterator(); i.hasNext();)
					s += " " + i.next();
				out.set(toIns.peek(), s);
				toIns.pop();
				Set<String> v = insts.pop();
				if (!insts.empty())
					insts.peek().addAll(v);
			} else if (line.startsWith("message")) {
				StringTokenizer t = new StringTokenizer(line);
				t.nextToken();
				t.nextToken();
				insts.peek().add(t.nextToken());
				insts.peek().add(t.nextToken());
			} else if (line.startsWith("condition")) {
				StringTokenizer t =
					new StringTokenizer(line.substring(line.lastIndexOf('\"') + 1));
				while (t.hasMoreTokens())
					insts.peek().add(t.nextToken());
			} else if (line.startsWith("local_action")) {
				StringTokenizer t = new StringTokenizer(line);
				t.nextToken();
				insts.peek().add(t.nextToken());
			}
		}
		return out;
	}
	
	Vector<String> m2b(Vector<String> in) {
		Vector<String> out = new Vector<String>();
		
		Stack<Integer> labels = new Stack<Integer>();
		Stack<Vector<String> > insts = new Stack<Vector<String> >();
		Stack<String> type = new Stack<String>();
		
		Vector<Integer> elseLabs = new Vector<Integer>();
		
		int label = 0;
		for (int i = 0; i < in.size(); i++) {
			String line = in.get(i);
			
			if (line.startsWith("message")) {
				StringTokenizer t = new StringTokenizer(line);
				t.nextToken();
				t.nextToken();
				String sender = t.nextToken(), receiver = t.nextToken();
				String msg =
					line.substring(line.indexOf('\"') + 1, line.lastIndexOf('\"'));
				
				out.add("send " + sender + " " + receiver + " \"" + msg + "\"");
				out.add("receive " + sender + " " + receiver + " \"" + msg + "\"");
			
			} else if (line.startsWith("local_action")) {
				out.add(line);
				
			} else if (line.startsWith("condition")) {
				StringTokenizer t = new StringTokenizer(line);
				t.nextToken();
				String temp = t.nextToken();
				String exp =
					line.substring(line.indexOf('\"') + 1, line.lastIndexOf('\"'));
				Vector<String> v = new Vector<String>();
				t = new StringTokenizer(line.substring(line.lastIndexOf('\"') + 1));
				while (t.hasMoreTokens())
					v.add(t.nextToken());
				
				out.add("sync_forward " + toS(v));
				out.add("condition " + temp + " \"" + exp + "\" " + v.lastElement());
				out.add("sync_backward " + toS(v));
				
			} else if (line.startsWith("prechart")) {
				labels.push(label);
				label += 2;
				insts.push(new Vector<String>());
				StringTokenizer t = new StringTokenizer(line);
				t.nextToken();
				while (t.hasMoreTokens())
					insts.peek().add(t.nextToken());
				type.push("prechart");
				
				out.add("subchart_start " + labels.peek() + " 1 " + toS(insts.peek()));
				
			} else if (line.startsWith("subchart")) {
				labels.push(label);
				label += 2;
				insts.push(new Vector<String>());
				StringTokenizer t = new StringTokenizer(line);
				t.nextToken();
				while (t.hasMoreTokens())
					insts.peek().add(t.nextToken());
				type.push("subchart");
				
				out.add("sync_forward " + toS(insts.peek()));
				out.add("sync_backward " + toS(insts.peek()));
				out.add("subchart_start " + labels.peek() + " 1 " + toS(insts.peek()));
				
			} else if (line.startsWith("loop")) {
				labels.push(label);
				label += 2;
				insts.push(new Vector<String>());
				StringTokenizer t = new StringTokenizer(line);
				t.nextToken();
				int toRun;
				String times = t.nextToken();
				if (t.equals("*"))
					toRun = Integer.MAX_VALUE;
				else toRun = Integer.parseInt(times);
				while (t.hasMoreTokens())
					insts.peek().add(t.nextToken());
				type.push("loop");
				
				out.add("label " + (labels.peek() + 1) + " " + toS(insts.peek()));
				out.add("sync_forward " + toS(insts.peek()));
				out.add("sync_backward " + toS(insts.peek()));
				out.add("subchart_start " + labels.peek() + " " + toRun + " " + toS(insts.peek()));
				out.add("anymore " + toS(insts.peek()));
				out.add("jon " + labels.peek() + " "  + toS(insts.peek()));
				
			} else if (line.startsWith("if")) {
				labels.push(label);
				label += 2;
				insts.push(new Vector<String>());
				StringTokenizer t =
					new StringTokenizer(line.substring(line.lastIndexOf('\"') + 1));
				while (t.hasMoreTokens())
					insts.peek().add(t.nextToken());
				type.push("if");
				String exp = line.substring(line.indexOf('\"') + 1, line.lastIndexOf('\"'));
				
				out.add("sync_forward " + toS(insts.peek()));
				out.add("condition if \"" + exp + "\" " + insts.peek().lastElement());
				out.add("sync_backward " + toS(insts.peek()));
				out.add("subchart_start " + labels.peek() + " 1 " + toS(insts.peek()));
				out.add("jon " + (labels.peek() + 1) + " " + toS(insts.peek()));
				
			} else if (line.startsWith("else")) {
				out.add("jump " + labels.peek() + " " + toS(insts.peek()));
				out.add("label " + (labels.peek() + 1) + " " + toS(insts.peek()));
				elseLabs.add(labels.peek() + 1);
				
			} else if (line.startsWith("main")) {
				out.add("sync_forward " + toS(insts.peek()));
				out.add("sync_backward " + toS(insts.peek()));
				out.add("label main " + toS(insts.peek()));
				
			} else if (line.startsWith("end")) {
				if (type.peek().equals("prechart") ||
						type.peek().equals("subchart")) {
					out.add("label " + labels.peek() + " " + toS(insts.peek()));
					out.add("sync_forward " + toS(insts.peek()));
					out.add("sync_backward " + toS(insts.peek()));
				} else if (type.peek().equals("if")) {
					if (!elseLabs.contains(labels.peek() + 1))
						out.add("label " + (labels.peek() + 1) + " " + toS(insts.peek()));
					out.add("label " + labels.peek() + " " + toS(insts.peek()));
					out.add("sync_forward " + toS(insts.peek()));
					out.add("sync_backward " + toS(insts.peek()));
				} else if (type.peek().equals("loop")) {
					out.add("jump " + (labels.peek() + 1) + " " + toS(insts.peek()));
					out.add("label " + labels.peek() + " " + toS(insts.peek()));
					out.add("sync_forward " + toS(insts.peek()));
					out.add("sync_backward " + toS(insts.peek()));
				}
				type.pop();
				labels.pop();
				insts.pop();
				
			} else if (line.startsWith("lsc")) {
				StringTokenizer t = new StringTokenizer(line);
				t.nextToken();
				String temp = t.nextToken();
				if (temp.equals("universal")) {
					String s = in.get(i + 1);
					s = s.substring(s.indexOf(" ") + 1);
					out.add("lsc " + temp + " " + s);
				} else {
					do {
						i++;
					} while (!in.get(i).startsWith("lsc"));
					i--;
				}
			}
		}
		return out;
	}

	Vector<String> findMinEvents(Vector<String> in) {
		Vector<String> minEvents = new Vector<String>();
		for (int i = 0; i < in.size(); i++)
			if (in.get(i).startsWith("lsc")) {
				int j = i + 1;
				while (j < in.size() &&
						!in.get(j).startsWith("lsc") &&
						!minEventCri(in.get(j))) {
					j++;
				}
				String s = "";
				if (j < in.size() && minEventCri(in.get(j))) {
					s = in.get(j);
					in.remove(j);
					in.add(i + 1, s);
				}
				minEvents.add(s);
			}
		return minEvents;
	}
	
	boolean minEventCri(String line) {
		return line.startsWith("send") ||
			line.startsWith("local_action");
	}
	
	void distribute(Vector<String> in, String path) {
		Vector<String> minEvents = findMinEvents(in);
		System.out.println("after findMinEvents:");
		System.out.println(in);
		Vector<Vector<String> > participants = new Vector<Vector<String> >();
		Vector<Vector<Vector<String> > > code = new Vector<Vector<Vector<String> > >();
		Set<String> conds = new HashSet<String>();
		Set<String> instances = new HashSet<String>();
		Set<String> localActions = new HashSet<String>();
		for (int i = 0; i < in.size(); i++) {
			while (i < in.size() && !in.get(i).startsWith("lsc"))
				i++;
			if (i >= in.size())
				break;
			// new chart discovered
			Vector<String> insts = new Vector<String>();
			Vector<Vector<String> > parts = new Vector<Vector<String> >();
			StringTokenizer tt = new StringTokenizer(in.get(i));
			tt.nextToken();
			tt.nextToken();
			while (tt.hasMoreTokens()) {
				String inst = tt.nextToken();
				Vector<String> part = new Vector<String>();
				for (int j = i + 1; j < in.size() && !in.get(j).startsWith("lsc"); j++) {
					String line = in.get(j);
					if (line.startsWith("condition")) {
						StringTokenizer t =
							new StringTokenizer(line.substring(line.lastIndexOf('\"') + 1));
						if (inst.equals(t.nextToken())) {
							part.add(line.substring(0, line.lastIndexOf('\"') + 1));
							conds.add(line.substring(line.indexOf('\"') + 1, line.lastIndexOf('\"')));
						}
					} else if (line.startsWith("send")) {
						StringTokenizer t = new StringTokenizer(line);
						t.nextToken();
						if (inst.equals(t.nextToken())) {
							part.add(line);
						}
					} else if (line.startsWith("receive")) {
						StringTokenizer t = new StringTokenizer(line);
						t.nextToken();
						t.nextToken();
						if (inst.equals(t.nextToken())) {
							part.add(line);
						}
					} else if (line.startsWith("local_action")) {
						StringTokenizer t = new StringTokenizer(line);
						t.nextToken();
						if (inst.equals(t.nextToken())) {
							part.add(line);
							localActions.add(line);
						}
					} else if (line.startsWith("jump") ||
							line.startsWith("jon") ||
							line.startsWith("label") ||
							line.startsWith("subchart_start") ||
							line.startsWith("anymore")) {
						StringTokenizer t = new StringTokenizer(line);
						String out = "";
						out += t.nextToken();
						if (!line.startsWith("anymore"))
							out += " " + t.nextToken();
						if (line.startsWith("subchart_start"))
							out += " " + t.nextToken();
						boolean flag = false;
						while (t.hasMoreTokens() && !flag)
							if (inst.equals(t.nextToken()))
								flag = true;
						if (flag)
							part.add(out);
					} else if (line.startsWith("sync_forward")) {
						StringTokenizer t = new StringTokenizer(line);
						t.nextToken();
						Vector<String> v = new Vector<String>();
						while (t.hasMoreTokens())
							v.add(t.nextToken());
						if (v.contains(inst)) {
							int p = v.indexOf(inst);
							if (p != 0)
								part.add("ret_receive " + v.get(p - 1) + " " + inst);
							if (p != v.size() - 1)
								part.add("ret_send " + inst + " " + v.get(p + 1));
						}
					} else if (line.startsWith("sync_backward")) {
						StringTokenizer t = new StringTokenizer(line);
						t.nextToken();
						Vector<String> v = new Vector<String>();
						while (t.hasMoreTokens())
							v.add(t.nextToken());
						if (v.contains(inst)) {
							int p = v.indexOf(inst);
							if (p != v.size() - 1)
								part.add("ret_receive " + v.get(p + 1) + " " + inst);
							if (p != 0)
								part.add("ret_send " + inst + " " + v.get(p - 1));
						}
					}
				}
				insts.add(inst);
				parts.add(part);
			}
			participants.add(insts);
			instances.addAll(insts);
			code.add(parts);
		}
		System.out.println("after distribute:");
		System.out.println(code);
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(path + "/Main.java"));
			out.write(codeGen(instances, conds, minEvents, participants, code));
			out.close();
			for (Iterator<String> i = instances.iterator(); i.hasNext();) {
				String inst = i.next();
				out = new BufferedWriter(new FileWriter(path + "/" + big(inst) + ".java"));
				String s = "";
				s += "import lib.Instance;\n";
				s += "import lib.Sys;\n";
				s += "public class " + big(inst) + " extends Instance {\n";
				s += "\tpublic " + big(inst) + "(Sys s) {\n";
				s += "\t\tsuper(s);\n";
				s += "\t}\n";
				String ss = "";
				for (Iterator<String> j = localActions.iterator(); j.hasNext();) {
					String line = j.next();
					StringTokenizer t = new StringTokenizer(line);
					t.nextToken();
					String instLocal = t.nextToken();
					if (instLocal.equals(inst)) {
						String call =
							line.substring(line.indexOf('\"') + 1, line.lastIndexOf('\"'));
						ss += "\t\tif (s.equals(\"" + call + "\"))\n";
						ss += "\t\t\t" + call + ";\n";
					}
				}
				if (!ss.equals("")) {
					s += "\tpublic void methodCall(String s) {\n";
					s += ss;
					s += "\t}\n";
				}
				s += "}\n";
				out.write(s);
				out.close();
			}
		} catch (IOException e) {
			System.err.println(e);
		}
	}

	String codeGen(Set<String> instances,
			Set<String> conds,
			Vector<String> minEvents,
			Vector<Vector<String> > participants,
			Vector<Vector<Vector<String> > > code) {
		String out = "";
		out += "import lib.Instance;\n";
		out += "import lib.Sys;\n";
		out += "public class Main extends Sys {\n";
		for (Iterator<String> i = instances.iterator(); i.hasNext();) {
			String inst = i.next();
			out += "\t" + big(inst) + " " + inst + ";\n";
			out += "\tThread th" + big(inst) + " = new Thread();\n";
		}
		
		out += "\tpublic boolean eval(String s) {\n";
		for (Iterator<String> i = conds.iterator(); i.hasNext();) {
			String cond = i.next();
			out += "\t\tif (s.equals(\"" + cond + "\"))\n";
			out += "\t\t\treturn " + cond + ";\n";
		}
		out += "\t\treturn false;\n";
		out += "\t}\n";
		
		out += "\tpublic Instance getInst(String s) {\n";
		for (Iterator<String> i = instances.iterator(); i.hasNext();) {
			String inst = i.next();
			out += "\t\tif (s.equals(\"" + inst + "\"))\n";
			out += "\t\t\treturn " + inst + ";\n";
		}
		out += "\t\treturn null;\n";
		out += "\t}\n";
		
		out += "\tpublic Thread getThread(Instance i) {\n";
		for (Iterator<String> i = instances.iterator(); i.hasNext();) {
			String inst = i.next();
			out += "\t\tif (i.equals(" + inst + "))\n";
			out += "\t\t\treturn th" + big(inst) + ";\n";
		}
		out += "\t\treturn null;\n";
		out += "\t}\n";
		
		out += "\tpublic void renewThread(Instance i) {\n";
		for (Iterator<String> i = instances.iterator(); i.hasNext();) {
			String inst = i.next();
			out += "\t\tif (i.equals(" + inst + "))\n";
			out += "\t\t\tth" + big(inst) + " = new Thread(" + inst + ");\n";
		}
		out += "\t}\n";

		out += "\tpublic Main() {\n";
		for (Iterator<String> i = instances.iterator(); i.hasNext();) {
			String inst = i.next();
			out += "\t\t" + inst + " = new " + big(inst) + "(this);\n";
			out += "\t\tinsts.add(" + inst + ");\n";
		}
		
		out += "\t\tparticipants = new String[][] {\n";
		for (int i = 0; i < participants.size(); i++) {
			out += "\t\t\t{\n";
			out += "\t\t\t\t";
			for (int j = 0; j < participants.get(i).size(); j++)
				out += "\"" + participants.get(i).get(j) + "\", ";
			out += "\n";
			out += "\t\t\t},\n";
		}
		out += "\t\t};\n";
		
		out += "\t\tcode = new String[][][] {\n";
		for (int i = 0; i < code.size(); i++) {
			out += "\t\t\t{\n";
			for (int j = 0; j < code.get(i).size(); j++) {
				out += "\t\t\t\t{\n";
				for (int k = 0; k < code.get(i).get(j).size(); k++) {
					out += "\t\t\t\t\t\"" + convQuote(code.get(i).get(j).get(k)) + "\",\n";
				}
				out += "\t\t\t\t},\n";
			}
			out += "\t\t\t},\n";
		}
		out += "\t\t};\n";
		
		out += "\t\tactCond = new String[] {\n";
		for (int i = 0; i < minEvents.size(); i++)
			out += "\t\t\t\"" + convQuote(minEvents.get(i)) + "\",\n";
		out += "\t\t};\n";
		out += "\t}\n";
		
		out += "\tpublic static void main(String[] args) {\n";
		out += "\t\tMain m = new Main();\n";
		out += "\t\tm.run();\n";
		out += "\t}\n";
		out += "}\n";
		return out;
	}
	
	String convQuote(String s) {
		String out = "";
		for (int i = 0; i < s.length(); i++)
			if (s.charAt(i) == '\"')
				out += "\\" + s.charAt(i);
			else out += s.charAt(i);
		return out;
	}
	
	String big(String small) {
		return (char)(small.charAt(0) + ('A' - 'a')) + small.substring(1);
	}
	
	String toS(Vector<String> v) {
		String s = "";
		for (int i = 0; i < v.size(); i++)
			s += " " + v.get(i);
		return s.substring(1);
	}
}

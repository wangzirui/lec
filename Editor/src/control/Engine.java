	/* TODO 
	 * * position of hot message. Check if all positions are right.
	 * * Get the picture position adjustable.
	 * * Encrypt/decrypt.
	 */

package control;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.StringTokenizer;

import model.Condition;
import model.If;
import model.Instance;
import model.LSC;
import model.Location;
import model.Loop;
import model.Subchart;

public class Engine {
	static int xScale = 90, yScale = 25;
	Graphics2D g;
	BasicStroke solid;
	BasicStroke dashed;
	LSC lsc;
	Stack<Subchart> stack;
	public static int min, max;
	static int hOff;
	
	public Engine(Graphics2D g) {
		this.g = g;
		solid = (BasicStroke)g.getStroke();
		dashed = new BasicStroke(solid.getLineWidth(),
				solid.getEndCap(), solid.getLineJoin(), solid.getMiterLimit(),
				new float[] {4, 5}, solid.getDashPhase());
		lsc = new LSC();
	}
	
	public static void reset() {
		hOff = 90;
	}
	
	public static void adjust() {
		hOff = 90 + Math.max(0, -min + 20);
	}
	
	public static int x(int pos) {
		return pos * xScale + hOff;
	}
	
	public static int y(int ver) {
		return ver * yScale + 110;
	}
	
	public void draw(String s, boolean showRod, int rod) {
		min = Integer.MAX_VALUE;
		max = 0;
		int pos = 0;
		stack = new Stack<Subchart>();
		Subchart curr = null;
		StringTokenizer lines = new StringTokenizer(s, "\n");
		
		while (lines.hasMoreTokens()) {
			String line = lines.nextToken();
			StringTokenizer t = new StringTokenizer(line);
			String comm = t.nextToken();
			
			if (comm.equals("lsc")) {
				lsc.mode = t.nextToken();
				String name =
					line.substring(line.indexOf('\"') + 1, line.lastIndexOf('\"'));
				pos--;
				g.drawRect(10, 10, g.getFontMetrics().stringWidth(name) + 20, 30);
				g.drawString(name, 20, 30);
				
			} else if (comm.equals("message")) {
				String temp = t.nextToken();
				
				Instance a = new Instance(t.nextToken()), b = new Instance(t.nextToken());
				a = lsc.addAndGetInstance(a);
				b = lsc.addAndGetInstance(b);

				addInstanceToActiveCharts(a, stack);
				drawLifeline(a, pos);
				a.locations.add(new Location(pos, temp));
				if (!a.equals(b)) {
					addInstanceToActiveCharts(b, stack);
					drawLifeline(b, pos);
					b.locations.add(new Location(pos, temp));
				}
				
				int p = lsc.instances.indexOf(a), q = lsc.instances.indexOf(b);
				curr.adjustWidth(p);
				curr.adjustWidth(q);
				
				if (a.equals(b)) {
					if (temp.equals("cold")) {
						g.setColor(Color.BLUE);
						g.setStroke(dashed);
					} else {
						assert temp.equals("hot");
						g.setColor(Color.RED);
						g.setStroke(solid);
					}
					g.drawPolyline(new int[] { x(p), x(p) + 20, x(p) + 20, x(p) + 5 },
							new int[] { y(pos) - 10, y(pos) - 10, y(pos), y(pos) }, 4);
					g.setStroke(solid);
					g.drawPolygon(new int[] { x(p) + 5, x(p), x(p) + 5 },
							new int[] { y(pos) - 5, y(pos), y(pos) + 5}, 3);
					g.setColor(Color.BLACK);
					String message =
						line.substring(line.indexOf('\"') + 1, line.lastIndexOf('\"'));
					g.drawString(message, x(p) + 24, y(pos));
					curr.rightOff = Math.max(curr.rightOff,
							x(p) + 25 + g.getFontMetrics().stringWidth(message) - x(curr.right));
				} else {
					assert !a.equals(b);
					if (temp.equals("cold")) {
						g.setColor(Color.BLUE);
						g.setStroke(dashed);
					} else {
						assert temp.equals("hot");
						g.setColor(Color.RED);
						g.setStroke(solid);
					}
					if (q > p) {
						g.drawLine(x(p), y(pos), x(q) - 5, y(pos));
						g.setStroke(solid);
						g.drawPolygon(new int[] { x(q) - 5, x(q), x(q) - 5 },
								new int[] { y(pos) - 5, y(pos), y(pos) + 5 }, 3);
					} else {
						g.drawLine(x(p), y(pos), x(q) + 5, y(pos));
						g.setStroke(solid);
						g.drawPolygon(new int[] { x(q) + 5, x(q), x(q) + 5 },
								new int[] { y(pos) - 5, y(pos), y(pos) + 5 }, 3);
					}
					g.setColor(Color.BLACK);
					String message =
						line.substring(line.indexOf('\"') + 1, line.lastIndexOf('\"'));
					g.drawString(message, (x(p) + x(q)) / 2 - g.getFontMetrics().stringWidth(message) / 2, y(pos) - 5);
					curr.rightOff = Math.max(curr.rightOff,
							x(Math.min(p, q)) + 10 + g.getFontMetrics().stringWidth(message) - x(curr.right));
				}
				
			} else if (comm.equals("condition")) {
				String temp = t.nextToken(),
					content = line.substring(line.indexOf('\"') + 1, line.lastIndexOf('\"'));
				List<Instance> local = new ArrayList<Instance>();
				List<Integer> positions = new ArrayList<Integer>();
				int min = -1, max = -1;
				t = new StringTokenizer(line.substring(line.lastIndexOf('\"') + 1));
				while (t.hasMoreTokens()) {
					Instance i = new Instance(t.nextToken());
					i = lsc.addAndGetInstance(i);
					local.add(i);
					addInstanceToActiveCharts(i, stack);
					drawLifeline(i, pos, 10);
					i.locations.add(new Location(pos, "cold"));
					int p = lsc.instances.indexOf(i);
					positions.add(p);
					curr.adjustWidth(p);
					if (min == -1 || p < min) min = p;
					if (max == -1 || p > max) max = p;
					g.drawArc(x(p) - 4, y(pos) - 14, 8, 8, 0, -180);
					g.drawArc(x(p) - 4, y(pos) + 6, 8, 8, 0, 180);
				}
				// TODO Consider drawing it right in the middle. But left side cannot be readjusted.
				int left = x(min) - xScale / 3;
				int right = Math.max(x(max) + xScale / 3, left + g.getFontMetrics().stringWidth(content) + 20);
				if (curr.right == max)
					curr.rightOff = Math.max(curr.rightOff, right - x(max) + 10);
				Condition cond = new Condition();
				cond.instances = local;
				cond.pos = pos;
				cond.temp = temp;
				cond.left = left;
				cond.right = right;
				lsc.conditions.add(cond);
				
				if (temp.equals("cold")) {
					g.setStroke(dashed);
					g.setColor(Color.BLUE);
				} else {
					assert temp.equals("hot");
					g.setColor(Color.RED);
				}
				g.drawPolygon(new int[] { left, left + 10, right - 10, right, right - 10, left + 10 },
						new int[] { y(pos), y(pos) - 10, y(pos) - 10, y(pos), y(pos) + 10, y(pos) + 10 },
						6);
				g.setStroke(solid);
				g.setColor(Color.BLACK);
				g.drawString(content, left + 10, y(pos) + 5);
				
			} else if (comm.equals("local_action")) {
				Instance a = new Instance(t.nextToken());
				a = lsc.addAndGetInstance(a);
				
				addInstanceToActiveCharts(a, stack);
				drawLifeline(a, pos, 10);
				a.locations.add(new Location(pos, "cold", "local_action"));
				
				int p = lsc.instances.indexOf(a);
				curr.adjustWidth(p);
				
				String text = line.substring(line.indexOf('\"') + 1, line.lastIndexOf('\"'));
				g.drawRect(x(p) - g.getFontMetrics().stringWidth(text) / 2 - 5, y(pos) - 10,
						g.getFontMetrics().stringWidth(text) + 10, 20);
				g.drawString(text, x(p) - g.getFontMetrics().stringWidth(text) / 2, y(pos) + 5);
				
			} else if (comm.equals("subchart") ||
					comm.equals("prechart") ||
					comm.equals("loop") ||
					comm.equals("if")) {

				if (comm.equals("loop")) {
					Loop loop = new Loop();
					loop.times = t.nextToken();
					curr = loop;
				} else if (comm.equals("if")) {
					If i = new If();
					i.cond = line.substring(line.indexOf('\"') + 1, line.lastIndexOf('\"'));
					curr = i;
				} else curr = new Subchart();

				curr.pos = pos;
				stack.push(curr);
				lsc.subcharts.add(curr);
				
			} else if (comm.equals("else")) {
				((If)curr).elsePos = pos;
				
			} else if (comm.equals("end")) {
				stack.pop();
				if (curr.left == -1 && curr.right == -1) {
					curr.left = 0;
					curr.right = 0;
				}
				if (stack.empty() && lsc.mode.equals("existential")) {
					g.setStroke(dashed);
				} 
				curr.end = pos;
				g.drawRect(x(curr.left) - curr.leftOff,
						y(curr.pos) - (curr instanceof If ? 10 : 0),
						x(curr.right) + curr.rightOff - (x(curr.left) - curr.leftOff), 
						y(pos) - y(curr.pos) + (curr instanceof If ? 10 : 0));
				g.setStroke(solid);
				
				if (curr instanceof Loop) {
					Loop loop = (Loop)curr;
					g.drawString(loop.times, x(curr.left) - curr.leftOff + 5, y(curr.pos) + 15);
				} else if (curr instanceof If) {
					If i = (If)curr;
					int left = x(curr.left) - curr.leftOff;
					int right = left + g.getFontMetrics().stringWidth(i.cond) + 20;
					g.setStroke(dashed);
					g.setColor(Color.BLUE);
					g.drawPolygon(new int[] { left, left + 10, right - 10, right, right - 10, left + 10 },
							new int[] { y(i.pos), y(i.pos) - 10, y(i.pos) - 10, y(i.pos), y(i.pos) + 10, y(i.pos) + 10 },
							6);
					g.setStroke(solid);
					g.setColor(Color.BLACK);
					g.drawString(i.cond, left + 10, y(i.pos) + 5);
					
					Condition cond = new Condition();
					cond.pos = curr.pos;
					cond.left = left;
					cond.right = right;
					lsc.conditions.add(cond);

					if (i.elsePos != -1)
						g.drawLine(x(curr.left) - curr.leftOff, y(i.elsePos),
								x(curr.right) + curr.rightOff, y(i.elsePos));
				}
				
				for (Iterator<Instance> i = curr.instances.iterator(); i.hasNext();) {
					Instance ins = i.next();
					drawLifeline(ins, pos);
					ins.locations.add(new Location(pos, "cold"));
				}
				min = Math.min(x(curr.left) - curr.leftOff, min);
				max = Math.max(x(curr.right) + curr.rightOff, max);
				if (!stack.empty()) {
					Subchart prev = stack.peek();
					if (prev.left == -1 || curr.left <= prev.left) {
						prev.left = curr.left;
						prev.leftOff = curr.leftOff + 9;
					}
					if (prev.right == -1 || curr.right >= prev.right) {
						prev.right = curr.right;
						prev.rightOff = curr.rightOff + 9;
					}
					curr = prev;
				}
			} else if (comm.equals("main")) {
				Subchart newCh = new Subchart();
				newCh.pos = pos;
				if (!stack.empty()) {
					stack.pop();
					if (curr.left == -1 && curr.right == -1) {
						curr.left = 0;
						curr.right = 0;
					}
					g.setStroke(dashed);
					g.setColor(Color.BLUE);
					g.drawPolygon(
							new int[] { x(curr.left) - curr.leftOff, 
									x(curr.right) + curr.rightOff,
									x(curr.right) + curr.rightOff + 10,
									x(curr.right) + curr.rightOff,
									x(curr.left) - curr.leftOff,
									x(curr.left) - curr.leftOff - 10 },
							new int[] {	y(curr.pos), y(curr.pos),
									(y(curr.pos) + y(pos)) / 2,
									y(pos), y(pos), (y(curr.pos) + y(pos)) / 2 }, 6);
					g.setColor(Color.BLACK);
					g.setStroke(solid);
					for (Iterator<Instance> i = curr.instances.iterator(); i.hasNext();) {
						Instance ins = i.next();
						drawLifeline(ins, pos);
						ins.locations.add(new Location(pos, "cold"));
					}
					curr.end = pos;
					newCh.left = curr.left;
					newCh.right = curr.right;
					newCh.leftOff = curr.leftOff;
					newCh.rightOff = curr.rightOff;
					newCh.instances = curr.instances;
				}
				curr = newCh;
				/* I didn't set current.left = 0 alone. If you do, check if it works for this
				 * minimal test case: "prechart main end".
				 */
				stack.push(curr);
				lsc.subcharts.add(curr);
			}
			pos++;
		}

		if (showRod && pos - 2 >= rod) {
			g.setColor(Color.MAGENTA);
			g.setStroke(dashed);
			g.drawLine(min - 10, y(rod) + yScale / 2,	max + 10,
					y(rod) + yScale / 2);
			g.setStroke(solid);
			g.setColor(Color.BLACK);
		}
	}

	void addInstanceToActiveCharts(Instance a, Collection<Subchart> s) {
		for (Iterator<Subchart> i = s.iterator(); i.hasNext();) {
			Subchart chart = i.next();
			chart.instances.add(a);

			if (!a.locations.contains(new Location(chart.pos, "cold"))) {
				drawLifeline(a, chart.pos);
				a.locations.add(new Location(chart.pos, "cold"));
			}
		}
	}
	
	void drawLifeline(Instance a, int pos) {
		drawLifeline(a, pos, 0);
	}
	
	void drawLifeline(Instance a, int pos, int off) {
		int hor = lsc.instances.indexOf(a);
		Location l = a.locations.get(a.locations.size() - 1);
		if (l.pos == -1) {
			g.drawRect(x(hor) - 30, y(-2) - 5, 60, yScale);
			g.drawString(a.name, x(hor) - g.getFontMetrics().stringWidth(a.name) / 2, y(-1) - 12);
		}
		if (l.temp.equals("hot")) {
			g.setStroke(solid);
		} else {
			assert l.temp.equals("cold");
			g.setStroke(dashed);
		}
		// avoid
		int[] p = new int[lsc.subcharts.size() + lsc.conditions.size()];
		int pp = 0;
		Map<Integer, Object> map = new HashMap<Integer, Object>();
		for (Iterator<Subchart> i = lsc.subcharts.iterator(); i.hasNext();) {
			Subchart chart = i.next();
			p[pp++] = chart.pos;
			map.put(chart.pos, chart);
		}
		for (Iterator<Condition> i = lsc.conditions.iterator(); i.hasNext();) {
			Condition cond = i.next();
			p[pp++] = cond.pos;
			map.put(cond.pos, cond);
		}
		Arrays.sort(p);
		int topoff = "local_action".equals(l.type) ? 10 : 0;
		for (int i = 0; i < p.length; i++) {
			Object o = map.get(p[i]);
			if (o instanceof Subchart) {
				Subchart chart = (Subchart)o;
				if (chart.end != -1 && l.pos < chart.pos && x(hor) > x(chart.left) - chart.leftOff &&
						x(hor) < x(chart.right) + chart.rightOff) {
					g.drawLine(x(hor), y(l.pos) + topoff, x(hor), y(chart.pos));
					l.pos = chart.end;
					topoff = 0;
				}
			} else if (o instanceof Condition) {
				Condition cond = (Condition)o;
				if (cond.pos == l.pos) {
					topoff = 10;
				} else if (l.pos < cond.pos && x(hor) > cond.left && x(hor) < cond.right) {
					g.drawLine(x(hor), y(l.pos) + topoff, x(hor), y(cond.pos) - 10);
					topoff = 10;
					l.pos = cond.pos;
				}
			}
		}
		g.drawLine(x(hor), y(l.pos) + topoff, x(hor), y(pos) - off);
		g.setStroke(solid);
	}
}

package ui;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import translator.Translator;
import control.Display;
import control.Engine;

public class UserInterface implements ListSelectionListener {
	JScrollPane scroller;
	JTextArea text;
	JList list;
	JFrame frame;
	DefaultListModel listModel;
	Vector<String> texts;
	public int rod;
	public boolean showRod;
	
	public void createAndShowGUI() {
		frame = new JFrame("LSC Editor");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		scroller = new JScrollPane();
		text = new JTextArea();
		JButton update = new JButton("Save Changes");
		update.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				update();
			}
		});
		JPanel upper = new JPanel();
		upper.setLayout(new BoxLayout(upper, BoxLayout.LINE_AXIS));
		upper.add(new JLabel("LSCm Editor"));
		upper.add(Box.createHorizontalStrut(30));
		upper.add(update);
		text.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
		JPanel codeButton = new JPanel(new BorderLayout());
		codeButton.add(upper, BorderLayout.NORTH);
		codeButton.add(new JScrollPane(text));
		JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scroller, codeButton);
		split.setResizeWeight(0.8);
		JPanel lowerRight = new JPanel(new BorderLayout());
		lowerRight.add(split);
		
		JButton newChart = new JButton("New Chart");
		newChart.addActionListener(new ToolsListener());

		JButton rod = new JButton("Show/Hide Rod");
		rod.addActionListener(new ToolsListener());
		JButton up = new JButton("Up");
		up.addActionListener(new ToolsListener());
		JButton down = new JButton("Down");
		down.addActionListener(new ToolsListener());
		
		JButton delete = new JButton("Delete");
		delete.addActionListener(new ToolsListener());
		
		JButton message = new JButton("Message");
		message.addActionListener(new ToolsListener());
		JButton condition = new JButton("Condition");
		condition.addActionListener(new ToolsListener());
		JButton action = new JButton("Local Action");
		action.addActionListener(new ToolsListener());
		
		JButton ifElse = new JButton("If");
		ifElse.addActionListener(new ToolsListener());
		JButton loop = new JButton("Loop");
		loop.addActionListener(new ToolsListener());
		JButton subchart = new JButton("Subchart");
		subchart.addActionListener(new ToolsListener());
		
		JPanel line1 = new JPanel();
		line1.setLayout(new BoxLayout(line1, BoxLayout.LINE_AXIS));
		line1.add(newChart);
		line1.add(Box.createHorizontalStrut(4));
		line1.add(rod);
		line1.add(Box.createHorizontalStrut(2));
		line1.add(up);
		line1.add(Box.createHorizontalStrut(2));
		line1.add(down);
		line1.add(Box.createHorizontalStrut(4));
		line1.add(delete);
		JPanel line2 = new JPanel();
		line2.setLayout(new BoxLayout(line2, BoxLayout.LINE_AXIS));
		line2.add(message);
		line2.add(Box.createHorizontalStrut(2));
		line2.add(condition);
		line2.add(Box.createHorizontalStrut(2));
		line2.add(action);
		line2.add(Box.createHorizontalStrut(4));
		line2.add(ifElse);
		line2.add(Box.createHorizontalStrut(2));
		line2.add(loop);
		line2.add(Box.createHorizontalStrut(2));
		line2.add(subchart);
		JPanel tools = new JPanel();
		tools.setLayout(new BorderLayout());
		tools.add(line1, BorderLayout.NORTH);
		tools.add(new JLabel("Constructs:"));
		tools.add(line2, BorderLayout.SOUTH);
		
		JPanel right = new JPanel(new BorderLayout());
		right.add(tools, BorderLayout.NORTH);
		right.add(lowerRight);
		frame.getContentPane().add(right);
		
		listModel = new DefaultListModel();
		texts = new Vector<String>();
		list = new JList(listModel);
		list.addListSelectionListener(this);
		JButton remove = new JButton("Delete");
		remove.addActionListener(new Eraser());
		JPanel listTitleNDel = new JPanel();
		listTitleNDel.setLayout(new BoxLayout(listTitleNDel, BoxLayout.LINE_AXIS));
		listTitleNDel.add(new JLabel("List of Charts"));
		listTitleNDel.add(Box.createHorizontalStrut(30));
		listTitleNDel.add(remove);
		JPanel leftPane = new JPanel(new BorderLayout());
		leftPane.add(listTitleNDel, BorderLayout.NORTH);
		leftPane.add(new JScrollPane(list));
		frame.getContentPane().add(leftPane, BorderLayout.EAST);
		
		JMenuItem open = new JMenuItem("Open...", KeyEvent.VK_O);
		open.addActionListener(new MenuListener());
		JMenuItem save = new JMenuItem("Save...", KeyEvent.VK_S);
		save.addActionListener(new MenuListener());
		JMenu file = new JMenu("File");
		file.setMnemonic(KeyEvent.VK_F);
		file.add(open);
		file.add(save);
		
		JMenuItem synthesize = new JMenuItem("Synthesize...", KeyEvent.VK_S);
		synthesize.addActionListener(new MenuListener());
		JMenu toolMenu = new JMenu("Tools");
		toolMenu.setMnemonic(KeyEvent.VK_T);
		toolMenu.add(synthesize);
		
		JMenuBar bar = new JMenuBar();
		bar.add(file);
		bar.add(toolMenu);
		frame.setJMenuBar(bar);
		
		frame.setSize(1024, 740);
		frame.setVisible(true);
	}
	
	class ToolsListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			JButton source = (JButton)e.getSource();
			if (source.getText().equals("Show/Hide Rod")) {
				if (!text.getText().startsWith("lsc"))
					return;
				showRod = !showRod;
				update();
				
			} else if (source.getText().equals("Up")) {
				if (showRod && rod > 0) {
					rod--;
					update();
				}
				
			} else if (source.getText().equals("Down")) {
				if (showRod && 
						rod + 3 < new StringTokenizer(text.getText(), "\n").countTokens()) {
					rod++;
					update();
				}
				
			} else if (source.getText().equals("Message")) {
				if (showRod) {
					MessageReader mr = new MessageReader();
					if (mr.showDialog(frame) == MessageReader.APPROVAL) {
						text.setText(insert(text.getText(), rod + 2, mr.getText()));
						rod++;
						update();
					}
				}
				
			} else if (source.getText().equals("New Chart")) {
				NewChartReader nr = new NewChartReader();
				if (nr.showDialog(frame) == NewChartReader.APPROVAL) {
					text.setText(nr.getText());
					rod = 0;
					update();
				}
				
			} else if (source.getText().equals("Subchart")) {
				if (showRod) {
					text.setText(insert(text.getText(), rod + 2, "subchart\nend"));
					rod++;
					update();
				}
				
			} else if (source.getText().equals("If")) {
				if (showRod) {
					IfReader ir = new IfReader();
					if (ir.showDialog(frame) == IfReader.APPROVAL) {
						text.setText(insert(text.getText(), rod + 2, ir.getText()));
						rod++;
						update();
					}
				}
			} else if (source.getText().equals("Loop")) {
				if (showRod) {
					LoopReader lr = new LoopReader();
					if (lr.showDialog(frame) == LoopReader.APPROVAL) {
						text.setText(insert(text.getText(), rod + 2, lr.getText()));
						rod++;
						update();
					}
				}
			} else if (source.getText().equals("Delete")) {
				if (showRod) {
					text.setText(delete(text.getText(), rod + 2));
					update();
				}
				
			} else if (source.getText().equals("Condition")) {
				if (showRod) {
					ConditionReader cr = new ConditionReader();
					if (cr.showDialog(frame) == ConditionReader.APPROVAL) {
						text.setText(insert(text.getText(), rod + 2, cr.getText()));
						rod++;
						update();
					}
				}
				
			} else if (source.getText().equals("Local Action")) {
				if (showRod) {
					ActionReader ar = new ActionReader();
					if (ar.showDialog(frame) == ActionReader.APPROVAL) {
						text.setText(insert(text.getText(), rod + 2, ar.getText()));
						rod++;
						update();
					}
				}
			}
		}
	}

	String delete(String s, int p) {
		StringTokenizer t = new StringTokenizer(s, "\n");
		String r = "";
		while (p-- > 0)
			r += t.nextToken() + "\n";
		String line = t.nextToken();
		String comm = new StringTokenizer(line).nextToken();
		if (comm.equals("subchart") ||
				comm.equals("if") ||
				comm.equals("loop")) {
			int l = 1;
			while (l > 0) {
				comm = new StringTokenizer(t.nextToken()).nextToken();
				if (comm.equals("subchart") ||
						comm.equals("if") ||
						comm.equals("loop"))
					l++;
				else if (comm.equals("end"))
					l--;
			}
		} else if (comm.equals("message") || comm.equals("condition") ||
				comm.equals("local_action")) {
			// skip this line
		} else {
			r += line + "\n";
		}
		
		while (t.hasMoreTokens()) {
			r += t.nextToken() + "\n";
		}
		return r;
	}
	
	String insert(String a, int p, String b) {
		StringTokenizer t = new StringTokenizer(a, "\n");
		String r = "";
		while (p-- > 0)
			r += t.nextToken() + "\n";
		r += b + "\n";
		while (t.hasMoreTokens())
			r += t.nextToken() + "\n";
		return r;
	}
	
	class MenuListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			JMenuItem source = (JMenuItem)e.getSource();
			if (source.getText().equals("Open...")) {
				JFileChooser fc = new JFileChooser();
				fc.setAcceptAllFileFilterUsed(false);
				fc.addChoosableFileFilter(new LSCFilter());
				if (fc.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
					listModel.clear();
					texts.clear();
					
					File file = fc.getSelectedFile();
					try {
						BufferedReader in = new BufferedReader(new FileReader(file));
						String s;
						while ((s = in.readLine()) != null) {
							StringTokenizer t = new StringTokenizer(s);
							if (t.hasMoreTokens() && t.nextToken().equals("lsc")) {
								update();
								text.setText("");
							}
							text.setText(text.getText() + s + "\n");
						}
						update();
					} catch (IOException ioe) {
						System.err.println(ioe);
					}
				}
			} else if (source.getText().equals("Save...")) {
				JFileChooser fc = new JFileChooser();
				fc.setAcceptAllFileFilterUsed(false);
				fc.addChoosableFileFilter(new LSCFilter());
				if (fc.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION) {
					File file = fc.getSelectedFile();
					String s = "";
					for (Iterator<String> i = texts.iterator(); i.hasNext();) {
						s += i.next();
					}
					try {
						BufferedWriter out = new BufferedWriter(new FileWriter(file));
						out.write(s);
						out.close();
					} catch (IOException ioe) {
						System.err.println(ioe);
					}
				}
			} else if (source.getText().equals("Synthesize...")){
				JFileChooser fc = new JFileChooser();
				fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				if (fc.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION) {
					File file = fc.getSelectedFile();
					String s = "";
					for (Iterator<String> i = texts.iterator(); i.hasNext();)
						s += i.next();
					new Translator().translate(s, file.getAbsolutePath());
				}
			}
		}
	}
	
	class Eraser implements ActionListener { 
		public void actionPerformed(ActionEvent e) {
			int p = list.getSelectedIndex();
			if (p == -1)
				return;
			texts.remove(p);
			listModel.remove(p);
			if (p == listModel.size()) {
				p--;
			}
			if (p == -1) {
				text.setText("");
				redraw();
			} else {
				list.setSelectedIndex(p);
			}
		}
	}
	
	void update() {
		StringTokenizer t = new StringTokenizer(text.getText(), "\n");
		if (t.hasMoreTokens()) {
			if (text.getText().charAt(text.getText().length() - 1) != '\n')
				text.setText(text.getText() + "\n");
			String firstLine = t.nextToken();
			String name =
				firstLine.substring(firstLine.indexOf('\"') + 1, firstLine.lastIndexOf('\"'));
			if (listModel.contains(name)) {
				int p = listModel.indexOf(name);
				texts.set(p, text.getText());
				list.setSelectedIndex(p);
			} else {
				listModel.addElement(name);
				texts.add(text.getText());
				list.setSelectedIndex(listModel.size() - 1); // Questionable
			}
			redraw();
		}
	}
	
	void redraw() {
		Display trial = new Display(1, 1);
		Engine.reset();
		new Engine(trial.graphics).draw(text.getText(), showRod, rod);
		final Display display = new Display(Engine.max - Math.min(0, Engine.min) + 30,
				Engine.y(new StringTokenizer(text.getText(), "\n").countTokens()));
		Engine.adjust();
		new Engine(display.graphics).draw(text.getText(), showRod, rod);
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				scroller.setViewportView(display);
			}
		});
	}
	
	public void valueChanged(ListSelectionEvent e) {
		if (!e.getValueIsAdjusting() && list.getSelectedIndex() >= 0) {
			text.setText(texts.get(list.getSelectedIndex()));
			rod = 0;
			showRod = false;
			redraw();
		}
	}
}

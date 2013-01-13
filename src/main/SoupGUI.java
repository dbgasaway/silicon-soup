package main;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.*;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

import cellProcesses.Cell;
import cellProcesses.SoupManager;
import cellProcesses.Code;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Scanner;
import java.util.Stack;

import comparators.*;

public class SoupGUI extends JFrame implements Runnable {

	private static final long serialVersionUID = 5066959112531736616L;
	
	private SoupManager soup;
	private JPanel mainPanel;
	private final JMenuBar menuBar;
	private final JMenu fileMenu;
	private final JMenuItem fileSaveAsOption;
	private final JTextArea topCells;
	private final JTextArea soupInfo;
	private final JPanel options;
	private final JButton pauseButton;
	private final JTextField input;
	
	private Deque<Cell> cells;
	private Deque<byte[]> codes;

	public SoupGUI() {
		super();
		
		for(LookAndFeelInfo i : UIManager.getInstalledLookAndFeels()) {
			if("nimbus".equalsIgnoreCase(i.getName())) {
				try {
					UIManager.setLookAndFeel(i.getClassName());
					break;
				} catch (ClassNotFoundException e) { //see documentation in setLookAndFeel for info on these exceptions
					e.printStackTrace();
				} catch (InstantiationException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (UnsupportedLookAndFeelException e) {
					e.printStackTrace();
				}
			}
		}
		
		setTitle("SiliconSoup");
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		//this.setSize(500, 500);
		
		menuBar = new JMenuBar();
		fileMenu = new JMenu("File");
		fileMenu.setMnemonic(KeyEvent.VK_F);
		menuBar.add(fileMenu);
		
		fileSaveAsOption = new JMenuItem("Save As");
		fileSaveAsOption.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK + ActionEvent.SHIFT_MASK));
		fileSaveAsOption.setMnemonic(KeyEvent.VK_A);
		fileSaveAsOption.setDisplayedMnemonicIndex(5);
		fileMenu.add(fileSaveAsOption);
		add(menuBar, BorderLayout.PAGE_START);
		
		mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.X_AXIS));
		
		add(mainPanel, BorderLayout.WEST);
		
		topCells = new JTextArea();
		topCells.setColumns(50);
		topCells.setRows(10);
		topCells.setEditable(false);
		topCells.setLineWrap(false);
		
		soupInfo = new JTextArea();
		soupInfo.setColumns(14);
		soupInfo.setRows(5);
		soupInfo.setEditable(false);
		soupInfo.setLineWrap(false);
		
		options = new JPanel();
		options.setLayout(new BoxLayout(options, BoxLayout.Y_AXIS));
		
		pauseButton = new JButton("START/STOP");
		pauseButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				isRunning = !isRunning;
				System.out.println("Running= " + isRunning);
				if(!isRunning) {
					for(byte[] b : soup.get10Codes()) {
						if(b != null) {
							System.out.println(Arrays.toString(Code.getCodeNameList(b)));
						}
					}
				}
			}
		});
		
		input = new JTextField();
		input.setColumns(30);
		input.setText("hi");
		input.setBackground(Color.CYAN);
		input.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				String in = input.getText();
				Scanner scan = new Scanner(in);
				if(scan.hasNext()) {
					String command = scan.next();
					if(command.equals("comp")) {
						String[] optimals = getComparisonOptimals(scan);
						System.out.println(optimals[0] + "\n" + optimals[1]);
					} else if(command.equals("help")) {
						System.out.println("valid commands are: \nhelp\ncomp\ndisp");
					} else if(command.equals("disp")) {
						if(scan.hasNext()) {
							String aCell = scan.next();
							Code aCode = soup.getCode(aCell);
							if(aCode == null) {
								System.out.println("Code not found for: " + aCell);
							} else {
								System.out.println(aCell + ": " + Arrays.toString(Code.getCodeNameList(aCode.getCode())));
							}
						} else {
							System.out.println("disp uses 1 argument only");
						}
					} else if(command.equals("diff")) {
						String[] optimals = getComparisonOptimals(scan);
						optimals = SequenceAlignment.highlightDifferences(optimals[0], optimals[1]);
						System.out.println(optimals[0] + "\n" + optimals[1]);
					} else if(command.equals("ancestry")){
						if(scan.hasNext()) {
							String aCell = scan.next();
							Code aCode = soup.getCode(aCell);
							if(aCode == null) {
								System.out.println("Code not found for: " + aCell);
							} else {
								System.out.println(aCell + " descends from: " + aCode.getParent());
							}
						} else {
							System.out.println("ancestry currently uses 1 argument only");
						}
					} else if(command.equals("tree")) {
						String[] options = getArgs(scan);
						if(options.length == 0) {
							System.out.println("tree needs at least one argument");
						} else {
							if(options[0].equals("codes")) {
								Collection<Code> codes = soup.getAllCodes(); 
								DefaultMutableTreeNode origin = new DefaultMutableTreeNode("Codes from 6666god");
								Code c;
								for(Iterator<Code> i = codes.iterator(); codes.size() > 0;) {
									c = i.next();
									if(c.getParent() == "6666god") {
										origin.add(new DefaultMutableTreeNode(c));
										i.remove();
									} else {
										Enumeration e = origin.breadthFirstEnumeration();
										while(e.hasMoreElements()) {
											DefaultMutableTreeNode node = (DefaultMutableTreeNode) e.nextElement();
											if(c.getParent().equals(node.toString())) {
												node.add(new DefaultMutableTreeNode(c));
												i.remove();
											}
										}
										//i.remove();//can be used to insure tree clears
									}
									if(!i.hasNext()) {
										i = codes.iterator();
									}
								}
								JTree tree = new JTree(origin);
								JScrollPane view = new JScrollPane(tree);
								JFrame treeFrame = new JFrame("Code tree");
								treeFrame.add(view);
								treeFrame.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
								treeFrame.pack();
								treeFrame.setVisible(true);
							} else {
								System.out.println("Invalid option: " + options[0]);
							}
						}
					} else {
						System.out.println("invalid command");
					}
				}
				input.setText("");
			}
		});
		
		topCells.setAlignmentY(TOP_ALIGNMENT);
		mainPanel.add(topCells);
		soupInfo.setAlignmentY(TOP_ALIGNMENT);
		mainPanel.add(soupInfo);
		pauseButton.setAlignmentX(LEFT_ALIGNMENT);
		options.add(pauseButton);
		input.setAlignmentX(LEFT_ALIGNMENT);
		options.add(input);
		options.setAlignmentY(TOP_ALIGNMENT);
		mainPanel.add(options);

		soup = new SoupManager();
		cells = new ArrayDeque<Cell>();
		codes = new ArrayDeque<byte[]>();
		isRunning = false;
		
		pack();
		setVisible(true);
	}
	
	private String[] getArgs(Scanner scan) {
		String options = scan.nextLine().trim();
		String[] comps = options.split("[ ]+");
		return comps;
	}
	
	private String[] getComparisonOptimals(Scanner scan) {
		String options = scan.nextLine().trim();
		String[] comps = options.split("[ ]+");
		if(comps.length < 2) {
			System.out.println("Invalid comp: 2 arguments needed, given: " + options);
			input.setText("");
			return null;
		}
		Code c1 = soup.getCode(comps[0]);
		Code c2 = soup.getCode(comps[1]);
		if(c1 == null || c2 == null) {
			System.out.println("c1 or c2: null");
		} else {
			String s1 = Arrays.toString(Code.getCodeNameList(c1.getCode()));
			String s2 = Arrays.toString(Code.getCodeNameList(c2.getCode()));
			int[][] result = SequenceAlignment.findEditDistance(s1, s2);
			String[] optimals = SequenceAlignment.findOptimalString(result, s1, s2);
			return optimals;
		}
		return null;
	}
	
	private boolean isRunning;
	
	@Override
	public void run() {
		while(true) {
			//System.out.println("Run");
			if(isRunning) {
				//System.out.println("Really running");
				while(!cells.isEmpty()) {
					soup.addCell(cells.pop());
				}
				//System.out.println("Cleared new Cells");
				while(!codes.isEmpty()) {
					soup.addCell(codes.pop());
				}
				//System.out.println("Cleared new Codes");
				for(int i = 0; i < 1; i++) {
					soup.act();
				}
				//System.out.println("acted");
				String tx = "";
				String[] lines = soup.getTopGenes();
				for(String str : lines) {
					tx += str + "\n";
				}
				//System.out.println(tx);
				topCells.setText(tx);
				soupInfo.setText("Cycles: " + soup.getCycles()
						+ "\nTotal Cells: " + soup.getTotalCells()
						+ "\nTotal Codes: " + soup.getTotalCodes()
						+ "\nActive Codes: " + soup.getActiveCodes()
						+ "\nAllocated Space: " + soup.getAllocatedSpace()
						+ "\nTotal Cell Memory: " + soup.getCellReservedSpace()
						+ "\nMean Cell Memory: " + soup.getCellReservedSpace() / soup.getTotalCells());
			} else {
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	/**adds a cell to the soup*/
	public void addCell(Cell c) {
		cells.push(c);
	}
	
	/**adds a code as a cell to the soup*/
	public void addCell(byte[] code) {
		codes.push(code);
	}
}

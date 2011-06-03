package main;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.*;

import cellProcesses.Cell;
import cellProcesses.SoupManager;
import cellProcesses.Code;

import java.util.Arrays;
import java.util.Stack;

public class SoupGUI extends JFrame implements Runnable {
	/**
	 * default id
	 */
	private static final long serialVersionUID = 1L;
	
	private SoupManager s;
	private JPanel p;
	private final JTextArea t;
	private final JTextArea input;
	private final JTextArea totalCycles;
	private final JButton b;
	private Stack<Cell> cells;
	private Stack<byte[]> codes;

	public SoupGUI() {
		this.setTitle("SiliconSoup");
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		//this.setSize(500, 500);
		
		p = new JPanel();
		
		this.add(p);
		
		t = new JTextArea();
		t.setColumns(50);
		t.setRows(10);
		t.setEditable(false);
		t.setLineWrap(false);
		
		totalCycles = new JTextArea();
		totalCycles.setColumns(12);
		totalCycles.setRows(5);
		totalCycles.setEditable(false);
		totalCycles.setLineWrap(false);
		
		b = new JButton("START/STOP");
		b.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				isRunning = !isRunning;
				System.out.println("Running= " + isRunning);
				if(!isRunning) {
					for(byte[] b : s.get10Codes()) {
						System.out.println(Arrays.toString(Code.getCodeNameList(b)));
					}
				}
			}
		});
		
		input = new JTextArea();
		input.setColumns(30);
		input.setRows(1);
		//input.
		input.setLineWrap(false);
		input.setText("hi");
		input.setBackground(Color.CYAN);
		input.addKeyListener(new KeyListener() {
			@Override
			public void keyPressed(KeyEvent arg0) {
				// TODO Auto-generated method stub
				
			}
			@Override
			public void keyReleased(KeyEvent arg0) {
				// TODO Auto-generated method stub
				
			}
			@Override
			public void keyTyped(KeyEvent arg0) {
				char c = arg0.getKeyChar();
				if(c == '\n' || c == 'r') {
					Code cd = s.getCode(input.getText());
					System.out.println(cd);
					if(cd != null) System.out.println(Arrays.toString(Code.getCodeNameList(cd.getCode())));
					input.setText("");
				}
			}			
		});
		
		p.add(t);
		p.add(totalCycles);
		p.add(b);
		p.add(input);

		s = new SoupManager();
		cells = new Stack<Cell>();
		codes = new Stack<byte[]>();
		isRunning = false;
		
		this.pack();
		this.setVisible(true);
	}
	
	private boolean isRunning;
	
	@Override
	public void run() {
		while(true) {
			//System.out.println("Run");
			if(isRunning) {
				//System.out.println("Really running");
				while(!cells.empty()) {
					s.addCell(cells.pop());
				}
				//System.out.println("Cleared new Cells1");
				while(!codes.empty()) {
					s.addCell(codes.pop());
				}
				//System.out.println("Cleared new Cells2");
				for(int i = 0; i < 1; i++) {
					s.act();
				}
				//System.out.println("acted");
				String tx = "";
				String[] lines = s.getTopGenes();
				for(String str : lines) {
					tx += str + "\n";
				}
				//System.out.println(tx);
				t.setText(tx);
				totalCycles.setText("Cycles: " + s.getCycles()
						+ "\nTotal Cells: " + s.getTotalCells()
						+ "\nTotal Codes: " + s.getTotalCodes()
						+ "\nAllocated Space: " + s.getAllocatedSpace());
			} else {
				Object o = new Object();
				synchronized(o) {
					try {
						o.wait(10);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	public void addCell(Cell c) {
		cells.push(c);
	}
	
	public void addCell(byte[] code) {
		codes.push(code);
	}
}

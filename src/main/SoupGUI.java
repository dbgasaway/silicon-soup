package main;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

import cellProcesses.Cell;
import cellProcesses.SoupManager;

import java.util.Stack;

public class SoupGUI extends JFrame implements Runnable {
	/**
	 * default id
	 */
	private static final long serialVersionUID = 1L;
	
	private SoupManager s;
	private JPanel p;
	private final JTextArea t;
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
		
		b = new JButton("START/STOP");
		b.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				isRunning = !isRunning;
				System.out.println("Running= " + isRunning);
			}
		});
		
		p.add(t);
		p.add(b);

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
				for(int i = 0; i < 100; i++) {
					s.act();
				}
				System.out.println("acted");
				String tx = "";
				String[] lines = s.getTopGenes();
				for(String str : lines) {
					tx += str + "\n";
				}
				//System.out.println(tx);
				t.setText(tx);
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

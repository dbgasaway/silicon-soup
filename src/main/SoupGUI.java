package main;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

import cellProcesses.Cell;
import cellProcesses.SoupManager;

public class SoupGUI extends JFrame implements Runnable {
	/**
	 * default id
	 */
	private static final long serialVersionUID = 1L;
	
	private SoupManager s;
	private JPanel p;
	private final JTextArea t;
	private final JButton b;

	public SoupGUI() {
		this.setTitle("SiliconSoup");
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
			}
		});
		
		p.add(t);

		s = new SoupManager();
		isRunning = false;
		
		this.setVisible(true);
	}
	
	private boolean isRunning;

	@Override
	public void run() {
		while(true) {
			if(isRunning) {
				for(int i = 0; i < 100; i++) {
					s.act();
				}
				String tx = "";
				String[] lines = s.getTopGenes();
				for(String str : lines) {
					tx += str + "\n";
				}
				t.setText(tx);
			} else {
				Object o = new Object();
				synchronized(o) {
					try {
						o.wait(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	public void addCell(Cell c) {
		//TODO: allow concurrent cell addition
		s.addCell(c);
	}
}

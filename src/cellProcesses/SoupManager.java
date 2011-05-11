package cellProcesses;

import java.util.*;

public class SoupManager {
	private byte[] soup;
	private boolean[] lockedMem;
	private LinkedList<Cell> cells;
	private ArrayList<Code> codes;
	
	private static final int SOUP_SIZE = 10000;
	
	public SoupManager() {
		soup = new byte[SOUP_SIZE];
		lockedMem = new boolean[SOUP_SIZE];
		cells = new LinkedList<Cell>();
		codes = new ArrayList<Code>();
	}
	
	public byte getValue(int ip) {
		ip = ip % soup.length;
		return soup[ip];
	}
	
	public byte[] getSoup() {
		return soup;
	}
	
	private void garbageCollect() {
		//TODO: arranges memory to increase contiguous space
	}
	
	private boolean addCell(Cell c) {
		int ix = findSpace(c.getSize());
		//TODO: this
	}

	private int findSpace(int size) {
		// TODO Auto-generated method stub
		return 0;
	}
}

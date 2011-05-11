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
	
	/**returns the value at ix with circular memory*/
	public byte getValue(int ix) {
		ix = ix % soup.length;
		return soup[ix];
	}
	
	/**returns the locked status at ix assuming circular memory*/
	public boolean getLockVal(int ix) {
		ix = ix % lockedMem.length;
		return lockedMem[ix];
	}
	
	/**returns the distance between two addresses in circular memory*/
	public int getDist(int ix, int iy) {
		ix = ix % SOUP_SIZE;
		iy = iy % SOUP_SIZE;
		return Math.abs(ix - iy);
	}
	
	public byte[] getSoup() {
		return soup.clone();
	}
	
	public int getSoupSize() {
		return SOUP_SIZE;
	}
	
	/**Rearranges soup memory to increase contiguous space*/
	private void garbageCollect() {
		//TODO: arranges memory to increase contiguous space
	}
	
	/**Adds a cell to the soup, and returns if it was successful*/
	public boolean addCell(Cell c) {
		int ix = findSpace(c.getSize());
		//TODO: this
		return true;
	}
	
	private void protectMem(int start, int size) {
		//TODO:this
	}

	
	/**finds the largest contiuous space of size size
	 * @param size - the size of the space to find
	 * @return the index of the location containing the requested space, or -1
	 *  if no such space exists*/
	private int findSpace(int size) {
		for(int i = 0; i < lockedMem.length; i++) {
			if(!getLockVal(i)) {
				int space = getSpaceAt(i);
				if(space >= size) {
					return i;
				}
			}
		}
		return -1;
	}
	
	/**Returns the contiguous free space at ix*/
	private int getSpaceAt(int ix) {
		int count = 0;
		for(int i = 0; i < lockedMem.length; i++) {
			if(!getLockVal(i + ix)) {
				count++;
			} else {
				break;
			}
		}
		return count;
	}

	/**Returns the range of values in the soup from ix to iy, circularly. iy must be
	 *  greater than ix and the difference must be less that the soup size*/
	public byte[] getRange(int ix, int iy) {
		byte[] ret = new byte[iy - ix];
		int i;
		for(i = 0; ix <= iy; ix++, i++) {
			ret[i] = this.getValue(ix);
		}
		return ret;
	}

	public int allocate(Cell c, int size) {
		int head = c.getHead();
		int space = getSpaceAt(head);
		if(space >= size) {
			c.setAlloc(space);
		} else {
			garbageCollect();
			space = getSpaceAt(head);
			if(space >= size) {
				c.setAlloc(space);
			} else {
				while(space < size) {
					killTop();
					garbageCollect();
					space = getSpaceAt(head);
				}
				c.setAlloc(space);
			}
		}
		protectMem(head, head + space);
		return head + space;
	}
	
	public void killTop() {
		Cell c = cells.removeLast();
		
	}
}

package cellProcesses;

import java.util.*;

public class SoupManager {
	private byte[] soup;
	private boolean[] lockedMem;
	private LinkedList<Cell> cells;
	private ArrayList<Code> codes;
	private String[] names;
	
	private static final int SOUP_SIZE = 10000;
	
	public SoupManager() {
		soup = new byte[SOUP_SIZE];
		lockedMem = new boolean[SOUP_SIZE];
		Arrays.fill(lockedMem, false);
		cells = new LinkedList<Cell>();
		codes = new ArrayList<Code>();
		names = new String[10000];
		Arrays.fill(names, "aaa");
	}
	
	/**returns the value at ix with circular memory*/
	public byte getValue(int ix) {
		ix = ix % SOUP_SIZE;
		return soup[ix];
	}
	
	/**sets the value at ix with val in the soup
	 * @param ix - the locate to set
	 * @param val - value to set (ix) to
	 * @return if the operation was successful*/
	public boolean setValue(int ix, byte val) {
		ix = ix % SOUP_SIZE;
		if(getLockVal(ix)) {
			return false;
		} else {
			soup[ix] = val;
			return true;
		}
	}
	
	/**returns the locked status at ix assuming circular memory*/
	public boolean getLockVal(int ix) {
		ix = ix % lockedMem.length;
		return lockedMem[ix];
	}
	
	/**sets the value at ix with val in the memory protection array*/
	private void setLockVal(int ix, boolean val) {
		ix = ix % lockedMem.length;
		lockedMem[ix] = val;
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
		int ix = allocate(c.getSize());
		cells.addFirst(c);
		for(int i = 0; i < c.getSize(); i++) {
			setValue(ix + i, c.getCode().getCode()[i]);
			setLockVal(ix + i, true);
		}
		int loc = Collections.binarySearch(codes, c.getCode());
		if(loc < 0) {
			codes.add(-(loc + 1),c.getCode());
		}
		c.activate();
		return true;
	}
	
	/**adds a cell to the soup given the given code
	 * @param range - the area of code to make the new cell with*/
	private void addCell(byte[] range) {
		boolean isSame = false;
		Code d = null;
		for(int i = 0; i < codes.size(); i++) {
			d = codes.get(i);
			if(Arrays.equals(range, d.getCode())) {
				isSame = true;
				break;
			}
		}
		if(isSame) {
			d = new Code(range, d.getName());
		} else {
			d = new Code(range, names[range.length]);
			names[range.length] = incName(range.length);
		}
		addCell(new Cell(d, 0, range.length, this));
	}
	
	/**Shuffles the names so each code gets a unique name, and returns the new name;*/
	private String incName(int length) {
		String str = names[length];
		char[] c = str.toCharArray();
		if(c[0] <= 'z') {
			c[0]++;
		} else {
			c[0] = 'a';
			c[1]++;
		}
		for(int i = 1; i < str.length() - 1; i++) {
			if(c[i] > 'z') {
				c[i] = 'a';
				c[i+1]++;
			}
		}
		if(c[c.length - 1] > 'z') {
			Arrays.fill(c, 'a');
		}
		str = String.copyValueOf(c);
		return str;
	}

	/**protects memory starting at start with a size of size*/
	private void protectMem(int start, int size) {
		for(int i = 0; i < size; i++, start++) {
			setLockVal(start, true);
		}
	}

	/**finds an area with the size of size, or creates it if no room is available
	 * @param size - amount of space to allocate
	 * @return the index of the start of the free space*/
	private int allocate(int size) {
		int ix;
		ix = findSpace(size);
		if(ix != -1) {
			return ix;
		} else {
			garbageCollect();		
		}
		ix = findSpace(size);
		while(ix == -1) {
			killTop();
			garbageCollect();
			ix = findSpace(size);
		}
		return ix;
	}
	
	/**finds the largest continuous space of size size
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
	
	/**Amount of memory a cell can allocate relative to its size (e.g. 3 means a cell can allocate
	 *  three times its size in memory*/
	private static final double MAX_MEM_ALLOC_RATIO = 3;
	
	/**Allocates memory for a cell
	 * @param c - cell to find memory for
	 * @param size - size of space to allocate
	 * @return address of the end of the allocated space, or -1 on failure*/
	public int allocate(Cell c, int size) {
		if(size > MAX_MEM_ALLOC_RATIO * c.getSize()) return -1;
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
		//TODO:make it so cells allocate in chunks and not all at once
		releaseMem(head + c.getSize(), c.getAlloc());
		protectMem(head + c.getSize(), c.getSize() + space);
		return head + space;
	}
	
	/**Kills the cell at the top of the kill queue*/
	public void killTop() {
		Cell c = cells.removeLast();
		releaseMem(c.getHead(), c.getSize() + c.getAlloc());
	}

	/**Deallocates memory
	 * @param ix - the start of the area to be freed, inclusive
	 * @param size - the size of the area to free*/
	private void releaseMem(int ix, int size) {
		for(int i = 0; i < size; i++, ix++) {
			setLockVal(ix, false);
		}
	}

	/**makes a new cell*/
	public void splitCell(Cell c) {
		this.addCell(this.getRange(c.getHead() + c.getSize(), c.getHead() + c.getSize() + c.getAlloc()));
		c.setAlloc(0);
	}

}

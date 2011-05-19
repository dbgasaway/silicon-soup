package cellProcesses;

import java.util.*;

public class SoupManager {
	private byte[] soup;
	private boolean[] lockedMem;
	private LinkedList<Cell> cells;
	private ArrayList<Code> codes;
	private String[] names;
	private long cycles;
	private ListIterator<Cell> i;
	
	private static final int SOUP_SIZE = 10000;
	
	public SoupManager() {
		soup = new byte[SOUP_SIZE];
		lockedMem = new boolean[SOUP_SIZE];
		Arrays.fill(lockedMem, false);
		cells = new LinkedList<Cell>();
		codes = new ArrayList<Code>();
		names = new String[10000];
		Arrays.fill(names, "aaa");
		cycles = 0;
		i = cells.listIterator();
	}
	
	/**returns the value at ix with circular memory*/
	public byte getValue(int ix) {
		//System.out.println(-1 % 5); // results in -1?
		while(ix < 0) ix += SOUP_SIZE;
		ix = ix % SOUP_SIZE;
		return soup[ix];
	}
	
	/**sets the value at ix with val in the soup, if c has permission to do so, or if c is null
	 * @param ix - the locate to set
	 * @param val - value to set (ix) to
	 * @param c - the calling cell, or null
	 * @return if the operation was successful*/
	public boolean setValue(int ix, byte val, Cell c) {
		ix = ix % SOUP_SIZE;
		if(c != null) {
			c.setHead(c.getHead() % SOUP_SIZE);
		}
		if(getLockVal(ix) && !c.isInAlloc(ix) && c!= null) {
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
		while(ix < 0) ix += lockedMem.length;
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
		if(cells.size() > 0) i.previous();
		i.add(c);
		if(i.nextIndex() < cells.size()) i.next();
		for(int i = 0; i < c.getSize(); i++) {
			setValue(ix + i, c.getCode().getCode()[i], null);
			setLockVal(ix + i, true);
		}
		int loc = Collections.binarySearch(codes, c.getCode());
		if(loc < 0) {
			codes.add(-(loc + 1),c.getCode());
		}
		c.activate();
		//System.out.println("activated");
		return true;
	}
	
	/**adds a cell to the soup given the given code
	 * @param range - the area of code to make the new cell with*/
	public void addCell(byte[] range) {
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
	
	private void addExistingCell(int head, int size) {
		byte[] range = getRange(head, head + size - 1);
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
		Cell c = new Cell(d, head, size, this);
		i.previous();
		i.add(c);
		i.next();
		for(int i = 0; i < c.getSize(); i++) {
			setLockVal(head + i, true);
		}
		c.activate();
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
	
	/**returns the address of a contiguous space of size size*/
	private int findAndCreateSpace(int size) {
		int ix = findSpace(size);
		while(ix == -1) {
			killTop();
			ix = findSpace(size);
		}
		return ix;
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
	 *  greater than ix and the difference must be less that the soup size. If 
	 *  iy is not greater than ix then it returns an empty array*/
	public byte[] getRange(int ix, int iy) {
		/*if(ix >= iy) {
			//System.out.println("ERROR : false precondition, ix: " + ix + ", iy: "+ iy);
		}*/
		byte[] ret = new byte[iy - ix + 1];
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
	 * @return address of the start of the allocated space, or -1 on failure*/
	public int allocate(Cell c, int size) {
		if(size > MAX_MEM_ALLOC_RATIO * c.getSize()) return -1;
		int ix = findAndCreateSpace(size);
		if(c.getMalLoc() != -1) {
			releaseMem(c.getMalLoc(), c.getAlloc());
		}
		protectMem(ix, size);
		return ix;
		/*int head = c.getHead();
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
		releaseMem(head + c.getSize(), c.getAlloc());
		protectMem(head + c.getSize(), c.getSize() + space);
		return head + c.getSize();*/
	}
	
	/**Kills the cell at the top of the kill queue*/
	public void killTop() {
		Cell c;
		int ix = i.previousIndex();
		System.out.println("Running Total Cells: " + cells.size());
		i = cells.listIterator(cells.size());
		c = i.previous();
		if(i.nextIndex() != ix) {
			i.remove();
		} else if(cells.size() > 1) {
			c = i.previous();
			i.remove();
		}
		releaseMem(c.getHead(), c.getSize() + c.getAlloc());
	}

	/**Deallocates memory
	 * @param ix - the start of the area to be freed, inclusive
	 * @param size - the size of the area to free*/
	private void releaseMem(int ix, int size) {
		System.out.println("Releasing: size: " + size + ", ix: " + ix);
		//System.out.println(Arrays.toString(Arrays.copyOfRange(lockedMem, ix, ix + size)));
		for(int i = 0; i < size; i++, ix++) {
			setLockVal(ix, false);
		}
		//System.out.println(Arrays.toString(Arrays.copyOfRange(lockedMem, ix, ix + size)));
	}

	/**makes a new cell*/
	public void splitCell(Cell c) {
		releaseMem(c.getMalLoc(), c.getAlloc());
		this.addExistingCell(c.getMalLoc(), c.getAlloc());
		c.setAlloc(0);
	}
	
	private static final int RAND_FEED = 0;
	private static final int CONST_FEED = 1;
	private static final int BASE_FEED = 30;
	
	private static int feedType = RAND_FEED;
	
	/**Cycles through the cell queue once*/
	public void act() {
		i = cells.listIterator();
		while(i.hasNext()) {
			Cell c = i.next();
			//TODO: fix concurrency issues
			//System.out.println("Cell start!");
			int cycles = 0;
			switch(feedType) {
			case RAND_FEED:
				cycles = (int)(BASE_FEED * 2 * Math.random());
				break;
			case CONST_FEED:
				cycles = BASE_FEED;
				break;
			default:
				throw new IllegalArgumentException("Invalid feed type: " + feedType);
			}
			//System.out.println("Cell mid");
			try {
				c.act(cycles);
			} catch(Exception e) {
				e.printStackTrace();
				System.out.println(Arrays.toString(Arrays.copyOfRange(soup, 0, 200)));
				System.out.println(Arrays.toString(Arrays.copyOfRange(lockedMem, 0, 200)));
				System.exit(1);
			}
			//System.out.println("Cell done!");
			this.cycles++;
		}
	}
	
	/**Returns the top 10 most populous genes in the soup*/
	public String[] getTopGenes() {
		ArrayList<Code> list = new ArrayList<Code>();
		ArrayList<Integer> amounts = new ArrayList<Integer>();
		//System.out.println(cells.size());
		for(Cell c : cells) {
			int ix = list.lastIndexOf(c.getCode());
			if(ix == -1) {
				list.add(c.getCode());
				amounts.add(1);
			} else {
				amounts.set(ix, amounts.get(ix) + 1);
			}
		}
		//TODO: check correct reporting of population
		//System.out.println("cells: " + cells);
		//System.out.println(amounts.size() + " " + list.size());
		int[] tops = new int[10];
		Arrays.fill(tops, 0);
		Code[] topCodes = new Code[10];
		for(int n = 0; n < amounts.size(); n++) {
			int i = amounts.get(n);
			for(int k = 0; k < tops.length ; k++) {
				if(i > tops[k]) {
					for(int j = tops.length - 1; j > k; j--) {
						tops[j] = tops[j - 1];
						topCodes[j] = topCodes[j - 1];
					}
					tops[k] = i;
					topCodes[k] = list.get(n);
					break;
				}
			}
			amounts.remove(n);
			list.remove(n);
			n--;
		}
		String[] ret = new String[10];
		//System.out.println(Arrays.toString(topCodes));
		for(int i = 0; i < ret.length; i++) {
			Code item = topCodes[i];
			if(item != null) {
				ret[i] = item.getFullName() + " " + tops[i];
			} else {
				ret[i] = "null";
			}
		}
		return ret;
	}
	
	public long getCycles() {
		return cycles;
	}
	
	public int getTotalCells() {
		return cells.size();
	}
}

package cellProcesses;

import java.util.*;
//import comparators.*;

public class SoupManager {
	private byte[] soup;
	private boolean[] lockedMem;
	private LinkedList<Cell> cells;
	private LinkedList<Cell> deathList;
	private ArrayList<Code> codes;
	private String[] names;
	private long cycles;
	private ListIterator<Cell> i;
	
	private static final int SOUP_SIZE = 50000;
	
	public SoupManager() {
		soup = new byte[SOUP_SIZE];
		lockedMem = new boolean[SOUP_SIZE];
		Arrays.fill(lockedMem, false);
		cells = new LinkedList<Cell>();
		deathList = new LinkedList<Cell>();
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
		while(ix < 0) ix += SOUP_SIZE;
		ix = ix % SOUP_SIZE;
		if(c != null) {
			while(c.getHead() < 0) c.setHead(c.getHead() + SOUP_SIZE);
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
		while(ix < 0) ix += SOUP_SIZE;
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
		while(ix < 0) ix += SOUP_SIZE;
		ix = ix % SOUP_SIZE;
		while(iy < 0) iy += SOUP_SIZE;
		iy = iy % SOUP_SIZE;
		return Math.abs(ix - iy);
	}
	
	public byte[] getSoup() {
		return soup.clone();
	}
	
	public int getSoupSize() {
		return SOUP_SIZE;
	}
	
	/**Adds a cell to the soup, and returns if it was successful*/
	public boolean addCell(Cell c) {
		int ix = allocate(c.getSize());
		if(cells.size() > 0) i.previous();
		i.add(c);
		deathList.addFirst(c);
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
	
	/*mutation rates are per gene, 1/x*/
	private double flipMutationRate = 300;
	private double addMutationRate = 300;
	private double subMutationRate = 300;
	
	/**Adds a cell to the cell list that already has its code in the soup,
	 * and mutates the new cell*/
	private void addExistingCell(Cell parent, int head, int size) {
		//TODO: descent with modification
		ArrayList<Byte> data = new ArrayList<Byte>(size);
		byte[] range = getRange(head, head + size - 1);
		for(byte by : range) {
			data.add(by);
		}
		//long time = System.nanoTime();
		for(int i = 0; i < data.size(); i++) {
			if((int)(Math.random() * flipMutationRate) == 0) {
				data.set(i, Code.getRandomCode());
			}
			if((int)(Math.random() * addMutationRate) == 0) {
				data.add(i, Code.getRandomCode());
				i++;
			}
			if((int)(Math.random() * subMutationRate) == 0) {
				data.remove(i);
				i--;
			}
		}
		//System.out.println("divtime: " + (System.nanoTime() - time));
		range = new byte[data.size()];
		for(int i = 0; i < data.size(); i++) {
			range[i] = data.get(i);
		}
		//TODO:make so ranges are calculated properly
		int k;
		for(k = 0; k < range.length; k++) {
			if(!setValue(head + k, range[k], parent)) break;
		}
		int length = k;
		//System.out.println("rangelength: " + range.length);
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
		int loc = Collections.binarySearch(codes, d);
		if(loc < 0) {
			codes.add(-(loc + 1),d);
		}
		Cell c = new Cell(d, head, length, this);
		//System.out.println(cells);
		i.previous();
		i.add(c);
		i.next();
		deathList.addFirst(c);
		//System.out.println(cells);
		for(int i = 0; i < length; i++) {
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
		}
		ix = findSpace(size);
		while(ix == -1) {
			killTop();
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
		/*if(ix > iy) {
			System.out.println("ERROR : false precondition, ix: " + ix + ", iy: "+ iy);
		}*/
		byte[] ret = new byte[iy - ix + 1];
		int i;
		for(i = 0; ix <= iy; ix++, i++) {
			ret[i] = this.getValue(ix);
		}
		return ret;
	}
	
	/**Gets a range of values in the allocation table, inclusive, 
	 * fails if ix is more than iy*/
	public boolean[] getLockRange(int ix, int iy) {
		boolean[] ret = new boolean[iy - ix + 1];
		int i;
		for(i = 0; ix <= iy; ix++, i++) {
			ret[i] = this.getLockVal(ix);
		}
		return ret;
	}
	
	/**Amount of memory a cell can allocate relative to its size (e.g. 3 means a cell can allocate
	 *  three times its size in memory*/
	private static final double MAX_MEM_ALLOC_RATIO = 2;
	
	/**Allocates memory for a cell
	 * @param c - cell to find memory for
	 * @param size - size of space to allocate
	 * @return address of the start of the allocated space, or -1 on failure*/
	public int allocate(Cell c, int size) {
		if(size > MAX_MEM_ALLOC_RATIO * c.getSize() || size > 9999) return -1;
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
		//System.out.println("Killer: " + i.previous());
		//i.next();
		//System.out.println(cells);
		//System.out.println(deathList);
		Cell c;
		//int ix = i.previousIndex();
		//System.out.println("KILL: Running Total Cells: " + cells.size());
		/*i = cells.listIterator(cells.size());*/
		c = i.previous();
		Cell c2 = deathList.getLast();
		if(c2 != c) {
			cells.remove(c2);
			deathList.removeLast();
		} else {
			ListIterator<Cell> k = null;
			//try {
			k = deathList.listIterator(deathList.size() - 1);
			c2 = k.previous();
			/*} catch(NoSuchElementException e) {
				System.out.println(cells);
				System.out.println(deathList);
				e.printStackTrace();
			}*/
			cells.remove(c2);
			deathList.remove(c2);
		}
		/*if(i.nextIndex() != ix) {
			i.remove();
		} else if(cells.size() > 1) {
			c = i.previous();
			i.remove();
		}*/
		//System.out.println("Killed top at: " + c.getHead() + ", with size: " + c.getSize());
		releaseMem(c2.getHead(), c2.getSize());
		if(c2.getAlloc() != 0) releaseMem(c2.getMalLoc(), c2.getAlloc());
		i = cells.listIterator(cells.lastIndexOf(c));
		i.next();
		//System.out.println(cells);
		//System.out.println(deathList);
	}

	/**Deallocates memory
	 * @param ix - the start of the area to be freed, inclusive
	 * @param size - the size of the area to free*/
	private void releaseMem(int ix, int size) {
		//System.out.println("Releasing: size: " + size + ", ix: " + ix);
		//if(size != 80 && size != 0) throw new IllegalArgumentException("Invalid size: " + size);
		//System.out.println(Arrays.toString(Arrays.copyOfRange(lockedMem, ix, ix + size)));
		for(int i = 0; i < size; i++, ix++) {
			setLockVal(ix, false);
		}
		//System.out.println(Arrays.toString(Arrays.copyOfRange(lockedMem, ix, ix + size)));
	}

	/**Smallest size of a cell*/
	private int minCellSize = 12;
	
	public int getMinCellSize() {
		return minCellSize;
	}
	
	/**makes a new cell
	 * @param c - the cell that is replicating*/
	public void splitCell(Cell c) {
		releaseMem(c.getMalLoc(), c.getAlloc());
		this.addExistingCell(c, c.getMalLoc(), c.getAlloc());
		c.setAlloc(0);
		shiftDownCellDeath(c);
	}
	
	/**Moves the cell  down the death list 1 spot, if possible*/
	public void shiftDownCellDeath(Cell c) {
		int ix = deathList.lastIndexOf(c);
		if(ix != 0) {
			ListIterator<Cell> k = deathList.listIterator(ix);
			k.next();
			k.remove();
			k.previous();
			k.add(c);
		}
	}
	
	private static final int RAND_FEED = 0;
	private static final int CONST_FEED = 1;
	private static final int BASE_FEED = 100;
	
	private static int feedType = CONST_FEED;
	
	/**Cycles through the cell queue once*/
	public void act() {
		long time = System.nanoTime();
		i = cells.listIterator();
		while(i.hasNext()) {
			Cell c = i.next();
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
				//System.out.println(Arrays.toString(Arrays.copyOfRange(soup, 0, 80)));
				//System.out.println(Arrays.toString(Arrays.copyOfRange(lockedMem, 0, 80)));
				//System.out.println(Arrays.toString(Arrays.copyOfRange(soup, 80, 160)));
				//System.out.println(Arrays.toString(Arrays.copyOfRange(lockedMem, 80, 160)));
				//System.out.println(Arrays.toString(Arrays.copyOfRange(soup, 160, 240)));
				//System.out.println(Arrays.toString(Arrays.copyOfRange(lockedMem, 160, 240)));
				//int[][] comp = SequenceAlignment.findEditDistance(Arrays.toString(Arrays.copyOfRange(soup, 0, 80)), Arrays.toString(Arrays.copyOfRange(soup, 160, 240)));
				//System.out.println(SequenceAlignment.findOptimalString(comp, Arrays.toString(Arrays.copyOfRange(soup, 0, 80)), Arrays.toString(Arrays.copyOfRange(soup, 160, 240))));
				System.out.println(cells);
				System.out.println(codes);
				System.out.println("Some codes:");
				for(byte[] b : get10Codes()) {
					if(b != null) System.out.println(Arrays.toString(b));
				}
				try {
					this.wait();
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				//System.exit(1);
			}
			//System.out.println("Cell done!");
			this.cycles++;
		}
		System.out.println("Cycle Time: " + (System.nanoTime() - time));
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
		//System.out.println("Total codes: " + codes.size());
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
	
	public int getTotalCodes() {
		return codes.size();
	}
	
	public byte[][] get10Codes() {
		byte[][] ret = new byte[10][];
		for(int i = 0; i < ret.length && i < codes.size(); i++) {
			ret[i] = codes.get(i).getCode();
		}
		return ret;
	}
	
	public int getAllocatedSpace() {
		int count = 0;
		for(int i = 0; i < lockedMem.length; i++) {
			if(lockedMem[i]) count++;
		}
		return count;
	}
	
	/** Finds the code with the given string id, works like binarySearch
	 * @param start - inclusive
	 * @param end - exclusive*/
	private int findCode(String code) {
		//TODO: check to see if working
		/*if(start > end) return null;
		int mid = (start + end) / 2;
		Code midval = codes.get(mid);
		int result = midval.getFullName().compareTo(code);
		if(result == 0) {
			return midval;
		} else if(result > 0) {
			return findCode(code, start, mid);
		} else if(start != end) {
			return findCode(code, mid + 1, end);
		} else {
			return null;
		}*/
		if(codes.size() == 0) return -1;
		int start = 0;
		int end = codes.size();
		int mid = (start + end) / 2;
		int t;
		///System.out.println(codes);
		while(start < end) {
			mid = (start + end) / 2;
			//System.out.println(mid + ": " + codes.get(mid));
			t = code.compareTo(codes.get(mid).getFullName());
			//System.out.println("t: " + t);
			if(t == 0) {
				return mid;
			} else if(t < 0) {
				end = mid;
			} else {
				start = mid + 1;
			}
		}
		return -mid - 1;
	}
	
	public Code getCode(String code) {
		code = code.trim();
		//System.out.println(code);
		int ix = findCode(code);
		//System.out.println("ix: " + ix);
		if(ix >= 0) {
			return codes.get(ix);
		} else {
			return null;
		}
	}
}

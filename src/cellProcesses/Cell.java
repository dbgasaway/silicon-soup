package cellProcesses;

import java.util.Arrays;

public class Cell {
        private Code c;
        private int head;
        private int size;
        private CPU cpu;
        /**start of area of mem allocation, or -1 if none*/
        private int malLoc;
        private int alloc;
        private SoupManager soup;
        
        public Cell(Code c, int head, int size, SoupManager soup) {
                this.c = c;
                this.head = head;
                this.size = size;
                this.soup = soup;
                cpu = null;
                alloc = 0;
                malLoc = -1;
        }
        
        public void activate() {
        	cpu = new CPU(head, soup, this);
        }
        
        public void act(int cycles) {
                cpu.act(cycles);
        }
        
        public int getSize() {
        	return this.size;
        }
        
        public int getMalLoc() {
        	return malLoc;
        }
        
        public void setAlloc(int a) {
        	this.alloc += a;
        }
        
        public int getAlloc() {
        	return alloc;
        }
        
        public boolean isInAlloc(int ix) {
        	return((ix >= head && ix < head + size) ||
        			(ix >= malLoc && ix < malLoc + alloc));
        }
        
        public int getHead() {
        	return head;
        }
        
        public void setHead(int ix) {
        	head = ix;
        }
        
        public Code getCode() {
        	return c;
        }
        
        /**Allocates memory and returns the address of its start, or -1 if not successful*/
        public int allocate(int size) {
        	int ix = soup.allocate(this, size + alloc);
        	malLoc = ix;
        	if(ix == -1) {
        		return -1;
        	} else {
        		//TODO: (or not, seems to not be correct, maybe just drop allocate space) make it so <code>alloc += size</code> works right
        		alloc = size;
        		return ix;
        	}
        }
}

class CPU {
        private int ip;
        private int sp;
        private int a;
        private int b;
        private int c;
        private int d;
        private int[] stack;
        private int cycles;
        private Cell cell;
        private SoupManager soup;

        public CPU(int ip, SoupManager soup, Cell c) {
                this.ip = ip;
                this.sp = 0;
                this.a = 0;
                this.b = 0;
                this.c = 0;
                this.d = 0;
                this.stack = new int[5];
                Arrays.fill(stack, 0);
                this.cycles = 0;
                this.soup = soup;
                cell = c;
        }
        
        public void act(int cycles) {
                this.cycles += cycles;
                while(this.cycles > 0) {
                	byte b = soup.getValue(ip);
                	this.execute(b);
                	this.cycles--;
                	System.out.println("Cycle: " + this.cycles + ", IP: " + ip + ", b: " + b);
                }
        }
        
        /**pushes val on the stack, returns false on failure (if stack is full) and true otherwise*/
        private boolean push(int val) {
        	/*if(sp >= stack.length - 1) return false;
        	sp++;
        	stack[sp] = val;*/
        	stack[sp] = val;
        	sp = (sp + 1) % stack.length;
        	return true;
        }
        
        private int pop() {
        	/*if(sp <= 0) {
        		return -1;
        	} else {
        		sp--;
        		return stack[sp + 1];
        	}*/
        	int x = stack[sp];
        	sp--;
        	if(sp < 0) sp += stack.length;
        	return x;
        }

        private void execute(byte by) {
        	//System.out.println(by);
        	byte[] template;
        	int ix;
        	int p;
        	switch(by) {
        	case Code.ZERO:
        		c = 0;
        		ip++;
        		break;
        	case Code.SUBACB:
        		this.c = c - a;
        		ip++;
        		break;
        	case Code.SUBACA:
        		this.a = c - a;
        		ip++;
        		break;
        	case Code.JUMP:
        		template = this.getTemplate();
        		System.out.println(Arrays.toString(template));
        		ix = search(template, OUT);
        		if(ix != ip) {
        			ip = ix + template.length;
        		} else {
        			ip += template.length;
        		}
        	case Code.JUMPF:
        		template = this.getTemplate();
        		ix = search(template, FORWARD);
        		if(ix != ip) {
        			ip = ix + template.length;
        		} else {
        			ip += template.length;
        		}
        	case Code.JUMPB:
        		template = this.getTemplate();
        		ix = search(template, BACK);
        		if(ix != ip) {
        			ip = ix + template.length;
        		} else {
        			ip += template.length;
        		}
        		break;
        	case Code.SEARCH:
        		template = this.getTemplate();
        		ix = search(template, OUT);
        		if(ix != ip) {
        			ip += template.length;
        			c = template.length;
        		} else {
        			ip++;
        		}
        	case Code.SEARCHB:
        		template = this.getTemplate();
        		ix = search(template, BACK);
        		a = ix;
        		if(ix != ip) {
        			ip += template.length;
        			c = template.length;
        		} else {
        			ip++;
        		}
        	case Code.SEARCHF:
        		template = this.getTemplate();
        		ix = search(template, FORWARD);
        		a = ix;
        		if(ix != ip) {
        			ip += template.length;
        			c = template.length;
        		} else {
        			ip++;
        		}
        		break;
        	case Code.DIVIDE:
        		if(cell.getAlloc() > 0){
        			soup.splitCell(cell);
        		}
        		ip++;
        		//TODO:move down death queue one slot
        		break;
        	case Code.MOVEIXBA:
        		soup.setValue(a, soup.getValue(b), cell);
        		ip++;
        		break;
        	case Code.LOADAB:
        		this.b = this.a;
        		ip++;
        		break;
        	case Code.LOADCD:
        		this.d = this.c;
        		ip++;
        		break;
        	case Code.ALLOC:
        		if(c > 0 || cell.getAlloc() != c) {
        			//TODO:implement memory properly: not contiguous always
        			a = cell.allocate(c);
        		}
        		ip++;
        		//TODO:move down death queue one slot
        		break;
        	case Code.PUSHA:
        		push(a);
        		ip++;
        		break;
        	case Code.PUSHB:
        		push(by);
        		ip++;
        		break;
        	case Code.PUSHC:
        		push(c);
        		ip++;
        		break;
        	case Code.PUSHD:
        		push(d);
        		ip++;
        		break;
        	case Code.POPA:
        		p = pop();
        		if(p < 0) p = 0;
        		a = p;
        		ip++;
        		break;
        	case Code.POPB:
        		p = pop();
        		if(p < 0) p = 0;
        		this.b = p;
        		ip++;
        		break;
        	case Code.POPC:
        		p = pop();
        		if(p < 0) p = 0;
        		c = p;
        		ip++;
        		break;
        	case Code.POPD:
        		p = pop();
        		if(p < 0) p = 0;
        		d = p;
        		ip++;
        		break;
        	case Code.INCA:
        		a++;
        		ip++;
        		break;
        	case Code.INCB:
        		b++;
        		ip++;
        		break;
        	case Code.INCC:
        		c++;
        		ip++;
        		break;
        	case Code.DECC:
        		c--;
        		ip++;
        		break;
        	case Code.NOT0C:
        		c = c ^ a;
        		ip++;
        		break;
        	case Code.LSHIFTC:
        		c = c << 1;
        		ip++;
        		break;
        	case Code.IFCZ:
        		if(c == 0) {
        			ip++;
        		} else {
        			ip += 2;
        		}
        		break;
        	case Code.CALL:
        		template = this.getTemplate();
        		c = template.length;
        		ix = search(template, OUT);
        		push(ip + template.length + 1);
        		ip += template.length + ix;
        		break;
        	case Code.RET:
        		ix = pop();
        		if(ix < 0) {
        			ix = 0;
        		}
        		ip = ix;
        		break;
        	default:
        		ip++;
        		break;
        	}
        }

        /**The valid template values. One searches for the inverse of the template after
         *  the ip, e.g. NOP1, NOP0 -> NOP0, NOP1*/
		private static final byte[] TEMPLATE_VALUES = {Code.NOP0, Code.NOP1};
        
        /**returns the template after the current instruction*/
		private byte[] getTemplate() {
			int i;
			for(i = 1; i < soup.getSoupSize(); i++) {
				boolean containsTemplate = false;
				for(byte b : TEMPLATE_VALUES) {
					if(soup.getValue(ip + i) == b) {
						containsTemplate = true;
						break;
					}
				}
				if(!containsTemplate) {
					break;
				}
			}
			i--;
			byte[] base = soup.getRange(ip + 1, ip + i);
			for(int k = 0; k < base.length; k++) {
				switch(base[k]) {
				case Code.NOP0:
					base[k] = Code.NOP1;
					break;
				case Code.NOP1:
					base[k] = Code.NOP0;
					break;
				default:
					throw new IllegalArgumentException("Invalid template type");
				}
			}
			return base;
		}
		
		private static final int OUT = 0;
		private static final int FORWARD = 1;
		private static final int BACK = -1;
		private static final int MAX_TEMPLATE_SIZE = 10;
		
		/**returns the index of the nearest template
		 * @param template - the template to search for
		 * @param i - the search method: -1 is back, 0 is out, 1 is forward
		 * @return the index of the nearest template, or ip if none is found or the template is too big*/
        private int search(byte[] template, int i) {
        	if(template.length > MAX_TEMPLATE_SIZE) return ip;
        	if(i == OUT) {
        		return searchOut(template);
        	} else if(i == BACK) {
        		return searchBack(template);
        	} else if(i == FORWARD) {
        		return searchForward(template);
        	} else {
        		throw new IllegalArgumentException("Bad search type");
        	}
		}

		private int searchOut(byte[] template) {
        	if(template.length == 0) return ip;
        	int a = searchBack(template);
        	int b = searchForward(template);
        	if(soup.getDist(a, ip) < soup.getDist(b, ip)) {
        		return a;
        	} else {
        		return b;
        	}
		}

		/**Returns the index of the nearest template after the ip*/
		private int searchBack(byte[] template) {
        	if(template.length == 0) return ip;
			for(int i = -1; i > -soup.getSoupSize(); i--) {
				if(Arrays.equals(template, soup.getRange(ip + i - template.length, ip + i))) {
					return ip + i - template.length;
				}
			}
			return ip;
		}
		
		/**Returns the index of the nearest template after the ip*/
		private int searchForward(byte[] template) {
        	if(template.length == 0) return ip;
			for(int i = 1; i < soup.getSoupSize(); i++) {
				if(Arrays.equals(template, soup.getRange(ip + i, ip + i + template.length))) {
					return ip + i - template.length;
				}
			}
			return ip;
		}
}

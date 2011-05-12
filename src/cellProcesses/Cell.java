package cellProcesses;

import java.util.Arrays;

public class Cell {
        private Code c;
        private int head;
        private int size;
        private CPU cpu;
        private int alloc;
        private SoupManager soup;
        
        public Cell(Code c, int head, int size, SoupManager soup) {
                this.c = c;
                this.head = head;
                this.size = size;
                this.soup = soup;
                cpu = null;
                alloc = 0;
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
        
        public void setAlloc(int a) {
        	this.alloc += a;
        }
        
        public int getAlloc() {
        	return alloc;
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
        
        public boolean allocate(int size) {
        	int ix = soup.allocate(this, size + alloc);
        	if(ix == -1) {
        		return false;
        	} else {
        		//TODO: make it so <code>alloc += size</code> works right
        		alloc = size;
        		return true;
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
        
        /**NOP and template value*/
        private static final byte NOP0 = 0;
        /**NOP and template value*/
        private static final byte NOP1 = 1;
        private static final byte ZERO = 2;
        private static final byte SUBACB = 3;
        private static final byte SUBACA = 23;
        private static final byte JUMPF = 4;
        private static final byte JUMP = 5;
        private static final byte JUMPB = 6;
        private static final byte SEARCHF = 7;
        private static final byte SEARCHB = 8;
        private static final byte SEARCH = 9;
        private static final byte DIVIDE = 10;
        private static final byte MOVEIXBA = 11;
        private static final byte LOADAB = 12;
        private static final byte LOADCD = 22;
        private static final byte ALLOC = 13;
        private static final byte PUSHA = 14;
        private static final byte PUSHB = 15;
        private static final byte PUSHC = 16;
        private static final byte PUSHD = 20;
        private static final byte POPA = 17;
        private static final byte POPB = 18;
        private static final byte POPC = 19;
        private static final byte POPD = 21;
        private static final byte INCA = 24;
        private static final byte INCB = 25;
        private static final byte INCC = 26;
        private static final byte DECC = 27;
        private static final byte NOT0C = 28;
        private static final byte LSHIFTC = 29;
        private static final byte IFCZ = 30;
        private static final byte CALL = 31;
        private static final byte RET = 32;
        
        /**contains all valid instructions*/
        private static final byte[] VALID_INSTRUCTIONS = {NOP0, NOP1, ZERO, SUBACB, 
        	JUMPF, JUMP, JUMPB, SEARCHF, SEARCHB, SEARCH, DIVIDE, MOVEIXBA, LOADAB, 
        	ALLOC, PUSHA, PUSHB, PUSHC, POPA, POPB, POPC, PUSHD, POPD, LOADCD, SUBACA,
        	INCA, INCB, INCC, DECC, NOT0C, LSHIFTC, IFCZ, CALL, RET
        };

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
        	sp = (sp - 1) % stack.length;
        	return x;
        }

        private void execute(byte by) {
        	 byte[] template;
        	 int ix;
        	 int p;
        	 switch(by) {
        	 case ZERO:
        		 c = 0;
        		 ip++;
        		 break;
        	 case SUBACB:
        		 this.c = c - a;
        		 ip++;
        		 break;
        	 case SUBACA:
        		 this.a = c - a;
        		 ip++;
        		 break;
        	 case JUMP:
        		 template = this.getTemplate();
        		 ix = search(template, OUT);
        		 if(ix != ip) {
        			 ip = ix + template.length;
        		 } else {
        			 ip += template.length;
        		 }
        	 case JUMPF:
        		 template = this.getTemplate();
        		 ix = search(template, FORWARD);
        		 if(ix != ip) {
        			 ip = ix + template.length;
        		 } else {
        			 ip += template.length;
        		 }
        	 case JUMPB:
        		 template = this.getTemplate();
        		 ix = search(template, BACK);
        		 if(ix != ip) {
        			 ip = ix + template.length;
        		 } else {
        			 ip += template.length;
        		 }
        		 break;
        	 case SEARCH:
        		 template = this.getTemplate();
        		 ix = search(template, OUT);
        		 if(ix != ip) {
        			 ip += template.length;
            		 c = template.length;
        		 } else {
        			 ip++;
        		 }
        	 case SEARCHB:
        		 template = this.getTemplate();
        		 ix = search(template, BACK);
        		 a = ix;
        		 if(ix != ip) {
        			 ip += template.length;
            		 c = template.length;
        		 } else {
        			 ip++;
        		 }
        	 case SEARCHF:
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
        	 case DIVIDE:
        		 //TODO:implement division: daughter is put last in time queue
        		 soup.splitCell(cell);
        		 ip++;
        		//TODO:move down death queue one slot
        		 break;
        	 case MOVEIXBA:
        		 soup.setValue(a, soup.getValue(b));
        		 ip++;
        		 break;
        	 case LOADAB:
        		 this.b = this.a;
        		 break;
        	 case LOADCD:
        		 this.d = this.c;
        		 break;
        	 case ALLOC:
        		 if(c > 0 || cell.getAlloc() == c) {
        			 //TODO:implement memory allocation, should put new address in a and the new address may not always be continuous
        			 cell.allocate(c);
        			 a = cell.getHead() + cell.getSize();
        		 }
        		 ip++;
        		 //TODO:move down death queue one slot
        		 break;
        	 case PUSHA:
        		 push(a);
        		 ip++;
        		 break;
        	 case PUSHB:
        		 push(by);
        		 ip++;
        		 break;
        	 case PUSHC:
        		 push(c);
        		 ip++;
        		 break;
        	 case PUSHD:
        		 push(d);
        		 ip++;
        		 break;
        	 case POPA:
        		 p = pop();
        		 if(p < 0) p = 0;
        		 a = p;
        		 ip++;
        		 break;
        	 case POPB:
        		 p = pop();
        		 if(p < 0) p = 0;
        		 this.b = p;
        		 ip++;
        		 break;
        	 case POPC:
        		 p = pop();
        		 if(p < 0) p = 0;
        		 c = p;
        		 ip++;
        		 break;
        	 case POPD:
        		 p = pop();
        		 if(p < 0) p = 0;
        		 d = p;
        		 ip++;
        		 break;
        	 case INCA:
        		 a++;
        		 ip++;
        		 break;
        	 case INCB:
        		 b++;
        		 ip++;
        		 break;
        	 case INCC:
        		 c++;
        		 ip++;
        		 break;
        	 case DECC:
        		 c--;
        		 ip++;
        		 break;
        	 case NOT0C:
        		 c = c ^ a;
        		 ip++;
        		 break;
        	 case LSHIFTC:
        		 c = c << 1;
        		 ip++;
        		 break;
        	 case IFCZ:
        		 if(c == 0) {
        			 ip++;
        		 } else {
        			 ip += 2;
        		 }
        		 break;
        	 case CALL:
        		 template = this.getTemplate();
        		 c = template.length;
        		 ix = search(template, OUT);
        		 push(ip + template.length + 1);
        		 ip += template.length + ix;
        		 break;
        	 case RET:
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
		private static final byte[] TEMPLATE_VALUES = {NOP0, NOP1};
        
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
			byte[] base = soup.getRange(ip + 1, ip + i);
			for(int k = 0; k < base.length; k++) {
				switch(base[k]) {
				case NOP0:
					base[k] = NOP1;
					break;
				case NOP1:
					base[k] = NOP0;
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

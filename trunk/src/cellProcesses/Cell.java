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
                cpu = new CPU(head, soup);
                alloc = 0;
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
        
        public int getHead() {
        	return head;
        }
        
        public void setHead(int ix) {
        	head = ix;
        }
        
        public boolean allocate(int size) {
        	int ix = soup.allocate(this, size + alloc);
        	if(ix == -1) {
        		return false;
        	} else {
        		alloc += size;
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
        private SoupManager soup;
        
        /**NOP and template value*/
        private static final byte NOP0 = 0;
        /**NOP and template value*/
        private static final byte NOP1 = 1;
        private static final byte ZERO = 2;
        private static final byte ADDACB = 3;
        private static final byte JUMPF = 4;
        private static final byte JUMP = 5;
        private static final byte JUMPB = 6;
        private static final byte SEARCHF = 7;
        private static final byte SEARCHB = 8;
        private static final byte SEARCH = 9;
        private static final byte DIVIDE = 10;
        private static final byte MOVEAB = 11;
        private static final byte LOADAB = 12;
        private static final byte ALLOC = 13;
        private static final byte PUSHA = 14;
        private static final byte PUSHB = 15;
        private static final byte PUSHC = 16;
        private static final byte POPA = 17;
        private static final byte POPB = 18;
        private static final byte POPC = 19;
        
        /**contains all valid instructions*/
        private static final byte[] VALID_INSTRUCTIONS = {NOP0, NOP1, ZERO, ADDACB, 
        	JUMPF, JUMP, JUMPB, SEARCHF, SEARCHB, SEARCH, DIVIDE, MOVEAB, LOADAB, 
        	ALLOC, PUSHA, PUSHB, PUSHC, POPA, POPB, POPC
        };

        public CPU(int ip, SoupManager soup) {
                this.ip = ip;
                this.sp = 0;
                this.a = 0;
                this.b = 0;
                this.c = 0;
                this.d = 0;
                this.stack = new int[5];
                for(int i : stack) i = 0;
                this.cycles = 0;
                this.soup = soup;
        }
        
        public void act(int cycles) {
                this.cycles += cycles;
                while(this.cycles > 0) {
                        byte b = soup.getValue(ip);
                        this.execute(b);
                }
        }

        private void execute(byte b) {
        	 byte[] template;
        	 int ix;
                switch(b) {
                case ZERO:
                        c = 0;
                        break;
                case ADDACB:
                        this.b = a + c;
                        break;
                case JUMP:
                	    template = this.getTemplate();
                        ix = search(template, OUT);
                        ip = ix + template.length;
                        break;
                case JUMPF:
                	template = this.getTemplate();
                    ix = search(template, FORWARD);
                    ip = ix + template.length;
                    break;
                case JUMPB:
                	template = this.getTemplate();
                    ix = search(template, BACK);
                    ip = ix + template.length;
                    break;
                case SEARCH:
                	template = this.getTemplate();
            	    c = template.length;
                    ix = search(template, OUT);
                    a = ix;
                    break;
                case SEARCHB:
                	template = this.getTemplate();
            	    c = template.length;
                    ix = search(template, BACK);
                    a = ix;
                    break;
                case SEARCHF:
                	template = this.getTemplate();
            	    c = template.length;
                    ix = search(template, FORWARD);
                    a = ix;
                    break;
                case DIVIDE:
                	//TODO:implement division
                default:
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
		
		/**returns the index of the nearest template
		 * @param template - the template to search for
		 * @param i - the search method: -1 is back, 0 is out, 1 is forward
		 * @return the index of the nearest template, or ip if none is found*/
        private int search(byte[] template, int i) {
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

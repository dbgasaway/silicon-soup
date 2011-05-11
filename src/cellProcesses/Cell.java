package cellProcesses;

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
        
        private static final byte NOP0 = 0;
        private static final byte NOP1 = 1;
        private static final byte ZERO = 2;
        private static final byte ADDACB = 3;
        private static final byte JUMPF = 4;
        private static final byte JUMP = 5;
        private static final byte JUMPB = 6;
        private static final byte SEARCHF = 7;
        private static final byte SEARCHB = 8;
        private static final byte SEARCH = 9;
        private static final byte DIVIDE = 0;

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
                switch(b) {
                case ZERO:
                        c = 0;
                        break;
                case ADDACB:
                        this.b = a + c;
                        break;
                case JUMP:
                        
                default:
                        break;
                }
        }
}

package cellProcesses;

public class Code implements Comparable<Code> {
	private String name;
	private byte[] instr;
	
	/**NOP and template value*/
    public static final byte NOP0 = 0;
    /**NOP and template value*/
    public static final byte NOP1 = 1;
    public static final byte ZERO = 2;
    public static final byte SUBACB = 3;
    public static final byte SUBACA = 23;
    public static final byte JUMPF = 4;
    public static final byte JUMP = 5;
    public static final byte JUMPB = 6;
    public static final byte SEARCHF = 7;
    public static final byte SEARCHB = 8;
    public static final byte SEARCH = 9;
    public static final byte DIVIDE = 10;
    public static final byte MOVEIXBA = 11;
    public static final byte LOADAB = 12;
    public static final byte LOADCD = 22;
    public static final byte ALLOC = 13;
    public static final byte PUSHA = 14;
    public static final byte PUSHB = 15;
    public static final byte PUSHC = 16;
    public static final byte PUSHD = 20;
    public static final byte POPA = 17;
    public static final byte POPB = 18;
    public static final byte POPC = 19;
    public static final byte POPD = 21;
    public static final byte INCA = 24;
    public static final byte INCB = 25;
    public static final byte INCC = 26;
    public static final byte DECC = 27;
    public static final byte NOT0C = 28;
    public static final byte LSHIFTC = 29;
    public static final byte IFCZ = 30;
    public static final byte CALL = 31;
    public static final byte RET = 32;
    
    /**contains all valid instructions*/
    public static final byte[] VALID_INSTRUCTIONS = {NOP0, NOP1, ZERO, SUBACB, 
    	JUMPF, JUMP, JUMPB, SEARCHF, SEARCHB, SEARCH, DIVIDE, MOVEIXBA, LOADAB, 
    	ALLOC, PUSHA, PUSHB, PUSHC, POPA, POPB, POPC, PUSHD, POPD, LOADCD, SUBACA,
    	INCA, INCB, INCC, DECC, NOT0C, LSHIFTC, IFCZ, CALL, RET
    };
	
	public Code(byte[] instr, String name) {
		this.instr = instr;
		this.name = name;
	}
	
	public byte[] getCode() {
		return instr.clone();
	}
	
	public String getName() {
		return name;
	}

	public String getFullName() {
		return instr.length + name;
	}
	
	@Override
	public int compareTo(Code c) {
		if(this.instr.length != c.instr.length) {
			return this.instr.length - c.instr.length;
		} else {
			return this.name.compareTo(c.name);
		}
	}
	
	@Override
	public String toString() {
		return this.getFullName();
	}
}

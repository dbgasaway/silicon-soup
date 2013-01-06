package cellProcesses;

import java.util.Arrays;
//import java.util.HashMap;

public class Code implements Comparable<Code> {
	private String name;
	private byte[] instr;
	private String parent;
	
	/**NOP and template value*/
    public static final byte NOP0 = 0;
    /**NOP and template value*/
    public static final byte NOP1 = 1;
    public static final byte ZERO = 2;
    public static final byte SUBBAC = 3;
    public static final byte SUBCAA = 23;
    //public static final byte JUMPF = 4;//unused
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
    public static final byte RET = 4;
    
    public static final int BITS = 5;
    
    //TODO:use this: public HashMap<Byte, String> s;
    
    /**contains all valid instructions*/
    public static final byte[] VALID_INSTRUCTIONS = {NOP0, NOP1, ZERO, SUBBAC, 
    	/*JUMPF,*/ JUMP, JUMPB, SEARCHF, SEARCHB, SEARCH, DIVIDE, MOVEIXBA, LOADAB, 
    	ALLOC, PUSHA, PUSHB, PUSHC, POPA, POPB, POPC, PUSHD, POPD, LOADCD, SUBCAA,
    	INCA, INCB, INCC, DECC, NOT0C, LSHIFTC, IFCZ, CALL, RET
    };
	
	public Code(byte[] instr, String name, String parent) {
		this.instr = instr;
		this.name = name;
		this.parent = parent;
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
	
	public String getParent() {
		return parent;
	}
	
	public static String getCodeName(byte b) {
		switch(b) {
		case NOP0:
			return "NOP0";
		case NOP1:
			return "NOP1";
		case ZERO:
			return "ZERO";
		case SUBBAC:
			return "SUBBAC";
		/*case JUMPF:
			return "JUMPF";*/
		case JUMP:
			return "JUMP";
		case JUMPB:
			return "JUMPB";
		case SEARCHF:
			return "SEARCHF";
		case SEARCHB:
			return "SEARCHB";
		case SEARCH:
			return "SEARCH";
		case DIVIDE:
			return "DIVIDE";
		case MOVEIXBA:
			return "MOVEIXBA";
		case LOADAB:
			return "LOADAB";
		case ALLOC:
			return "ALLOC";
		case PUSHA:
			return "PUSHA";
		case PUSHB:
			return "PUSHB";
		case PUSHC:
			return "PUSHC";
		case POPA:
			return "POPA";
		case POPB:
			return "POPB";
		case POPC:
			return "POPC";
		case PUSHD:
			return "PUSHD";
		case POPD:
			return "POPD";
		case LOADCD:
			return "LOADCD";
		case SUBCAA:
			return "SUBCAA";
		case INCA:
			return "INCA";
		case INCB:
			return "INCB";
		case INCC:
			return "INCC";
		case DECC:
			return "DECC";
		case NOT0C:
			return "NOT0C";
		case LSHIFTC:
			return "LSHIFTC";
		case IFCZ:
			return "IFCZ";
		case CALL:
			return "CALL";
		case RET:
			return "RET";
		default:
			return Byte.toString(b);
		}
	}
	
	public static byte bitMutate(byte b) {
		byte mask = 1;
		int shift = (int)(BITS * Math.random());
		mask = (byte)(mask << shift);
		return (byte)(b ^ mask);
	}
	
	public static byte getRandomCode() {
		return (byte)(Math.random() * VALID_INSTRUCTIONS.length);
	}
	
	public static String[] getCodeNameList(byte[] data) {
		String[] ret = new String[data.length];
		for(int i = 0; i < data.length; i++) {
			ret[i] = getCodeName(data[i]);
		}
		return ret;
	}
	
	@Override
	/**Compares two codes, first by length, and then by name*/
	public int compareTo(Code c) {
		/*if(this.instr.length != c.instr.length) {
			return this.instr.length - c.instr.length;
		} else {
			return this.name.compareTo(c.name);
		}*/
		return this.getFullName().compareTo(c.getFullName());
	}
	
	@Override
	public String toString() {
		return this.getFullName();
	}
	
	@Override
	/**Compares the code part of two codes only*/
	public boolean equals(Object o) {
		if(o instanceof Code) {
			Code c = (Code)o;
			return Arrays.equals(getCode(), c.getCode());
		} else {
			throw new IllegalArgumentException("Invalid Object: " + o);
		}
	}
}

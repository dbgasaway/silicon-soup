package cellProcesses;

public class Code implements Comparable<Code> {
	private String name;
	private byte[] instr;
	
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
}

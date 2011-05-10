package cellProcesses;

public class Code {
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
}

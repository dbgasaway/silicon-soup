package main;

import cellProcesses.*;

public class SiliconSoup {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		SoupGUI g = new SoupGUI();
		g.addCell(BASE_CELL_CODE);
		Thread t = new Thread(g);
		t.start();
	}
	
	/**Code for the example cell*/
	private static final byte[] BASE_CELL_CODE = {Code.NOP1, Code.NOP1, Code.NOP1, 
		Code.NOP1, Code.ZERO, Code.NOT0C, Code.LSHIFTC, Code.LSHIFTC, Code.LOADCD, 
		Code.SEARCHB, Code.NOP0, Code.NOP0, Code.NOP0, Code.NOP0, Code.SUBCAA, 
		Code.LOADAB, Code.SEARCHF, Code.NOP0, Code.NOP0, Code.NOP0, Code.NOP1, 
		Code.INCA, Code.SUBBAC, Code.NOP1, Code.NOP1, Code.NOP0, Code.NOP1, 
		Code.ALLOC, Code.CALL, Code.NOP0, Code.NOP0, Code.NOP1, Code.NOP1, 
		Code.DIVIDE, Code.JUMP, Code.NOP0, Code.NOP0, Code.NOP1, Code.NOP0, 
		Code.IFCZ, Code.NOP1, Code.NOP1, Code.NOP0, Code.NOP0, Code.PUSHA, 
		Code.PUSHB, Code.PUSHC, Code.NOP1, Code.NOP0, Code.NOP1, Code.NOP0, 
		Code.MOVEIXBA, Code.DECC, Code.IFCZ, Code.JUMP, Code.NOP0, Code.NOP1 ,
		Code.NOP0, Code.NOP0, Code.INCA, Code.INCB, Code.JUMP, Code.NOP0, Code.NOP1, 
		Code.NOP0, Code.NOP1, Code.IFCZ, Code.NOP1, Code.NOP0, Code.NOP1, Code.NOP1, 
		Code.POPC, Code.POPB, Code.POPA, Code.RET, Code.NOP1, Code.NOP1, Code.NOP1, 
		Code.NOP0, Code.IFCZ};
}

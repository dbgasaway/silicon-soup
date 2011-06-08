package comparators;

import java.util.*;

public class SequenceAlignment {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Scanner s = new Scanner(System.in);
		
		System.out.println("enter two strings to compare:");
		//String s1 = "[NOP0, INCC, INCA, SUBBAC, NOP1, NOP1, NOP0, NOP1, ALLOC, CALL, NOP0, NOP0, NOP1, DECC, NOP1, DIVIDE, JUMP, NOP0, NOP0, NOP1, NOP0, IFCZ, NOP1, NOP1, NOP0, NOP0, PUSHA, PUSHB, PUSHC, NOP1, NOP0, NOP1, NOP0, MOVEIXBA, DECC, IFCZ, JUMP, NOP0, NOP0, NOP0, INCA, INCB, JUMP, NOP0, NOP1, NOP0, NOP1, IFCZ, NOP1, NOP0, NOP1, IFCZ, NOP1, NOP0, NOP1, NOP1, POPC, POPB, NOP1, NOP1, NOP1, NOP1, MOVEIXBA, DECC, IFCZ, NOP1, NOP1, NOP1, NOP1, JUMPF, NOT0C, LSHIFTC, LSHIFTC, LOADCD, SEARCHB, NOP0, NOP0, NOP0, NOP0, SUBCAA, LOADAB, SEARCHF, NOP0, NOP0, NOP0, NOP1, INCA, SUBBAC, DIVIDE, NOP1, NOP0, NOP1, ALLOC, CALL, NOP0, NOP0, NOP1, NOP1, DIVIDE, JUMP, NOP0, NOP1, NOP1, NOP0, IFCZ, NOP1, NOP1, NOP0, NOP0, PUSHA, PUSHB, PUSHC, NOP1, NOP0, NOP1, NOP0, MOVEIXBA, DECC, IFCZ, JUMP, NOP0, NOP1, NOP0, NOP0, INCA, INCB, JUMP, NOP0, NOP1, NOP0, NOP1, IFCZ, NOP1, IFCZ, PUSHA, PUSHB, PUSHB, PUSHC, NOP1, PUSHD, PUSHA, NOP1, NOP0, MOVEIXBA, DECC, IFCZ, JUMP, NOP0, NOP1, NOP0, NOP0, NOP0, NOP0, NOP0, NOP0, NOP0, NOP0, NOP0, NOP0, NOP0, NOP0, NOP0, NOP0, NOP0, NOP0, NOP0, NOP0, NOP0, NOP0, NOP0, NOP1, NOP1, NOP1, NOP1, ZERO, NOT0C, LSHIFTC, LOADCD, SEARCHB, NOP0, NOP0, NOP0, NOP0, SUBCAA, LOADAB, SEARCHF, NOP0, NOP0, NOP0, NOP1, INCA, SUBBAC, NOP1, NOP1, NOP0, ALLOC, CALL, NOP0, NOP0, NOP1, NOP1, DIVIDE, JUMP, NOP0, NOP0, NOP1, NOP0, IFCZ, NOP1, NOP1, NOP0, NOP0, PUSHA, PUSHB, PUSHC, NOP1, NOP0, NOP1, NOP0, DECC, MOVEIXBA, NOP1, DECC, IFCZ, JUMP, NOP0, NOP1, NOP1, NOP1, NOP1, ZERO, NOT0C, LSHIFTC, LSHIFTC, LOADCD, SEARCHB, NOP0, NOP0, NOP0, NOP0, SUBCAA, LOADAB, SEARCHF, NOP0, NOP0, NOP0, NOP1, INCA, SUBBAC, NOP1, NOP1, NOP0, NOP1, ALLOC, CALL, NOP0, NOP0, NOP1, NOP1, NOP1, JUMP, NOP0, NOP0, NOP1, NOP0, IFCZ, NOP1, NOP1, NOP0, NOP0, PUSHA, PUSHB, PUSHC, IFCZ, NOP1, NOP0, NOP1, NOP0, MOVEIXBA, DECC, IFCZ, JUMP, JUMP, NOP1, NOP0, NOP0, INCA, INCB, JUMP, NOP0, NOP1, NOP0, NOP1, IFCZ, PUSHA, PUSHB, PUSHC, NOP1, NOP0, NOP1, NOP0, MOVEIXBA, DECC, IFCZ, NOP1, NOP1, NOP1, NOP1, ZERO, NOT0C, LSHIFTC, LSHIFTC, LOADCD, SEARCHB, NOP0, NOP0, POPB, NOP0, SUBCAA, LOADAB, SEARCHF, NOP0, NOP0, NOP0, NOP1, INCA, SUBBAC, NOP1, NOP1, DIVIDE, NOP1, ALLOC, CALL, NOP0, NOP0]";
		//String s2 = "[NOP1, NOP1, NOP1, NOP1, ZERO, NOT0C, LSHIFTC, LSHIFTC, LOADCD, SEARCHB, NOP0, NOP0, NOP0, NOP0, SUBCAA, LOADAB, SEARCHF, NOP0, NOP0, NOP0, NOP1, INCA, SUBBAC, NOP1, NOP1, NOP0, NOP1, ALLOC, CALL, NOP0, NOP0, NOP1, NOP1, DIVIDE, JUMP, NOP0, NOP0, NOP1, NOP0, IFCZ, NOP1, NOP1, NOP0, NOP0, PUSHA, PUSHB, PUSHC, NOP1, NOP0, NOP1, NOP0, MOVEIXBA, DECC, IFCZ, JUMP, NOP0, NOP1, NOP0, NOP0, INCA, INCB, JUMP, NOP0, NOP1, NOP0, NOP1, IFCZ, NOP1, NOP0, NOP1, NOP1, POPC, POPB, POPA, RET, NOP1, NOP1, NOP1, NOP0, IFCZ]";
		String s1 = s.nextLine();
		String s2 = s.nextLine();
		int[][] scores = findEditDistance(s1, s2);
		/*if(s1.length() < s2.length()) {
			String temp = s1;
			s1 = s2;
			s2 = temp;
		}*/
		String optimal = findOptimalString(scores, s1, s2);
		System.out.println(scores[0][0]);
		System.out.println(optimal);
	}

	private static int min(int a, int b, int c) {
		if(a <= b) {
			if(a <= c) {
				return a;
			} else{
				return c;
			}
		} else {
			if(b <= c) {
				return b;
			} else {
				return c;
			}
		}
	}
	
	public static int missingWeight = 1;
	public static int replacementWeight = 2;
	
	public static int findEditDistanceNumber(String x, String y) {
		return(findEditDistance(x, y)[0][0]);
	}
	
	/**finds the edit distance between two strings
	 * @return the edit distance matrix, which has the distance at 0, 0; it also holds the optimal configuration*/
	public static int[][] findEditDistance(String x, String y) {
		int xL = x.length();
		int yL = y.length();
		int[][] opts = new int[xL + 1][yL + 1];
		for(int i = xL; i >= 0; i--) {
			for(int k = yL; k >= 0; k--) {
				if(k == yL && i == xL) {
					opts[i][k] = 0;
				} else {
					if(k == yL) {
						opts[i][k] = (xL - i) * missingWeight;
					} else {
						if(i == xL) {
							opts[i][k] = (yL - k) * missingWeight;
						} else {
							int opt1 = opts[i + 1][k + 1];
							if(x.charAt(i) != y.charAt(k)) opt1 += replacementWeight;
							int opt2 = opts[i + 1][k] + missingWeight;
							int opt3 = opts[i][k + 1] + missingWeight;
							opts[i][k] = min(opt1, opt2, opt3);
						}
					}
				}
			}
		}
		
		/*for(int[] row : opts) {
			System.out.println(Arrays.toString(row));
		}*/
		return(opts);
	}
	
	/**Finds the optimal matching between two strings given a edit distance matrix.
	 * It is required to use the strings in the same order as used to generate the edit distance matrix
	 * @param opts - the edit distance matrix
	 * @return the optimal configuration, with '-' used to represent a blank,
	 * in the form "str1\nstr2"*/
	public static String findOptimalString(int[][] opts, String x, String y) {
		String rX = "";
		String rY = "";
		int i = 0;
		int k = 0;
		while(true) {
			if(i == x.length() && k == y.length()) {
				break;
			}
			int comp = opts[i][k];
			int condAdd = 0;
			if(x.charAt(i) != y.charAt(k)) condAdd += replacementWeight;
			if(comp == opts[i + 1][k + 1] + condAdd) {
				rX = rX.concat("" + x.charAt(i));
				rY = rY.concat("" + y.charAt(k));
				i++;
				k++;
			} else {
				if(comp == opts[i + 1][k] + missingWeight) {
					rX = rX.concat("" + x.charAt(i));
					rY = rY.concat("-");
					i++;
				} else {
					rX = rX.concat("-");
					rY = rY.concat("" + y.charAt(k));
					k++;
				}
			}
		}
		return(rX + "\n" + rY);
	}
	
	public static int findEditDistanceNumber(byte[] x, byte[] y) {
		return(findEditDistance(x, y)[0][0]);
	}

	public static int[][] findEditDistance(byte[] x, byte[] y) {
		int xL = x.length;
		int yL = y.length;
		int[][] opts = new int[xL + 1][yL + 1];
		for(int i = xL; i >= 0; i--) {
			for(int k = yL; k >= 0; k--) {
				if(k == yL && i == xL) {
					opts[i][k] = 0;
				} else {
					if(k == yL) {
						opts[i][k] = (xL - i) * missingWeight;
					} else {
						if(i == xL) {
							opts[i][k] = (yL - k) * missingWeight;
						} else {
							int opt1 = opts[i + 1][k + 1];
							if(x[i] != y[k]) opt1 += replacementWeight;
							int opt2 = opts[i + 1][k] + missingWeight;
							int opt3 = opts[i][k + 1] + missingWeight;
							opts[i][k] = min(opt1, opt2, opt3);
						}
					}
				}
			}
		}
		
		/*for(int[] row : opts) {
			System.out.println(Arrays.toString(row));
		}*/
		return(opts);
	}
	
	public static String findOptimalComparison(int[][] opts, byte[] x, byte[] y) {
		String rX = "";
		String rY = "";
		int i = 0;
		int k = 0;
		while(true) {
			if(i >= x.length - 1 && k >= y.length - 1 || (i == x.length || k == y.length)) {
				break;
			}
			int comp = opts[i][k];
			int condAdd = 0;
			if(x[i] != y[k]) condAdd += replacementWeight;
			if(comp == opts[i + 1][k + 1] + condAdd) {
				rX = rX.concat("" + x[i]);
				rY = rY.concat("" + y[k]);
				i++;
				k++;
			} else {
				if(comp == opts[i + 1][k] + missingWeight) {
					rX = rX.concat("" + x[i]);
					rY = rY.concat("-");
					i++;
				} else {
					rX = rX.concat("-");
					rY = rY.concat("" + y[k]);
					k++;
				}
			}
		}
		return(rX + "\n" + rY);
	}
}

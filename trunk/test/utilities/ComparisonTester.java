/**
 * Tests the public functions in the SequenceAlignment class
 */
package utilities;

import static org.junit.Assert.*;

import org.junit.Test;

import comparators.SequenceAlignment;

/**
 * @author Derek
 *
 */
public class ComparisonTester {

	@Test
	public void testDifferenceHighlighter1() {
		String s1 = "[NOP1, NOP1, NOP1, NOP1, ZERO, NOT0C, LSHIFTC";
		String s2 = "[NOP1, NOP1, ZERO, NOP1, ZERO, NOT0C, LSHIFTC";
		String[] results = SequenceAlignment.highlightDifferences(s1, s2);
		assertEquals("             NOP1                            ", results[0]);
		assertEquals("             ZERO                            ", results[1]);
	}
	
	@Test
	public void testDifferenceHighlighter2() {
		String s1 = "buffoon";
		String s2 = "buttern";
		String[] results = SequenceAlignment.highlightDifferences(s1, s2);
		assertEquals("  ffoo ", results[0]);
		assertEquals("  tter ", results[1]);
	}
}

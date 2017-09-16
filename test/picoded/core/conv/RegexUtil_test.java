package picoded.core.conv;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class RegexUtil_test {
	
	@Before
	public void setUp() {
	}
	
	@After
	public void tearDown() {
		
	}
	
	//
	// Expected exception testing
	//
	
	/// Invalid constructor test
	@Test(expected = IllegalAccessError.class)
	public void invalidConstructor() throws Exception {
		new RegexUtil();
	}
	
	@Test
	public void removeAllWhiteSpaceTest() {
		assertEquals("THereWILLBeNOWHITEISSPACE",
			RegexUtil.removeAllWhiteSpace("T   H ere  WILL  Be N O W H I T E I S S PACE"));
	}
	
	@Test
	public void removeAllNonAlphaNumericTest() {
		assertEquals(RegexUtil.removeAllWhiteSpace("N NONALPHANUMERICS ARE ALLOWED hsdofhsd"),
			RegexUtil.removeAllWhiteSpace(RegexUtil
				.removeAllNonAlphaNumeric("N@ NON-ALPHANUMERICS ARE ALLOWED :- !#@!hsdofhsd")));
	}
	
	@Test
	public void removeAllNonAlphaNumericAllowUnderscoreDashFullstopTest() {
		assertEquals(
			"removesallnon-alphanumericsexcept_-.",
			RegexUtil
				.removeAllNonAlphaNumeric_allowUnderscoreDashFullstop("removes all non-alphanumerics except _-."));
	}
	
	@Test
	public void removeAllNonAlphaNumericAllowCommonSeparatorsTest() {
		assertEquals(
			"removesnon-alphanumericsexceptcommonseprators_",
			RegexUtil
				.removeAllNonAlphaNumeric_allowCommonSeparators("removes non-alphanumerics except common seprators /_,"));
	}
	
	@Test
	public void removeAllNonNumericTest() {
		assertEquals("12345",
			RegexUtil.removeAllNonNumeric("T 1  H 2er3e4  WILL 5 Be N O W H I T E I S S PACE"));
	}
	
}

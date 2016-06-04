package picodedTests.conv;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import picoded.conv.RegexUtils;

public class RegexUtils_test {
	// / Dummy constructor test
	protected RegexUtils regexUtils = null;
	
	@Before
	public void setUp() {
		regexUtils = new RegexUtils();
	}
	
	@Test
	public void removeAllWhiteSpace() {
		assertEquals("THereWILLBeNOWHITEISSPACE",
			RegexUtils.removeAllWhiteSpace("T   H ere  WILL  Be N O W H I T E I S S PACE"));
	}
	
	@Test
	public void removeAllNonAlphaNumeric() {
		assertEquals(RegexUtils.removeAllWhiteSpace("N NONALPHANUMERICS ARE ALLOWED hsdofhsd"),
			RegexUtils.removeAllWhiteSpace(RegexUtils
				.removeAllNonAlphaNumeric("N@ NON-ALPHANUMERICS ARE ALLOWED :- !#@!hsdofhsd")));
	}
	
	@Test
	public void removeAllNonAlphaNumeric_allowUnderscoreDashFullstop() {
		assertEquals("removesallnon-alphanumericsexcept_-.",
			RegexUtils.removeAllNonAlphaNumeric_allowUnderscoreDashFullstop("removes all non-alphanumerics except _-."));
	}
	
	@Test
	public void removeAllNonAlphaNumeric_allowCommonSeparators() {
		assertEquals("removesnon-alphanumericsexceptcommonseprators_",
			RegexUtils
				.removeAllNonAlphaNumeric_allowCommonSeparators("removes non-alphanumerics except common seprators /_,"));
	}
	
}

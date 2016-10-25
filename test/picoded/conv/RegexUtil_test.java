package picoded.conv;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

public class RegexUtil_test {
	/// Dummy constructor test
	protected RegexUtil regexUtils = null;
	
	@Before
	public void setUp() {
		regexUtils = new RegexUtil();
	}
	
	@Test
	public void removeAllWhiteSpace() {
		assertEquals("THereWILLBeNOWHITEISSPACE",
			RegexUtil.removeAllWhiteSpace("T   H ere  WILL  Be N O W H I T E I S S PACE"));
	}
	
	@Test
	public void removeAllNonAlphaNumeric() {
		assertEquals(RegexUtil.removeAllWhiteSpace("N NONALPHANUMERICS ARE ALLOWED hsdofhsd"),
			RegexUtil.removeAllWhiteSpace(RegexUtil
				.removeAllNonAlphaNumeric("N@ NON-ALPHANUMERICS ARE ALLOWED :- !#@!hsdofhsd")));
	}
	
	@Test
	public void removeAllNonAlphaNumeric_allowUnderscoreDashFullstop() {
		assertEquals("removesallnon-alphanumericsexcept_-.",
			RegexUtil.removeAllNonAlphaNumeric_allowUnderscoreDashFullstop("removes all non-alphanumerics except _-."));
	}
	
	@Test
	public void removeAllNonAlphaNumeric_allowCommonSeparators() {
		assertEquals("removesnon-alphanumericsexceptcommonseprators_",
			RegexUtil
				.removeAllNonAlphaNumeric_allowCommonSeparators("removes non-alphanumerics except common seprators /_,"));
	}
	
}

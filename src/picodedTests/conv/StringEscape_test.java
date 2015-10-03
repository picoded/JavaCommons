package picodedTests.conv;

import static org.junit.Assert.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import picoded.conv.StringEscape;

/// The following test case covers the encode/decodeURI extension added to stringEscapeUtils
public class StringEscape_test {
	
	@Test
	public void encodeAndDecodeURI() {
		assertEquals("abc%2Bxyz", StringEscape.encodeURI("abc+xyz"));
		assertEquals("qwe abc+xyz", StringEscape.decodeURI("qwe+abc%2Bxyz"));
	}
	
}

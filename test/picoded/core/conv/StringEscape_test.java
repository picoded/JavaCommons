package picoded.core.conv;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.UnsupportedEncodingException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/// The following test case covers the encode/decodeURI extension added to stringEscapeUtils
public class StringEscape_test {
	
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
		new StringEscape();
		
	}
	
	@Test
	public void encodeAndDecodeURI() throws UnsupportedEncodingException {
		assertEquals("abc%2Bxyz", StringEscape.encodeURI("abc+xyz"));
		assertNotNull("abc%2Bxyz", StringEscape.encodeURI("0918azbyAZBY"));
		assertEquals("qwe abc+xyz", StringEscape.decodeURI("qwe+abc%2Bxyz"));
		assertNull(StringEscape.decodeURI("%xy"));
		assertNotNull(StringEscape.encodeURI("-_.!~*'()\""));
	}
	
	@Test
	public void testEscapeHtml() {
		String str = "A 'quote' is <b>bold</b>";
		String encodedStr = null;
		
		assertNotNull(encodedStr = StringEscape.escapeHtml(str));
		
		assertEquals(encodedStr, StringEscape.escapeHtml(str));
		assertEquals(str, StringEscape.unescapeHtml(encodedStr));
		
		assertNotNull(encodedStr = StringEscape.escapeCsv(str));
		assertEquals(encodedStr, StringEscape.escapeCsv(str));
		assertEquals(str, StringEscape.unescapeCsv(encodedStr));
		
		str = "I didn't  say \"you to run!\"";
		assertNotNull(encodedStr = StringEscape.escapeCsv(str));
	}
	
	@Test
	public void stringEscapeCommonEscapeCharactersIntoAsciiTest() {
		assertEquals("&#92;&#60;test&#92;&#62;",
			StringEscape.commonHtmlEscapeCharacters("\\<test\\>"));
	}
}

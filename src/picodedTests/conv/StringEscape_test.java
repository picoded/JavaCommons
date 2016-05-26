package picodedTests.conv;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import picoded.conv.StringEscape;

/// The following test case covers the encode/decodeURI extension added to stringEscapeUtils
public class StringEscape_test {
	
	@Test
	public void encodeAndDecodeURI() {
		assertEquals("abc%2Bxyz", StringEscape.encodeURI("abc+xyz"));
		assertEquals("qwe abc+xyz", StringEscape.decodeURI("qwe+abc%2Bxyz"));
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
}

package picoded.core.struct.query.internal;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class QueryUtils_test {
	
	@Before
	public void setUp() {
		
	}
	
	@After
	public void tearDown() {
		
	}
	
	@Test(expected = IllegalAccessError.class)
	public void QueryUtilsTest() {
		assertNotNull(new QueryUtils());
	}
	
	@Test
	public void getFieldValueTest() {
		assertEquals("s", QueryUtils.getFieldValue("s", null));
		assertEquals("s", QueryUtils.getFieldValue("s", "this"));
	}
	
	@Test
	public void normalizeNumberTest() {
		Integer i = 1;
		Double d = 1d;
		assertEquals(d, QueryUtils.normalizeNumber(i));
		Float f = 1f;
		assertEquals(d, QueryUtils.normalizeNumber(f));
		assertEquals(d, QueryUtils.normalizeNumber(d));
	}
	
	@Test
	public void normalizeObjectTest() {
		Integer i = 1;
		Double d = 1d;
		assertEquals(d, QueryUtils.normalizeObject(i));
		assertEquals(d, QueryUtils.normalizeObject("1"));
		d = 1.1d;
		assertEquals(d, QueryUtils.normalizeObject("1.1"));
	}
	
	@Test(expected = RuntimeException.class)
	public void normalizeObjectInvalidTest() {
		Double d = 1d;
		assertEquals(d, QueryUtils.normalizeObject("a.b"));
	}
	
	@Test
	public void unwrapFieldNameTest() {
		assertEquals("field", QueryUtils.unwrapFieldName("\"field\""));
		assertEquals("field", QueryUtils.unwrapFieldName("'field'"));
		assertEquals("field", QueryUtils.unwrapFieldName("[field]"));
	}
}

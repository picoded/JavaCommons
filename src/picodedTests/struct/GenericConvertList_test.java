package picodedTests.struct;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import picoded.struct.GenericConvertArrayList;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class GenericConvertList_test {
	
	private GenericConvertArrayList convertArrayList = null;
	
	@Before
	public void setUp() {
	}
	
	@After
	public void tearDown() {
	}
	
	// / The following, test severals assumptions regarding Object[] instanceof
	// tests
	// / done in java, this is required for the generic toXArray functions.
	@Test
	public void arrayInstanceOfTest() {
		String sample = "a";
		assertFalse(((Object) sample instanceof Object[]));
		
		String[] strArr = new String[] { "1", "2", "3" };
		Object[] objArr = new Object[] { "1", "2", "3" };
		Integer[] intArr = new Integer[] { 1, 2, 3 };
		
		// / String array tests
		assertTrue((strArr instanceof Object[]));
		assertTrue(((Object) strArr instanceof Object));
		
		assertTrue(((Object) strArr instanceof String[]));
		assertFalse(((Object) strArr instanceof Integer[]));
		assertFalse(((Object) strArr instanceof String));
		assertFalse(((Object) strArr instanceof Integer));
		
		// / Integer array tests
		assertTrue((intArr instanceof Object[]));
		assertTrue(((Object) intArr instanceof Object));
		
		assertFalse(((Object) intArr instanceof String[]));
		assertTrue(((Object) intArr instanceof Integer[]));
		assertFalse(((Object) intArr instanceof String));
		assertFalse(((Object) intArr instanceof Integer));
		
		// / Object array tests
		assertTrue((objArr instanceof Object[]));
		assertTrue(((Object) objArr instanceof Object));
		
		assertFalse(((Object) objArr instanceof String[]));
		assertFalse(((Object) objArr instanceof Integer[]));
		assertFalse(((Object) objArr instanceof String));
		assertFalse(((Object) objArr instanceof Integer));
	}
	
	@Test
	public void classLoockUpTest() {
		assertEquals(String.class, String.class);
		assertEquals(String[].class, String[].class);
		assertNotEquals(String.class, String[].class);
	}
	
	@Test
	public void intTest() {
		convertArrayList = new GenericConvertArrayList<String>();
		assertTrue(convertArrayList.isEmpty());
		convertArrayList.add("itemA");
		assertEquals(1, convertArrayList.size());
		convertArrayList.add("itemB");
		assertEquals(2, convertArrayList.size());
	}
	
	@Test
	public void testAddAndGet() {
		convertArrayList = new GenericConvertArrayList<String>();
		convertArrayList.add(42);
		convertArrayList.add(-3);
		convertArrayList.add(17);
		convertArrayList.add(99);
		assertEquals(42, convertArrayList.get(0));
		assertEquals(-3, convertArrayList.get(1));
		assertEquals(17, convertArrayList.get(2));
		assertEquals(99, convertArrayList.get(3));
		assertEquals("second attempt", 42, convertArrayList.get(0));
		assertEquals("second attempt", 99, convertArrayList.get(3));
	}
	
	@Test
	public void testSize() {
		convertArrayList = new GenericConvertArrayList<String>();
		assertEquals(0, convertArrayList.size());
		convertArrayList.add(42);
		assertEquals(1, convertArrayList.size());
		convertArrayList.add(-3);
		assertEquals(2, convertArrayList.size());
		convertArrayList.add(17);
		assertEquals(3, convertArrayList.size());
		convertArrayList.add(99);
		assertEquals(4, convertArrayList.size());
		assertEquals("second attempt", 4, convertArrayList.size());
	}
	
	@Test
	public void testIsEmpty() {
		convertArrayList = new GenericConvertArrayList<String>();
		assertTrue(convertArrayList.isEmpty());
		convertArrayList.add(42);
		assertFalse("should have one element", convertArrayList.isEmpty());
		convertArrayList.add(-3);
		assertFalse("should have two elements", convertArrayList.isEmpty());
	}
	
	@Test
	public void testIsEmpty1() {
		convertArrayList = new GenericConvertArrayList<String>();
		convertArrayList.add(42);
		convertArrayList.add(-3);
		assertFalse("should have two elements", convertArrayList.isEmpty());
		convertArrayList.remove(1);
		convertArrayList.remove(0);
		assertTrue("after removing all elements", convertArrayList.isEmpty());
		convertArrayList.add(42);
		assertFalse("should have one element", convertArrayList.isEmpty());
	}
	
}

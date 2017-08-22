package picoded.core.struct;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.NoSuchElementException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ArbitaryListIterator_test {
	
	// Test list
	private ArbitaryListIterator<Object> listIterator = null;
	private List<Object> list = null;
	private int inIdx = 0;
	
	@Before
	public void setUp() {
		list = new ArrayList<Object>();
		list.add("hello");
		list.add("world");
		listIterator = new ArbitaryListIterator<>(list, inIdx);
	}
	
	@After
	public void tearDown() {
		list = null;
		listIterator = null;
	}
	
	@Test
	public void notNullTest() {
		assertNotNull(listIterator);
	}
	
	@Test
	public void hasNextTest() {
		assertTrue(listIterator.hasNext());
	}
	
	@Test
	public void nextTest() {
		assertEquals("hello", listIterator.next());
	}
	
	@Test(expected = NoSuchElementException.class)
	public void nextExceptionTest() {
		listIterator.next();
		listIterator.next();
		assertEquals("hello", listIterator.next());
	}
	
	@Test(expected = ConcurrentModificationException.class)
	public void nextException1Test() {
		listIterator.detectedChange = true;
		assertEquals("hello", listIterator.next());
	}
	
	@Test(expected = ConcurrentModificationException.class)
	public void nextException2Test() {
		listIterator.initialSize = 4;
		assertEquals("hello", listIterator.next());
	}
	
	@Test
	public void removeTest() {
		listIterator.next();
		listIterator.remove();
		assertTrue(listIterator.hasNext());
	}
	
	@Test(expected = IllegalStateException.class)
	public void removeExceptionTest() {
		listIterator.remove();
		assertTrue(listIterator.hasNext());
	}
	
	@Test
	public void hasPreviousTest() {
		listIterator.next();
		assertTrue(listIterator.hasPrevious());
	}
	
	@Test
	public void nextIndexTest() {
		listIterator.next();
		assertEquals(1, listIterator.nextIndex());
	}
	
	@Test
	public void previousIndexTest() {
		listIterator.next();
		assertEquals(0, listIterator.previousIndex());
	}
	
	@Test(expected = NoSuchElementException.class)
	public void previousExceptionTest() {
		assertEquals("hello", listIterator.previous());
	}
	
	@Test
	public void previousTest() {
		listIterator.next();
		assertEquals("hello", listIterator.previous());
	}
	
	@Test(expected = IllegalStateException.class)
	public void setExceptionTest() {
		listIterator.set("temp");
	}
	
	@Test
	public void setTest() {
		listIterator.next();
		listIterator.next();
		listIterator.set("temp");
		assertFalse(listIterator.hasNext());
	}
	
	@Test(expected = IllegalStateException.class)
	public void addExceptionTest() {
		listIterator.add("temp");
	}
	
	@Test
	public void addTest() {
		listIterator.next();
		listIterator.add("temp");
		listIterator.next();
		assertFalse(listIterator.hasNext());
	}
}

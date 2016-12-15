package picoded.struct;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.ListIterator;
import java.util.Set;
import java.util.function.UnaryOperator;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/// Test the java standard ArrayList
/// This is used to ensure code coverage, and implmentation is consistent,
/// between standard List, and UnsupportedDefaultList
public class StandardArrayList_test {
	
	// Test list 
	List<Object> list = null;
	
	@Before
	public void setUp() {
		list = new ArrayList<Object>();
	}
	
	@After
	public void tearDown() {
		list = null;
	}
	
	//
	// List implementation testing
	//
	
	/// Simple add and get
	@Test
	public void simpleAddAndGet() {
		list.add("zero");
		list.add("one");
		list.add("two");
		
		assertEquals("zero", list.get(0));
		assertEquals("one", list.get(1));
		assertEquals("two", list.get(2));
	}
	
	@Test
	public void addWithIndexAndGetTest() {
		list.add("zero");
		list.add("one");
		list.add("two");
		list.add(2, "TWO");
		assertEquals("zero", list.get(0));
		assertEquals("one", list.get(1));
		assertEquals("TWO", list.get(2));
		assertEquals("two", list.get(3));
	}
	
	@Test
	public void addWithCollectionAndGetTest() {
		Set<String> set = new HashSet<>();
		set.add("zero");
		set.add("one");
		set.add("two");
		list.addAll(set);
		assertEquals("zero", list.get(0));
		assertEquals("one", list.get(1));
		assertEquals("two", list.get(2));
	}
	
	@Test
	public void addWithCollectionWithIndexAndGetTest() {
		Set<String> set = new HashSet<>();
		list.add("ZERO");
		set.add("one");
		set.add("two");
		list.addAll(1, set);
		assertEquals("ZERO", list.get(0));
		assertEquals("one", list.get(1));
		assertEquals("two", list.get(2));
	}
	
	@Test
	public void clearAndSizeAndContainsTest() {
		list.add("zero");
		list.add("one");
		list.add("two");
		
		assertEquals("zero", list.get(0));
		assertEquals("one", list.get(1));
		assertTrue(list.contains("two"));
		list.clear();
		assertEquals(0, list.size());
	}
	
	@Test
	public void containsAllTest() {
		Set<String> set = new HashSet<>();
		set.add("zero");
		set.add("one");
		set.add("two");
		list.addAll(set);
		assertTrue(list.containsAll(set));
	}
	
	@Test
	public void indexOfTest() {
		Set<String> set = new HashSet<>();
		set.add("zero");
		set.add("one");
		set.add("two");
		list.addAll(set);
		assertEquals(1, list.indexOf("one"));
	}
	
	@Test
	public void isEmptyTest() {
		assertTrue(list.isEmpty());
		list.add("zero");
		list.add("one");
		list.add("two");
		assertFalse(list.isEmpty());
		Iterator<Object> iterator = list.iterator();
		assertNotNull(iterator);
	}
	
	@Test
	public void iteratorsTest() {
		assertTrue(list.isEmpty());
		list.add("zero");
		list.add("one");
		list.add("two");
		Iterator<Object> iterator = list.iterator();
		assertNotNull(iterator);
		ListIterator<Object> listIterator = list.listIterator();
		assertNotNull(listIterator);
		ListIterator<Object> listIteratorInt = list.listIterator(1);
		assertNotNull(listIteratorInt);
		assertEquals("one", listIteratorInt.next());
	}
	
	@Test
	public void removeTest() {
		list.add("zero");
		list.add("one");
		list.add("two");
		list.remove("one");
		assertEquals("two", list.get(1));
		list.remove(1);
		assertEquals(1, list.size());
		
	}
	
	@Test
	public void removeAllTest() {
		Set<String> set = new HashSet<>();
		list.add("ZERO");
		set.add("one");
		set.add("two");
		list.addAll(1, set);
		assertEquals(3, list.size());
		list.removeAll(set);
		assertEquals(1, list.size());
	}
	
	@Test
	public void retainAllTest() {
		Set<String> set = new HashSet<>();
		list.add("ZERO");
		set.add("one");
		set.add("two");
		list.addAll(1, set);
		assertEquals(3, list.size());
		list.retainAll(set);
		assertEquals(2, list.size());
	}
	
	@Test
	public void replaceAllTest() {
		list.add("zero");
		list.add("one");
		list.add("two");
		MyOperator<Object> operator = new MyOperator<>();
		operator.arg1 = "TEN";
		list.replaceAll(operator);
		assertEquals("TEN", list.get(0));
		assertEquals("TEN", list.get(1));
		assertEquals("TEN", list.get(2));
	}
	
	private class MyOperator<T> implements UnaryOperator<T> {
		T arg1;
		
		@Override
		public T apply(T param) {
			return arg1;
		}
	}
}

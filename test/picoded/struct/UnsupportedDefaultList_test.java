package picoded.struct;

import java.util.List;
import java.util.ArrayList;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/// Test the UnsupportedDefaultList,
/// Most of the test coverage is actually provided by StandardArrayList test
public class UnsupportedDefaultList_test extends StandardArrayList_test {
	
	//
	// Class implementation of UnsupportedDefaultList
	//
	
	/// Blank implmentation of UnsupportedDefaultList
	class UnsupportedTest<E> implements UnsupportedDefaultList<E> {
	}
	
	/// Implementation of core default list functions, to an arraylist
	class ProxyTest<E> implements UnsupportedDefaultList<E> {
		// Base list to implement
		ArrayList<E> base = new ArrayList<E>();
		
		// Implementation to actual base list
		@Override
		public E get(int key) {
			return base.get(key);
		}
		
		// Implementation to actual base list
		@Override
		public void add(int index, E value) {
			base.add(index, value);
		}
		
		// Implementation to actual base list
		@Override
		public E remove(int index) {
			return base.remove(index);
		}
		
		// Implementation to actual base list
		@Override
		public int size() {
			return base.size();
		}
	}
	
	//
	// List implementation objects
	//
	
	// unsupported implementation
	List<Object> unsupported = null;
	
	// Test list 
	// List<Object> list = null;
	
	@Override
	@Before
	public void setUp() {
		unsupported = new UnsupportedTest<Object>();
		list = new ProxyTest<Object>();
	}
	
	@Override
	@After
	public void tearDown() {
		unsupported = null;
		list = null;
	}
	
	//
	// Unsupported Operation Exception
	//
	
	@Test(expected = UnsupportedOperationException.class)
	public void getUnsupportedTest() {
		unsupported.get(0);
	}
	
	@Test(expected = UnsupportedOperationException.class)
	public void addUnsupportedTest() {
		unsupported.add(1, "value");
	}
	
	@Test(expected = UnsupportedOperationException.class)
	public void removeUnsupportedTest() {
		unsupported.remove("key");
	}
	
	@Test(expected = UnsupportedOperationException.class)
	public void removeWithParamIntUnsupportedTest() {
		unsupported.remove(1);
	}
	
	@Test(expected = UnsupportedOperationException.class)
	public void sizeUnsupportedTest() {
		unsupported.remove("key");
	}
	
}

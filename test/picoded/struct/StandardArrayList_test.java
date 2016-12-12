package picoded.struct;

import java.util.List;
import java.util.ArrayList;
import java.util.Observer;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

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
}

package picoded.core.conv;

//Target test class
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ListValueConv_test {
	
	List<Object> listObj = null;
	
	@Before
	public void setUp() {
		listObj = new ArrayList<Object>();
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
		new ListValueConv();
		
	}
	
	@Test
	public void objectListToStringArray() {
		listObj.add("str");
		assertEquals("str", ListValueConv.objectListToStringArray(listObj)[0]);
		listObj = new ArrayList<Object>();
		listObj.add(null);
		assertNull(ListValueConv.objectListToStringArray(listObj)[0]);
		listObj = new ArrayList<Object>();
		listObj.add("");
		assertEquals("", ListValueConv.objectListToStringArray(listObj)[0]);
	}
	
	@Test
	public void objectToString() {
		listObj = new ArrayList<Object>();
		listObj.add("str");
		assertEquals("str", ListValueConv.objectToString(listObj).get(0));
		listObj = new ArrayList<Object>();
		listObj.add(null);
		assertNull(ListValueConv.objectToString(listObj).get(0));
		listObj = new ArrayList<Object>();
		listObj.add("");
		assertEquals("", ListValueConv.objectToString(listObj).get(0));
	}
	
	@Test
	public void deduplicateValuesWithoutArrayOrder() {
		List<String> listStr = new ArrayList<String>();
		listStr.add("str");
		listStr.add("str");
		assertEquals("str", ListValueConv.deduplicateValuesWithoutArrayOrder(listStr).get(0));
		
		listStr = new ArrayList<String>();
		listStr.add(null);
		listStr.add(null);
		assertNull(ListValueConv.deduplicateValuesWithoutArrayOrder(listStr).get(0));
	}
	
	@Test
	public void toStringSet() {
		List<String> listStr = new ArrayList<String>();
		listStr.add("str");
		listStr.add("str");
		assertNotNull(ListValueConv.toStringSet(listStr));
	}
	
}

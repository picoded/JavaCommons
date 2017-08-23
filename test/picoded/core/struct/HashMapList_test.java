package picoded.core.struct;

// Target test class
import static org.junit.Assert.assertArrayEquals;

import java.util.ArrayList;
// Test Case include
import java.util.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import picoded.core.struct.HashMapList;

///
/// Test Case for picoded.core.struct.HashMapList
///
public class HashMapList_test {
	
	@Before
	public void setUp() {
	}
	
	@After
	public void tearDown() {
	}
	
	///
	/// Testing against one append at a time
	///
	@Test
	public void arrayAppendTest() {
		String[] exp = new String[] { "brand", "new", "world" };
		List<String> list = new ArrayList<String>();
		
		list.add(exp[0]);
		list.add(exp[1]);
		list.add(exp[2]);
		
		assertArrayEquals(exp, list.toArray(new String[list.size()]));
	}
	
	///
	/// Testing against .toMapArray conversion
	///
	@Test
	public void toMapArrayConversion() {
		String[] exp = new String[] { "brand", "new", "world" };
		HashMapList<String, String> tObj = new HashMapList<String, String>();
		
		tObj.append("hello", exp[0]);
		tObj.append("hello", exp[1]);
		tObj.append("hello", exp[2]);
		
		Map<String, String[]> cObj = tObj.toMapArray(exp);
		assertArrayEquals(exp, cObj.get("hello"));
	}
	
	///
	/// Testing against multiple append
	///
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void multiArrayAppendTest() {
		
		String[] dataArr = new String[] { "one", "two", "three" };
		List<String> data = Arrays.asList(dataArr);
		List<String> blankList = Arrays.asList(new String[] {});
		
		HashMapList<String, String> tObj = new HashMapList<String, String>();
		
		// Data to test
		tObj.append("hello", data);
		
		// Test that blank data is handled peacefully
		tObj.append("hello", blankList);
		tObj.append("hello", (List) null);
		tObj.append("hello", (Enumeration) null);
		
		Map<String, String[]> cObj = tObj.toMapArray(dataArr);
		assertArrayEquals(dataArr, cObj.get("hello"));
	}
}

package picoded.core.struct;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ArrayListMap_test {
	
	private ArrayListMap<String, String> arrayListMap = null;
	
	@Before
	public void setUp() {
		arrayListMap = new ArrayListMap<String, String>();
	}
	
	@After
	public void tearDown() {
		arrayListMap = null;
	}
	
	@Test
	public void notnullObjectTest() {
		assertNotNull(arrayListMap);
	}
	
	@Test
	public void addToListTest() {
		arrayListMap.addToList("key", "val");
		assertEquals(1, arrayListMap.size());
	}
	
	@Test
	public void toStringTest() {
		arrayListMap.addToList("key", "val");
		assertNotNull(arrayListMap.toString());
		
	}
}

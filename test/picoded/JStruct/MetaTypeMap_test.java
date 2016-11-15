package picoded.JStruct;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class MetaTypeMap_test {
	
	MetaTypeMap metaTypeMap = null;
	
	@Before
	public void setUp() {
		metaTypeMap = new MetaTypeMap();
	}
	
	@After
	public void tearDown() {
		
	}
	
	/// Invalid constructor test
	@Test
	public void constructorTest() {
		metaTypeMap = new MetaTypeMap();
	}
	
	/// Invalid constructor test
	@Test(expected = Exception.class)
	public void putTest() throws Exception {
		assertNotNull(metaTypeMap.put("key", "value"));
		assertNotNull(metaTypeMap.put("key", (Object) MetaType.BINARY));
	}
	
	@Test
	public void putTest1() {
		assertNull(metaTypeMap.put("key", (Object) MetaType.BINARY));
		assertNotNull(metaTypeMap.put("key", (Object) "null"));
	}
}

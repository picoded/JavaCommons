package picoded.JStruct;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class MetaType_test {
	MetaType metaType;
	
	@BeforeClass
	public static void setUp() {
	}
	
	@AfterClass
	public static void tearDown() {
	}
	
	@Test
	public void getNameTest() {
		metaType = MetaType.STRING;
		assertNotNull(metaType.getName());
		assertNotNull(metaType.toString());
	}
	
	@Test
	public void fromIDTest() {
		metaType = MetaType.METATABLE;
		Map<String, MetaType> nameToTypeMap = new HashMap<String, MetaType>();
		Map<Integer, MetaType> idToTypeMap = null;
		
		MetaType.nameToTypeMap = nameToTypeMap;
		MetaType.idToTypeMap = idToTypeMap;
		MetaType.initializeTypeMaps();
		
		idToTypeMap = new HashMap<Integer, MetaType>();
		MetaType.nameToTypeMap = nameToTypeMap;
		MetaType.idToTypeMap = idToTypeMap;
		MetaType.initializeTypeMaps();
		
		MetaType.nameToTypeMap = null;
		MetaType.idToTypeMap = idToTypeMap;
		MetaType.initializeTypeMaps();
		
		idToTypeMap.put(3, MetaType.METATABLE);
		MetaType.idToTypeMap = idToTypeMap;
		assertNotNull(MetaType.fromID(3));
		
	}
	
	@Test
	public void fromNameTest() {
		metaType = MetaType.METATABLE;
		Map<String, MetaType> nameToTypeMap = new HashMap<String, MetaType>();
		nameToTypeMap.put("TEST", MetaType.METATABLE);
		MetaType.nameToTypeMap = nameToTypeMap;
		assertNotNull(MetaType.fromName("UUID"));
	}
	
	@Test(expected = Exception.class)
	public void fromTypeObjectTest() throws Exception {
		Map<Integer, MetaType> idToTypeMap = new HashMap<Integer, MetaType>();
		idToTypeMap.put(3, MetaType.METATABLE);
		MetaType.idToTypeMap = idToTypeMap;
		assertNull(MetaType.fromTypeObject(null));
		assertNotNull(MetaType.fromTypeObject(MetaType.METATABLE));
		assertNotNull(MetaType.fromTypeObject(3));
		assertNotNull(MetaType.fromTypeObject("test"));
	}
}

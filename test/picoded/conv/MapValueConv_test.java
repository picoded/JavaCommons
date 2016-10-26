package picoded.conv;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class MapValueConv_test {
	
	//
	// Temp vars - To setup
	//
	Map<String, Object> unqualifiedMap;
	
	/// Setup the temp vars
	@Before
	public void setUp() {
		unqualifiedMap = new HashMap<String, Object>();
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
		new MapValueConv();
	}
	
	@Test
	public void testTo() {
		File unqualifiedMapFile = new File("./test-files/test-specific/conv/unqualifiedMap.js");
		String jsonString = "";
		try {
			jsonString = FileUtils.readFileToString(unqualifiedMapFile);
		} catch (Exception ex) {
			
		}
		unqualifiedMap = ConvertJSON.toMap(jsonString);
		
		Map<String, Object> qualifiedMap = MapValueConv.toFullyQualifiedKeys(unqualifiedMap, "", ".");
		
		assertNotNull(qualifiedMap);
		assertEquals("1", qualifiedMap.get("agentID"));
		
		assertEquals("Sam", qualifiedMap.get("clients[0].name"));
		assertEquals("Eugene", qualifiedMap.get("clients[1].name"));
		assertEquals("Murong", qualifiedMap.get("clients[2].name"));
		
		assertEquals("12345", qualifiedMap.get("clients[0].nric"));
		assertEquals("23456", qualifiedMap.get("clients[1].nric"));
		assertEquals("34567", qualifiedMap.get("clients[2].nric"));
	}
	
	@Test
	@SuppressWarnings("unchecked")
	public void testFrom() {
		Map<String, Object> unqualifiedMap = new HashMap<String, Object>();
		
		File unqualifiedMapFile = new File("./test-files/test-specific/conv/unqualifiedMap.js");
		String jsonString = "";
		try {
			jsonString = FileUtils.readFileToString(unqualifiedMapFile);
		} catch (Exception ex) {
			
		}
		unqualifiedMap = ConvertJSON.toMap(jsonString);
		
		Map<String, Object> qualifiedMap = MapValueConv.toFullyQualifiedKeys(unqualifiedMap, "", ".");
		
		unqualifiedMap.clear();
		unqualifiedMap = MapValueConv.fromFullyQualifiedKeys(qualifiedMap);
		
		assertNotNull(unqualifiedMap);
		assertEquals("1", unqualifiedMap.get("agentID"));
		
		assertTrue(unqualifiedMap.get("clients") instanceof List);
		List<Object> innerList = (List<Object>) unqualifiedMap.get("clients");
		assertTrue(innerList.size() == 3);
		
		Map<String, Object> innerMap = null;
		innerMap = (Map<String, Object>) innerList.get(0);
		assertEquals("Sam", innerMap.get("name"));
		assertEquals("12345", innerMap.get("nric"));
		
		innerMap = (Map<String, Object>) innerList.get(1);
		assertEquals("Eugene", innerMap.get("name"));
		assertEquals("23456", innerMap.get("nric"));
		
		innerMap = (Map<String, Object>) innerList.get(2);
		assertEquals("Murong", innerMap.get("name"));
		assertEquals("34567", innerMap.get("nric"));
		
	}
	
	@Test
	public void chaosMonkeyFinal() {
		File chaosMonkeyFile = new File("./test-files/test-specific/conv/chaosmonkey.js");
		String jsonString = "";
		try {
			jsonString = FileUtils.readFileToString(chaosMonkeyFile);
		} catch (Exception ex) {
			
		}
		
		Map<String, Object> jsonMap = ConvertJSON.toMap(jsonString);
		
		Map<String, Object> qualifiedChaosMap = MapValueConv.toFullyQualifiedKeys(jsonMap, "", ".");
		assertNotNull(qualifiedChaosMap);
		
		Map<String, Object> unqualifiedChaosMap = MapValueConv.fromFullyQualifiedKeys(qualifiedChaosMap);
		assertNotNull(unqualifiedChaosMap);
		
	}
	
	@Test
	public void listToArrayTest() {
		Map<Object, List<Object>> map = new HashMap<Object, List<Object>>();
		Map<Object, Object[]> mapArrayObj = null;
		map.put("test", null);
		mapArrayObj = MapValueConv.listToArray(map, null);
		assertNotNull(mapArrayObj);
		List<Object> list = new ArrayList<Object>();
		list.add("abc");
		map.put("test", list);
		mapArrayObj = MapValueConv.listToArray(map, new String[] { "abc" });
	}
	
	@Test
	public void singleToArrayTest() {
		Map<Object, Object> map = new HashMap<Object, Object>();
		Map<Object, Object[]> mapArrayObj = null;
		map.put("test", null);
		mapArrayObj = MapValueConv.singleToArray(map, new String[] {});
		assertNotNull(mapArrayObj);
		map.put("test", "abc");
		mapArrayObj = MapValueConv.singleToArray(map, new String[] { "abc" });
		assertNotNull(mapArrayObj);
	}
	
	@Test
	public void toFullyQualifiedKeysTest() {
		assertNotNull(MapValueConv.toFullyQualifiedKeys(123, null, new String()));
		assertNotNull(MapValueConv.toFullyQualifiedKeys("abc", null, new String()));
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("abc", "xyz");
		assertNotNull(MapValueConv.toFullyQualifiedKeys(map, null, new String()));
		assertNotNull(MapValueConv.toFullyQualifiedKeys(map, "abc", new String()));
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		map = new HashMap<String, Object>();
		map.put("abc", "test");
		list.add(map);
		assertNotNull(MapValueConv.toFullyQualifiedKeys(list, null, new String()));
		
	}
	
	@Test
	public void recreateObjectTest() {
		
		Map<String, Object> unqualifiedMap = new HashMap<String, Object>();
		assertNotNull(MapValueConv.fromFullyQualifiedKeys(unqualifiedMap));
		
		unqualifiedMap.put("", "");
		assertNotNull(MapValueConv.fromFullyQualifiedKeys(unqualifiedMap));
		
		unqualifiedMap = new HashMap<String, Object>();
		unqualifiedMap.put("[]", "");
		assertNotNull(MapValueConv.fromFullyQualifiedKeys(unqualifiedMap));
		
		unqualifiedMap = new HashMap<String, Object>();
		unqualifiedMap.put(".", "");
		assertNotNull(MapValueConv.fromFullyQualifiedKeys(unqualifiedMap));
		
		unqualifiedMap = new HashMap<String, Object>();
		unqualifiedMap.put(".[]", "");
		assertNotNull(MapValueConv.fromFullyQualifiedKeys(unqualifiedMap));
		
		unqualifiedMap.put("[].", "");
		assertNotNull(MapValueConv.fromFullyQualifiedKeys(unqualifiedMap));
		
	}
}

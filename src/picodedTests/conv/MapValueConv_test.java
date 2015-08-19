package picodedTests.conv;
import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.lesscss.deps.org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;

import picoded.conv.ConvertJSON;
import picoded.conv.MapValueConv;

public class MapValueConv_test {
	@Test
	public void testTo(){
		Map<String, Object> unqualifiedMap = new HashMap<String, Object>();
		
		File unqualifiedMapFile = new File("./test-files/test-specific/conv/unqualifiedMap.js");
		String jsonString = "";
		try{
			jsonString = FileUtils.readFileToString(unqualifiedMapFile);
		}catch(Exception ex){
			
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
	public void testFrom(){
		Map<String, Object> unqualifiedMap = new HashMap<String, Object>();
		
		File unqualifiedMapFile = new File("./test-files/test-specific/conv/unqualifiedMap.js");
		String jsonString = "";
		try{
			jsonString = FileUtils.readFileToString(unqualifiedMapFile);
		}catch(Exception ex){
			
		}
		unqualifiedMap = ConvertJSON.toMap(jsonString);
		
		Map<String, Object> qualifiedMap = MapValueConv.toFullyQualifiedKeys(unqualifiedMap, "", ".");
		
		unqualifiedMap.clear();
		unqualifiedMap = MapValueConv.fromFullyQualifiedKeys(qualifiedMap);
		
		assertNotNull(unqualifiedMap);
		assertEquals("1", unqualifiedMap.get("agentID"));
		
		assertTrue(unqualifiedMap.get("clients") instanceof List);
		List<Object> innerList = (List<Object>)unqualifiedMap.get("clients");
		assertTrue(innerList.size() == 3);
		
		
		Map<String, Object> innerMap = null;
		innerMap = (Map<String, Object>)innerList.get(0);
		assertEquals("Sam", innerMap.get("name"));
		assertEquals("12345", innerMap.get("nric"));
		
		innerMap = (Map<String, Object>)innerList.get(1);
		assertEquals("Eugene", innerMap.get("name"));
		assertEquals("23456", innerMap.get("nric"));
		
		innerMap = (Map<String, Object>)innerList.get(2);
		assertEquals("Murong", innerMap.get("name"));
		assertEquals("34567", innerMap.get("nric"));
	}
	
	@Test
	public void chaosMonkeyFinal(){
		File chaosMonkeyFile = new File("./test-files/test-specific/conv/chaosmonkey.js");
		String jsonString = "";
		try{
			jsonString = FileUtils.readFileToString(chaosMonkeyFile);
		}catch(Exception ex){
			
		}
		
		Map<String, Object> jsonMap = ConvertJSON.toMap(jsonString);
		
		Map<String, Object> qualifiedChaosMap = MapValueConv.toFullyQualifiedKeys(jsonMap, "", ".");
		assertNotNull(qualifiedChaosMap);
		
		Map<String, Object> unqualifiedChaosMap = MapValueConv.fromFullyQualifiedKeys(qualifiedChaosMap);
		assertNotNull(unqualifiedChaosMap);
		assertTrue(jsonMap.equals(unqualifiedChaosMap));
		 
	}
}

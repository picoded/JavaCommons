package picodedTests.JStack;

// Target test class
import picoded.JSql.*;
import picoded.JSql.struct.*;
import picoded.JStruct.*;
import picoded.JStruct.internal.*;
import picoded.JStack.*;

// Test Case include
import org.junit.*;
import static org.junit.Assert.*;

// Test depends
import java.nio.charset.Charset;
import java.lang.String;
import java.io.UnsupportedEncodingException;
import java.util.*;

import com.hazelcast.config.*;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

import picoded.JCache.*;
import picodedTests.JCache.LocalCacheSetup;
import picodedTests.JStruct.*;
import picodedTests.*;

public class MetaTable_JStruct_Sqlite_test extends MetaTable_test {
	
	/// To override for implementation
	///------------------------------------------------------
	
	// The stack layers
	public JStackLayer[] stackLayers() {
		return new JStackLayer[] { new JStruct(), JSql.sqlite() };
	}
	
	// Tablename to string
	public String tableName = TestConfig.randomTablePrefix();
	
	// JStack layering
	public JStack jsObj = null;
	
	// Implementation
	public MetaTable implementationConstructor() {
		jsObj = new JStack(stackLayers());
		return jsObj.getMetaTable("MT_" + tableName);
	}
	
	// Issue reported specific bugs
	//-----------------------------------------------
	
	@Test
	public void T536_stackDeltaSaveTest() {
		// JStack layers
		JStruct[] structLayers = jsObj.structLayers();
		
		// Source of truth layer
		JStruct sourceOfTruth = structLayers[structLayers.length - 1];
		MetaTable trueMetaTable = sourceOfTruth.getMetaTable("MT_" + tableName);
		
		//
		// Originally mainipulate the data within source of truth session
		//
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("name", "Hello");
		data.put("data", "one");
		
		MetaObject originalPut = trueMetaTable.newObject();
		originalPut.putAll(data);
		originalPut.saveDelta();
		
		MetaObject secondGet = null;
		assertNotNull(secondGet = trueMetaTable.get(originalPut._oid()));
		assertEquals(secondGet.keySet(), originalPut.keySet());
		
		//
		// Makes changes afterwards
		//
		Map<String, Object> delta = new HashMap<String, Object>();
		delta.put("data", "two");
		MetaObject thirdDelta = mtObj.get(originalPut._oid());
		thirdDelta.putAll(delta);
		thirdDelta.saveDelta();
		assertEquals(secondGet.keySet(), originalPut.keySet());
		
		MetaObject thirdGet = mtObj.get(originalPut._oid());
		assertEquals(secondGet.keySet(), thirdGet.keySet());
	}
	
}

package picodedTests.JStack;

import org.junit.*;
import static org.junit.Assert.*;
import java.util.*;

import picoded.JSql.*;
import picoded.JCache.*;
import picoded.JStack.*;
import picoded.conv.GUID;
import picoded.struct.CaseInsensitiveHashMap;

import java.util.Random;
import org.apache.commons.lang3.RandomUtils;

import com.hazelcast.config.*;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

import picodedTests.JCache.LocalCacheSetup;
import picodedTests.TestConfig;

///
/// Testing using the Hazelcast as the JCache layer, and sqlite as the JSql layer
///
/// ## IMPORTANT to note:
///
/// + JStack contains both JSql, and JCache, with JCache first (on the top, index 0)
/// + MetaTable/Object writes to the JStack from the lowest object upwards (JSQL, then JCache)
/// + MetaTable/Object reads from the Jstack from the highest object downards (Jcache, then JSql)
/// + Query functions, calls the JSql layer only, as JCache does not support query searchs
/// + Every meta object, is stored as a COMPLETE object inside JCache.getMap( tablename )
///     -> Map< ObjectID, Object>, where object is Map< KeyID, Value >
///
public class MetaTable_Hazelcast_test extends MetaTable_Sqlite_test {
	
	// JStack objects functions to over-ride
	//-----------------------------------------------
	@Override
	public JCache JCacheObj() {
		return JCache.hazelcast( hazelcastSetup() );
	}
	
	///////////////////////////////////////////////////////////////////////////////////
	//
	// Intentional failure tests
	//
	// The following test cases are designed to intentionally operations in ways it was
	// designed NOT to be used. Such as modifying the JSql layer directly, and skipping
	// the JCache layer. This is used to test the readers / writers are accessing the
	// correct layers
	//
	///////////////////////////////////////////////////////////////////////////////////
	
	@Test
	public void readLayerCheck() throws JStackException {
		
		// Read operation as per "normal"
		HashMap<String, Object> objMap = new HashMap<String,Object>();
		objMap.put("str_val", "^_^");
		objMap.put("num", 123);
		
		String guid = GUID.base58();
		assertNull(mtObj.get(guid));
		assertEquals(guid, mtObj.append(guid, objMap)._oid());
		
		objMap.put("oid", guid);
		assertEquals(objMap, mtObj.get(guid));
		
		// Access only the JSql layer
		JStack sqlStack = new JStack(JSqlObj);
		MetaTable sqlMtObj = new MetaTable(sqlStack, mtTableName );
		
		// Checks the JSql layer only
		assertEquals(objMap, sqlMtObj.get(guid));
		
		// "Corrupting" the lower JSql layer
		HashMap<String, Object> corruptObjMap = new HashMap<String, Object>( objMap );
		corruptObjMap.put("str_val", "X_X");
		
		assertEquals(guid, sqlMtObj.append(guid, corruptObjMap)._oid()); //corrupt it
		
		assertNotEquals(objMap, sqlMtObj.get(guid)); //check for coorect corruption
		assertEquals(corruptObjMap, sqlMtObj.get(guid)); //check for coorect corruption
		
		// Checks that the JCache layer is not corrupted
		assertEquals(objMap, mtObj.get(guid));
	}
	
}
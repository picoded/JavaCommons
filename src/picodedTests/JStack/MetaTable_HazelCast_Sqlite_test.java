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

public class MetaTable_HazelCast_Sqlite_test extends MetaTable_test {
	
	/// JCache hazelcast setup
	///------------------------------------------------------
	
	static protected String hazelcastClusterName;
	static protected ClientConfig hazelcastConfig;
	
	static protected JCache hazelcastJCacheObj = null;
	
	/// Setsup the testing server
	@BeforeClass
	public static void oneTimeSetUp() {
		// one-time initialization code
		hazelcastClusterName = LocalCacheSetup.setupHazelcastServer();
		
		// Config to use
		hazelcastConfig = new ClientConfig();
		hazelcastConfig.getGroupConfig().setName(hazelcastClusterName);
		hazelcastConfig.setProperty("hazelcast.logging.type", "none");
		
	}
	
	/// Dispose the testing server
	@AfterClass
	public static void oneTimeTearDown() {
		
		// Close JCache if needed (reduce false error)
		if (hazelcastJCacheObj != null) {
			hazelcastJCacheObj.dispose();
			hazelcastJCacheObj = null;
		}
		
		try {
			Thread.sleep(5000);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		// one-time cleanup code
		LocalCacheSetup.teardownHazelcastServer();
	}
	
	/// To override for implementation
	///------------------------------------------------------
	
	// The stack layers
	public JStackLayer[] stackLayers() {
		return new JStackLayer[] { JCache.hazelcast(hazelcastConfig), JSql.sqlite() };
	}
	
	// Tablename to string
	public String tableName = TestConfig.randomTablePrefix();
	
	// Implementation
	public MetaTable implementationConstructor() {
		JStack jsObj = new JStack(stackLayers());
		return jsObj.getMetaTable("MT_" + tableName);
	}
	
}

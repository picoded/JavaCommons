package picodedTests.JCache.struct;

// Target test class
import picoded.JCache.*;
import picoded.JCache.struct.*;
import picoded.JStruct.*;
import picoded.JStruct.internal.*;

// Test Case include
import org.junit.*;
import static org.junit.Assert.*;

// Test depends
import java.nio.charset.Charset;
import java.lang.String;
import java.io.UnsupportedEncodingException;
import java.util.*;

import picodedTests.JStruct.*;
import picodedTests.JCache.LocalCacheSetup;
import picodedTests.*;

import com.hazelcast.config.*;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

public class MetaTable_Hazelcast_test extends MetaTable_test {
	
	/// JCache setup / teardown
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
	
	public JCache jCacheObj() {
		return JCache.hazelcast(hazelcastConfig);
	}
	
	public String tableName = TestConfig.randomTablePrefix();
	
	public MetaTable implementationConstructor() {
		JCacheStruct jcObj = new JCacheStruct(jCacheObj());
		return jcObj.getMetaTable("KVM_" + tableName);
	}
	
}

package picodedTests.libs;

// Target test class
import com.hazelcast.core.*;
import com.hazelcast.config.*;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

// Test Case include
import org.junit.*;
import static org.junit.Assert.*;

// Classes used in test case
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Map;
import java.util.Queue;

import picodedTests.TestConfig;

///
/// Test Case for Hazelcast basic functionality, this is to ensure the library "is working"
///
public class HazelcastSimple_test {

	@Before
	public void setUp() {

	}

	@After
	public void tearDown() {

	}

	///
	/// Test the basic memory cache put and get
	///
	@Test
	public void basicPutAndGet() {

		String clusterName = TestConfig.randomTablePrefix();

		Config clusterConfig = new Config();
		clusterConfig.getGroupConfig().setName(clusterName);
		clusterConfig.setProperty("hazelcast.logging.type", "none");

		ClientConfig clientConfig = new ClientConfig();
		clientConfig.getGroupConfig().setName(clusterName);
		clientConfig.setProperty("hazelcast.logging.type", "none");

		HazelcastInstance instance = Hazelcast.newHazelcastInstance(clusterConfig);
		Map<Integer, String> mapCustomers = instance.getMap("customers");
		mapCustomers.put(1, "Joe");

		HazelcastInstance client = HazelcastClient.newHazelcastClient(clientConfig);
		Map<Integer, String> map = client.getMap("customers");

		assertEquals(1, map.size());
		assertEquals("Joe", map.get(1));

	}
}
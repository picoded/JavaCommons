package picodedTests.features;

// Target test class
import picoded.JCache.*;
import picoded.JCache.embedded.*;
import picoded.conv.*;

// Test Case include
import org.junit.*;
import static org.junit.Assert.*;

// Classes used in test case
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;
import java.util.logging.Logger;
import java.io.File;

import picodedTests.TestConfig;
import picodedTests.JCache.LocalCacheSetup;

///
/// Test feature implementation of elasticsearch
///
public class ElasticsearchRemote_test extends Elasticsearch_test {
	
	/// Use a remote setup client
	public void setupClient() {
		//debuggingSleep();
		client = new ElasticsearchClient(clusterName, "localhost", 9300);
	}
	
}

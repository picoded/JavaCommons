package picoded.JCache.dataStore;

import java.util.Queue;
import java.util.concurrent.locks.Lock;

import picoded.JCache.JCache;
import picoded.JCache.JCacheException;
import picoded.JCache.JCacheMap;
import picoded.JCache.JCacheType;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.core.HazelcastInstance;

/// Implements the JCache interface for hazelcast. Note that this uses hazelcast library
/// internally to handle the actual implementations.
///
/// @see picoded.JCache.JCache
/// @see https://github.com/mrniko/redisson/wiki/Usage-examples
///
public class JCache_hazelcast extends JCache {
	
	/// Overwrite as redis
	public JCacheType cacheType = JCacheType.hazelcast;
	
	/// Logger (if needed)
	//private static Logger logger = Logger.getLogger(JCache_redis.class.getName());
	
	/// redisson config setting, used for server setup
	protected ClientConfig hazelcastConfig = null;
	
	/// The internal redisson object
	protected HazelcastInstance hazelcastObj = null;
	
	/// Setsup Redis client, in single server mode
	///
	/// @param  clusterName  Local Network cluster name for hazelcast
	public JCache_hazelcast(String clusterName) {
		hazelcastConfig = new ClientConfig();
		hazelcastConfig.getGroupConfig().setName(clusterName);
		hazelcastConfig.setProperty("hazelcast.logging.type", "none");
		
		//hazelcastConfig.useSingleServer().setAddress(ipAddressWithPort);
		
		recreate(true);
	}
	
	/// Setsup Redis client, in single server mode
	///
	/// @param  clusterName  Local Network cluster name for hazelcast
	/// @param password password of Local Network cluster
	public JCache_hazelcast(String clusterName, String password) {
		hazelcastConfig = new ClientConfig();
		hazelcastConfig.getGroupConfig().setName(clusterName);
		hazelcastConfig.getGroupConfig().setPassword(password);
		hazelcastConfig.setProperty("hazelcast.logging.type", "none");
		recreate(true);
	}
	
	/// Setsup Redis client, in single server mode
	///
	/// @param  clusterName  Local Network cluster name for hazelcast
	/// @param password password of Local Network cluster
	/// @param ipAddressWithPort IP address and Port number of the Cluster Name
	public JCache_hazelcast(String clusterName, String password, String ipAddressWithPort) {
		hazelcastConfig = new ClientConfig();
		hazelcastConfig.getGroupConfig().setName(clusterName);
		hazelcastConfig.getGroupConfig().setPassword(password);
		hazelcastConfig.getNetworkConfig().addAddress(ipAddressWithPort);
		hazelcastConfig.setProperty("hazelcast.logging.type", "none");
		recreate(true);
	}
	
	/// Setsup Redis client, using more complex config via redissonConfigObject
	/// You may refer to the source material for more complex setups
	///
	/// @see https://github.com/mrniko/redisson/wiki/Config-examples
	/// @see https://github.com/mrniko/redisson/blob/master/src/main/java/org/redisson/Config.java
	///
	/// @param  hazelcastConfig  hazelcast based config object
	public JCache_hazelcast(com.hazelcast.client.config.ClientConfig hazelcastConfigObj) {
		hazelcastConfig = hazelcastConfigObj;
		
		recreate(true);
	}
	
	/// Recreates the JCache connection if it has already been disposed of.
	/// Option to forcefully recreate the connection if needed.
	///
	/// This is also used internally using constructors
	///
	/// @param  force  Boolean if true, forces the existing connection to terminate and be recreated
	public void recreate(boolean force) {
		if (force) {
			if (!isDisposed()) {
				dispose();
			}
		} else {
			if (hazelcastObj != null) {
				return; //already created, skips
			}
		}
		
		//Recreates as needed
		hazelcastObj = HazelcastClient.newHazelcastClient(hazelcastConfig);
	}
	
	/// Gets a ConcurrentMap with the given name
	///
	/// @param  name  The concurrent map storage name
	public <K, V> JCacheMap<K, V> getMap(String name) throws JCacheException {
		throwIfIsDispose();
		return new JCacheMap<K, V>(hazelcastObj.getMap(name));
	}
	
	/// Gets a distributed concurrent lock
	///
	/// @param  name  The concurrent lock name
	public Lock getLock(String name) throws JCacheException {
		throwIfIsDispose();
		return hazelcastObj.getLock(name);
	}
	
	/// Gets a distributed concurrent Queue
	///
	/// @param  name  The concurrent Queue name
	public <K> Queue<K> getQueue(String name) throws JCacheException {
		throwIfIsDispose();
		return hazelcastObj.getQueue(name);
	}
	
	/// Returns true, if dispose() function was called prior
	///
	/// @return boolean value, where true indicates the current cache connection has been terminated.
	public boolean isDisposed() {
		return (hazelcastObj == null);
	}
	
	/// Dispose the connection unless it has already been disposed
	public void dispose() {
		if (hazelcastObj != null) {
			hazelcastObj.shutdown(); //shutdown the hazelcast connection
			hazelcastObj = null;
		}
	}
}

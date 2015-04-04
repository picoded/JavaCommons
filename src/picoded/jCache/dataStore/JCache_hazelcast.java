package picoded.jCache.dataStore;

import picoded.jCache.*;
import picoded.jCache.dataStore.BaseInterface;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;
import java.util.Queue;

import com.hazelcast.core.*;
import com.hazelcast.config.*;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

/// Implements the jCache interface for hazelcast. Note that this uses hazelcast library
/// internally to handle the actual implementations.
///
/// @see picoded.jCache.JCache
/// @see https://github.com/mrniko/redisson/wiki/Usage-examples
///
public class JCache_hazelcast extends JCache implements BaseInterface {
	
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
	public <K, V> ConcurrentMap<K, V> getMap(String name) throws JCacheException {
		throwIfIsDispose();
		return hazelcastObj.getMap(name);
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

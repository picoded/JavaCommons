package picoded.JCache.dataStore;

import picoded.JCache.*;
import picoded.JCache.dataStore.BaseInterface;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;
import java.util.Queue;

import org.redisson.*;

/// Implements the JCache interface for redis. Note that this uses redisson library
/// internally to handle the actual implementations.
///
/// @see picoded.JCache.JCache
/// @see https://github.com/mrniko/redisson/wiki/Usage-examples
///
public class JCache_redis extends JCache {
	
	/// Overwrite as redis
	public JCacheType cacheType = JCacheType.redis;
	
	/// Logger (if needed)
	//private static Logger logger = Logger.getLogger(JCache_redis.class.getName());
	
	/// redisson config setting, used for server setup
	protected Config redissonConfig = null;
	
	/// The internal redisson object
	protected Redisson redissonObj = null;
	
	/// Setsup Redis client, in single server mode
	///
	/// @param  ipAddressWithPort  IP address string, with port. eg:"127.0.0.1:6379"
	public JCache_redis(String ipAddressWithPort) {
		redissonConfig = new Config();
		redissonConfig.useSingleServer().setAddress(ipAddressWithPort);
		
		recreate(true);
	}
	
	/// Setsup Redis client, using more complex config via redissonConfigObject
	/// You may refer to the source material for more complex setups
	///
	/// @see https://github.com/mrniko/redisson/wiki/Config-examples
	/// @see https://github.com/mrniko/redisson/blob/master/src/main/java/org/redisson/Config.java
	///
	/// @param  redissonConfigObj  redisson based config object
	public JCache_redis(org.redisson.Config redissonConfigObj) {
		redissonConfig = redissonConfigObj;
		
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
			if (redissonObj != null) {
				return; //already created, skips
			}
		}
		
		//Recreates as needed
		redissonObj = Redisson.create(redissonConfig);
	}
	
	/// Gets a ConcurrentMap with the given name
	///
	/// @param  name  The concurrent map storage name
	public <K, V> ConcurrentMap<K, V> getMap(String name) throws JCacheException {
		throwIfIsDispose();
		return redissonObj.getMap(name);
	}
	
	/// Gets a distributed concurrent lock
	///
	/// @param  name  The concurrent lock name
	public Lock getLock(String name) throws JCacheException {
		throwIfIsDispose();
		return redissonObj.getLock(name);
	}
	
	/// Gets a distributed concurrent Queue
	///
	/// @param  name  The concurrent Queue name
	public <K> Queue<K> getQueue(String name) throws JCacheException {
		throwIfIsDispose();
		return redissonObj.getQueue(name);
	}
	
	/// Returns true, if dispose() function was called prior
	///
	/// @return boolean value, where true indicates the current cache connection has been terminated.
	public boolean isDisposed() {
		return (redissonObj == null);
	}
	
	/// Dispose the connection unless it has already been disposed
	public void dispose() {
		if (redissonObj != null) {
			redissonObj.shutdown(); //shutdown the redission connection
			redissonObj = null;
		}
	}
}

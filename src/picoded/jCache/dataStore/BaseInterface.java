package picoded.jCache.dataStore;

import picoded.jSql.JSqlResult;
import picoded.jSql.JSqlException;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.SynchronousQueue;
import java.util.List;

/// Standardised jCache interface for jCache functionalities.
/// in which additional server cache interface types are implemented.
///
/// Note that the interface intentionaly does not include a "constructor" as that may be cache implementation dependent
public interface BaseInterface {
	
	// Internal refrence of the current sqlType the system is running as, or so it should be by default
	//public JCacheType cacheType  = JCacheType;
	
	/// Returns true, if dispose() function was called prior
	public boolean isDisposed();
	
	/// Dispose of the respective SQL driver / connection
	public void dispose();
	
	/// Gets a ConcurrentMap with the given name
	ConcurrentMap<String, ?> getMap(String name);
	
	/// Gets the distributed list
	List<?> getList(String name);
	
	/// Gets the cache queue list
	SynchronousQueue<?> getQueue(String name);
	
	/// Gets the cache locking machnesim name
	Lock getLock(String name);
	
	/// Recreates the JCache connection if it has already been disposed of. Option to forcefully recreate the connection if needed.
	public void recreate(boolean force);
	
	/*
	 // Just incase a user forgets to dispose "as per normal"
	 //protected void finalize();
	 */
}
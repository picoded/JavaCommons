package picoded.objectSetDB;

import picoded.objectSetDB.*;
import picoded.objectSetDB.internal.*;
import picoded.jSql.*;
import picoded.jCache.*;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.Map;
import java.util.Set;
import java.util.List;
import java.util.Collections;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Iterator;

///
/// An object map. this represents an object, found in an object set.
/// And does the final interface with the DB
///
public class ObjectMap extends AbstractMap<String, Object> {
	
	/// Internal data stack of ObjectSetDB (shared across subclasses)
	protected DataStack dStack;
	
	/// Internal set name
	protected String sName;
	protected String mName;
	protected ObjectSet pSet;
	
	/// Returns the "safe", protected setName
	public String setName() {
		return sName;
	}
	
	/// Returns the "safe", protected map ID
	public String mapID() {
		return mName;
	}
	
	/// Returns the parent set
	public ObjectSet parentSet() {
		return pSet;
	}
	
	/// Constructor for the ObjectSet, this should not be called directly, except via ObjectSetDB
	public ObjectMap(String setName, String objID, DataStack dObj, ObjectSet parent) {
		sName = setName;
		mName = objID;
		dStack = dObj;
		pSet = parent;
		
		setupJCacheLocks();
	}
	
	///----------------------------------------
	/// JCache lock handling
	///----------------------------------------
	
	protected Lock[] JCacheLockArray = null;
	protected boolean[] JCacheLockState = null;
	
	protected void setupJCacheLocks() {
		int l = dStack.ACID_JCache.length;
		JCacheLockArray = new Lock[l];
		JCacheLockState = new boolean[l];
		
		try {
			for (int a = 0; a < l; ++a) {
				JCacheLockArray[a] = dStack.ACID_JCache[a].getLock("OL$" + sName + "$" + mName);
				JCacheLockState[a] = false;
			}
		} catch (JCacheException e) {
			throw new RuntimeException(e);
		}
	}
	
	/// Performs unlock on all the map locks, from the innermost to out.
	/// ForcePt is used to override the default check for "locked", use -1 for default behaviour
	protected void unlock(int forcePt) {
		int l = JCacheLockArray.length;
		for (int a = (l - 1); a >= 0; --a) {
			if (a <= forcePt || JCacheLockState[a] == true) {
				JCacheLockArray[a].unlock();
				JCacheLockState[a] = false;
			}
		}
	}
	
	/// Performs unlock on all the map locks, from the innermost to out.
	/// This uses ForcePt default value of -1
	protected void unlock() {
		unlock(-1);
	}
	
	/// Try to acquire a lock for the entire object map, with milliseconds timeout
	protected boolean tryLock(long milliseconds) {
		long startTime = System.currentTimeMillis();
		int lockProgress = 0;
		boolean ret = false;
		
		try {
			for (; lockProgress < JCacheLockArray.length; ++lockProgress) {
				
				long lockTime = System.currentTimeMillis();
				//long timeSpent = (startTime - lockTime);
				long timeLeft = milliseconds - (startTime - lockTime);
				
				//Terminates the lock process
				if (timeLeft <= 0 || //no time left
					!JCacheLockArray[lockProgress].tryLock(timeLeft, TimeUnit.MILLISECONDS) //failed lock
				) {
					--lockProgress;
					break;
				} else {
					JCacheLockState[lockProgress] = true;
				}
			}
		} catch (InterruptedException e) {
			//let finally block handle
		} finally {
			if (lockProgress >= JCacheLockArray.length) {
				ret = true;
			} else {
				/// Lock acquisition failed, rolls back
				for (; lockProgress >= 0; --lockProgress) {
					JCacheLockArray[lockProgress].unlock();
					JCacheLockState[lockProgress] = false;
				}
				ret = false;
			}
		}
		return ret;
	}
	
	protected void lockOrException(long milliseconds) throws ObjectSetException {
		if (!tryLock(milliseconds)) {
			throw new ObjectSetException("ObjectMap lock timeout (" + milliseconds + ")");
		}
	}
	
	///----------------------------------------
	/// JCache data layer fetching
	///----------------------------------------
	protected HashMap<String, Object> JCache_dataCopy = null;
	protected boolean JCache_dataCopyChanged = false;
	
	protected void setupJCacheDataCopy() throws ObjectSetException {
		/// Fetch the current cached copy
		if (JCache_dataCopy == null) {
			JCache_dataCopy = getRawObjMapVia_ACID_JCache();
			if (JCache_dataCopy == null) {
				JCache_dataCopy = new HashMap<String, Object>();
			}
			JCache_dataCopyChanged = false;
		}
	}
	
	/// Gets the object from jCache layer
	protected HashMap<String, Object> getRawObjMapVia_ACID_JCache() throws ObjectSetException {
		try {
			HashMap<String, Object> r = null;
			JCache[] ACID_JCache = dStack.ACID_JCache;
			for (int a = 0; a < ACID_JCache.length; ++a) {
				ConcurrentMap<String, HashMap<String, Object>> setMap = ACID_JCache[a].getMap("OS$" + sName);
				
				r = setMap.get(mName);
				if (r != null) {
					return r;
				}
			}
			return null;
		} catch (JCacheException e) {
			throw new ObjectSetException(e);
		}
	}
	
	///----------------------------------------
	/// JCache syncronizing
	///----------------------------------------
	protected void updateCacheCopyVia_ACID_JCache_withoutLocking(boolean forceUpdate) throws ObjectSetException {
		setupJCacheDataCopy();
		
		try {
			if (JCache_dataCopyChanged || forceUpdate) {
				HashMap<String, Object> existing = getRawObjMapVia_ACID_JCache();
				HashMap<String, Object> dataCopy = JCache_dataCopy;
				
				if (existing == null) {
					JCache_dataCopy = dataCopy;
				} else {
					// @TODO multi-thread safe
					// This is currently thread unsafe, as race conditions can occur (even with locks)
					// as the hashmap merger currently DOES NOT handle conlfict resolution
					existing.putAll(dataCopy);
					JCache_dataCopy = existing;
				}
				
				JCache[] ACID_JCache = dStack.ACID_JCache;
				for (int a = 0; a < ACID_JCache.length; ++a) {
					ConcurrentMap<String, HashMap<String, Object>> setMap = ACID_JCache[a].getMap("OS$" + sName);
					setMap.put(mName, JCache_dataCopy);
				}
			}
		} catch (JCacheException e) {
			throw new ObjectSetException(e);
		}
	}
	
	protected void updateCacheCopyVia_ACID_JCache_withLocking(boolean forceUpdate) throws ObjectSetException {
		if (JCache_dataCopyChanged || forceUpdate) {
			try {
				// Acquire object lock first, throws an exception if failed
				lockOrException(dStack.cacheLockTimeout);
				
				updateCacheCopyVia_ACID_JCache_withoutLocking(forceUpdate);
			} finally {
				unlock();
			}
		}
	}
	
	///----------------------------------------
	/// JCache auto syncronizing
	///----------------------------------------
	protected boolean allowAutoCacheSync = false;
	
	/// Returns the current setting
	public boolean autoCacheSync() {
		return allowAutoCacheSync;
	}
	
	/// Returns the original setting
	public boolean autoCacheSync(boolean enable) throws ObjectSetException {
		boolean ori = allowAutoCacheSync;
		allowAutoCacheSync = enable;
		runAutoCacheSync();
		
		return ori;
	}
	
	public void runAutoCacheSync() throws ObjectSetException {
		if (allowAutoCacheSync && JCache_dataCopyChanged) {
			updateCacheCopyVia_ACID_JCache_withLocking(false);
		}
	}
	
	///----------------------------------------
	/// JCache local copy handling
	///----------------------------------------
	
	/// Gets the raw value from jCache layer
	protected Object getRawVia_ACID_JCacheCopy(String meta, int idx) throws ObjectSetException {
		setupJCacheDataCopy();
		
		/// Prefix for values
		String mPrefix = meta + "$" + idx + "$";
		
		/// Expire value check
		Number expireObj = (Number) (JCache_dataCopy.get(mPrefix + "e"));
		long expireVal = (expireObj != null) ? (expireObj.longValue()) : -1;
		
		if (expireVal > 10 && expireVal < (System.currentTimeMillis() / 1000L)) {
			return null;
		}
		
		/// Actual value get
		Object valueObj = (JCache_dataCopy.get(mPrefix + "v"));
		if (valueObj == null) {
			return null;
		}
		
		/// Value type get
		Number typeObj = (Number) (JCache_dataCopy.get(mPrefix + "t"));
		int vTyp = (typeObj != null) ? (typeObj.intValue()) : -1;
		
		if (vTyp == 1) {
			if (valueObj instanceof String) {
				return valueObj;
			}
		} else if (valueObj instanceof Number) {
			if (vTyp == 2) {
				return new Long(((Number) valueObj).longValue());
			} else if (vTyp == 3) {
				return new Double(((Number) valueObj).doubleValue());
			} else if (vTyp == 4) {
				return new Float(((Number) valueObj).floatValue());
			}
		}
		
		// No data : return null
		return null;
	}
	
	/// Inserts the raw value into the JSql stack
	protected void putRawVia_ACID_JCacheCopy(String meta, int idx, Object val, long expireUnixTime, long updateTime,
		long createTime) throws ObjectSetException {
		setupJCacheDataCopy();
		
		/// Prefix for values
		String mPrefix = meta + "$" + idx + "$";
		
		int vTyp = 0;
		Object vVal = null;
		
		if (val == null) {
			vTyp = 0;
		} else if (String.class.isInstance(val)) {
			vTyp = 1;
			vVal = val.toString();
		} else if (Integer.class.isInstance(val) || Long.class.isInstance(val)) {
			vTyp = 2;
			vVal = (new Long(((Number) val).longValue()));
		} else if (Double.class.isInstance(val)) {
			vTyp = 3;
			vVal = (new Double(((Number) val).doubleValue()));
		} else if (Float.class.isInstance(val)) {
			vTyp = 4;
			vVal = (new Float(((Number) val).floatValue()));
		} else {
			String valClassName = val.getClass().getName();
			throw new ObjectSetException("Unknown value object type : " + (valClassName));
		}
		
		JCache_dataCopy.put(mPrefix + "e", new Long(expireUnixTime));
		JCache_dataCopy.put(mPrefix + "c", new Long(createTime));
		JCache_dataCopy.put(mPrefix + "u", new Long(updateTime));
		JCache_dataCopy.put(mPrefix + "t", new Integer(vTyp));
		JCache_dataCopy.put(mPrefix + "v", vVal);
		
		JCache_dataCopyChanged = true;
	}
	
	/// Removed value from cache copy, to force cache recreate?
	protected void removeVia_ACID_JCacheCopy(String meta, int idx) throws ObjectSetException {
		setupJCacheDataCopy();
		
		/// Prefix for values
		String mPrefix = meta + "$" + idx + "$";
		
		JCache_dataCopy.remove(mPrefix + "e");
		JCache_dataCopy.remove(mPrefix + "c");
		JCache_dataCopy.remove(mPrefix + "u");
		JCache_dataCopy.remove(mPrefix + "t");
		JCache_dataCopy.remove(mPrefix + "v");
		
		JCache_dataCopyChanged = true;
	}
	
	///----------------------------------------
	/// JSql data layer fetching
	///----------------------------------------
	protected JSqlResult getRawVia_ACID_JSql(String meta, int idx) throws ObjectSetException {
		try {
			ObjectSet_JSql[] ACID_JSqlSet = pSet.ACID_JSqlSet;
			for (int a = 0; a < ACID_JSqlSet.length; ++a) {
				JSqlResult r = ACID_JSqlSet[a].getRaw(mName, meta, idx, 1);
				
				if (r.rowCount() > 0) {
					return r;
				}
			}
			return null;
		} catch (JSqlException e) {
			throw new ObjectSetException(e);
		}
	}
	
	/// Inserts the raw value into the JSql stack
	protected void putRawVia_ACID_JSql(String meta, int idx, Object val, long expireUnixTime, long updateTime)
		throws ObjectSetException {
		try {
			ObjectSet_JSql[] ACID_JSqlSet = pSet.ACID_JSqlSet;
			
			// Updates from inner most to outer most
			for (int a = ACID_JSqlSet.length - 1; a >= 0; --a) {
				ACID_JSqlSet[a].put(mName, meta, idx, val, expireUnixTime, (System.currentTimeMillis() / 1000L));
			}
		} catch (JSqlException e) {
			throw new ObjectSetException(e);
		}
	}
	
	/// No Expire time varient of putRawVia_ACID_JSql
	protected void putRawVia_ACID_JSql(String meta, int idx, Object val) throws ObjectSetException {
		putRawVia_ACID_JSql(meta, idx, val, -1, (System.currentTimeMillis() / 1000L));
	}
	
	///----------------------------------------
	/// Basic get / put functions
	///----------------------------------------
	
	public Object get(String meta) throws ObjectSetException {
		return get(meta, 0);
	}
	
	/// Gets the stored value
	public Object get(String meta, int vIdx) throws ObjectSetException {
		Object ret = getRawVia_ACID_JCacheCopy(meta, vIdx);
		
		if (ret == null) {
			JSqlResult r = getRawVia_ACID_JSql(meta, vIdx);
			ret = ObjectSet_JSql.valueFromRawResult(r, 0);
			
			if (r != null) {
				putRawVia_ACID_JCacheCopy(meta, 0, ret, //
					ObjectSet_JSql.expireValueFromRawResult(r, 0), //
					ObjectSet_JSql.updatedValueFromRawResult(r, 0), //
					ObjectSet_JSql.createdValueFromRawResult(r, 0) //
				);
			}
			
			runAutoCacheSync();
		}
		
		return ret;
	}
	
	/// Overwrite the stored value,
	/// note that this maybe silently ignored if the previous value is identical
	///
	/// !IMPORTANT, DOES NOT GURANTEE RETURN OF PREVIOUS VALUE
	public Object put(String meta, Object value) {
		return put(meta, value, 0);
	}
	
	public Object put(String meta, Object value, int vIdx) {
		boolean cacheSyncConfig = false;
		boolean cacheSyncOri = false;
		try {
			// Acquire object lock first, throws an exception if failed
			lockOrException(dStack.cacheLockTimeout);
			
			cacheSyncOri = autoCacheSync(false);
			cacheSyncConfig = true;
			
			putRawVia_ACID_JSql(meta, vIdx, value);
			
			// Remove to force refetching of result
			removeVia_ACID_JCacheCopy(meta, vIdx);
			
			// Gets and update the cache
			get(meta, vIdx);
			
			// Update the cache cluster
			updateCacheCopyVia_ACID_JCache_withoutLocking(false);
			
		} catch (ObjectSetException e) {
			throw new RuntimeException(e);
		} finally {
			unlock();
			
			// Reconfigure the cache sync settings
			if (cacheSyncConfig && cacheSyncOri) {
				try {
					autoCacheSync(cacheSyncOri);
				} catch (ObjectSetException e) {
					throw new RuntimeException(e);
				}
			}
		}
		return null;
	}
	
	///----------------------------------------
	/// Map compliance
	///----------------------------------------
	@Override
	public Set<Map.Entry<String, Object>> entrySet() {
		throw new RuntimeException("entrySet / Iterator support is not (yet) implemented");
		
		/*
		if (entries == null) {
			entries = new AbstractSet() {
				public void clear() {
					list.clear();
				}

				public Iterator iterator() {
					return list.iterator();
				}

				public int size() {
					return list.size();
				}
			};
		}
		return entries;
		// */
	}
}

package picoded.JCache.struct;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import picoded.JSql.*;
import picoded.JCache.*;
import picoded.JStruct.*;
import picoded.struct.*;

public class JCacheStruct extends JStruct {
	
	/// The JCache object implmentation
	protected JCache jCacheObj = null;
	
	/// Setup with the JCache object
	public JCacheStruct(JCache cache) {
		jCacheObj = cache;
	}
	
	
}

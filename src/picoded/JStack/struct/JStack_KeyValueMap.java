package picoded.JStack.struct;

import java.util.*;
import java.util.logging.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import picoded.struct.*;
import picoded.security.NxtCrypt;
import picoded.JStruct.KeyValueMap;

import picoded.struct.*;
import picoded.JStack.*;
import picoded.JCache.*;
import picoded.JSql.*;
import picoded.JCache.struct.*;
import picoded.JSql.struct.*;
import picoded.conv.*;
import picoded.JStruct.*;
import picoded.JStruct.internal.*;
import picoded.security.NxtCrypt;

import org.apache.commons.lang3.RandomUtils;

public class JStack_KeyValueMap extends JStruct_KeyValueMap {
	
	///
	/// Temporary logger used to make sure incomplete implmentation is noted
	///--------------------------------------------------------------------------
	
	/// Standard java logger
	protected static Logger logger = Logger.getLogger(JStack_KeyValueMap.class.getName());
	
	///
	/// Constructor setup
	///--------------------------------------------------------------------------
	
	/// The inner sql object
	protected JStack stackObj = null;
	
	/// The tablename for the key value pair map
	protected String stackTablename = null;
	
	/// JStack setup
	public JStack_KeyValueMap(JStack inStack, String tablename) {
		super();
		stackObj = inStack;
		stackTablename = tablename;
	}
	
	///
	/// Internal implementation layers handling
	///--------------------------------------------------------------------------
	
	/// The cached structure implmentation layers
	protected KeyValueMap[] _implemenationLayers = null;
	
	/// Getting the implmentation layers
	protected KeyValueMap[] implemenationLayers() {
		if (_implemenationLayers != null) {
			return _implemenationLayers;
		}
		
		// Get the structure layers
		JStruct[] struct = stackObj.structLayers();
		KeyValueMap[] ret = new KeyValueMap[struct.length];
		
		// Fetch their respective key value map
		for (int a = 0; a < struct.length; ++a) {
			
			// Safety check
			if (struct[a] == null) {
				ret[a] = null;
				continue;
			}
			
			// Fetch the implementation
			ret[a] = struct[a].getKeyValueMap(stackTablename);
		}
		
		return (_implemenationLayers = ret);
	}
	
	///
	/// Backend system setup / teardown
	///--------------------------------------------------------------------------
	
	/// Setsup the backend storage table, etc. If needed
	public void systemSetup() {
		for (KeyValueMap i : implemenationLayers()) {
			i.systemSetup();
		}
	}
	
	/// Teardown and delete the backend storage table, etc. If needed
	public void systemTeardown() {
		for (KeyValueMap i : implemenationLayers()) {
			i.systemTeardown();
		}
	}
	
	/// Perform maintenance, mainly removing of expired data if applicable
	public void maintenance() {
		for (KeyValueMap i : implemenationLayers()) {
			i.maintenance();
		}
	}
	
	/// Perform maintenance, mainly removing of expired data if applicable
	public void incrementalMaintenance() {
		for (KeyValueMap i : implemenationLayers()) {
			i.incrementalMaintenance();
		}
	}
	
	/// Perform maintenance, mainly removing of expired data if applicable
	public void clear() {
		for (KeyValueMap i : implemenationLayers()) {
			i.clear();
		}
	}
	
}

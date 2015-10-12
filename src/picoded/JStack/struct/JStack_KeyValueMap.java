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
	
}

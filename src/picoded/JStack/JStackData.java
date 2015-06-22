package picoded.JStack;

/// Java imports
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Collection;

/// Picoded imports
import picoded.conv.GUID;
import picoded.JSql.*;
import picoded.JCache.*;
import picoded.struct.CaseInsensitiveHashMap;

/// JStackData represents a standard JStack data interface, in which common setup, and SQL derivatives are formed
public class JStackData {
	
	//
	// Internal config vars
	//--------------------------------------------------------------------------
	
	/// Internal JStackObj
	protected JStack JStackObj = null;
	
	/// Internal table name, before prefix?
	protected String tableName = "JStackData";
	
	/// SQL Table name, with automated prefix adding
	protected String sqlTableName(JSql sql) {
		return (sql.getTablePrefix() + tableName);
	}
	
	//
	// Constructor setup
	//--------------------------------------------------------------------------
	
	/// Setup the default JStack layer
	public JStackData(JStack inStack) {
		JStackObj = inStack;
	}
	
	/// Setup the metatable with the given stack
	public JStackData(JStack inStack, String inTableName) {
		JStackObj = inStack;
		
		if (inTableName == null) {
			throw new IllegalArgumentException("Invalid table name (null): " + inTableName);
		}
		
		final String numericString = "0123456789";
		if (numericString.indexOf(inTableName.substring(0, 1)) > 0) {
			throw new IllegalArgumentException("Invalid table name (cannot start with numbers): " + inTableName);
		}
		
		tableName = inTableName;
	}
	
	//
	// Extendable JStackIteration template
	//--------------------------------------------------------------------------
	
	/// Extendable JStackReader class, used to iterate the stack
	///
	/// Note at any point, return false terminates the iterations
	protected interface JStackReader {
		
		/// Reads the JStackLayer object (which is everything)
		public default Object readJStackLayer(JStackLayer in, Object ret) throws JStackException {
			return ret;
		}
		
		/// Reads only the JSQL layer
		public default Object readJSqlLayer(JSql sql, Object ret) throws JSqlException, JStackException {
			return ret;
		}
		
		/// Reads only the JCacheLayer
		public default Object readJCacheLayer(JCache jc, Object ret) throws JCacheException, JStackException {
			return ret;
		}
	}
	
	/// Iteration function, used to iterate the entire JStack while automatically handling converting exception to a JStackException
	protected Object JStackIterate(JStackReader readerClass) throws JStackException {
		try {
			Object ret = null;
			JStackLayer[] sl = JStackObj.stackLayers();
			
			for (int a = 0; a < sl.length; ++a) {
				ret = readerClass.readJStackLayer(sl[a], ret);
				
				// JSql specific setup
				if (sl[a] instanceof JSql) {
					ret = readerClass.readJSqlLayer((JSql)sl[a], ret);
				} else if (sl[a] instanceof JCache) {
					ret = readerClass.readJCacheLayer((JCache)sl[a], ret);
				}
			}
			
			return ret;
		} catch (JSqlException e) {
			throw new JStackException(e);
		} catch (JCacheException e) {
			throw new JStackException(e);
		}
	}
	
	/// Same as JStackIterate, but reversed
	protected Object JStackReverseIterate(JStackReader readerClass) throws JStackException {
		try {
			Object ret = null;
			JStackLayer[] sl = JStackObj.stackLayers();
			
			for (int a = sl.length - 1; a >= 0; --a) {
				ret = readerClass.readJStackLayer(sl[a], ret);
				
				// JSql specific setup
				if (sl[a] instanceof JSql) {
					ret = readerClass.readJSqlLayer((JSql)sl[a], ret);
				} else if (sl[a] instanceof JCache) {
					ret = readerClass.readJCacheLayer((JCache)sl[a], ret);
				}
			}
			
			return ret;
		} catch (JSqlException e) {
			throw new JStackException(e);
		} catch (JCacheException e) {
			throw new JStackException(e);
		}
	}
	
	//
	// JStack common setup functions
	//--------------------------------------------------------------------------
	
	/// Performs the full stack setup for the data object
	public void stackSetup() throws JStackException {
		JStackIterate(new JStackReader() {
			public Object readJSqlLayer(JSql in, Object ret) throws JSqlException, JStackException {
				return JSqlSetup(in);
			}
			public Object readJCacheLayer(JCache in, Object ret) throws JCacheException, JStackException {
				return JCacheSetup(in);
			}
		} );
		//return this;
	}
	
	/// Performs the full stack teardown for the data object
	public void stackTeardown() throws JStackException {
		JStackIterate(new JStackReader() {
			public Object readJSqlLayer(JSql in, Object ret) throws JSqlException, JStackException {
				return JSqlTeardown(in);
			}
			public Object readJCacheLayer(JCache in, Object ret) throws JCacheException, JStackException {
				return JCacheTeardown(in);
			}
		} );
		//return this;
	}
	
	//
	// JStack common inner functions, to over-ride
	//--------------------------------------------------------------------------
	
	/// To override: JSql setup
	protected boolean JSqlSetup(JSql in) throws JSqlException, JStackException {
		return true;
	}
	
	/// To override: JCache setup
	protected boolean JCacheSetup(JCache in) throws JCacheException, JStackException {
		return true;
	}
	
	/// To override: JSql teardown
	protected boolean JSqlTeardown(JSql in) throws JSqlException, JStackException {
		return true;
	}
	
	/// To override: JCache teardown
	protected boolean JCacheTeardown(JCache in) throws JCacheException, JStackException {
		return true;
	}
	
	
}
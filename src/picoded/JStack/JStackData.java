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
		public default boolean readJStackLayer(JStackLayer in) throws JStackException {
			return true;
		}
		
		/// Reads only the JSQL layer
		public default boolean readJSqlLayer(JSql in) throws JSqlException, JStackException {
			return true;
		}
		
		/// Reads only the JCacheLayer
		public default boolean readJCacheLayer(JCache in) throws JCacheException, JStackException {
			return true;
		}
	}
	
	/// Iteration function, used to iterate the entire JStack while automatically handling converting exception to a JStackException
	protected boolean JStackIterate(JStackReader readerClass) throws JStackException {
		try {
			JStackLayer[] sl = JStackObj.stackLayers();
			for (int a = 0; a < sl.length; ++a) {
				
				if( readerClass.readJStackLayer(sl[a]) == false ) {
					return false;
				}
				
				// JSql specific setup
				if (sl[a] instanceof JSql) {
					if( readerClass.readJSqlLayer((JSql)sl[a]) == false ) {
						return false;
					}
				} else if (sl[a] instanceof JCache) {
					if( readerClass.readJCacheLayer((JCache)sl[a]) == false ) {
						return false;
					}
				}
			}
		} catch (JSqlException e) {
			throw new JStackException(e);
		} catch (JCacheException e) {
			throw new JStackException(e);
		}
		return true;
	}
	
	//
	// JStack common setup functions
	//--------------------------------------------------------------------------
	
	/// Performs the full stack setup for the data object
	public JStackData stackSetup() throws JStackException {
		JStackIterate(new JStackReader() {
			public boolean readJSqlLayer(JSql in) throws JSqlException, JStackException {
				return JSqlSetup(in);
			}
			public boolean readJCacheLayer(JCache in) throws JCacheException, JStackException {
				return JCacheSetup(in);
			}
		} );
		return this;
	}
	
	/// Performs the full stack teardown for the data object
	public JStackData stackTeardown() throws JStackException {
		JStackIterate(new JStackReader() {
			public boolean readJSqlLayer(JSql in) throws JSqlException, JStackException {
				return JSqlTeardown(in);
			}
			public boolean readJCacheLayer(JCache in) throws JCacheException, JStackException {
				return JCacheTeardown(in);
			}
		} );
		return this;
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
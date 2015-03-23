package picoded.mmData;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import picoded.jSql.*;
import picoded.jCache.*;
import java.util.logging.*;
import org.apache.commons.lang3.StringUtils;

/// mmData.JStack, the centralised setup class for mmData stack. This is where,
/// the various default, and overwrites for JCache and JSql stack is configured
/// and setup.
///
/// *******************************************************************************
///
/// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~{.java}
///
/// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
///
/// *******************************************************************************
///
public class JStack {
	
	//---------------------//
	// Constructor         //
	//---------------------//
	
	/// Shared constructor step
	public void _sharedConstructor(JCache[] jCacheArr, JSql[] jSqlArr) {
		custom_jCacheArr = new HashMap<String, JCache[]>();
		custom_jSqlArr = new HashMap<String, JSql[]>();
		
		base_jCacheArr = (jCacheArr != null) ? jCacheArr : null;
		base_jSqlArr = (jSqlArr != null) ? jSqlArr : null;
	}
	
	/// Constructor with the JCache, and JSql objects
	public JStack(JCache[] jCacheArr, JSql[] jSqlArr) {
		_sharedConstructor(jCacheArr, jSqlArr);
	}
	
	/// Constructor with the JCache, and JSql objects
	public JStack(JCache jCacheObj, JSql jSqlObj) {
		_sharedConstructor( //
			(jCacheObj != null) ? (new JCache[] { jCacheObj }) : null,//
			(jSqlObj != null) ? (new JSql[] { jSqlObj }) : null //
		);
	}
	
	//--------------------------------------------------//
	// Protected variables, containing the stack setup  //
	//--------------------------------------------------//
	public static final JCache[] empty_jCacheArr = new JCache[0];
	public static final JSql[] empty_jSqlArr = new JSql[0];
	
	protected JCache[] base_jCacheArr = null;
	protected JSql[] base_jSqlArr = null;
	
	protected Map<String, JCache[]> custom_jCacheArr = null;
	protected Map<String, JSql[]> custom_jSqlArr = null;
	
	//--------------------------------------------------//
	// set / get the stck config for JCache stack       //
	//--------------------------------------------------//
	
	/// Returns the custom map specfic JCache stack if it set,
	/// Else fallsback to the default stack
	public final JCache[] getJCacheStack(String mapName) {
		return normalizeEmptyJCacheArr( //
		(custom_jCacheArr.containsKey(mapName)) ? //
		custom_jCacheArr.get(mapName)
			: base_jCacheArr //
		);
	}
	
	/// Returns the custom map specfic JCache stack if it set,
	/// Else fallsback to the default stack
	public final JCache[] setJCacheStack(String mapName, JCache[] jCacheArr) {
		return normalizeEmptyJCacheArr(custom_jCacheArr.put(mapName, jCacheArr));
	}
	
	/// Resets the configured JCacheStack to default values, return its previous (non-default) value
	public final JCache[] resetJCacheStack(String mapName) {
		return normalizeEmptyJCacheArr(custom_jCacheArr.remove(mapName));
	}
	
	/// internal function that normalize empty JCacheArr to a consistent value
	protected final JCache[] normalizeEmptyJCacheArr(JCache[] ret) {
		return (ret == null || ret.length <= 0) ? empty_jCacheArr : ret;
	}
	
	//--------------------------------------------------//
	// set / get the stck config for JSql stack         //
	//--------------------------------------------------//
	
	/// Returns the custom map specfic JCache stack if it set,
	/// Else fallsback to the default stack
	public final JSql[] getJSqlStack(String mapName) {
		return normalizeEmptyJSqlArr( //
		(custom_jSqlArr.containsKey(mapName)) ? //
		custom_jSqlArr.get(mapName)
			: base_jSqlArr //
		);
	}
	
	/// Returns the custom map specfic JCache stack if it set,
	/// Else fallsback to the default stack
	public final JSql[] setJSqlStack(String mapName, JSql[] jSqlArr) {
		return normalizeEmptyJSqlArr(custom_jSqlArr.put(mapName, jSqlArr));
	}
	
	/// Resets the configured JCacheStack to default values, return its previous (non-default) value
	public final JSql[] resetJSqlStack(String mapName) {
		return normalizeEmptyJSqlArr(custom_jSqlArr.remove(mapName));
	}
	
	/// internal function that normalize empty JSqlArr to a consistent value
	protected final JSql[] normalizeEmptyJSqlArr(JSql[] ret) {
		return (ret == null || ret.length <= 0) ? empty_jSqlArr : ret;
	}
	
}

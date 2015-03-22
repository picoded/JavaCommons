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
	
	/// Constructor with the JCache, and JSql objects
	public JStack(JCache[] jCacheArr, JSql[] jSqlArr) {
		custom_jCacheArr = new HashMap<String, JCache[]>();
		custom_jSqlArr = new HashMap<String, JSql[]>();
		
		base_jCacheArr = (jCacheArr != null) ? jCacheArr : new JCache[0];
		base_jSqlArr = (jSqlArr != null) ? jSqlArr : new JSql[0];
	}
	
	//--------------------------------------------------//
	// Protected variables, containing the stack setup  //
	//--------------------------------------------------//
	protected JCache[] base_jCacheArr = null;
	protected JSql[] base_jSqlArr = null;
	
	protected Map<String, JCache[]> custom_jCacheArr = null;
	protected Map<String, JSql[]> custom_jSqlArr = null;
	
	//--------------------------------------------------//
	// Protected variables, containing the stack setup  //
	//--------------------------------------------------//
}

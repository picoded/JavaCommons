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
import picoded.struct.UnsupportedDefaultMap;

///
/// MetaTypeMap plays a sub role in the creation and setup 
/// of the MetaTable JSql management. This includes the setup
/// and management of its indexed views, and query generation.
///
///
/// Due to the majority of this code base reliance on the original MetaTable,
/// there is no need for complete code isolation from the MetaTable class.
///
/// Due deligence however should still be done to isolate the 2 class code files
/// whenever it is possible.
///
/// @TODO Support for JCache based value type check and/or restriction handling
///
public class MetaTypeMap extends HashMap<String, MetaType> {
	
	//
	// Constructor and setup vars
	//
	// The consturctor is setup, with refence to the original MetaTable object,
	// As several configuration based variables (like the table name) will
	// be extracted as such from the main class.
	//--------------------------------------------------------------------------

	/// The original main table, used to refence some setup vars
	public MetaTable mainTable = null;
	
	/// Constructor with MetaTable variables
	public MetaTypeMap(MetaTable mTable) {
		mainTable = mTable;
	}
	
	/// The default fallback meta type
	public MetaType defaultType = new MetaType(MetaType.TYPE_MIXED);
	
	//
	// Internal Map mapping
	//
	// This overrides the default Map operations,
	// to help faciltate the simultanious case sensitive and insensitive
	// operations. Such as put, get, remove.
	//--------------------------------------------------------------------------

	/// The inner case insensitive varient
	CaseInsensitiveHashMap<String, MetaType> caseInsensitiveVarient = new CaseInsensitiveHashMap<String, MetaType>();
	
	/// (Case INsensitive) Get the meta type
	@Override
	public MetaType get(Object key) {
		String name;
		if (key == null || (name = key.toString().trim()).length() <= 0) {
			throw new RuntimeException("Name parameter cannot be NULL or BLANK");
		}
		return caseInsensitiveVarient.get(name);
	}
	
	/// (Case sensitive) Put the meta type
	@Override
	public MetaType put(String name, MetaType type) {
		if (name == null || (name = name.toString().trim()).length() <= 0) {
			throw new RuntimeException("Name parameter cannot be NULL or BLANK");
		}
		
		if (name.equals("_oid") || name.equals("_otm")) {
			throw new RuntimeException("Name parameter uses reserved name " + name);
		}
		
		if( type == null ) {
			throw new RuntimeException("Type parameter is invalid (or null)");
		}
		
		if(super.containsKey(name)) {
			throw new RuntimeException("Type mapping already contains this key: "+name);
		}
		
		if(caseInsensitiveVarient.containsKey(name)) {
			throw new RuntimeException("Case insensitive key collision: "+name);
		}
		
		caseInsensitiveVarient.put(name, type);
		return super.put(name, type);
	}
	
	/// Clears the meta type map values
	@Override
	public void clear() {
		caseInsensitiveVarient.clear();
		super.clear();
	}
	
	/// (Case sensitive) Removes from the stored map
	@Override
	public MetaType remove(Object key) {
		caseInsensitiveVarient.remove(key);
		return super.remove(key);
	}
	
	/// Put using the object alterantive
	public MetaType putObject(String name, Object type) {
		return put(name, MetaType.fromTypeObject(type));
	}
	
	/// (Case sensitive) Put all the values from the object map
	public void putObjectMap( Map<String,Object> in ) {
		for( String key : in.keySet() ) {
			putObject( key, in.get(key) );
		}
	}
	
	//
	// Values handling based on meta map
	//
	//--------------------------------------------------------------------------

	/// Values to option set conversion, 
	/// this takes in the value to be stored, performs any needed checks on it (todo)
	/// and store it respectively.
	///
	/// @TODO: Support the various numeric value
	/// @TODO: Support string / text
	/// @TODO: Support array sets
	/// @TODO: Support GUID hash
	/// @TODO: Support MetaTable
	/// @TODO: Check against configured type
	/// @TODO: Convert to configured type if possible (like numeric)
	/// @TODO: Support proper timestamp handling (not implemented)
	///
	protected Object[] valueToOptionSet(String key, Object value) throws JSqlException {
		if (value instanceof Integer) {
			return new Object[] { new Integer(MetaType.TYPE_INTEGER), value, null, null }; //Typ, N,S,I,T
		} else if (value instanceof String) {
			return new Object[] { new Integer(MetaType.TYPE_STRING), 0, ((String) value).toLowerCase(), value }; //Typ, N,S,I,T
		}

		throw new JSqlException("Object type not yet supported: "+key+" = "+ value);
	}

	//
	// Internal JSql index setup
	//
	// The following handles the setup and managment of the (hopefully) optimized
	// indexed view. 
	//
	// The logic is that even if the view query itself is not optimized
	// The requesting query string, itself is reduced in complexity and size greatly.
	//
	// Lastly repeated request through the view can eventually be optimized later
	//
	//--------------------------------------------------------------------------

	/// Creates the indexed view configuration tracking table
	///
	/// This stores the current index view defination,
	/// and is checked against on setup if the view needs to be recreated.
	///
	/// This is used in place of the actual view properties lookup, as there is currently
	/// no standardised way to lookup table / view properties across all the SQL platform
	/// (or there is, and i never did bother to figure out how to do so)
	///
	protected void JSqlIndexViewConfigSetup(String configTableName, JSql sql) throws JSqlException {
		
		// Create the config table
		//------------------------------------------------
		sql.createTableQuerySet( //
			configTableName, //
			new String[] { //
				"nme", //Index column name
				"typ", //Index column type
				"con" //Index type string value
			}, //
			new String[] { //
				mainTable.keyColumnType, //
				mainTable.typeColumnType, //
				mainTable.keyColumnType //
			} //
		).execute(); //
		
		// Unique index collumn
		//------------------------------------------------
		sql.createTableIndexQuerySet( //
			configTableName, "nme", "UNIQUE", "unq" //
		).execute();
	}
	
	/// Populates the view config, with the current view setting
	/// 
	protected void JSqlIndexViewConfigWrite(String configTableName, JSql sql) throws JSqlException {
		// Iterator cache
		String key = null;
		MetaType type = null;
		
		// For each meta type, build view
		for (Map.Entry<String, MetaType> e : this.entrySet()) {
			key = e.getKey();
			type = e.getValue();
			
			sql.upsertQuerySet( //
				configTableName, //
				//
				new String[] { "nme" },
				new String[] { key },
				//
				new String[] { "typ", "con" },
				new Object[] { new Integer(type.valueType), type.valueConfig }
				//
			).execute(); //
		}
	}
	
	/// Extract the current stored configuration
	///
	/// Reads and retursn the current stored configuration setting, found inside the database
	///
	protected Map<String,MetaType> JSqlIndexViewConfigRead(String configTableName, JSql sql) throws JSqlException {
		Map<String,MetaType> ret = new HashMap<String,MetaType>();
		
		JSqlResult r = sql.selectQuerySet( //
			configTableName, //
			"*",  //
			null, //
			null  //
		).query(); //
		
		int rowCount = r.fetchAllRows();
		List<Object> nme = r.get("nme");
		List<Object> typ = r.get("typ");
		List<Object> con = r.get("con");
		
		for( int a=0; a<rowCount; ++a ) {
			ret.put( (String)(nme.get(a)), new MetaType( ((Number)(typ.get(a))).intValue(), (String)(con.get(a)) ) );
		}
		
		return ret;
	}
	
	/// Gets and scan the current stored configuration of view needs updating
	/// against the current configuration set. This is intended to be used with
	/// JSqlIndexViewConfigRead.
	///
	/// Not that the difference check is not strict. In the sense that if there is additional
	/// collumns defined inside the database, it is not considered as invalid.
	///
	/// It is only considered different, when the current configuration has items 
	/// missing from the database.
	///
	protected boolean viewConfigIsDifferent(Map<String,MetaType> currentConfig) {
		for( Map.Entry<String,MetaType> e : caseInsensitiveVarient.entrySet() ) {
			MetaType dbConfig = currentConfig.get(e.getKey());
			MetaType loConfig = caseInsensitiveVarient.get(e.getKey());
			
			if( loConfig != null && dbConfig == null ) {
				return true;
			} else if( loConfig == null && dbConfig == null ) {
				//skips
			} else if( dbConfig.valueType != loConfig.valueType ) {
				return true;
			} else if( 
				!(
					(dbConfig.valueConfig == null || dbConfig.valueConfig.length() <= 0) && 
					(loConfig.valueConfig == null || loConfig.valueConfig.length() <= 0)
				) 
			) {
				return true;
			} else if( !(dbConfig.valueConfig.equals(loConfig.valueConfig)) ) {
				return true;
			}
		}
		
		return false;
	}
	
	/// The index view setup
	/// 
	/// Takes the meta table name, and setup the view. Using the current MetaType mapping.
	/// This is performed using a "complex" inner left join, created by this function, based
	/// on the various registered index key. 
	///
	/// This index view, is a form of query optimization of meta values search and lookup
	///
	protected void JSqlIndexViewSetup(String viewName, String tableName, JSql sql) throws JSqlException {
		
		// View configuration
		String lBracket = "'";
		String rBracket = "'";
		String joinType = "LEFT";
		if (sql.sqlType == JSqlType.mssql) {
			lBracket = "[";
			rBracket = "]";
		}
		
		// View building start
		StringBuilder sb = new StringBuilder("CREATE VIEW "); //OR REPLACE
		sb.append(viewName);
		sb.append(" AS ");
		
		// Main final select statment, with defaults for _oid, and _otm
		StringBuilder select = new StringBuilder(" SELECT B.oID AS ");
		select.append(lBracket + "_oid" + rBracket);
		select.append(", B.oTm AS ");
		select.append(lBracket + "_otm" + rBracket);
		
		// Following from statments, which builds the additional selects
		StringBuilder from = new StringBuilder(" FROM ");
		from.append("(SELECT DISTINCT oID, oTm FROM " + tableName + ")");
		from.append(" AS B");
		
		// Iterator cache
		String key = null;
		MetaType type = null;
		int joinCount = 0;
		
		// Escaped arguments list, not in use =(
		// ArrayList<Object> argList = new ArrayList<Object>();

		// For each meta type, build view
		for (Map.Entry<String, MetaType> e : this.entrySet()) {
			key = e.getKey();
			type = e.getValue();
			
			// // Add numeric based index
			if (type.valueType >= MetaType.TYPE_INTEGER && type.valueType <= MetaType.TYPE_FLOAT) {

				select.append(", N" + joinCount + ".nVl AS ");
				select.append(lBracket + key + rBracket);

				from.append(" "+joinType+" JOIN " + tableName + " AS N" + joinCount);
				from.append(" ON B.oID = N" + joinCount + ".oID");
				from.append(" AND N" + joinCount + ".idx = 0 AND N" + joinCount + ".kID = '" + key + "'");
				
				// Add string value based index, do note it has _lc lower case and standard varients
			} else if (type.valueType == MetaType.TYPE_STRING) {

				select.append(", S" + joinCount + ".tVl AS ");
				select.append(lBracket + key + rBracket);

				select.append(", S" + joinCount + ".sVl AS ");
				select.append(lBracket + key + "_lc" + rBracket);

				from.append(" "+joinType+" JOIN " + tableName + " AS S" + joinCount);
				from.append(" ON B.oID = S" + joinCount + ".oID");
				from.append(" AND S" + joinCount + ".idx = 0 AND S" + joinCount + ".kID = '" + key + "'");
				
				// Adds a string text value based index, DOES NOT have _lc lower case varient
			} else if (type.valueType == MetaType.TYPE_TEXT) {

				select.append(", S" + joinCount + ".tVl AS ");
				select.append(lBracket + key + rBracket);

				from.append(" "+joinType+" JOIN " + tableName + " AS S" + joinCount);
				from.append(" ON B.oID = S" + joinCount + ".oID");
				from.append(" AND S" + joinCount + ".idx = 0 AND S" + joinCount + ".kID = '" + key + "'");
			}

			++joinCount;
		}
		
		// Appends the select, and from statements together in the fina lstring builder
		sb.append(select);
		sb.append(from);
		
		// Execute the final string 
		sql.execute(sb.toString());
		//System.out.println( sb.toString() );
	}
	
	/// Rebuild the index view, deleting the existing view and configuration data
	///
	protected void JSqlIndexViewCleanAndBuild(String configTableName, String viewName, String tableName, JSql sql) throws JSqlException {
		
		// Truncate the current table config
		sql.execute("DELETE FROM "+configTableName);
		
		// Populate the config
		JSqlIndexViewConfigWrite( configTableName, sql );
		
		// Drop the view
		try {
			sql.execute("DROP VIEW "+viewName);
		} catch(JSqlException e) {
			// This is silenced, as JSql does not support "DROP VIEW IF NOT EXISTS"
			// @TODO: drop view if not exists to JSql, this is under issue: 
			// http://gitlab.picoded-dev.com/picoded/javacommons/issues/69
		}
		
		// build the view
		JSqlIndexViewSetup(viewName, tableName, sql);
	}
	
	/// Rebuild the index view, deleting the existing view and configuration data, only if needed
	///
	protected void JSqlIndexViewUpdateIfNeeded(String configTableName, String viewName, String tableName, JSql sql) throws JSqlException {
		if( viewConfigIsDifferent( JSqlIndexViewConfigRead(configTableName, sql) ) ) {
			JSqlIndexViewCleanAndBuild(configTableName, viewName, tableName, sql);
		}
	}
	
	/// Checks and rebuild the index view if needed
	///
	protected void JSqlIndexViewFullBuild(String configTableName, String viewName, String tableName, JSql sql) throws JSqlException {
		// Ensures the config table is created
		JSqlIndexViewConfigSetup(configTableName, sql);
		
		// Scans if the view update is required, and if so does it
		JSqlIndexViewUpdateIfNeeded(configTableName, viewName, tableName, sql);
	}
}

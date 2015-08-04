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

public class MetaTypeMap extends CaseInsensitiveHashMap<String, MetaType> {
	
	/// The default fallback meta type
	public MetaType defaultType = new MetaType(MetaType.TYPE_MIXED);
	
	/// Get the meta type
	@Override
	public MetaType get(Object key) {
		String name;
		if (key == null || (name = key.toString().trim()).length() <= 0) {
			throw new RuntimeException("Name parameter cannot be NULL or BLANK");
		}
		return super.get(name);
	}
	
	/// Put the meta type
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
		
		if(this.containsKey(name)) {
			throw new RuntimeException("Type mapping already contains this key: "+name);
		}
		
		return super.put(name, type);
	}
	
	/// Put using the object alterantive
	public MetaType putObject(String name, Object type) {
		return put(name, MetaType.fromTypeObject(type));
	}
	
	/// Put all the values from the object map
	public void putObjectMap( Map<String,Object> in ) {
		for( String key : in.keySet() ) {
			putObject( key, in.get(key) );
		}
	}
	
	///
	/// Internal JSql index setup
	///--------------------------------------------------------------------------
	
	/// The index view setup
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
		String key;
		MetaType type;
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
				
				// Adds a string text value based index, does not have _lc lower case varient
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
}

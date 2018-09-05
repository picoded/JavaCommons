package picoded.dstack.jsql;

import java.util.*;

/// Utility function used to format JSQL rows
public class JSql_Rows {
	public String[] uniqueColumns = null;
	public String[] insertColumns = null;
	public String[] defaultColumns = null;
	public String[] miscColumns = null;
	
	public List<Object[]> uniqueValuesList = null;
	public List<Object[]> insertValuesList = null;
	public List<Object[]> defaultValuesList = null;
	
	public JSql_Rows(){
		uniqueColumns = new String[]{};
		insertColumns = new String[]{};
		defaultColumns = new String[]{};
		miscColumns = new String[]{};
		
		uniqueValuesList = new ArrayList<Object[]>();
		insertValuesList = new ArrayList<Object[]>();
		defaultValuesList = new ArrayList<Object[]>();
	}
	
	public JSql_Rows(
		String[] inUniqueColumns, 
		List<Object[]> inUniqueValuesList,
		
		String[] inInsertColumns,
		List<Object[]> inInsertValuesList,
		
		String[] inDefaultColumns,			
		List<Object[]> inDefaultValuesList,
		
		String[] inMiscColumns
	){
		uniqueColumns = inUniqueColumns;
		insertColumns = inInsertColumns;
		defaultColumns = inDefaultColumns;
		miscColumns = inMiscColumns;
		
		uniqueValuesList = inUniqueValuesList;
		insertValuesList = inInsertValuesList;
		defaultValuesList = inDefaultValuesList;
	}

	/**
	 * Iterates the relevent keyList, and appends its value from the objMap, into the sql colTypes database
	 *
	 * @param {String} _oid               - object id to store the key value pairs into
	 * @param {Map<String,Object>} objMap - map to extract values to store from
	 * @param {Set<String>} keyList       - keylist to limit append load
	 **/
	@SuppressWarnings("unchecked")
	public static JSql_Rows JSqlConvertToRows(
		String _oid, 
		Map<String, Object> objMap, 
		Set<String> keyList
	) throws JSqlException {
		
		// Nothing to update, nothing to do
		if (keyList == null) {
			return new JSql_Rows();
		}
		
		// Curent timestamp
		long now = getCurrentTimestamp();
		
		//
		// Iterate and store ONLY values inside the keyList
		// This helps optimize writes to only changed data
		//---------------------------------------------------------
		
		// Prepare the large multiUpsert values
		List<Object[]> uniqueValuesList = new ArrayList<Object[]>();
		List<Object[]> insertValuesList = new ArrayList<Object[]>();
		List<Object[]> defaultValuesList = new ArrayList<Object[]>();
		
		// Iterate the key list to apply updates
		for (String k : keyList) {
			// Skip reserved key, otm is not allowed to be saved
			// (to ensure blank object is saved)
			if (k.equalsIgnoreCase("_otm")) { //reserved
				continue;
			}
			
			// Key length size protection
			if (k.length() > 64) {
				throw new RuntimeException(
					"Attempted to insert a key value larger then 64 for (_oid = " + _oid + "): " + k);
			}
			
			// Get the value to insert
			Object v = objMap.get(k);
			
			// Delete support
			if (v == ObjectToken.NULL || v == null) {
				// Skip reserved key, oid key is allowed to be removed directly
				if (k.equalsIgnoreCase("oid") || k.equalsIgnoreCase("_oid")) {
					continue;
				}
			} else {
				// Converts it into a type set, and store it
				Object[] typSet = valueToValueTypeSet(v);
				
				// Setup the multiUpsert
				uniqueValuesList.add(new Object[] { _oid, k, 0 });
				insertValuesList.add(new Object[] { typSet[0], typSet[1], typSet[2], typSet[3],
					typSet[4], now, 0 });
				defaultValuesList.add(new Object[] { now });
			}
		}
		
		// Nothing to update, nothing to do
		if (insertValuesList.size() <= 0) {
			return new JSql_Rows();
		}
		
		// Does the actual multi upsert
		return new JSql_Rows(
			// "pKy" is auto generated by SQL db
			new String[] { "oID", "kID", "idx" }, // The unique column names
			uniqueValuesList, // The row unique identifier values
			// Value / Text / Raw storage + Updated / Expire time stamp
			new String[] { "typ", "nVl", "sVl", "tVl", "rVl", "uTm", "eTm" }, //
			insertValuesList, //
			// Created timestamp setup
			new String[] { "cTm" }, //
			defaultValuesList, //
			null // The only misc col, is pKy, which is being handled by DB
		);
	}
}


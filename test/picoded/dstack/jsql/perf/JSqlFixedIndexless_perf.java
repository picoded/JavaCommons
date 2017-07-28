package picoded.dstack.jsql.perf;

// Test system include
import static org.junit.Assert.*;
import org.junit.*;
import com.carrotsearch.junitbenchmarks.AbstractBenchmark;
import com.carrotsearch.junitbenchmarks.BenchmarkOptions; 
import com.carrotsearch.junitbenchmarks.BenchmarkRule; 

// Java includes
import java.util.*;

// External lib includes
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.ArrayUtils;

import picoded.conv.ConvertJSON;
// Test depends
import picoded.conv.GUID;
import picoded.struct.CaseInsensitiveHashMap;
import picoded.dstack.jsql.connector.*;
import picoded.dstack.jsql.*;
import picoded.TestConfig;

/// Testing of metatable full indexless fixed table performance
public class JSqlFixedIndexless_perf extends JSqlClob_perf {

	public String[] collumnNames = null;
	public String[] collumnTypes = null;

	public String[] collumnNamesWithOID = null;
	public String[] collumnTypesWithOID = null;

	public void tableSetup() {

		collumnNames = new String[largeCols*2];
		collumnTypes = new String[largeCols*2];

		for(int i=0; i<largeCols; ++i) {
			collumnNames[i] = "N"+i;
			collumnTypes[i] = "DECIMAL(36,12)";

			collumnNames[i+largeCols] = "S"+i;
			collumnTypes[i+largeCols] = "VARCHAR(MAX)";
		}

		collumnNamesWithOID = ArrayUtils.addAll(new String[] { "oID" }, collumnNames);
		collumnTypesWithOID = ArrayUtils.addAll(new String[] { "VARCHAR(64)" }, collumnTypes);

		// Primary key, as classic int, this is used to lower SQL
		// fragmentation level, and index memory usage. And is not accessible.
		// Sharding and uniqueness of system is still maintained by GUID's
		String[] collumnNamesWithPKy = ArrayUtils.addAll(new String[] { "pKy" }, collumnNamesWithOID);
		String[] collumnTypesWithPKy = ArrayUtils.addAll(new String[] { "BIGINT PRIMARY KEY AUTOINCREMENT" }, collumnTypesWithOID);

		jsqlObj.createTable( tablename, collumnNamesWithPKy, collumnTypesWithPKy );

		// This optimizes query by object keys
		// + oID
		jsqlObj.createIndex( //
			tablename, "oID", "UNIQUE", "unq" //
		); //
	}

	// Insert / update commands to "implement"
	//-----------------------------------------------------

	/// Extract values from a map to an array
	public Object[] extractValueFromMap(String[] names, Map<String,Object> obj) {
		Object[] ret = new Object[names.length];
		for(int i=0; i<names.length; ++i) {
			ret[i] = obj.get( names[i] );
		}
		return ret;
	}

	/// Insert an object, and return a GUID.
	/// This is intentionally still an upsert to closer replicate actual behaviour in production
	public String insert(Map<String,Object>obj) {
		String oID = GUID.base58();
		jsqlObj.upsert(  //
			tablename,
			//
			new String[] { "oID" }, // The unique column names
			new Object[] { oID }, // The row unique identifier values
			//
			collumnNames, // Columns names to update
			extractValueFromMap(collumnNames, obj) // Values to update
		);
		return oID;
	}

	/// Completely replace an object, with a GUID
	public void replace(String guid, Map<String,Object>obj) {
		jsqlObj.upsert(  //
			tablename,
			//
			new String[] { "oID" }, // The unique column names
			new Object[] { guid }, // The row unique identifier values
			//
			collumnNames, // Columns names to update
			extractValueFromMap(collumnNames, obj) // Values to update
		);
		return;
	}

	/// Does a delta update of changes, with a GUID and original object for refrence
	public void update(String guid, Map<String,Object> ori, Map<String,Object>delta) {
		String[] deltaKeys = delta.keySet().toArray(new String[0]);
		String[] miscKeys = ArrayUtils.removeElements(collumnNames, deltaKeys);

		jsqlObj.upsert(  //
			tablename,
			//
			new String[] { "oID" }, // The unique column names
			new Object[] { guid }, // The row unique identifier values
			//
			deltaKeys, // Columns names to update
			extractValueFromMap(deltaKeys, delta), // Values to update
			null, //default names
			null, //default values
			miscKeys //misc keys
		);
		return;
	}

}
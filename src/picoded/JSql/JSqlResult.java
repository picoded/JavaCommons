package picoded.JSql;

import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

// OracleSQL types
import oracle.sql.CLOB;
import picoded.conv.ClobString;
import picoded.struct.CaseInsensitiveHashMap;

/// JSql result set, data is either prefetched, or fetch on row request. For example usage, refer to JSql
///
/// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~{.java}
/// // Below is the inner data structure format of JSqlResult,
/// // Where its respective fieldname/row can be accessed natively.
/// HashMap<String /*fieldName*/, ArrayList<Object> /*rowResults array*/ > JSqlResultFormat;
/// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
///
/// *******************************************************************************
///
/// [LOW PRIROTY TODO LIST]
/// * Change from ArrayList<Object> to Object[] list. Since the system is always in "fetchAll" mode anyway.
///   Note that this change has high number of implications when it occurs, due to being a core data structure.
/// * readRow() : Without the unchecked suppression flag
/// * rowAffected() : used to get the amount of rows affected for an update statment
/// * Exception for readRow / readRowCol on out-of-bound row number
/// * Non silent (exception), and silent varient (returns null) for readRow / readRowCol
/// * isDisposed: boolean function for the fetch checking.
///
/// IGNORED feature list (not fully supported, and drop to ensure consistancy in sqlite/sql mode)
/// - SingleRow fetch and related features was seen to be buggy in sqlite.
///   - fetchRowData varient of fetchAllRows : Fetching 1 row from the database at a timeon both ends
///   - readRow / readRowCol : To support unfetched rows -> ie, automatically fetchs to the desired row count
///   - isFetchedAll / isFetchedRow
///
public class JSqlResult extends CaseInsensitiveHashMap<String /* fieldName */, List<Object> /*
 * rowNumber
 * array
 */> {
	protected static final long serialVersionUID = 1L;
	
	// / Internal self used logger
	private static final Logger LOGGER = Logger.getLogger(JSqlResult.class.getName());
	
	// / Total row count for query
	private int rowCount = 0;
	
	private PreparedStatement sqlStmt = null;
	private ResultSet sqlRes = null;
	
	// / Constructor with SQL resultSet
	// / (Used internally by JSql : not to be used directly)
	public JSqlResult(PreparedStatement ps, ResultSet rs) {
		sqlStmt = ps;
		sqlRes = rs;
	}
	
	// / Empty constructor, used as place holder
	public JSqlResult() { // empty
	
	}
	
	// / Fetches all the row data, and store it into the local hashmap &
	// respective array list
	// / Returns the rowCount on success.
	// /
	// / Important note: This step is redundent for sqlite, as data is prefetch
	// on all executions
	public int fetchAllRows() throws JSqlException {
		int pt;
		int colCount;
		String colName;
		List<Object> colArr;
		Object tmpObj;
		
		if (sqlRes != null) {
			try {
				ResultSetMetaData rsmd = sqlRes.getMetaData();
				colCount = rsmd.getColumnCount();
				while (sqlRes.next()) {
					for (pt = 0; pt < colCount; pt++) {
						colName = rsmd.getColumnName(pt + 1);
						// remove single quote if it is first and last
						// character.
						if (colName != null && colName.trim().length() > 0) {
							if (colName.charAt(0) == '\'' || colName.charAt(0) == '"') {
								colName = colName.substring(1);
							}
							if (colName.charAt(colName.length() - 1) == '\''
								|| colName.charAt(colName.length() - 1) == '"') {
								colName = colName.substring(0, colName.length() - 1);
							}
						}
						
						if (this.containsKey(colName)) {
							colArr = this.get(colName);
						} else {
							colArr = new ArrayList<Object>();
							this.put(colName, colArr);
						}
						
						// Auto conversion from Oracle type variables, to a more
						// Expected format for mysql (basic data structs)
						tmpObj = sqlRes.getObject(pt + 1);
						if (BigDecimal.class.isInstance(tmpObj)) {
							tmpObj = ((BigDecimal) tmpObj).doubleValue();
						} else if (CLOB.class.isInstance(tmpObj)) {
							try {
								tmpObj = ClobString.toStringNoisy((CLOB) tmpObj);
							} catch (SQLException e) {
								throw new JSqlException("CLOB Processing Error", e);
							}
						} else if (Blob.class.isInstance(tmpObj)) {
							Blob bob = (Blob) tmpObj;
							tmpObj = bob.getBytes(1, (int) bob.length());
							bob.free();
						}
						
						colArr.add(rowCount, tmpObj);
					}
					
					++rowCount;
				}
				
				dispose();
			} catch (Exception e) {
				throw new JSqlException("Error fetching sql row " + rowCount + ": ", e);
			}
			
		}
		
		// throw new JSqlException("SQL format is not yet implemented");
		return rowCount;
	}
	
	// / Return current row count
	public int rowCount() {
		return rowCount;
	}
	
	// / Read a fetched row in a single hashmap
	public Map<String, Object> readRow(int pt) {
		if (pt >= rowCount) {
			return null;
		}
		Map<String, Object> ret = new HashMap<String, Object>();
		Iterator<Map.Entry<String, List<Object>>> it = this.entrySet().iterator();
		Map.Entry<String, List<Object>> pairs;
		String colName;
		List<Object> colArr;
		while (it.hasNext()) {
			pairs = it.next();
			colName = pairs.getKey();
			colArr = pairs.getValue();
			ret.put(colName, colArr.get(pt));
		}
		return ret;
	}
	
	// / Read a fetched row column value and returns it (if row/value exists)
	public Object readRowCol(int pt, String name) {
		List<Object> colArr = this.get(name);
		if (colArr != null) {
			return colArr.get(pt);
		}
		return null;
	}
	
	// / JSql result set to an object array, from a single collumn field
	public Object[] readCol(String field) {
		List<Object> resList = get(field);
		return (resList != null) ? resList.toArray(new Object[resList.size()]) : null;
	}
	
	// / JSql result set to an string array, from a single collumn field
	public String[] readCol_StringArr(String field) {
		List<Object> resList = get(field);
		int len = resList.size();
		
		int pt = 0;
		String[] res = new String[len];
		for (Object data : resList) {
			res[pt] = (String) data;
			pt++;
		}
		return res;
	}
	
	// / Dispose and closes the result connection
	public void dispose() {
		try {
			if (sqlRes != null) {
				sqlRes.close();
				sqlRes = null;
			}
		} catch (Exception e) {
			// Logg the exception as warning
			LOGGER.log(Level.WARNING, "JSqlResult.dispose result exception", e);
		}
		
		try {
			if (sqlStmt != null) {
				sqlStmt.close();
				sqlStmt = null;
			}
		} catch (Exception e) {
			// Logg the exception as warning
			LOGGER.log(Level.WARNING, "JSqlResult.dispose statement exception", e);
		}
	}
	
	// / Fetch table Meta Data info
	public Map<String, String> fetchMetaData() throws JSqlException {
		Map<String, String> ret = null;
		if (sqlRes != null) {
			ret = new HashMap<String, String>();
			try {
				while (sqlRes.next()) {
					ret.put(sqlRes.getString("COLUMN_NAME").toUpperCase(Locale.ENGLISH), sqlRes
						.getString("TYPE_NAME").toUpperCase(Locale.ENGLISH));
				}
			} catch (Exception e) {
				throw new JSqlException("Error fetching sql meta data", e);
			}
		}
		return ret;
	}
}

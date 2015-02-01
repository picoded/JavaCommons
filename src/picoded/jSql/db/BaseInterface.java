package picoded.jSql.db;

import picoded.jSql.JSqlResult;
import picoded.jSql.JSqlException;

/// Standardised database interface for JSql functionalities.
/// in which additional database interface types are implemented.
/// 
/// Note that the interface intentionaly does not include a "constructor" as that may be database implementation dependent
public interface BaseInterface {

	// Internal refrence of the current sqlType the system is running as, or so it should be by default
	// public JSqlType sqlType = JSqlType.invalid;

	/// Executes the argumented query, and returns the result object *without* 
	/// fetching the result data from the database. (not fetching may not apply to all implementations)
	///
	/// **Note:** Only queries starting with 'SELECT' will produce a JSqlResult object that has fetchable results
	public JSqlResult executeQuery(String qString, Object... values) throws JSqlException;

	/// Executes the argumented query, and immediately fetches the result from
	/// the database into the result set.
	///
	/// **Note:** Only queries starting with 'SELECT' will produce a JSqlResult object that has fetchable results
	public JSqlResult query(String qString, Object... values) throws JSqlException;

	/// Executes and dispose the sqliteResult object.
	///
	/// Returns false if no result is given by the execution call, else true on success
	public boolean execute(String qString, Object... values) throws JSqlException;

	/// Raw varient of executeQuery, which DOES NOT perform any SQL to Implementation conversion
	public JSqlResult executeQuery_raw(String qString, Object... values) throws JSqlException;

	/// Raw varient of query, which DOES NOT perform any SQL to Implementation conversion
	public JSqlResult query_raw(String qString, Object... values) throws JSqlException;

	/// Raw varient of execute, which DOES NOT perform any SQL to Implementation conversion
	public boolean execute_raw(String qString, Object... values) throws JSqlException;

	/// Returns true, if dispose() function was called prior
	public boolean isDisposed();

	/// Dispose of the respective SQL driver / connection
	public void dispose();

	/// Recreates the JSql connection if it has already been disposed of. Option to forcefully recreate the connection if needed.
	public void recreate(boolean force);

	// Just incase a user forgets to dispose "as per normal"
	//protected void finalize();
}
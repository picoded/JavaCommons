package picoded.jSql;

/// JSql type options enum, see JSql.sqlType for its usage
public class JSqlQuerySet {
	
	/// SQL query set
	protected String sqlQuery = null;
	
	/// SQL arguments set
	protected Object[] sqlArgs = null;
	
	/// The jSql object to query against
	protected JSql JSqlObj = null;
	
	/// Initialize the query set with the following options
	public JSqlQuerySet( String query, Object[] args, JSql dbObj ) {
		sqlQuery = query;
		sqlArgs = args;
		JSqlObj = dbObj;
	}
	
	/// Gets the stored sql query string
	public String getQuery() {
		return sqlQuery;
	}
	
	/// Gets the stored arguments list
	public Object[] getArguments() {
		return sqlArgs;
	}
	
	/// Gets the stored arguments list
	public JSql getJSql() {
		return JSqlObj;
	}
	
	/// Executes the argumented query, and returns the result object *without*
	/// fetching the result data from the database. This is raw execution.
	///
	/// **Note:** Only queries starting with 'SELECT' will produce a JSqlResult object that has fetchable results
	public JSqlResult executeQuery(String qString, Object... values) throws JSqlException {
		return JSqlObj.executeQuery( sqlQuery, sqlArgs );
	}
	
	/// Executes the argumented query, and immediately fetches the result from
	/// the database into the result set. This is raw execution.
	///
	/// **Note:** Only queries starting with 'SELECT' will produce a JSqlResult object that has fetchable results
	public JSqlResult query(String qString, Object... values) throws JSqlException {
		return JSqlObj.query( sqlQuery, sqlArgs );
	}
	
	/// Executes and dispose the sqliteResult object. Similar to executeQuery
	/// Returns false if no result object is given by the execution call. This is raw execution.
	public boolean execute() throws JSqlException {
		return JSqlObj.execute( sqlQuery, sqlArgs );
	}
}

package picoded.jSql;

/// JSql type options enum, see JSql.sqlType for its usage
public class JSqlQuerySet {
	
	/// SQL query set
	protected String sqlQuery = null;
	
	/// SQL arguments set
	protected Object[] sqlArgs = null;
	
	/// Initialize the query set with the following options
	public JSqlQuerySet( String query, Object[] args ) {
		sqlQuery = query;
		sqlArgs = args;
	}
	
	/// Gets the stored sql query string
	public String getQuery() {
		return sqlQuery;
	}
	
	/// Gets the stored arguments list
	public Object[] getArguments() {
		return sqlArgs;
	}
}

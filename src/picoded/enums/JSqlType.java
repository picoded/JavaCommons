package picoded.enums;

/// JSql type options enum, see JSql.sqlType for its usage
public enum JSqlType {
	/// invalid type (reserved for base class)
	invalid,
	
	/// sqlite jdbc or file access
	sqlite,
	
	/// mysql connection mode
	mysql,
	
	/// oracle connection mode
	oracle,
	
	/// MS-Sql connection mode
	mssql,
	
	/// Others
	others
}

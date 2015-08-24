package picoded.JSql.struct.internal;

public class MetaTableJSql_columnTypes {
	
	/// Object ID field type
	public String objColumnType = "VARCHAR(64)";

	/// Key name field type
	public String keyColumnType = "VARCHAR(64)";

	/// Type collumn type
	public String typeColumnType = "TINYINT";

	/// Index collumn type
	public String indexColumnType = "TINYINT";

	/// String value field type
	/// @TODO: Investigate performance issues for this approach
	public String numColumnType = "DECIMAL(36,12)";

	/// String value field type
	public String strColumnType = "VARCHAR(64)";

	/// Full text value field type
	public String fullTextColumnType = "VARCHAR(MAX)";

	/// Timestamp field type
	public String tStampColumnType = "BIGINT";

	/// Primary key type
	public String pKeyColumnType = "BIGINT PRIMARY KEY AUTOINCREMENT";

	// Indexed view prefix, this is used to handle index conflicts between "versions" if needed
	// public String viewSuffix = "";
	
}

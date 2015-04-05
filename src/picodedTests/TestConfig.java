package picodedTests;

import java.lang.String;
import org.apache.commons.lang3.RandomStringUtils;

///
/// The SQL configuration vars, this is to ensure a centralized place to change the various test configuration values
/// Value access were made as functions, to facilitate future migration to config files??
///
public class TestConfig {
	
	///
	/// Randomly generated table prefix, used to prevent multiple running tests from colliding
	///
	static public String randomTablePrefix() {
		return RandomStringUtils.randomAlphanumeric(8).toUpperCase();
	}
	
	//-------------------------------//
	// Default Credentials for MYSQL //
	//-------------------------------//
	static private String MYSQL_CONN = "54.169.34.78:3306";
	static private String MYSQL_DATA = "JAVACOMMONS";
	static private String MYSQL_USER = "JAVACOMMONS";
	static private String MYSQL_PASS = "JAVACOMMONS";
	
	static public String MYSQL_CONN() {
		return MYSQL_CONN;
	}
	
	static public String MYSQL_DATA() {
		return MYSQL_DATA;
	}
	
	static public String MYSQL_USER() {
		return MYSQL_USER;
	}
	
	static public String MYSQL_PASS() {
		return MYSQL_PASS;
	}
	
	//-------------------------------//
	// Default Credentials for MSSQL //
	//-------------------------------//
	static private String MSSQL_CONN = "54.169.34.78";
	static private String MSSQL_NAME = "JAVACOMMONS";
	static private String MSSQL_USER = "JAVACOMMONS";
	static private String MSSQL_PASS = "JAVACOMMONS";
	
	static public String MSSQL_CONN() {
		return MSSQL_CONN;
	}
	
	static public String MSSQL_NAME() {
		return MSSQL_NAME;
	}
	
	static public String MSSQL_USER() {
		return MSSQL_USER;
	}
	
	static public String MSSQL_PASS() {
		return MSSQL_PASS;
	}
	
	//--------------------------------//
	// Default Credentials for ORACLE //
	//--------------------------------//
	static private String ORACLE_PATH = "JAVACOMMONS@//54.169.34.78/xe";
	static private String ORACLE_USER = "JAVACOMMONS";
	static private String ORACLE_PASS = "JAVACOMMONS";
	
	static public String ORACLE_PATH() {
		return ORACLE_PATH;
	}
	
	static public String ORACLE_USER() {
		return ORACLE_USER;
	}
	
	static public String ORACLE_PASS() {
		return ORACLE_PASS;
	}
	
}

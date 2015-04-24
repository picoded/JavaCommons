package picodedTests;

import java.util.Properties;
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
	static private String MYSQL_CONN_URL = "jdbc:mysql://54.169.34.78:3306/JAVACOMMONS";
	
	static public String MYSQL_CONN_URL() {
		return MYSQL_CONN_URL;
	}
	
	static public Properties MYSQL_CONN_PROPS() {
		Properties connectionProps = new Properties();
		connectionProps.put("user", "JAVACOMMONS");
		connectionProps.put("password", "JAVACOMMONS");
		connectionProps.put("autoReconnect", "true");
		connectionProps.put("failOverReadOnly", "false");
		connectionProps.put("maxReconnects", "5");
		return connectionProps;
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

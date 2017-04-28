package picoded.dstack.jsql.connector;

import static org.junit.Assert.*;
import org.junit.*;

import java.util.Map;

import picoded.conv.ConvertJSON;
import picoded.TestConfig;

///
/// JSql Test case which is specific for MYSQL
///
public class JSql_Mysql_test extends JSql_Base_test {
	
	///
	/// SQL implmentation to actually overwrite
	///
	public JSql sqlImplementation() {
		return JSql.mysql( 
			TestConfig.MYSQL_CONN(),
			TestConfig.MYSQL_DATA(),
			TestConfig.MYSQL_USER(),
			TestConfig.MYSQL_PASS()
		);
	}

}
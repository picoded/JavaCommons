package picodedTests.util;

import java.util.List;
import java.util.Map;

import picoded.conv.GUID;
import picoded.JSql.JSql;
import picoded.JSql.JSqlException;

// Target test class
import picoded.util.ServletLogging;

// Test Case include
import org.junit.*;
import static org.junit.Assert.*;

///
/// Test Case for picoded.struct.CaseInsensitiveHashMap
///
public class ServletLogging_test {
	
	protected JSql JSqlObj = null;
	protected ServletLogging slObj = null;

	@BeforeClass
	public static void oneTimeSetUp() {
		// one-time initialization code
	}
	
	@AfterClass
	public static void oneTimeTearDown() {
		// one-time cleanup code
	}
	
	@Before
	public void setUp() throws JSqlException {
		JSqlObj = JSql.sqlite();
		
		slObj = new ServletLogging(JSqlObj, JSqlObj);
		slObj.tableSetup();
	}
	
	@After
	public void tearDown() throws JSqlException {
		if (JSqlObj != null) {
			JSqlObj.executeQuery("DROP TABLE IF EXISTS `config`").dispose();
			JSqlObj.executeQuery("DROP TABLE IF EXISTS `logFormat`").dispose();
			JSqlObj.executeQuery("DROP TABLE IF EXISTS `logStrHashes`").dispose();
			JSqlObj.executeQuery("DROP TABLE IF EXISTS `logTable`").dispose();
			JSqlObj.executeQuery("DROP TABLE IF EXISTS `excStrHash`").dispose();
			JSqlObj.executeQuery("DROP TABLE IF EXISTS `exception`").dispose();
			JSqlObj.dispose();
			JSqlObj = null;
		}
	}
	
	@Test
	public void validateSystemHash() throws JSqlException {
		assertNotNull("SQL result returns as expected", slObj.validateSystemHash());
	}

	@Test
	public void systemHash() throws JSqlException {
		assertNotNull("SQL result returns as expected", slObj.systemHash());
	}

	@Test
	public void addFormat() throws JSqlException {
		String fmtHash = GUID.base58();
		slObj.addFormat(fmtHash, "log user %s, at time %s from %s.");
	}
	
	@Test
	public void log() throws JSqlException {
		Object[] args = {"XYZ", (int)(System.currentTimeMillis() / 1000), "SG"};
		slObj.log("log user %s, at time %i from %s.", args);
	}
	
	@Test
	public void list() throws JSqlException {
		int time = (int)(System.currentTimeMillis() / 1000);
		// save log
		Object[] args = {"brown", "dog", "over", "wall", time, time, time, time, "SG"};
		slObj.log("The %s lazy %s jumps %s the %s, at time %i %i %i %i in %s.", args);

		// fetch all logs
		List<Map<String, Object>> list = slObj.list();
		// System.out.println(list);
		assertNotNull("SQL result returns as expected", list);
	}
	
}
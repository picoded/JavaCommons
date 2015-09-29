package picodedTests.ServletLogging;

import java.util.List;
import java.util.Map;

import picoded.conv.GUID;
import picoded.JSql.JSql;
import picoded.JSql.JSqlException;

// Target test class
import picoded.ServletLogging.LogMessage;
import picoded.ServletLogging.ServletLogging;

// Test Case include
import org.junit.*;
import static org.junit.Assert.*;

///
/// Test Case for picoded.struct.CaseInsensitiveHashMap
///
public class ServletLogging_test {
	
	protected JSql jSqlObj = null;
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
		jSqlObj = JSql.sqlite();
		
		slObj = new ServletLogging(jSqlObj, jSqlObj);
		slObj.tableSetup();
	}
	
	@After
	public void tearDown() throws JSqlException {
		if (jSqlObj != null) {
			try {
				slObj.tearDown(jSqlObj);
				jSqlObj.dispose();
				jSqlObj = null;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	@Test
	public void validateSystemHash() throws JSqlException {
		assertNotNull("SQL result returns as expected", slObj.validateSystemHash(jSqlObj));
	}
	
	@Test
	public void systemHash() throws JSqlException {
		assertNotNull("SQL result returns as expected", slObj.systemHash(jSqlObj));
	}
	
	@Test
	public void addFormat() throws Exception {
		String format = "log user %s, at time %s from %s.";
		//String fmtHash = GUID.base58();
		String fmtHash = slObj.md5Base58(format);
		slObj.addFormat(jSqlObj, fmtHash, format);
		
		assertEquals(fmtHash, slObj.hashFormat(jSqlObj, format));
		
	}
	
	@Test
	public void log() throws Exception {
		Object[] args = { "XYZ", (int) (System.currentTimeMillis() / 1000), "SG" };
		slObj.log(jSqlObj, "log user %s, at time %i from %s.", args);
	}
	
	@Test
	public void list() throws Exception {
		int time = (int) (System.currentTimeMillis() / 1000);
		// save log
		String formatStr = "The %s lazy %s jumps %s the %s, at time %d %d %d %d in %s.";
		Object[] args = { "brown", "dog", "over", "wall", time, time, time, time, "SG" };
		
		// save format string and log details
		slObj.log(jSqlObj, formatStr, args);
		
		// fetch all logs
		List<LogMessage> list = slObj.list(jSqlObj);
		
		// Check for NULL
		assertNotNull("SQL result returns as expected", list);
		
		// Assert Format string 
		assertEquals(formatStr, list.get(0).getFormat());
		
		// Assert agruments size 
		assertEquals(args.length, list.get(0).getArgs().size());
		
		// Assert formatted string 
		assertEquals(String.format(formatStr, args), list.get(0).toString());
	}
	
}
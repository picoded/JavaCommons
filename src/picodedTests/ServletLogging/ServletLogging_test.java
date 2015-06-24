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
			try {
				slObj.tearDown();
				JSqlObj.dispose();
				JSqlObj = null;
			} catch(Exception e) {
         	e.printStackTrace();
			}
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
		String format = "log user %s, at time %s from %s.";
	  //String fmtHash = GUID.base58();
	   String fmtHash = slObj.generateHash(format);
		slObj.addFormat(fmtHash, format);

		assertEquals(fmtHash, slObj.hashFormat(format));
		
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
		String formatStr = "The %s lazy %s jumps %s the %s, at time %d %d %d %d in %s.";
		Object[] args = {"brown", "dog", "over", "wall", time, time, time, time, "SG"};
		
		// save format string and log details
		slObj.log(formatStr, args);

		// fetch all logs
		List<LogMessage> list = slObj.list();
		
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
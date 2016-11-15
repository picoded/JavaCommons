package picoded.JStruct;

import java.util.logging.Level;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class EventLogger_test extends Mockito {
	EventLogger eventLogger = new EventLogger() {
	};
	Exception exception = null;
	
	@Before
	public void setUp() {
		
	}
	
	@After
	public void tearDown() {
		
	}
	
	@Test
	public void logTest() {
		
		eventLogger.log(Level.ALL, exception, "JSON", "test");
		exception = new Exception();
		eventLogger.log(Level.ALL, exception, "JSON", "test");
	}
	
	@Test
	public void log1Test() {
		eventLogger.log(Level.ALL, "JSON", "test");
	}
	
	@Test
	public void infoTest() {
		eventLogger.info(exception, "JSON", "test");
		
	}
	
	@Test
	public void info1Test() {
		eventLogger.info("JSON", "test");
	}
	
	@Test
	public void warnTest() {
		eventLogger.warn(exception, "JSON", "test");
	}
	
	@Test
	public void warnTest1() {
		eventLogger.warn("JSON", "test");
	}
	
	@Test
	public void error() {
		eventLogger.error(exception, "JSON", "test");
	}
	
	@Test
	public void error1Test() {
		eventLogger.error("JSON", "test");
	}
	
}

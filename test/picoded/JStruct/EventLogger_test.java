package picoded.JStruct;

import java.util.logging.Level;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class EventLogger_test extends Mockito {
	EventLogger eventLogger = null;
	
	@Before
	public void setUp() {
		eventLogger = mock(picoded.JStruct.EventLogger.class);
	}
	
	@After
	public void tearDown() {
		
	}
	
	@Test
	public void logTest() {
		eventLogger.log(Level.ALL, new Exception(), "JSON", "test");
		eventLogger.log(Level.ALL, new Exception("test - ALL"), "JSON", "test");
		eventLogger.log(Level.CONFIG, new Exception("test - CONFIG"), "JSON", "test");
		eventLogger.log(Level.FINE, new Exception("test - FINE"), "JSON", "test");
		eventLogger.log(Level.FINER, new Exception("test - FINER"), "JSON", "test");
		eventLogger.log(Level.INFO, new Exception("test - INFO"), "JSON", "test");
		eventLogger.log(Level.OFF, new Exception("test- OFF"), "JSON", "test");
		eventLogger.log(Level.SEVERE, new Exception("test - SEVERE"), "JSON", "test");
		eventLogger.log(Level.WARNING, new Exception("test - WARNING"), "JSON", "test");
	}
	
	@Test
	/// Log
	public void log1Test() {
		eventLogger.log(Level.ALL, "JSON", "test");
	}
	
	@Test
	/// Info with exception
	public void infoTest() {
		eventLogger.info(new Exception("test - SEVERE"), "JSON", "test");
		
	}
	
	@Test
	/// Info
	public void info1Test() {
		eventLogger.info("JSON", "test");
	}
	
	@Test
	/// Info with exception
	public void warnTest() {
		eventLogger.warn(new Exception("test - SEVERE"), "JSON", "test");
	}
	
	@Test
	/// Info
	public void warnTest1() {
		eventLogger.warn("JSON", "test");
	}
	
	@Test
	/// Error with exception
	public void error() {
		eventLogger.error(new Exception("test - SEVERE"), "JSON", "test");
	}
	
	@Test
	/// Error
	public void error1Test() {
		eventLogger.error("JSON", "test");
	}
	
}

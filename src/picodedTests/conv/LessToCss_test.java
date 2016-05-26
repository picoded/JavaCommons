package picodedTests.conv;

import static org.junit.Assert.assertNotNull;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import picoded.conv.LessToCss;

public class LessToCss_test {
	
	protected LessToCss lcObj = null;
	
	@Before
	public void setUp() {
		lcObj = new LessToCss();
	}
	
	@After
	public void tearDown() {
		lcObj = null;
	}
	
	@Test
	public void constructor() {
		assertNotNull(lcObj);
	}
	
}

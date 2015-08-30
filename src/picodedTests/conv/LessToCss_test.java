package picodedTests.conv;

import static org.junit.Assert.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Arrays;

import picoded.conv.*;

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
		assertNotNull( lcObj );
	}
		
}

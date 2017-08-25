package picoded.core.conv;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ArrayConv_test {
	
	@Before
	public void setUp() {
	}
	
	@After
	public void tearDown() {
		
	}
	
	//
	// Expected exception testing
	//
	
	/// Invalid constructor test
	@Test(expected = IllegalAccessError.class)
	public void invalidConstructor() throws Exception {
		new ArrayConv();
	}
	
}

package picoded.JStruct.internal;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class JStruct_KeyValueMap_test {
	JStruct_KeyValueMap jStruct_KeyValueMap = null;
	
	@Before
	public void setUp() {
		jStruct_KeyValueMap = new JStruct_KeyValueMap();
	}
	
	@After
	public void tearDown() {
		
	}
	
	//
	// Expected exception testing
	//
	
	// / Invalid constructor test
	@Test(expected = IllegalAccessError.class)
	public void invalidConstructor() throws Exception {
		new JStruct_KeyValueMap();
		
	}
}

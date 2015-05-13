package picodedTests.JStack;

import org.junit.*;
import static org.junit.Assert.*;
import java.util.*;

import picoded.JSql.*;
import picoded.JCache.*;
import picoded.JStack.*;
import picodedTests.TestConfig;

public class MetaTable_sqlite_test {
	
	// JStack setup
	//-----------------------------------------------
	
	protected JStack JStackObj = null;
	
	protected void JStackSetup() {
		JStackObj = new JStack( JSql.sqlite() );
	}
	
	protected void JStackTearDown() {
		JStackObj = null;
	}
	
	// Metatable setup
	//-----------------------------------------------
	
	protected MetaTable mtObj = null;
	
	protected void mtObjSetup() throws JStackException {
		mtObj = new MetaTable( JStackObj );
	}
	
	protected void mtObjTearDown() {
		
	}
	
	// Actual setup / teardown
	//-----------------------------------------------
	
	@Before
	public void setUp() throws JStackException {
		JStackSetup();
		mtObjSetup();
	}
	
	@After
	public void tearDown() {
		mtObjTearDown();
		JStackTearDown();
	}
	
	// Test cases
	//-----------------------------------------------
	
	@Test
	public void constructor() {
		assertNotNull(JStackObj);
	}
	
	

}
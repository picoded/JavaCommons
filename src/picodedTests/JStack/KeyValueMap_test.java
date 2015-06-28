package picodedTests.JStack;

import static org.junit.Assert.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import picoded.JSql.JSql;
import picoded.JSql.JSqlException;
import picoded.JStack.KeyValueMap;

public class KeyValueMap_test {
	
	protected JSql jSqlObj = null;
	protected KeyValueMap keyValObj = null;
	
	
	@Before
	public void setUp() throws JSqlException {
		//create connection
		jSqlObj = JSql.sqlite();
		
		keyValObj = new KeyValueMap(jSqlObj);
		keyValObj.tableSetup();
	}
	
   @After
	public void tearDown() throws JSqlException {
		if (jSqlObj != null) {
			try {
				keyValObj.tearDown();
				jSqlObj.dispose();
				jSqlObj = null;
			} catch(Exception e) {
         	e.printStackTrace();
			}
		}
	}
	
	@Test
	public void constructorTest() {
		assertNotNull("jSql constructed object must not be null", jSqlObj);
		assertNotNull("keyValueMap constructed object must not be null", keyValObj);
	}
	
	@Test
	public void put_test() throws JSqlException{
		String col = "col1";
		String expTime = String.valueOf(keyValObj.getUnixTime());

	   boolean result = keyValObj.put(col, expTime);
		assertTrue(result);
		
	}
	
	@Test
	public void get_test() throws JSqlException{
	   
	   keyValObj.put("col1", String.valueOf(keyValObj.getUnixTime()));
      keyValObj.put("col2", String.valueOf(keyValObj.getUnixTime()));
		
		assertNotNull(keyValObj.get("col1"));
		assertNotNull(keyValObj.get("col2"));
		
		
	}
	
	@Test
	public void size_test() throws JSqlException{
		keyValObj.put("col1", String.valueOf(keyValObj.getUnixTime()));
      keyValObj.put("col2", String.valueOf(keyValObj.getUnixTime()));
		
		assertEquals(2,keyValObj.size());
	}

   @Test
   public void remove_test() throws JSqlException{
   	keyValObj.put("col1", String.valueOf(keyValObj.getUnixTime()));
   	keyValObj.remove("col1");
   	
   	assertEquals(0,keyValObj.size());
   }
	
	@Test
	public void isEmpty_test() throws JSqlException{
		keyValObj.put("col1", String.valueOf(keyValObj.getUnixTime()));
   	keyValObj.remove("col1");
   	assertTrue(keyValObj.isEmpty());
	}
}

package picoded.struct.query;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class OrderBy_test {

	private OrderBy orderBy = new OrderBy<>("ASC");
	
	@Before
	public void setUp() {
		
	}
	
	@After
	public void tearDown() {
		
	}
	
	@Test
	public void orderTypeTest() {
		assertEquals("ASC", OrderBy.OrderType.ASC.toString());
	}
	
	@Test
	public void orderTypeDescTest() {
		assertEquals("DESC", OrderBy.OrderType.DESC.name());
	}
	
	@Test
	public void comparisionConfigTest() {
		assertNotNull("DESC", orderBy._comparisionConfig);
	}
	
	//@Test
	public void constructorNullParamTest() {
		assertNull(new OrderBy<String>(null));
	}
	
	@Test (expected = RuntimeException.class)
	public void constructorEmptyParamTest() {
		assertNull(new OrderBy<String>(""));
	}
	
	@Test 
	public void constructorComplexParamTest() {
		assertNotNull(new OrderBy<String>("name asc, dept desc"));
	}
	
	@Test 
	public void constructorInvalidParamTest() {
		assertEquals("\"name as\" ASC, \"dept\" DESC", new OrderBy<String>("name as, dept desc").toString());
	}
	
}

package picoded.servlet.api;

import static org.junit.Assert.*;
import org.junit.*;

public class ApiBuilder_test {
	
	/// The root API builder to test
	public ApiBuilder root = null;
	
	@Before
	public void setUp() {
		root = new ApiBuilder();
	}
	
	@After
	public void tearDown() {
		root = null;
	}
	
	@Test
	public void constructor() {
		assertNotNull(root);
	}
	
}

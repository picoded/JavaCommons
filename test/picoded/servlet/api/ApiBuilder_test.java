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
	
	@Test
	public void versioning() {
		assertNotNull(root.version("0"));
		
		ApiBuilder v = root.version("0");
		assertEquals(root.version("0"), v);
		assertNotEquals(root, v);
		
		assertNotEquals(root.version("0"), root.version("1"));
	}
	@Test(expected=UnsupportedOperationException.class)
	public void rootSetupException() {
		root.setup(null,null);
	}
	
	@Test
	public void helloWorld() {
		ApiBuilder v = root.version("0");
		ApiBuilder p = null;
		
		assertNotNull(p = v.path("hello/world"));
		assertEquals(p, v.path("hello.world"));
		assertNotEquals(v, p);
		
		assertNotNull(p.setup(
			// Definition function
			(def) -> {
				
			},
			// implmentation function
			(req,res) -> {
				
				return res;
			}
		));
	}
}

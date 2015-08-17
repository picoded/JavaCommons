package picodedTests.struct;

// Target test class
import picoded.JStruct.*;
import picoded.struct.*;

// Test Case include
import org.junit.*;
import static org.junit.Assert.*;

// other includes
import java.util.*;

///
/// Test Case for picoded.struct.LayeredMap
///
public class LayeredMap_test {
	
	public Map<String,Object> layer1 = null;
	public Map<String,Object> layer2 = null;
	public Map<String,Object> layer3 = null;
	
	public LayeredMap<String,Object> lmObj = null;
	
	@Before
	public void setUp() {
		lmObj = new LayeredMap<String,Object>();
		
		layer1 = new HashMap<String,Object>();
		layer2 = new HashMap<String,Object>();
		layer3 = new HashMap<String,Object>();
		
		lmObj.layers().add( layer1 );
		lmObj.layers().add( layer2 );
		lmObj.layers().add( layer3 );
	}
	
	@After
	public void tearDown() {
		lmObj = null;
	}
	
	@Test
	public void constructor() {
		assertNotNull(lmObj);
	}
	
	/// Test the following functions
	///
	/// + put
	/// + get
	@Test
	public void basicPutAndGet() {
		
	}
}

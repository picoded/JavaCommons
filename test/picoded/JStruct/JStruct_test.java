package picoded.JStruct;

// Target test class
import picoded.JStruct.*;

// Test Case include
import org.junit.*;
import org.junit.runners.*;
import static org.junit.Assert.*;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class JStruct_test {
	
	private static JStruct jStructObj = new JStruct();
	private static KeyValueMap keyValueMap;
	private static MetaTable metaTable;
	private static AccountTable accountTable;
	
	@BeforeClass
	public static void setUp() {
		jStructObj.systemSetup();
	}
	
	@AfterClass
	public static void tearDown() {
		jStructObj.systemTeardown();
		jStructObj = null;
	}
	
	@Test
	public void getKeyValueMapTest1() {
		keyValueMap = jStructObj.getKeyValueMap("test");
		assertNotNull(keyValueMap);
		assertTrue(keyValueMap.isEmpty());
	}
	
	@Test
	public void getKeyValueMapTest2() {
		KeyValueMap keyValueMapLocal = jStructObj.getKeyValueMap("test");
		assertNotNull(keyValueMapLocal);
		assertEquals(keyValueMap, keyValueMapLocal);
	}
	
	@Test
	public void getMetaTableTest1() {
		metaTable = jStructObj.getMetaTable("test");
		assertNotNull(metaTable);
		assertTrue(metaTable.isEmpty());
	}
	
	@Test
	public void getMetaTableTest2() {
		MetaTable metaTableLocal = jStructObj.getMetaTable("test");
		assertNotNull(metaTableLocal);
		assertEquals(metaTable, metaTableLocal);
	}
	
	@Test
	public void getAccountTableTest1() {
		accountTable = jStructObj.getAccountTable("test");
		assertNotNull(accountTable);
		assertTrue(accountTable.isEmpty());
	}
	
	@Test
	public void getAccountTableTest2() {
		AccountTable accountTableLocal = jStructObj.getAccountTable("test");
		assertNotNull(accountTableLocal);
		assertEquals(accountTable, accountTableLocal);
	}
	
}
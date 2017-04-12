package picoded.dstack.module.vfs;

// Target test class
import static org.junit.Assert.*;
import org.junit.*;

// Test depends
import java.util.*;
import picoded.dstack.*;
import picoded.dstack.module.vfs.*;
import picoded.dstack.struct.simple.*;

public class VirtualFileSystem_test {
	
	// Test object for reuse
	public VirtualFileSystem testVFS = null;
	
	// To override for implementation
	//-----------------------------------------------------
	
	/// Note that this implementation constructor
	/// is to be overriden for the various backend
	/// specific test cases
	public VirtualFileSystem implementationConstructor() {
		return new VirtualFileSystem(new StructSimple_MetaTable(), new StructSimple_MetaTable());
	}
	
	// Setup and sanity test
	//-----------------------------------------------------
	@Before
	public void systemSetup() {
		testVFS = implementationConstructor();
	}
	
	@After
	public void systemDestroy() {
		// if (testVFS != null) {
		// 	testVFS.systemDestroy();
		// }
		testVFS = null;
	}
	
	@Test
	public void constructorTest() {
		assertNotNull(testVFS);
	}

	@Test
	public void rootFileTest() {
		// Test data
		byte[] helloWorldBytes = "Hello World".getBytes();

		// Making sure file is null
		assertNull( testVFS.getVirtualFileObject("helloWorld.txt"));
		
		// Making sure the file is saved, with the right data
		VirtualFileObject testFile = testVFS.saveFile("helloWorld.txt" , helloWorldBytes);
		assertNotNull(testFile);
		assertEquals( new String(helloWorldBytes) , new String( testFile.getData() ) );

		// Making sure the return data is the same
		testFile = testVFS.getVirtualFileObject("helloWorld.txt");
		assertNotNull(testFile);
		assertEquals( new String(helloWorldBytes) , new String( testFile.getData() ) );
	}

	@Test
	public void rootFileSaveTwice() {
		// Test data
		byte[] data1 = "Good Morning".getBytes();
		byte[] data2 = "Good Night".getBytes();

		// Making sure the file is saved, with the right data
		assertNotNull( testVFS.saveFile("helloWorld.txt" , data1) );
		assertNotNull( testVFS.saveFile("helloWorld.txt" , data2) );

		// Ensure the file count is valid
		assertEquals( 1, testVFS.listFileNames().length );

		// Get files
		assertNotNull(testVFS.getVirtualFileObject("helloWorld.txt"));
		assertEquals( new String(data2) , new String( testVFS.getVirtualFileObject("helloWorld.txt").getData() ) );
	}
}

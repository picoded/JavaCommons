package picoded.dstack.module;

// Target test class
import static org.junit.Assert.*;
import org.junit.*;

// Test depends
import java.util.*;
import picoded.dstack.*;
import picoded.dstack.module.*;
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
	public void constructorSetupAndMaintenance() {
		// not null check
		assertNotNull(testVFS);
		
		// // run maintaince, no exception?
		// testObj.maintenance();
		
		// // run incremental maintaince, no exception?
		// testObj.incrementalMaintenance();
	}
	
	@Test
	public void folderCreation() {
		///creating the first folder and then checking for the length of the folder list
		assertEquals(0, testVFS.listFolderNames(null).length);
		assertTrue(testVFS.createFolder("hello"));
		assertEquals(1, testVFS.listFolderNames(null).length);
		assertEquals(new String[] { "hello" }, testVFS.listFolderNames(null));
		
		
		///creating the second folder and then checking for the folder list
		assertEquals(1, testVFS.listFolderNames("").length);
		assertTrue(testVFS.createFolder("helloWorld"));
		assertEquals(2, testVFS.listFolderNames(null).length);
		testVFS.listFolderNames(null);

		///creating third folder , but it has the same name as that of the second folder then , 
		///as the folder already exists , it throws a run time exception saying " the folder already exists "
	//	assertEquals(2, testVFS.listFolderNames(null).length);
	//	assertTrue(testVFS.createFolder("helloWorld"));


		/// creating the third folder with a different name
		assertEquals(2, testVFS.listFolderNames(null).length);
		assertTrue(testVFS.createFolder("helloNewWorld"));
		assertEquals(3, testVFS.listFolderNames(null).length);
	    
		///list of folders
		//assertEquals(new String[] { "helloWorld", "hello", "helloNewWorld" }, testVFS.listFolderNames(null));
	}
	
	// @Test
	// public void getingFolderByName() {
	//     assertTrue(testVFS.getFolderByName("hello"));
	// }
	
	// @Test
	// public void renamingFolder() {
	//     assertEquals(new String() , testVFS.renameFolder("hello1"));
	//     assertTrue(testVFS.getFolderByName("hello1"));
	// }
	
	// @Test
	// public void movingFolder() {
	//     assertTrue(testVFS.moveFolder(path));
	//     assertEquals(2, testVFS.listFolderNames((null).lengthg));
	// }
	
	// @Test 
	// public void deletingFolder() {
	//     assertTrue(testVFS.deleteFolders("hello"));
	// }
}

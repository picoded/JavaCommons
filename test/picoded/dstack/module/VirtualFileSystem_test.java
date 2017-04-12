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
	
	///FOLDER TESTS
	// @Test
	// public void simpleFolderCreation() {
	// 	//creating a simple folder with parent ID being null .i.e. parentID = ROOT
	// 	assertTrue(testVFS.createFolder("helloWorld"));
	
	// 	//checking if the folder is created or not
	// 	assertTrue(testVFS.hasFolder("helloWorld"));
	// }
	
	// @Test
	// public void simpleNestedFolderCreation() {
	
	// 	//creating a folder with in another folder .i.e. parentID != ROOT
	// 	assertTrue(testVFS.createFolder("parent/child"));
	
	// 	//checking if the folder is created or not
	// 	assertTrue(testVFS.hasFolder("parent/child"));
	
	// 	//creating another folder inside the child folder
	// 	assertTrue(testVFS.createFolder("parent/child/child2"));
	
	// 	//checking if the above folder is created 
	// 	assertTrue(testVFS.hasFolder("parent/child/child2"));
	
	// 	//creating a folder with the parentID = ROOT
	// 	assertTrue(testVFS.createFolder("java"));
	
	// 	//creating another folder taking the above folder created as a parent folder
	// 	//here it should check if the java folder exists and then create javaScript under the java folder
	// 	assertTrue(testVFS.createFolder("java/javaScript"));
	
	// 	//check for folder javaScript
	// 	//it should throw an error as its root folder is not null
	// 	assertFalse(testVFS.hasFolder("javaScript"));
	// 	assertTrue(testVFS.hasFolder("java/javaScript"));
	
	// 	//check for the parent folder 
	// 	//assertTrue(testVFS.hasFolder("parent"));
	//     assertTrue(testVFS.hasFolder("java"));
	
	// }
	
	///FILE TESTS
	@Test
	//	public void simpleFileCreation() {
	/// creating a simple file in the root
	//		assertTrue(testVFS.createFile("firstFile"));
	///checking of the file has been created
	//		assertTrue(testVFS.hasFile("ROOT", "firstFile"));
	//	}
	//	public void simpleAddFileContent() {
	///adding content to the file which ia already created 
	//	assertTrue(testVFS.createFileContent("ROOT" , "firstFile" , "firstFileContent"));
	///adding content to the file that does not exists
	//assertTrue(testVFS.createFileContent("ROOT", "secondFile", "secondFileContent"));
	///cheking for the content added
	//	assertTrue(testVFS.hasFileContent("ROOT", "firstFile", "firstFileContent"));
	//}
	public void simpleFileContent() {
		byte[] helloWorldBytes = "Hello World".getBytes();
		
		assertTrue(testVFS.saveFile("firstFile", helloWorldBytes));
		assertEquals(helloWorldBytes, testVFS.getFile("firstFile"));
	}
}

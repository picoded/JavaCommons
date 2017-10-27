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
		return new VirtualFileSystem(new StructSimple_DataTable(), new StructSimple_DataTable());
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
		assertNull(testVFS.getFile("helloWorld.txt"));
		
		// Making sure the file is saved, with the right data
		VirtualFileObject testFile = testVFS.saveFile("helloWorld.txt", helloWorldBytes);
		assertNotNull(testFile);
		assertEquals(new String(helloWorldBytes), new String(testFile.getData()));
		
		// Making sure the return data is the same
		testFile = testVFS.getFile("helloWorld.txt");
		assertNotNull(testFile);
		assertEquals(new String(helloWorldBytes), new String(testFile.getData()));
	}
	
	@Test
	public void listRootFileTest() {
		
		//test data1
		byte[] data5 = "Fast and Robust".getBytes();
		byte[] data6 = "UI Test Automation".getBytes();
		
		//making sure the file is null
		assertNull(testVFS.getFile("uilicious1.txt"));
		assertNull(testVFS.getFile("uilicious2.txt"));
		
		//creating and returning the file with the data
		VirtualFileObject testFile1 = testVFS.saveFile("uilicious1.txt", data5);
		VirtualFileObject testFile2 = testVFS.saveFile("uilicious2.txt", data6);
		assertNotNull(testFile1);
		assertEquals(new String(data5), new String(testFile1.getData()));
		assertNotNull(testFile2);
		assertEquals(new String(data6), new String(testFile2.getData()));
		
		// list the number of root files
		assertEquals(2, testVFS.listFileNames().length);
	}
	
	@Test
	public void rootFileSaveTwice() {
		// Test data
		byte[] data1 = "Good Morning".getBytes();
		byte[] data2 = "Good Night".getBytes();
		
		// Making sure the file is saved, with the right data
		assertNotNull(testVFS.saveFile("helloWorld.txt", data1));
		assertEquals(1, testVFS.listFileNames().length);
		assertEquals(new String(data1), new String(testVFS.getFile("helloWorld.txt").getData()));
		
		assertNotNull(testVFS.saveFile("helloWorld1.txt", data2));
		assertEquals(2, testVFS.listFileNames().length);
		assertEquals(new String(data2), new String(testVFS.getFile("helloWorld1.txt").getData()));
	}
	
	@Test
	public void rootFolderTest() {
		
		//Make sure that the folder is null
		assertNull(testVFS.getDirectory("folder1"));
		
		//Creating and returning a root folder
		assertNotNull(testVFS.makeDirectory("folder1"));
		assertNotNull(testVFS.getDirectory("folder1"));
	}
	
	@Test
	public void folderFileTest() {
		
		//test data
		byte[] data3 = "Good Evening".getBytes();
		byte[] data4 = "Good".getBytes();
		
		//creating and returning folder under the root
		VirtualFileObject folder = testVFS.makeDirectory("folder");
		assertNotNull(testVFS.getDirectory("folder"));
		
		//creating a second folder under the root
		VirtualFileObject folder2 = testVFS.makeDirectory("folder2");
		assertNotNull(testVFS.getDirectory("folder2"));
		
		//creating and returning a file under the first folder
		VirtualFileObject file = folder.saveFile("hello.txt", data3);
		assertNotNull(file);
		assertEquals(new String(data3), new String(file.getData()));
		
		///creating and returning another file under the first folder
		VirtualFileObject file1 = folder.saveFile("hello1.txt", data4);
		assertNotNull(file1);
		assertEquals(new String(data4), new String(file1.getData()));
		
		/// list of the files created under the first folder
		assertEquals(2, folder.listFileNames().length);
		
		///list of the files under the second folder
		assertEquals(0, folder2.listFileNames().length);
		
		///list teh number of folders under the root
		///now the number of folders under the root is one
		assertEquals(2, testVFS.listDirectoryNames().length);
	}
	
	@Test
	//this test throws an error as the file cant be created under another file
	//it has to be a folder
	public void fileInFileTest() {
		
		//test data
		byte[] data7 = "Software".getBytes();
		byte[] data8 = "engineer".getBytes();
		
		Exception caughtError = null;
		
		try {
			//creating a file in the root
			VirtualFileObject file = testVFS.saveFile("fileSoftware", data7);
			//assertNotNull(testVFS.getFile("fileSoftware"));
			
			//creating and returning a file under the other file
			assertNotNull(file.saveFile("fileEngineer", data8));
			//assertNotNull(file)
			
			//list the files under the root
			assertEquals(1, testVFS.listFileNames().length);
			
			// list the file under the other file
			assertEquals(1, file.listFileNames().length);
		} catch (Exception e) {
			caughtError = e;
		}
	}
	
	@Test
	public void folderInFolder() {
		
		//creating a folder in the root
		VirtualFileObject rootFolder = testVFS.makeDirectory("rFolder");
		assertNotNull(rootFolder);
		
		//creating a folder under the root folder
		assertNotNull(rootFolder.makeDirectory("secondFolder"));
		assertNotNull(rootFolder.getDirectory("secondFolder"));
		
	}
	
	@Test
	public void fileInFolder() {
		
		//test data
		byte[] data9 = "Morning".getBytes();
		byte[] data10 = "Evening".getBytes();
		byte[] data11 = "Night".getBytes();
		
		//creating and returning a root folder
		VirtualFileObject rFolder = testVFS.makeDirectory("rootFolder");
		assertNotNull(rFolder);
		
		//creating and returning a folder under the root folder
		VirtualFileObject sFolder = rFolder.makeDirectory("anotherFolder");
		assertNotNull(sFolder);
		
		//creating and returning a file under the root
		VirtualFileObject rFile = testVFS.saveFile("rootFile", data9);
		assertNotNull(rFile);
		
		//create and return a file under the root folder
		VirtualFileObject sFile = rFolder.saveFile("secondFile", data10);
		assertNotNull(sFile);
		
		//create and return a file under the second folder that is created under the root folder
		VirtualFileObject uFile = sFolder.saveFile("thirdFile", data11);
		assertNotNull(uFile);
	}
	
	@Test
	public void duplicateNameForFileAndFolder() {
		
		//test data
		byte[] data12 = "Friends".getBytes();
		
		//creating and returning a folder in the root
		assertNotNull(testVFS.makeDirectory("friends"));
		assertNotNull(testVFS.getDirectory("friends"));
		
		Exception caughtError = null;
		VirtualFileObject fFile = null;
		try {
			//creating and returning a file in the root with the same name as that of the folder name
			//it will throw an error stating that -- "the name already exists for the folder"
			fFile = testVFS.saveFile("friends", data12);
		} catch (Exception e) {
			caughtError = e;
		}
		assertNotNull(caughtError);
		assertNull(fFile);
		
		//list the files in the root
		assertEquals(0, testVFS.listFileNames().length);
		
		//list the folders in the root
		assertEquals(1, testVFS.listDirectoryNames().length);
	}
	
	@Test
	public void duplicateNameFiles() {
		
		//test data
		byte[] data13 = "software".getBytes();
		byte[] data14 = "engineeer".getBytes();
		// byte [] data15 = "developer".getBytes();
		
		//creating two files with the same name in the root and returning them
		//we cant create two files with the same name
		//it will throw error stating - "file name already exists"
		VirtualFileObject firstFile = testVFS.saveFile("uiliciousFileName", data13);
		assertNotNull(firstFile);
		assertEquals(new String(data13), new String(firstFile.getData()));
		
		Exception caughtError = null;
		VirtualFileObject secondFile = null;
		
		try {
			//when we create the second file with the same name as the first file
			//then it will throw an error stating that the file name already exisists
			secondFile = testVFS.saveFile("uiliciousFileName", data14);
		} catch (Exception e) {
			caughtError = e;
		}
		
		assertNotNull(caughtError);
		assertNull(secondFile);
		
		//list the number of files in the root
		assertEquals(1, testVFS.listFileNames().length);
	}
	
	//
	@Test
	public void duplicateNameFolders() {
		
		//creating two folders in the root with the same name
		//this will throw a error stating  - "the folder name already exists"
		//so , we cant create folders with the same name
		
		assertNotNull(testVFS.makeDirectory("firstFolderName"));
		assertNotNull(testVFS.getDirectory("firstFolderName"));
		
		//creating the second folder with the same name
		Exception caughtError1 = null;
		VirtualFileObject duplicateName = null;
		try {
			duplicateName = testVFS.makeDirectory("firstFolderName");
		} catch (Exception e) {
			caughtError1 = e;
		}
		
		assertNotNull(caughtError1);
		assertNull(duplicateName);
		
		//list the number of folders created in the root
		assertEquals(1, testVFS.listDirectoryNames().length);
	}
	
	@Test
	public void deleteExistingFolder() {
		
		//create a new folder
		
		assertNotNull(testVFS.makeDirectory("firstFolder"));
		assertNotNull(testVFS.getDirectory("firstFolder"));
		
		assertEquals(1, testVFS.listDirectoryNames().length);
		
		//delete the folder which we created above
		assertNotNull(testVFS.deleteDirectory("firstFolder"));
		
		//check if the folder still exists
		assertEquals(0, testVFS.listDirectoryNames().length);
		
		//files.remove(obj._oid());
	}
	
	@Test
	public void deleteExistingFile() {
		
		//create a new folder
		
		//test data
		byte[] data20 = "testFile".getBytes();
		
		//VirtualFileObject deleteFile = null;
		
		VirtualFileObject deleteFile = testVFS.saveFile("temporaryFile", data20);
		assertEquals(1, testVFS.listFileNames().length);
		
		//delete the file which we created above
		assertNotNull(testVFS.deleteFile("temporaryFile"));
		
		//check if the folder still exists
		assertEquals(0, testVFS.listFileNames().length);
		
		//files.remove(obj._oid());
	}
	
	@Test
	public void deleteFileUnderFolder() {
		
		//test data
		byte[] data21 = "deletefile".getBytes();
		
		//create a new folder under the root
		VirtualFileObject rootFolder = testVFS.makeDirectory("rootFolder");
		
		//geting the length od folders in the root
		assertEquals(1, testVFS.listDirectoryNames().length);
		
		//create file under the folder
		VirtualFileObject fileUndeerFolder = rootFolder.saveFile("fileUnderFolder", data21);
		
		//geting the length of the files inside the folder
		assertEquals(1, rootFolder.listFileNames().length);
		
		//delete the folder and the files created under the folder
		assertNotNull(testVFS.deleteDirectory("rootFolder"));
		
		//reconfirming that the folder and the files inside the folder are deleted
		assertNull(testVFS.getDirectory("rootFolder"));
		assertNull(rootFolder.getFile("fileUnderFolder"));
		
	}
	
	@Test
	public void deleteFileAndFolderUnderFolder() {
		
		//test data
		byte[] data22 = "deleteFile1".getBytes();
		byte[] data23 = "deleteFile2".getBytes();
		
		//create a new folder in the root
		VirtualFileObject rootFolderMain = testVFS.makeDirectory("MainFolder");
		
		//create a second folder in the root
		VirtualFileObject secondRootFolder = testVFS.makeDirectory("SecondMainFolder");
		
		//create a file under the root folder
		VirtualFileObject fileUnderMainFolder = rootFolderMain.saveFile("firstFileUnderTheFolder",
			data22);
		
		//create a folder under the root folder
		VirtualFileObject folderUnderMainFolder = rootFolderMain
			.makeDirectory("firstFolderUnderTheMain");
		
		//create a file under the folder created under teh root folder
		VirtualFileObject secondFileUnderSecondFolder = folderUnderMainFolder.saveFile(
			"secondFileUnderSecondFolder", data23);
		
		//get the length of the folders in the root
		assertEquals(2, testVFS.listDirectoryNames().length);
		
		//get the length of files in the root folder
		assertEquals(1, rootFolderMain.listFileNames().length);
		
		//get the length of the folders in the root folder
		assertEquals(1, rootFolderMain.listDirectoryNames().length);
		
		//get the length of the files inside the folder under the root folder
		assertEquals(1, folderUnderMainFolder.listFileNames().length);
		
		//delete the root folder
		assertNotNull(testVFS.deleteDirectory("MainFolder"));
		
		//get the list of the folders in the root
		assertEquals(1, testVFS.listDirectoryNames().length);
		
		//get the length of files in the root folder
		assertEquals(0, rootFolderMain.listFileNames().length);
		
		//get the length of the folders in the root folder
		assertEquals(0, rootFolderMain.listDirectoryNames().length);
		
		//get the length of the files inside the folder under the root folder
		assertEquals(0, folderUnderMainFolder.listFileNames().length);
		
	}
	
	@Test
	public void fileToMove() {
		
		byte[] data24 = "movefile".getBytes();
		
		//create a file in the root
		VirtualFileObject beforeFileMove = testVFS.saveFile("firstFile", data24);
		
		//move the file
		VirtualFileObject afterMoveFile = testVFS.moveFile("firstFile", "secondFile");
		
		//get the new moved file
		assertNotNull(testVFS.getFile("secondFile"));
	}
	
	@Test
	public void folderToMove() {
		
		//create a folder in the root
		VirtualFileObject beforeFolderMove = testVFS.makeDirectory("firstFolder");
		
		//move the folder
		VirtualFileObject afterMoveFile = testVFS.moveDirectory("firstFolder", "secondFolder");
		
		//get the moved folder
		assertNotNull(testVFS.getDirectory("secondFolder"));
	}
	
	@Test
	public void moveFilesUnderFolder() {
		
		//test data
		byte[] data25 = "movefilesunderfolder".getBytes();
		
		//create a folder in the root
		VirtualFileObject mainFolder = testVFS.makeDirectory("MainFolder");
		
		//create the file inside the folder
		VirtualFileObject file = mainFolder.saveFile("firstFile", data25);
		
		//get the length of the files inside the folder
		assertEquals(1, mainFolder.listFileNames().length);
		
		//move the folder in the root
		VirtualFileObject movedFolder = testVFS.moveDirectory("MainFolder", "Folder");
		
		//get the length of the files inside the moved folder
		assertEquals(1, movedFolder.listFileNames().length);
	}
}

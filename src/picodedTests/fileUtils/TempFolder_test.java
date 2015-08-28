package picodedTests.fileUtils;

import static org.junit.Assert.*;

import java.io.File;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.lesscss.deps.org.apache.commons.io.FileUtils;

import picoded.fileUtils.PDFGenerator;
import picoded.fileUtils.TempFolder;

public class TempFolder_test{
	
	private String _tempFolderName = "testTempFolder";
	private TempFolder _tempFolder = null;
	private File _tempFile = null;
	
	private static String writeStringFileName = "testFileWriteString.txt";
	private static String writeByteArrayFileName = "testFileWriteByteArray.txt";
	
	@BeforeClass
	public static void setUp() {
		File parentFile = null;
		try{
			parentFile = new File(TempFolder.getTempFolderPath());
			FileUtils.deleteDirectory(parentFile);
		}catch(Exception e){
		}
	}
	
	@AfterClass
	public static void checkWrittenData(){
		File parentFile = null;
		try{
			parentFile = new File(TempFolder.getTempFolderPath());
			
			File writeStringFile = new File(parentFile.getPath() + "/" +writeStringFileName);
			assertEquals("hello there", FileUtils.readFileToString(writeStringFile));
			
			
			File writeByteArrayFile = new File(parentFile.getPath() + "/" +writeByteArrayFileName);
			assertEquals(new byte[]{1, 2, 3, 4, 5}, FileUtils.readFileToByteArray(writeByteArrayFile));	
		}catch(Exception e){
		}
	}
	
	@Test
	public void createNamedTempFolder(){
		_tempFolder = new TempFolder(_tempFolderName);
		
		assertNotNull(_tempFolder);
		
		try{
			_tempFile = _tempFolder.getTempFolder();
			assertNotNull(_tempFile);
			
		}catch(Exception e){
			
		}
	}
	
	@Test
	public void createGUIDTempFolder(){
		_tempFolder = new TempFolder();
		
		assertNotNull(_tempFolder);
		
		try{
			_tempFile = _tempFolder.getTempFolder();
			assertNotNull(_tempFile);
			
			String _tempFileName = _tempFile.getName();
			assertNotNull(_tempFileName);
			assertTrue(_tempFile.exists());
			assertFalse(_tempFileName.equalsIgnoreCase(""));
		}catch(Exception e){
			
		}
	}
	
	@Test
	public void createTempFolderChildFile(){
		_tempFolder = new TempFolder(_tempFolderName);
		String childFileName = "testFileA";
		
		assertNotNull(_tempFolder);
		
		try{
			_tempFile = _tempFolder.getTempFolder();
			assertNotNull(_tempFile);
			
			File childFile = _tempFolder.createChildFile(childFileName);
			assertNotNull(childFile);
		}catch(Exception e){
			
		}
	}
	
	@Test
	public void writeStringToChildFile(){
		_tempFolder = new TempFolder(_tempFolderName);
		String childFileName = "testFileWriteString.txt";
		
		assertNotNull(_tempFolder);
		
		try{
			_tempFile = _tempFolder.getTempFolder();
			assertNotNull(_tempFile);
			
			assertTrue(_tempFolder.writeToChildFile(childFileName, "hello there"));
		}catch(Exception e){
			
		}
	}
	
	@Test
	public void writeByteArrayToChildFile(){
		_tempFolder = new TempFolder(_tempFolderName);
		String childFileName = "testFileWriteByteArray.txt";
		
		assertNotNull(_tempFolder);
		
		try{
			_tempFile = _tempFolder.getTempFolder();
			assertNotNull(_tempFile);
			
			assertTrue(_tempFolder.writeToChildFile(childFileName, new byte[]{1, 2, 3, 4, 5}));
		}catch(Exception e){
			
		}
	}
}
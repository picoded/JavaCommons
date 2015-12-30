package picodedTests.webTemplateEngines.JSML;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import org.junit.*;
import org.lesscss.deps.org.apache.commons.io.FileUtils;

import static org.junit.Assert.*;
import picoded.conv.ConvertJSON;
import picoded.webTemplateEngines.JSML.JSMLForm;
import picoded.webTemplateEngines.JSML.JSMLFormSet;
import picodedTests.BaseTestClass;

public class JSMLForm_test {
	
	static JSMLForm jsmlForm = null;
	static JSMLFormSet jsmlFormSet = null;
	static Map<String, Object> data = null;
	
	static String rootFolder = "./test-files/test-specific/webTemplateEngines/JSML/JSMLForm"; //absolute root folder path
	static String contextFolder = "./test-files/test-specific/webTemplateEngines/JSML/JSMLForm";
	static String tempFolder = "tmp"; //absolute path to resource folder
	
	@BeforeClass
	public static void classSetUp() {
		jsmlFormSet = new JSMLFormSet(rootFolder, contextFolder);
		
		jsmlForm = new JSMLForm(rootFolder, contextFolder, tempFolder);
		
		assertNotNull(jsmlForm);
		
		try {
			File declareFile = new File(rootFolder + "/formDeclare.json");
			String declareFileString = FileUtils.readFileToString(declareFile);
			jsmlForm.setDefinition(declareFileString);
			
			File dataFile = new File(rootFolder + "/formData.json");
			String dataFileString = FileUtils.readFileToString(dataFile);
			data = ConvertJSON.toMap(dataFileString);
			
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
	}
	
	//	@Test
	public void generateHTMLDataTest() {
		StringBuilder sb = jsmlForm.generateHTML(data, false);
		assertNotNull(sb);
		
		String rawHTML = sb.toString();
		assertFalse(rawHTML.isEmpty());
		
		try {
			FileWriter fw = new FileWriter(new File(rootFolder + "/generated/generatedDisplayHTML.html"));
			fw.write(rawHTML);
			fw.flush();
			fw.close();
		} catch (Exception e) {
			
		}
	}
	
	//	@Test
	public void generateDisplayTest() {
		StringBuilder sb = jsmlForm.generateHTML(data, true);
		assertNotNull(sb);
		
		String rawHTML = sb.toString();
		assertFalse(rawHTML.isEmpty());
		
		try {
			FileWriter fw = new FileWriter(new File(rootFolder + "/generated/generatedDisplay.html"));
			fw.write(rawHTML);
			fw.flush();
			fw.close();
		} catch (Exception e) {
			
		}
	}
	
	@Test
	public void multipleDeclareTest() {
		try {
			File dataFile = new File(rootFolder + "/main/dummyData.json");
			String dataFileString = FileUtils.readFileToString(dataFile);
			data = ConvertJSON.toMap(dataFileString);
			
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		
		jsmlForm = jsmlFormSet.get("main");
		
		String rawHTML = jsmlForm.generateHTML(data, false).toString();
		assertFalse(rawHTML.isEmpty());
		
		try {
			FileWriter fw = new FileWriter(new File(rootFolder + "/main/multipleDeclare.html"));
			fw.write(rawHTML);
			fw.flush();
			fw.close();
		} catch (Exception e) {
			
		}
	}
	
	@Test
	public void clearTempFilesTest() {
		//jsmlForm = jsmlFormSet.get("main");
		String tmpFolderPath = rootFolder + "/" + tempFolder;
		
		// create a file in temp folder
		createFile(tmpFolderPath);
		
		// clear the temp folder
		jsmlFormSet.clearTempFiles();
		//jsmlForm.clearTempFilesOlderThenGivenAgeInSeconds(1);
		
		int count = 0;
		fileCount(tmpFolderPath, count);
		
		assertEquals(0, count);
	}
	
	private void fileCount(String dirPath, int count) {
		File folder = new File(dirPath);
		File[] files = folder.listFiles();
		
		if (files != null)
			for (int i = 0; i < files.length; i++) {
				
				if (files[i].isDirectory()) {
					fileCount(files[i].getAbsolutePath(), count);
				} else {
					count++;
				}
			}
	}
	
	private void createFile(String dirPath) {
		BufferedWriter fileWriter = null;
		try {
			File dirFile = new File(dirPath);
			if (!dirFile.exists()) {
				dirFile.mkdirs();
			}
			// Create file
			fileWriter = new BufferedWriter(new FileWriter(dirPath + "/test.txt"));
			// Write to output stream
			fileWriter.write("Hello Java");
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} finally {
			// Close the output stream
			if (fileWriter != null) {
				try {
					fileWriter.close();
				} catch (Exception e) {
				}
			}
		}
	}
	
}

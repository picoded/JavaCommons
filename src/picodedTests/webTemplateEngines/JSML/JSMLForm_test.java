package picodedTests.webTemplateEngines.JSML;

import java.io.File;
import java.io.FileWriter;
import java.util.*;

import org.junit.*;
import org.lesscss.deps.org.apache.commons.io.FileUtils;

import static org.junit.Assert.*;
import picoded.conv.ConvertJSON;
import picoded.webTemplateEngines.JSML.JSMLForm;

public class JSMLForm_test{
	
	static JSMLForm jsmlForm = null;
	static Map<String, Object> data = null;
	
	static String rootFolder = "./test-files/test-specific/htmlGenerator/JSML";
	static String tempFolder = rootFolder + "/resources/tmp";
	static String contextFolder = "file:///C:/Users/Samuel/workspace/JavaCommons/test-files/test-specific/htmlGenerator/JSML/";
	
	@BeforeClass
	public static void classSetUp(){
		jsmlForm = new JSMLForm(rootFolder, contextFolder, tempFolder);
		
		assertNotNull(jsmlForm);
		
		try{
			File declareFile = new File(rootFolder + "/formDeclare.json");
			String declareFileString = FileUtils.readFileToString(declareFile);
			jsmlForm.setDefinition(declareFileString);
			
			File dataFile = new File(rootFolder + "/formData.json");
			String dataFileString = FileUtils.readFileToString(dataFile);
			data = ConvertJSON.toMap(dataFileString);
		}catch(Exception e){
			throw new RuntimeException(e.getMessage());
		}
	}
	
	@Test
	public void generateHTMLTest(){
		StringBuilder sb = jsmlForm.generateHTML(null, false);
		assertNotNull(sb);
		
		String rawHTML = sb.toString();
		assertFalse(rawHTML.isEmpty());
		
		try{
			FileWriter fw = new FileWriter(new File(rootFolder+"/generated/generatedHTML.html"));
			fw.write(rawHTML);
			fw.flush();
			fw.close();
		}catch(Exception e){
			
		}
	}
	
	@Test
	public void generateHTMLDataTest(){
		StringBuilder sb = jsmlForm.generateHTML(data, false);
		assertNotNull(sb);
		
		String rawHTML = sb.toString();
		assertFalse(rawHTML.isEmpty());
		
		try{
			FileWriter fw = new FileWriter(new File(rootFolder+"/generated/generatedDisplayHTML.html"));
			fw.write(rawHTML);
			fw.flush();
			fw.close();
		}catch(Exception e){
			
		}
	}
	
	@Test
	public void generateDisplayTest(){
		StringBuilder sb = jsmlForm.generateHTML(data, true);
		assertNotNull(sb);
		
		String rawHTML = sb.toString();
		assertFalse(rawHTML.isEmpty());
		
		try{
			FileWriter fw = new FileWriter(new File(rootFolder+"/generated/generatedDisplay.html"));
			fw.write(rawHTML);
			fw.flush();
			fw.close();
		}catch(Exception e){
			
		}
	}
	
	@Test
	public void generatePDFTest(){
		byte[] pdfData = jsmlForm.generatePDF(data, true);
		assertNotNull(pdfData);
		assertTrue(pdfData.length > 0);
	}
}

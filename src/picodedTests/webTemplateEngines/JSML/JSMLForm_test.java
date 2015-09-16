package picodedTests.webTemplateEngines.JSML;

import java.io.File;
import java.io.FileWriter;
import java.util.*;

import org.junit.*;
import org.lesscss.deps.org.apache.commons.io.FileUtils;

import static org.junit.Assert.*;
import picoded.conv.ConvertJSON;
import picoded.webTemplateEngines.JSML.JSMLForm;
import picoded.webTemplateEngines.JSML.JSMLFormSet;

public class JSMLForm_test{
	
	static JSMLForm jsmlForm = null;
	static JSMLFormSet jsmlFormSet = null;
	static Map<String, Object> data = null;
	
	/// @TODO : =.= stop absoluting path to your PC only, this test fails on other computers
//	static String rootFolder = "C:/Users/Samuel/workspace/JavaCommons/test-files/test-specific/htmlGenerator/JSML"; //absolute root folder path
//	static String contextFolder = "C:/Users/Samuel/workspace/JavaCommons/test-files/test-specific/htmlGenerator/JSML"; //on pc, absolute path, but on tomcat, its contextURI
	
	
	
	static String rootFolder = "./test-files/test-specific/htmlGenerator/JSML"; //absolute root folder path
	static String contextFolder = "./test-files/test-specific/htmlGenerator/JSML";
	static String tempFolder = rootFolder + "/tmp"; //absolute path to resource folder
	
	@BeforeClass
	public static void classSetUp(){
		jsmlFormSet = new JSMLFormSet(rootFolder, contextFolder);
		
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
	
//	@Test
	public void generateHTMLTest(){
		//to remove
		jsmlForm = new JSMLForm(rootFolder + "/prefixtest", contextFolder + "/prefixtest", tempFolder);
		try{
			File declareFile = new File(rootFolder + "/prefixtest/formDeclare.json");
			String declareFileString = FileUtils.readFileToString(declareFile);
			jsmlForm.setDefinition(declareFileString);
			
			File dataFile = new File(rootFolder + "/prefixtest/formData.json");
			String dataFileString = FileUtils.readFileToString(dataFile);
			data = ConvertJSON.toMap(dataFileString);
			
		}catch(Exception e){
			throw new RuntimeException(e.getMessage());
		}//to remove
		
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
	
//	@Test
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
	
//	@Test
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
	
//	@Test
	public void generatePDFTest(){
		byte[] pdfData = jsmlForm.generatePDF(data);
		assertNotNull(pdfData);
		assertTrue(pdfData.length > 0);
	}
	
	@Test
	public void multipleDeclareTest(){
		try{
			File dataFile = new File(rootFolder + "/main/dummyData.json");
			String dataFileString = FileUtils.readFileToString(dataFile);
			data = ConvertJSON.toMap(dataFileString);
			
		}catch(Exception e){
			throw new RuntimeException(e.getMessage());
		}
		
		jsmlForm = jsmlFormSet.get("main");
		
		String rawHTML = jsmlForm.generateHTML(data, false).toString();
		assertFalse(rawHTML.isEmpty());
		
		try{
			FileWriter fw = new FileWriter(new File(rootFolder+"/main/multipleDeclare.html"));
			fw.write(rawHTML);
			fw.flush();
			fw.close();
		}catch(Exception e){
			
		}
	}
}

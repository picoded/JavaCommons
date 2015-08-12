package picodedTests.webTemplateEngines;

import picoded.conv.ConvertJSON;
import picoded.webTemplateEngines.*;

import org.apache.commons.io.FileUtils;
import org.junit.*;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileWriter;
import java.nio.charset.Charset;
import java.util.*;

public class FormGenerator_test {
	
	public FormGenerator testObj = null;
	
	@Before
	public void setUp() {
		testObj = new FormGenerator();
	}
	
	@After
	public void tearDown() {
		testObj = null;
	}
	
	@Test
	public void constructor() {
		assertNotNull(testObj);
	}
	
	private Map<String, Object> getPrefilledData(){
		File dataFile = new File("./test-files/test-specific/htmlGenerator/prefilledData.js");
		try{
			String jsonDataString = FileUtils.readFileToString(dataFile);
			Map<String,Object> prefilledData = ConvertJSON.toMap(jsonDataString);
			return prefilledData;
		}catch(Exception ex){
			return null;
		}
	}
	
//	@Test
	public void getHtmlAndPDFOutputFromJSONObject(){
		File jsonObjectFile = new File("./test-files/test-specific/htmlGenerator/testJSONObject.js");
		assertTrue(jsonObjectFile.canRead());
		String jsonFileString = "";
		try{
			jsonFileString = FileUtils.readFileToString(jsonObjectFile, Charset.defaultCharset());
		} catch (Exception ex){
			
		}
		
		//html portion
		String htmlVal = testObj.applyTemplating(jsonFileString, getPrefilledData());
		File htmlFile = new File("./test-files/test-specific/htmlGenerator/generatedFiles/htmlFromJSONObject.html");
		if(htmlFile.exists()){
			htmlFile.delete();
		}
		
		try{
			FileWriter writer = new FileWriter(htmlFile);
			writer.write(htmlVal);
			writer.flush();
			writer.close();
		}catch(Exception ex){
			
		}
		
		//pdf portion
		String pdfReadyHtmlString = testObj.generatePDFReadyHTML(jsonFileString, getPrefilledData());
		File pdfReadyHtmlFile = new File("./test-files/test-specific/htmlGenerator/generatedFiles/pdfReadyHtmlFromJSONObject.html");
		if(pdfReadyHtmlFile.exists()){
			pdfReadyHtmlFile.delete();
		}
		
		try{
			FileWriter writer = new FileWriter(pdfReadyHtmlFile);
			writer.write(pdfReadyHtmlString);
			writer.flush();
			writer.close();
		}catch(Exception ex){
			
		}
		
		String pdfFileString = "./test-files/test-specific/htmlGenerator/generatedFiles/pdfFromJSONObject.pdf";
		picoded.fileUtils.PDFGenerator.generatePDFfromRawHTML(pdfFileString, pdfReadyHtmlString);
	}
	
//	@Test
	public void getHtmlAndPDFOutputFromJSONArray(){
		File jsonObjectFile = new File("./test-files/test-specific/htmlGenerator/testJSONArray.js");
		assertTrue(jsonObjectFile.canRead());
		String jsonFileString = "";
		try{
			jsonFileString = FileUtils.readFileToString(jsonObjectFile, Charset.defaultCharset());
		} catch (Exception ex){
			
		}
		
		//html portion
		String htmlVal = testObj.applyTemplating(jsonFileString, getPrefilledData());
		File htmlFile = new File("./test-files/test-specific/htmlGenerator/generatedFiles/htmlFromJSONArray.html");
		if(htmlFile.exists()){
			htmlFile.delete();
		}
		
		try{
			FileWriter writer = new FileWriter(htmlFile);
			writer.write(htmlVal);
			writer.flush();
			writer.close();
		}catch(Exception ex){
			
		}
		
		//pdf portion
		String pdfReadyHtmlString = testObj.generatePDFReadyHTML(jsonFileString, getPrefilledData());
		File pdfReadyHtmlFile = new File("./test-files/test-specific/htmlGenerator/generatedFiles/pdfReadyHtmlFromJSONArray.html");
		if(pdfReadyHtmlFile.exists()){
			pdfReadyHtmlFile.delete();
		}
		
		try{
			FileWriter writer = new FileWriter(pdfReadyHtmlFile);
			writer.write(pdfReadyHtmlString);
			writer.flush();
			writer.close();
		}catch(Exception ex){
			
		}
		
		String pdfFileString = "./test-files/test-specific/htmlGenerator/generatedFiles/pdfFromJSONArray.pdf";
		picoded.fileUtils.PDFGenerator.generatePDFfromRawHTML(pdfFileString, pdfReadyHtmlString);
	}
	
	@Test
	public void getHtmlAndPDFOutputFromJSONKeysObject(){
		File jsonObjectFile = new File("./test-files/test-specific/htmlGenerator/testJSONKeys.js");
		assertTrue(jsonObjectFile.canRead());
		String jsonFileString = "";
		try{
			jsonFileString = FileUtils.readFileToString(jsonObjectFile, Charset.defaultCharset());
		} catch (Exception ex){
			
		}
		
		//html portion
		String htmlVal = testObj.applyTemplating(jsonFileString, getPrefilledData());
		File htmlFile = new File("./test-files/test-specific/htmlGenerator/generatedFiles/htmlFromJSONKeys.html");
		if(htmlFile.exists()){
			htmlFile.delete();
		}
		
		try{
			FileWriter writer = new FileWriter(htmlFile);
			writer.write(htmlVal);
			writer.flush();
			writer.close();
		}catch(Exception ex){ 
			
		}
		
		//pdf portion
		String pdfReadyHtmlString = testObj.generatePDFReadyHTML(jsonFileString, getPrefilledData());
		File pdfReadyHtmlFile = new File("./test-files/test-specific/htmlGenerator/generatedFiles/pdfReadyHtmlFromJSONKeys.html");
		if(pdfReadyHtmlFile.exists()){
			pdfReadyHtmlFile.delete();
		}
		
		try{
			FileWriter writer = new FileWriter(pdfReadyHtmlFile);
			writer.write(pdfReadyHtmlString);
			writer.flush();
			writer.close();
		}catch(Exception ex){
			
		}
		
		String pdfFileString = "./test-files/test-specific/htmlGenerator/generatedFiles/pdfFromJSONKeys.pdf";
		picoded.fileUtils.PDFGenerator.generatePDFfromRawHTML(pdfFileString, pdfReadyHtmlString);
	}
}

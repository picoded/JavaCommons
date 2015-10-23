package picodedTests.webTemplateEngines.FormGenerator;

import picoded.conv.ConvertJSON;
import picoded.webTemplateEngines.*;
import picoded.webTemplateEngines.FormGenerator.*;
import picodedTests.BaseTestClass;

import org.apache.commons.io.FileUtils;
import org.junit.*;

import com.mysql.jdbc.StringUtils;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileWriter;
import java.nio.charset.Charset;
import java.util.*;


//
// Tests the actual formgenerator class
//
public class FormGenerator_test extends BaseTestClass {
	
	public FormGenerator testObj = null;
	
	public FormGenerator_test(){
		super();
	}
	
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
	
	private String getFormGenOutput(String fileName, String dataFileName, boolean useData, boolean displayMode){
		String textString = readStringFromResourceFile(fileName);
		assertNotNull(textString);
		
		String textDataString = readStringFromResourceFile(dataFileName);
		assertNotNull(textDataString);
		
		Map<String, Object> jsonDataMap = ConvertJSON.toMap(textDataString);
		
		FormGenerator formGen = new FormGenerator();
		
		formGen.build(textString, jsonDataMap, displayMode);
		
		if(useData){
			return formGen.build(textString, jsonDataMap, displayMode).toString();
		}else{
			return formGen.build(textString, null, displayMode).toString();
		}
	}
	
	private boolean generateOutputFile(String outputFileName, String inputFileName, String inputDataFileName, boolean useData, boolean displayMode, boolean usePrefixAndSuffix){
		String textOutput = getFormGenOutput(inputFileName, inputDataFileName, useData, displayMode);
		assertNotNull(textOutput);
		assertFalse(StringUtils.isNullOrEmpty(textOutput));
		
		if(usePrefixAndSuffix){
			//read prefix and suffix
			String prefix = readStringFromResourceFile("prefix.html");
			String suffix = readStringFromResourceFile("suffix.html");
			textOutput = prefix + textOutput + suffix;
		}
		
		return writeStringToGeneratedFile(textOutput, outputFileName);
	}
	
	private boolean generateOutputPDFFile(String outputFileName, String inputFileName, String inputDataFileName, boolean useData, boolean displayMode, boolean usePrefixAndSuffix){
		String textOutput = getFormGenOutput(inputFileName, inputDataFileName, useData, displayMode);
		assertNotNull(textOutput);
		assertFalse(StringUtils.isNullOrEmpty(textOutput));
		
		if(usePrefixAndSuffix){
			//read prefix and suffix
			String prefix = readStringFromResourceFile("prefix.html");
			String suffix = readStringFromResourceFile("suffix.html");
			textOutput = prefix + textOutput + suffix;
		}
		
		return writeHTMLStringToGeneratedPDF(textOutput, outputFileName);
	}
	
	
	@Test
	public void generateGenericForm(){
		assertTrue(generateOutputFile("genericFormNoData.html", "genericForm.js", "genericFormData.js", false, false, false));
		assertTrue(generateOutputFile("genericFormWithData.html", "genericForm.js", "genericFormData.js", true, false, false));
		assertTrue(generateOutputFile("genericFormDisplayNoData.html", "genericForm.js", "genericFormData.js", false, true, false));
		assertTrue(generateOutputFile("genericFormDisplayWithData.html", "genericForm.js", "genericFormData.js", true, true, false));
		
		assertTrue(generateOutputPDFFile("genericFormNoData.pdf", "genericForm.js", "genericFormData.js", false, false, true));
		assertTrue(generateOutputPDFFile("genericFormWithData.pdf", "genericForm.js", "genericFormData.js", true, false, true));
		assertTrue(generateOutputPDFFile("genericFormDisplayNoData.pdf", "genericForm.js", "genericFormData.js", false, true, true));
		assertTrue(generateOutputPDFFile("genericFormDisplayWithData.pdf", "genericForm.js", "genericFormData.js", true, true, true));
	}
}

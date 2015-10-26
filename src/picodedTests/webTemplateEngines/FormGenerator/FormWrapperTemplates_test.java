package picodedTests.webTemplateEngines.FormGenerator;

import picoded.conv.ConvertJSON;
import picoded.webTemplateEngines.*;
import picoded.webTemplateEngines.FormGenerator.*;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.*;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileWriter;
import java.nio.charset.Charset;
import java.util.*;

public class FormWrapperTemplates_test {
	
	private String root = "./test-files/test-specific/webTemplateEngines/FormGenerator/FormWrapperTemplates/";
	private String resFolder = "./test-files/test-specific/webTemplateEngines/FormGenerator/FormWrapperTemplates/res/";
	
	private String getWrapperTemplatedJsonString(String jsonKeyName) {
		File jsonFile = new File(resFolder + jsonKeyName + ".js");
		try {
			String jsonFileString = FileUtils.readFileToString(jsonFile);
			Map<String, Object> jsonMap = ConvertJSON.toMap(jsonFileString);
			FormGenerator formGen = new FormGenerator();
			FormNode node = new FormNode(formGen, jsonMap, null);
			return formGen.wrapperInterface(false, node.getString("wrapper", "div")).apply(node).toString();
		} catch (Exception ex) {
			return "";
		}
	}
	
	private String getFullTemplatedJsonWithData(String jsonKeyName) {
		File jsonFile = new File(resFolder + jsonKeyName + ".js");
		File jsonDataFile = new File(resFolder + "jsonData.js");
		
		try {
			FormGenerator formGen = new FormGenerator();
			
			String jsonFileString = FileUtils.readFileToString(jsonFile);
			String jsonDataString = FileUtils.readFileToString(jsonDataFile);
			Map<String, Object> jsonDataMap = ConvertJSON.toMap(jsonDataString);
			
			return formGen.build(jsonFileString, jsonDataMap, false).toString();
		} catch (Exception ex) {
			return "";
		}
	}
	
	private String getHtmlString(String jsonKeyName) {
		switch (jsonKeyName) {
		case "div":
			return getStandardDivWrapper();
		case "divWithLabel":
			return getLabelWrapper();
		case "divWithChild":
			return getChildWrapper();
		case "fullTest":
			return getFullTestStringWithLabel();
		case "fullTestNoLabel":
			return getFullTestStringWithoutSecondIterationLabel();
		case "fullTestExtraAttributes":
			return getFullTestExtraAttributes();
		}
		
		return "";
	}
	
	private String getStandardDivWrapper() {
		return "<div class='pf_div'></div>";
	}
	
	private String getLabelWrapper() {
		return "<div class='pf_label'></div>";
	}
	
	private String getChildWrapper() {
		return "<div class='pf_child'></div>";
	}
	
	private String getFullTestStringWithLabel() {
		return "<div class='pf_div'>" + "<div class='pf_label'>TextField</div>" + "<div class='pf_child'>"
			+ "<div class='pf_div pff_childX pff_forChild0'>" + "<div class='pf_child'>" + "<div class='pf_div'>"
			+ "<div class='pf_label'>Title Label</div>" + "<h3 class='pfi_header'>Title</h3>" + "</div>"
			+ "<div class='pf_div'>" + "<input name='data' type='text' value='Person A' class='pfi_inputText'></input>"
			+ "</div>" + "</div>" + "</div>" + "<div class='pf_div pff_childX pff_forChild1'>"
			+ "<div class='pf_div'></div>" + "<div class='pf_child'>" + "<div class='pf_div'>"
			+ "<div class='pf_label'>Title Label</div>" + "<h3 class='pfi_header'>Title</h3>" + "</div>"
			+ "<div class='pf_div'>" + "<input name='data' type='text' value='Person B' class='pfi_inputText'></input>"
			+ "</div>" + "</div>" + "</div>" + "</div>" + "</div>";
	}
	
	private String getFullTestStringWithoutSecondIterationLabel() {
		return "<div class='pf_div'>" + "<div class='pf_label'>TextField</div>" + "<div class='pf_child'>"
			+ "<div class='pf_div pff_childX pff_forChild0'>" + "<div class='pf_child'>" + "<div class='pf_div'>"
			+ "<h3 class='pfi_header'>Title</h3>" + "</div>" + "<div class='pf_div'>"
			+ "<input name='data' type='text' value='Person A' class='pfi_inputText'></input>" + "</div>" + "</div>"
			+ "</div>" + "<div class='pf_div pff_childX pff_forChild1'>" + "<div class='pf_child'>"
			+ "<div class='pf_div'>" + "<h3 class='pfi_header'>Title</h3>" + "</div>" + "<div class='pf_div'>"
			+ "<input name='data' type='text' value='Person B' class='pfi_inputText'></input>" + "</div>" + "</div>"
			+ "</div>" + "</div>" + "</div>";
	}
	
	private String getFullTestExtraAttributes() {
		return "<div mapwrapperattribute='20' mapwrappersecondattribute='40' class='pf_div' singleWrapperAttrib='value' secondSinglWrappereAttrib='value2'>"
			+ "<div maplabelattribute='labelValue' class='pf_label' singleLabelAttrib='value'>TextField</div>"
			+ "<div mapchildwrapperattribute='childProperty' class='pf_child' singleChildWrapperAttrib='value'>"
			+ "<div class='pf_div pff_childX pff_forChild0'>"
			+ "<div class='pf_child'>"
			+ "<div class='pf_div'>"
			+ "<div class='pf_label'>Title Label</div>"
			+ "<h3 class='pfi_header' inputatribute='inputProperty'>Title</h3>"
			+ "</div>"
			+ "<div class='pf_div'>"
			+ "<input name='data' type='text' value='Person A' class='pfi_inputText'></input>"
			+ "</div>"
			+ "</div>"
			+ "</div>"
			+ "<div class='pf_div pff_childX pff_forChild1'>"
			+ "<div class='pf_child'>"
			+ "<div class='pf_div'>"
			+ "<div class='pf_label'>Title Label</div>"
			+ "<h3 class='pfi_header' inputatribute='inputProperty'>Title</h3>"
			+ "</div>"
			+ "<div class='pf_div'>"
			+ "<input name='data' type='text' value='Person B' class='pfi_inputText'></input>"
			+ "</div>"
			+ "</div>"
			+ "</div>" + "</div>" + "</div>";
	}
	
	@Test
	public void standardDivWrapperTest() {
		String jsonTemplatedOutput = getWrapperTemplatedJsonString("div");
		String rawHtmlString = getHtmlString("div");
		
		boolean compliancyCheck = htmlTagCompliancyCheck(rawHtmlString, jsonTemplatedOutput);
		assertTrue(compliancyCheck);
	}
	
	@Test
	public void divWithLabelWrapperTest() {
		String jsonTemplatedOutput = getWrapperTemplatedJsonString("divWithLabel");
		String rawHtmlString = getHtmlString("divWithLabel");
		
		boolean compliancyCheck = htmlTagCompliancyCheck(rawHtmlString, jsonTemplatedOutput);
		assertTrue(compliancyCheck);
	}
	
	@Test
	public void divWithChildrenWrapperTest() {
		String jsonTemplatedOutput = getWrapperTemplatedJsonString("divWithChild");
		String rawHtmlString = getHtmlString("divWithChild");
		
		boolean compliancyCheck = htmlTagCompliancyCheck(rawHtmlString, jsonTemplatedOutput);
		assertTrue(compliancyCheck);
	}
	
	@Test
	public void fullTestOfWrapperAndInputWithoutLabel() {
		String jsonTemplatedOutput = getFullTemplatedJsonWithData("fullTestNoLabel");
		String rawHtml = getHtmlString("fullTestNoLabel");
		
		boolean compliancyCheck = htmlTagCompliancyCheck(rawHtml, jsonTemplatedOutput);
		assertTrue(compliancyCheck);
	}
	
	@Test
	public void fullTestOfWrapperAndInput() {
		String jsonTemplatedOutput = getFullTemplatedJsonWithData("fullTest");
		String rawHtml = getHtmlString("fullTest");
		
		boolean compliancyCheck = htmlTagCompliancyCheck(rawHtml, jsonTemplatedOutput);
		assertTrue(compliancyCheck);
	}
	
	@Test
	public void fullTestOfInjectedAttributes() {
		String jsonTemplatedOutput = getFullTemplatedJsonWithData("fullTestExtraAttributes");
		String rawHtml = getHtmlString("fullTestExtraAttributes");
		
		boolean compliancyCheck = htmlTagCompliancyCheck(rawHtml, jsonTemplatedOutput);
		assertTrue(compliancyCheck);
	}
	
	@Test
	public void testTable() {
		String jsonTemplatedOutput = getFullTemplatedJsonWithData("table");
		String rawHtml = getHtmlString("table");
		
		boolean compliancyCheck = htmlTagCompliancyCheck(rawHtml, jsonTemplatedOutput);
		assertTrue(compliancyCheck);
	}
	
	@Test
	public void testCompatibility() {
		File jsonFile = new File("./test-files/test-specific/htmlGenerator/testJSONObject.js");
		File jsonDataFile = new File("./test-files/test-specific/htmlGenerator/prefilledData.js");
		
		File outputHTML = new File(
			"./test-files/test-specific/htmlGenerator/FormWrapperTemplates_test/compatibilityTestObject.html");
		File outputPDF = new File(
			"./test-files/test-specific/htmlGenerator/FormWrapperTemplates_test/compatibilityTestObjectDisplay.html");
		
		try {
			FormGenerator formGen = new FormGenerator();
			
			String jsonFileString = FileUtils.readFileToString(jsonFile);
			Map<String, Object> jsonMap = ConvertJSON.toMap(jsonFileString);
			
			String jsonDataString = FileUtils.readFileToString(jsonDataFile);
			Map<String, Object> jsonDataMap = ConvertJSON.toMap(jsonDataString);
			
			StringBuilder outHtml = formGen.build(jsonMap, jsonDataMap, false);
			StringBuilder outPDF = formGen.build(jsonMap, jsonDataMap, true);
			
			String finalOutput = outHtml.toString();
			FileWriter fw = new FileWriter(outputHTML);
			fw.write(finalOutput);
			;
			fw.flush();
			fw.close();
			
			String displayOutput = outPDF.toString();
			FileWriter writer = new FileWriter(outputPDF);
			writer.write(displayOutput);
			;
			writer.flush();
			writer.close();
		} catch (Exception ex) {
		}
	}
	
	@Test
	public void testJMTE() {
		FormGenerator formGen = new FormGenerator();
		
		String define = "{ \"type\" : \"jmte\", \"jmte_generic\":\"${hello}\" }";
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("hello", "world");
		
		data.put("display", "truth");
		data.put("input", "lies");
		
		assertEquals("world", formGen.build(define, data, true).toString());
		assertEquals("world", formGen.build(define, data, false).toString());
		
		define = "{ \"type\" : \"jmte\", \"jmte_input\":\"${input}\", \"jmte_display\":\"${display}\" }";
		
		assertEquals("truth", formGen.build(define, data, true).toString());
		assertEquals("lies", formGen.build(define, data, false).toString());
	}
	
	public boolean htmlTagCompliancyCheck(String source, String lookup) {
		String[] rawHtmlSplit = source.split("(>|<|=|\\s+|\"|\'|/)"); //this becomse "required params"
		
		for (String req : rawHtmlSplit) {
			if (!lookup.contains(req)) {
				return false;
			}
		}
		
		return true;
	}
	
}

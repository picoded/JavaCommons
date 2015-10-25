package picodedTests.webTemplateEngines.FormGenerator;

import picoded.conv.ConvertJSON;
import picoded.webTemplateEngines.FormGenerator.*;
import picodedTests.BaseTestClass;
import org.junit.*;

import com.mysql.jdbc.StringUtils;

import static org.junit.Assert.*;
import java.util.*;

//
// Tests the individual Input Templates
//
public class FormInputTemplates_test extends BaseTestClass{
	public FormInputTemplates_test(){
		super();
	}
	
	@Test
	public void input_text_test(){
		assertTrue(doNodeTest("text.js", "text"));
	}
	
	@Test
	public void title_test(){
		assertTrue(doNodeTest("title.js", "title"));
	}
	
	@Test
	public void checkbox_test(){
		assertTrue(doNodeTest("checkbox.js", "checkbox"));
	}
	
	@Test
	public void dropdown_test(){
		assertTrue(doNodeTest("dropdown.js", "dropdown"));
	}
	
	@Test
	public void textarea_test(){
		assertTrue(doNodeTest("textarea.js", "textarea"));
	}
	
	@Test
	public void number_test(){
		assertTrue(doNodeTest("number.js", "number"));
	}
	
	@Test
	public void signature_test(){
		
	}
	
	@Test
	public void date_test(){
		assertTrue(doNodeTest("date.js", "date"));
	}
	
	@Test
	public void image_test(){
		assertTrue(doNodeTest("image.js", "image"));
	}
	
	//
	// Does basic node test against expected output
	//
	private boolean doNodeTest(String inputFileName, String nodeType){
		String nodeOutput = getNodeOutput(inputFileName);
		assertNotNull(nodeOutput);
		assertFalse(StringUtils.isNullOrEmpty(nodeOutput));
		return testNodeOutputAgainstExpectedOutput(nodeOutput, nodeType);
	}
	
	//
	// Gets node html output after reading from file
	//
	private String getNodeOutput(String fileName){
		String textString = readStringFromResourceFile(fileName);
		assertNotNull(textString);
		Map<String, Object> jsonMap = ConvertJSON.toMap(textString);
		FormGenerator formGen = new FormGenerator();
		FormNode node = null;
		node = new FormNode(formGen, jsonMap, null);
		return node.inputHtml(false).toString();
	}
	
	private boolean testNodeOutputAgainstExpectedOutput(String nodeOutput, String nodeType){
		String expectedString = getHtmlString(nodeType);
		return htmlTagCompliancyCheck(nodeOutput, expectedString);
	}
	
	public boolean htmlTagCompliancyCheck(String source, String lookup) {
		String[] lookupHtmlSplit = lookup.split("(>|<|=|\\s+|\"|\'|/)");
		
		for (String req : lookupHtmlSplit) {
			if (!source.contains(req)) {
				return false;
			}
		}
		return true;
	}
	
	private String getHtmlString(String jsonKeyName) {
		switch (jsonKeyName) {
		case "title":
			return getTitleHtmlString();
		case "dropdown":
			return getDropdownHtmlString();
		case "text":
			return getTextHtmlString();
		case "checkbox":
			return getCheckboxHtmlString();
		case "date":
			return getDateHtmlString();
		case "number":
			return getNumberHtmlString();
		case "image":
			return getImageHtmlString();
		case "textarea":
			return getTextAreaHtmlString();
		}
		
		return "";
	}
	
	private String getTitleHtmlString() {
		return "<h3 class></h3>";
	}
	
	private String getDropdownHtmlString() {
		return "<select class name>" + "<option value></option>" + "</select>";
	}
	
	private String getTextHtmlString() {
		return "<input type text class value></input>";
	}
	
	private String getCheckboxHtmlString() {
		return "<input type checkbox value name class></input>";
	}
	
	private String getDateHtmlString(){
		return "<input type date value name class></input>";
	}
	
	private String getNumberHtmlString(){
		return "<input type number value name class></input>";
	}
	
	private String getImageHtmlString(){
		return "<img src></img>";
	}
	
	private String getTextAreaHtmlString(){
		return "<textarea class></input>";
	}
}
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

public class FormInputTemplates_test {
	
	private String getFinalTemplatedJsonString(String jsonKeyName){
		File jsonFile = new File("./test-files/test-specific/htmlGenerator/FormInputTemplates_test/"+jsonKeyName+".js");
		try{
			String jsonFileString = FileUtils.readFileToString(jsonFile);
			Map<String, Object> jsonMap = ConvertJSON.toMap(jsonFileString);
			FormGenerator formGen = new FormGenerator();
			FormNode node = new FormNode(formGen, jsonMap, null);
			return node.inputHtml(false).toString();
		}catch(Exception ex){
			return "";
		}
	}
	
	private String getHtmlString(String jsonKeyName){
		switch(jsonKeyName){
			case "title": return getTitleHtmlString();
			case "dropdown": return getDropdownHtmlString();
			case "text": return getTextHtmlString();
			case "dropdownWithOthers": return getDropdownWithOthersHtmlString();
		}
		
		return "";
	}
	
	private String getTitleHtmlString(){
		return "<h3 class='pfi_header pfi_input'>Hello there</h3>";
	}
	
	private String getDropdownHtmlString(){
		return "<select class='pfi_select pfi_input' name='dropdownfield'>"+
					"<option value=\"option1\">Option 1</option>"+
					"<option value=\"option2\">Option 2</option>"+
					"<option value=\"option3\">Option 3</option>"+
					"<option value=\"option4\">Option 4</option>"+
				"</select>";
	}
	
	private String getTextHtmlString(){
		return "<input type='text' class='pfi_inputText pfi_input' value=''></input>";
	}
	
	private String getDropdownWithOthersHtmlString(){
		return "<script>"+
					"function OnChangeDropDown() {"+
						"var dropDown = document.getElementById(\"dropdownField\");"+
						"var inputField = document.getElementById(\"dropdownTextField\");"+
						"if(dropDown.value == \"option4\"){"+
							"inputField.style.display = \"inline\";"+
						"}else{"+
							"inputField.style.display = \"none\";"+
						"}"+
					"};"+
				"</script>"+
				"<select class=\'pf_select\' onchange=\'OnChangeDropDown()\' name=\'dropdownfield\'>"+
					"<option value=\"option1\">Option 1</option>"+
					"<option value=\"option2\">Option 2</option>"+
					"<option value=\"option3\">Option 3</option>"+
					"<option value=\"option4\">Option 4</option>"+
				"</select>"+
				"<input class=\'pf_inputText\' style=\'display:none\' type=\'text\' name=\'dropdownTextField\'>";
	}
	
	@Test
	public void titleTest(){
		String jsonTemplatedOutput = getFinalTemplatedJsonString("title");
		String rawHtmlString = getHtmlString("title");
		
		assertNotNull(jsonTemplatedOutput);
		
		boolean compliancyCheck = htmlTagCompliancyCheck(rawHtmlString, jsonTemplatedOutput);
		assertTrue(compliancyCheck);
	}
	
	@Test
	public void dropdownTest(){
		String jsonTemplatedOutput = getFinalTemplatedJsonString("dropdown");
		String rawHtmlString = getHtmlString("dropdown");
		
		assertNotNull(jsonTemplatedOutput);
		
		boolean compliancyCheck = htmlTagCompliancyCheck(rawHtmlString, jsonTemplatedOutput);
		assertTrue(compliancyCheck);
	}
	
	@Test
	public void textTest(){
		String jsonTemplatedOutput = getFinalTemplatedJsonString("text");
		String rawHtmlString = getHtmlString("text");
		
		assertNotNull(jsonTemplatedOutput);
		
		boolean compliancyCheck = htmlTagCompliancyCheck(rawHtmlString, jsonTemplatedOutput);
		assertTrue(compliancyCheck);
	}
	
	@Test
	public void dropdownWithOthersTest(){
		String jsonTemplatedOutput = getFinalTemplatedJsonString("dropdownWithOthers");
		String rawHtmlString = getHtmlString("dropdownWithOthers");

		assertNotNull(jsonTemplatedOutput);
		
		boolean compliancyCheck = htmlTagCompliancyCheck(rawHtmlString, jsonTemplatedOutput);
		assertTrue(compliancyCheck);
	}
	
	/// Prototype lenientStringLookup
//	@Test
	public int lenientStringLookup(String source, String lookup) {
		int lowestOffset = -1;
		
		String singleTag = "";
		String cleanedTag = "";
		if(lookup.startsWith("<")){
			singleTag = lookup.substring(lookup.indexOf('<'), lookup.indexOf('>') + 1);
			cleanedTag = singleTag.substring(1,  singleTag.length() - 1);
		}else{
			char nextCharToBreak = lookup.indexOf('<') < lookup.indexOf('>') ? '<' : '>';
			singleTag = lookup.substring(0, lookup.indexOf(nextCharToBreak));
		}
		lookup = lookup.substring(singleTag.length(), lookup.length());
		
		String[] lookupArray = cleanedTag.split(" ");
		for(int a=0; a<lookupArray.length; ++a) {
			if(!source.contains(lookupArray[a])){
				return -1;
			}else{
				int index = source.indexOf(lookupArray[a]);
				lowestOffset = index > lowestOffset ? index + lookupArray[a].length() : lowestOffset;
			}
		}

		if(lookup.length() > 0){
			return lenientStringLookup(source, lookup);
		}else{
			return lowestOffset;
		}
	}
	
	public boolean htmlTagCompliancyCheck(String source, String lookup){
		String[] rawHtmlSplit = source.split("(>|<|=|\\s+|\"|\'|/)"); //this becomse "required params"
		
		for(String req:rawHtmlSplit){
			if(!lookup.contains(req)){
				return false;
			}
		}
		
		return true;
	}
	
}

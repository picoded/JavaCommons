package picodedTests.webTemplateEngines.FormGenerator;

import picoded.conv.ConvertJSON;
import picoded.conv.RegexUtils;
import picoded.fileUtils.PDFGenerator;
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
	
	private String getTemplatedJsonStringWithData(String jsonKeyName, boolean displayMode){
		File jsonFile = new File("./test-files/test-specific/htmlGenerator/FormInputTemplates_test/"+jsonKeyName+".js");
		File jsonDataFile = new File("./test-files/test-specific/htmlGenerator/FormInputTemplates_test/"+jsonKeyName+"Data.js");
		try{
			String jsonFileString = FileUtils.readFileToString(jsonFile);
			//Map<String, Object> jsonMap = ConvertJSON.toMap(jsonFileString);
			
			FormGenerator formGen = new FormGenerator();
			
			if(jsonDataFile.exists()){
				String jsonDataString = FileUtils.readFileToString(jsonDataFile);
				Map<String, Object> jsonDataMap = ConvertJSON.toMap(jsonDataString);
				return formGen.build(jsonFileString, jsonDataMap, displayMode).toString();
			}
			else{
				return formGen.build(jsonFileString, null, displayMode).toString();
			}
			
//			FormNode node = new FormNode(formGen, jsonMap, jsonDataMap);
//			return node.inputHtml(false).toString();
		}catch(Exception ex){
			return "";
		}
	}
	
	private String fullChildrenHtml(String jsonKeyName, boolean displayMode){
		File jsonFile = new File("./test-files/test-specific/htmlGenerator/FormInputTemplates_test/"+jsonKeyName+".js");
		File jsonDataFile = new File("./test-files/test-specific/htmlGenerator/FormInputTemplates_test/"+jsonKeyName+"Data.js");
		try{
			String jsonFileString = FileUtils.readFileToString(jsonFile);
			Map<String, Object> jsonMap = ConvertJSON.toMap(jsonFileString);
			
			String jsonDataString = FileUtils.readFileToString(jsonDataFile);
			Map<String, Object> jsonDataMap = ConvertJSON.toMap(jsonDataString);
			
			FormGenerator formGen = new FormGenerator();
//			return formGen.build(jsonMap, jsonDataMap, displayMode).toString();
			FormNode node = new FormNode(formGen, jsonMap, jsonDataMap);
			return node.fullChildrenHtml(false, "").toString();
//			return node.inputHtml(false).toString();
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
			case "checkbox": return getCheckboxHtmlString();
			case "checkboxData": return getCheckboxWithDataHtmlString();
			case "table": return getTableHtmlString();
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
				"<select class=\'pf_select\' onchange=\'OnChangeDropDown()\' id=\'dropdownfield\'>"+
					"<option value=\"option1\">Option 1</option>"+
					"<option value=\"option2\">Option 2</option>"+
					"<option value=\"option3\">Option 3</option>"+
					"<option value=\"option4\">Option 4</option>"+
				"</select>"+
				"<input class=\'pf_inputText\' style=\'display:none\' type=\'text\' id=\'dropdownTextField\'>";
	}
	
	private String getCheckboxHtmlString(){
		return "<input type=\'checkbox\' value=\'option1\' name=\'checkboxa\' class=\'pfi_inputCheckbox pfi_input\'>Option 1</input>"+
				"<input type=\'checkbox\' value=\'option2\' name=\'checkboxa\' class=\'pfi_inputCheckbox pfi_input\'>Option 2</input>"+
				"<input type=\'checkbox\' value=\'option3\' name=\'checkboxa\' class=\'pfi_inputCheckbox pfi_input\'>Option 3</input>"+
				"<input type=\'checkbox\' value=\'option4\' name=\'checkboxa\' class=\'pfi_inputCheckbox pfi_input\'>Option 4</input>";
	}
	
	private String getCheckboxWithDataHtmlString(){
		return "<input type=\'checkbox\' value=\'option1\' name=\'checkboxa\' class=\'pfi_inputCheckbox pfi_input\' checked=\'checked\'>Option 1</input>"+
				"<input type=\'checkbox\' value=\'option2\' name=\'checkboxa\' class=\'pfi_inputCheckbox pfi_input\'>Option 2</input>"+
				"<input type=\'checkbox\' value=\'option3\' name=\'checkboxa\' class=\'pfi_inputCheckbox pfi_input\' checked=\'checked\'>Option 3</input>"+
				"<input type=\'checkbox\' value=\'option4\' name=\'checkboxa\' class=\'pfi_inputCheckbox pfi_input\'>Option 4</input>";
	}
	
	private String getTableHtmlString(){
		return "<table>"+
					"<thead>Clients</thead>"+
					"<tr>"+
						"<th>Name</th>"+
						"<th>NRIC</th>"+
					"</tr>"+
					"<tr>"+
						"<td>A</td>"+
						"<td>X1</td>"+
					"</tr>"+
					"<tr>"+
						"<td>B</td>"+
						"<td>X2</td>"+
					"</tr>"+
					"<tr>"+
						"<td>C</td>"+
						"<td>X3</td>"+
					"</tr>"+
				"</table>";
	}
	
//	@Test
	public void titleTest(){
		String jsonTemplatedOutput = getFinalTemplatedJsonString("title");
		String rawHtmlString = getHtmlString("title");
		
		assertNotNull(jsonTemplatedOutput);
		
		boolean compliancyCheck = htmlTagCompliancyCheck(rawHtmlString, jsonTemplatedOutput);
		assertTrue(compliancyCheck);
	}
	
//	@Test
	public void dropdownTest(){
		String jsonTemplatedOutput = getFinalTemplatedJsonString("dropdown");
		String rawHtmlString = getHtmlString("dropdown");
		
		assertNotNull(jsonTemplatedOutput);
		
		boolean compliancyCheck = htmlTagCompliancyCheck(rawHtmlString, jsonTemplatedOutput);
		assertTrue(compliancyCheck);
	}
	
//	@Test
	public void textTest(){
		String jsonTemplatedOutput = getFinalTemplatedJsonString("text");
		String rawHtmlString = getHtmlString("text");
		
		assertNotNull(jsonTemplatedOutput);
		
		boolean compliancyCheck = htmlTagCompliancyCheck(rawHtmlString, jsonTemplatedOutput);
		assertTrue(compliancyCheck);
	}
	
//	@Test
	public void textDisplayTest(){
		String jsonTemplatedOutput = getTemplatedJsonStringWithData("text", false);
		String rawHtmlString = getHtmlString("text");
		
		assertNotNull(jsonTemplatedOutput);
		
		boolean compliancyCheck = htmlTagCompliancyCheck(rawHtmlString, jsonTemplatedOutput);
		assertTrue(compliancyCheck);
	}
	
//	@Test
	public void dropdownWithOthersTest(){
		String jsonTemplatedOutput = getFinalTemplatedJsonString("dropdownWithOthers");
		String rawHtmlString = getHtmlString("dropdownWithOthers");

		assertNotNull(jsonTemplatedOutput);
		
		boolean compliancyCheck = htmlTagCompliancyCheck(rawHtmlString, jsonTemplatedOutput);
//		assertTrue(compliancyCheck);
	}
	
//	@Test
	public void dropdownWithOthersDisplayTest(){
		String jsonTemplatedOutput = getTemplatedJsonStringWithData("dropdownWithOthers", true);
		String rawHtmlString = getHtmlString("dropdownWithOthers");

		assertNotNull(jsonTemplatedOutput);
		
		boolean compliancyCheck = htmlTagCompliancyCheck(rawHtmlString, jsonTemplatedOutput);
//		assertTrue(compliancyCheck);
	}
	
//	@Test
	public void dropdownWithOthersPDFTest(){
		String jsonTemplatedOutput = getTemplatedJsonStringWithData("dropdownWithOthers", true);
		
		PDFGenerator.generatePDFfromRawHTML("./test-files/test-specific/htmlGenerator/FormInputTemplates_test/dropdownWithOthersPDF.pdf", jsonTemplatedOutput);

		assertNotNull(jsonTemplatedOutput);
	}
	
//	@Test
	public void checkBoxTest(){
		String jsonTemplatedOutput = getFinalTemplatedJsonString("checkbox");
		String rawHtmlString = getHtmlString("checkbox");

		assertNotNull(jsonTemplatedOutput);
		
		boolean compliancyCheck = htmlTagCompliancyCheck(rawHtmlString, jsonTemplatedOutput);
		assertTrue(compliancyCheck);
	}
	
//	@Test
	public void checkBoxDisplayTest(){
		String jsonTemplatedOutput = getTemplatedJsonStringWithData("checkbox", true);
		String rawHtmlString = getHtmlString("checkboxData");

		assertNotNull(jsonTemplatedOutput);
		
		boolean compliancyCheck = htmlTagCompliancyCheck(rawHtmlString, jsonTemplatedOutput);
//		assertTrue(compliancyCheck);
	}
	
//	@Test
	public void checkBoxPDFTest(){
		String jsonTemplatedOutput = getTemplatedJsonStringWithData("checkbox", true);
		
		PDFGenerator.generatePDFfromRawHTML("./test-files/test-specific/htmlGenerator/FormInputTemplates_test/checkboxPDF.pdf", jsonTemplatedOutput);

		assertNotNull(jsonTemplatedOutput);
	}
	
	@Test
	public void tableTest(){
		String jsonTemplatedOutput = getTemplatedJsonStringWithData("table", false);
		String rawHtmlString = getHtmlString("table");

		assertNotNull(jsonTemplatedOutput);
		assertNotNull(rawHtmlString);
		
		System.out.println(rawHtmlString);
		
		boolean compliancyCheck = htmlTagCompliancyCheck(rawHtmlString, jsonTemplatedOutput);
		assertTrue(compliancyCheck);
	}
	
//	@Test
	public void tableDisplayTest(){
		String jsonTemplatedOutput = getTemplatedJsonStringWithData("table", true);
		String rawHtmlString = getHtmlString("table");

		assertNotNull(jsonTemplatedOutput);
		
		boolean compliancyCheck = htmlTagCompliancyCheck(rawHtmlString, jsonTemplatedOutput);
		assertTrue(compliancyCheck);
	}
	
//	@Test
	public void tablePDFTest(){
		String jsonTemplatedOutput = getTemplatedJsonStringWithData("table", true);
		
		PDFGenerator.generatePDFfromRawHTML("./test-files/test-specific/htmlGenerator/FormInputTemplates_test/tablePDF.pdf", jsonTemplatedOutput);

		assertNotNull(jsonTemplatedOutput);
	}
	
//	@Test
	public void verticalTableTest(){
		String jsonTemplatedOutput = getTemplatedJsonStringWithData("verticalTable", false);
		String rawHtmlString = getHtmlString("verticalTable");

		assertNotNull(jsonTemplatedOutput);
		
//		boolean compliancyCheck = htmlTagCompliancyCheck(rawHtmlString, jsonTemplatedOutput);
//		assertTrue(compliancyCheck);
	}
	
//	@Test
	public void verticalTableDisplayTest(){
		String jsonTemplatedOutput = getTemplatedJsonStringWithData("verticalTable", true);
		String rawHtmlString = getHtmlString("verticalTable");

		assertNotNull(jsonTemplatedOutput);
		
//		boolean compliancyCheck = htmlTagCompliancyCheck(rawHtmlString, jsonTemplatedOutput);
//		assertTrue(compliancyCheck);
	}
	
	// @Test
	public void verticalTablePDFTest(){
		String jsonTemplatedOutput = getTemplatedJsonStringWithData("verticalTable", true);
		
		PDFGenerator.generatePDFfromRawHTML("./test-files/test-specific/htmlGenerator/FormInputTemplates_test/verticalTablePDF.pdf", jsonTemplatedOutput);

		assertNotNull(jsonTemplatedOutput);
	}
//	
//	@Test
	public void imageTest(){
		String jsonTemplatedOutput = getTemplatedJsonStringWithData("image", false);
		assertNotNull(jsonTemplatedOutput);
	}
	
//	@Test
	public void imagePDFTest(){
		String jsonTemplatedOutput = getTemplatedJsonStringWithData("imagePDF", false);
		
		String pdfPath = "./test-files/test-specific/htmlGenerator/generatedFiles/newImgPDF.pdf";
//		String htmlPath = "./test-files/test-specific/htmlGenerator/FormInputTemplates_test/imgHTML.html";
//		String context = "file:///C:/Users/Samuel/workspace/JavaCommons/test-files/test-specific/htmlGenerator/FormInputTemplates_test/res/";
		
		PDFGenerator.generatePDFfromRawHTML(pdfPath, jsonTemplatedOutput, "");
	}
	
//	@Test
	public void signatureTest(){
		String jsonTemplatedOutput = getTemplatedJsonStringWithData("signature", false);
		assertNotNull(jsonTemplatedOutput);
		
		File prefix = new File("./test-files/test-specific/htmlGenerator/signature/prefix.html");
		
		
		File generatedHtml = new File("./test-files/test-specific/htmlGenerator/generatedFiles/sigHTML.html");
		try{
			String prefixString = FileUtils.readFileToString(prefix);
			
			FileWriter fw = new FileWriter(generatedHtml);
			fw.write(prefixString);
			fw.write(jsonTemplatedOutput);
			fw.flush();
			fw.close();
		}catch(Exception e){
			
		}
	}
	
//	@Test
//	public void dummy(){
//		String test = "hello*_-_*there";
//		String result = RegexUtils.removeAllNonAlphaNumeric_allowUnderscoreAndDash(test);
//		System.out.println(result);
//	}
	
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

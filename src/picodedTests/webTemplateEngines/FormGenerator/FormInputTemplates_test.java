package picodedTests.webTemplateEngines.FormGenerator;

import picoded.conv.ConvertJSON;
import picoded.conv.MapValueConv;
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
	
	String rootFolder = "./test-files/test-specific/htmlGenerator/FormInputTemplates_test";
	String generatedFilesFolder = rootFolder + "/testGenerated";
	
	private String getFinalTemplatedJsonString(String jsonKeyName){
		File jsonFile = new File(rootFolder + "/" +jsonKeyName+".js");
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
		return getTemplatedJSONString(jsonKeyName, displayMode, true);
	}
	
	private String getTemplatedJSONString(String jsonKeyName, boolean isDisplayMode, boolean loadDummyData){
		File jsonFile = new File(rootFolder + "/"+jsonKeyName+".js");
		File jsonDataFile = new File(rootFolder + "/" +jsonKeyName+"Data.js");
		try{
			String jsonFileString = FileUtils.readFileToString(jsonFile);
			FormGenerator formGen = new FormGenerator();
			
			if(loadDummyData && jsonDataFile.exists()){
				String jsonDataString = FileUtils.readFileToString(jsonDataFile);
				Map<String, Object> jsonDataMap = ConvertJSON.toMap(jsonDataString);

				return formGen.build(jsonFileString, jsonDataMap, isDisplayMode).toString();
			}
			else{
				return formGen.build(jsonFileString, null, isDisplayMode).toString();
			}
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
		return "<h3 class></h3>";
	}
	
	private String getDropdownHtmlString(){
		return "<select class name>"+
					"<option value></option>"+
				"</select>";
	}
	
	private String getTextHtmlString(){
		return "<input type class value></input>";
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
				"<select class onchange id>"+
					"<option value></option>"+
				"</select>"+
				"<input class style type id>";
	}
	
	private String getCheckboxHtmlString(){
		return "<input type value name class></input>";
	}
	
	private String getCheckboxWithDataHtmlString(){
		return "<input type value name class></input>";
	}
	
	private String getTableHtmlString(){
		return "<table>"+
					"<thead></thead>"+
					"<tr>"+
						"<th></th>"+
						"<th></th>"+
					"</tr>"+
					"<tr>"+
						"<td></td>"+
						"<td></td>"+
					"</tr>"+
				"</table>";
	}
	
	private boolean generateHTMLFile(String fileName, String output){
		if(output == null || output.isEmpty()){
			return false;
		}
		
		File outputFile = new File(generatedFilesFolder + "/" + fileName + ".html");
		
		try{
			FileWriter fw = new FileWriter(outputFile);
			fw.write(output);
			fw.flush();
			fw.close();
			
			return true;
		}catch(Exception e){
			return false;
		}
	}
	
//	@Test
	public void titleTest(){
		String jsonTemplatedOutput = getFinalTemplatedJsonString("title");
		String rawHtmlString = getHtmlString("title");
		
		assertNotNull(jsonTemplatedOutput);
		
		boolean compliancyCheck = htmlTagCompliancyCheck(rawHtmlString, jsonTemplatedOutput);
		assertTrue(compliancyCheck);
		
		assertTrue(generateHTMLFile("title", jsonTemplatedOutput));
	}
	
//	@Test
	public void dropdownTest(){
		String jsonTemplatedOutput = getFinalTemplatedJsonString("dropdown");
		String rawHtmlString = getHtmlString("dropdown");
		
		assertNotNull(jsonTemplatedOutput);
		
		boolean compliancyCheck = htmlTagCompliancyCheck(rawHtmlString, jsonTemplatedOutput);
		assertTrue(compliancyCheck);
		
		assertTrue(generateHTMLFile("dropdown", jsonTemplatedOutput));
	}
	
//	@Test
	public void textTest(){
		String jsonTemplatedOutput = getFinalTemplatedJsonString("text");
		String rawHtmlString = getHtmlString("text");
		
		assertNotNull(jsonTemplatedOutput);
		
		boolean compliancyCheck = htmlTagCompliancyCheck(rawHtmlString, jsonTemplatedOutput);
		assertTrue(compliancyCheck);
		
		assertTrue(generateHTMLFile("text", jsonTemplatedOutput));
	}
	
//	@Test
	public void textDisplayTest(){
		String jsonTemplatedOutput = getTemplatedJsonStringWithData("text", true);
		String rawHtmlString = getHtmlString("text");
		
		assertNotNull(jsonTemplatedOutput);
		
//		boolean compliancyCheck = htmlTagCompliancyCheck(rawHtmlString, jsonTemplatedOutput);
//		assertTrue(compliancyCheck);
		
		assertTrue(generateHTMLFile("textWithData", jsonTemplatedOutput));
	}
	
//	@Test
	public void dropdownWithOthersTest(){
		String jsonTemplatedOutput = getFinalTemplatedJsonString("dropdownWithOthers");
		String rawHtmlString = getHtmlString("dropdownWithOthers");

		assertNotNull(jsonTemplatedOutput);
		
		boolean compliancyCheck = htmlTagCompliancyCheck(rawHtmlString, jsonTemplatedOutput);
//		assertTrue(compliancyCheck);
		
		assertTrue(generateHTMLFile("dropdownWithOthers", jsonTemplatedOutput));
	}
	
//	@Test
	public void dropdownWithOthersDisplayTest(){
		String jsonTemplatedOutput = getTemplatedJsonStringWithData("dropdownWithOthers", true);
		String rawHtmlString = getHtmlString("dropdownWithOthers");

		assertNotNull(jsonTemplatedOutput);
		
		boolean compliancyCheck = htmlTagCompliancyCheck(rawHtmlString, jsonTemplatedOutput);
//		assertTrue(compliancyCheck);
		
		assertTrue(generateHTMLFile("dropdownWithOthersDisplay", jsonTemplatedOutput));
	}
	
//	@Test
	public void dropdownWithOthersPDFTest(){
		String jsonTemplatedOutput = getTemplatedJsonStringWithData("dropdownWithOthers", true);
		String pdfPath = generatedFilesFolder + "/dropdownWithOthersPDF.pdf";
		PDFGenerator.generatePDFfromRawHTML(pdfPath, jsonTemplatedOutput);

		assertNotNull(jsonTemplatedOutput);
		
		assertTrue(generateHTMLFile("dropdownWithOthersPDF", jsonTemplatedOutput));
	}
	
//	@Test
	public void checkBoxTest(){
		
		String sanitisedSelection = RegexUtils.removeAllNonAlphaNumeric_allowCommonSeparators("actuarial science");
		sanitisedSelection = RegexUtils.removeAllWhiteSpace(sanitisedSelection);
		
//		String jsonTemplatedOutput = getFinalTemplatedJsonString("checkbox");
		String jsonTemplatedOutput = getTemplatedJSONString("checkbox", false, true);
		String rawHtmlString = getHtmlString("checkbox");

		assertNotNull(jsonTemplatedOutput);
		
		boolean compliancyCheck = htmlTagCompliancyCheck(rawHtmlString, jsonTemplatedOutput);
		assertTrue(compliancyCheck);
		
		assertTrue(generateHTMLFile("checkboxList", jsonTemplatedOutput));
	}
	
//	@Test
	public void checkBoxMapTest(){
		String jsonTemplatedOutput = getFinalTemplatedJsonString("checkboxMap");
		String rawHtmlString = getHtmlString("checkbox");

		assertNotNull(jsonTemplatedOutput);
		
		boolean compliancyCheck = htmlTagCompliancyCheck(rawHtmlString, jsonTemplatedOutput);
		assertTrue(compliancyCheck);
		
		assertTrue(generateHTMLFile("checkboxMap", jsonTemplatedOutput));
	}
	
//	@Test
	public void checkBoxDisplayTest(){
		String jsonTemplatedOutput = getTemplatedJsonStringWithData("checkbox", true);
		String rawHtmlString = getHtmlString("checkboxData");

		assertNotNull(jsonTemplatedOutput);
		
		boolean compliancyCheck = htmlTagCompliancyCheck(rawHtmlString, jsonTemplatedOutput);
//		assertTrue(compliancyCheck);
		
		assertTrue(generateHTMLFile("checkboxData", jsonTemplatedOutput));
	}
	
//	@Test
	public void checkBoxPDFTest(){
		String jsonTemplatedOutput = getTemplatedJsonStringWithData("checkbox", true);
		String pdfPath = generatedFilesFolder + "/checkboxPDF.pdf";
		PDFGenerator.generatePDFfromRawHTML(pdfPath, jsonTemplatedOutput);

		assertNotNull(jsonTemplatedOutput);
		
		assertTrue(generateHTMLFile("checkboxPDFOutput", jsonTemplatedOutput));
	}
	
	@Test
	public void tableTest(){
		String jsonTemplatedOutput = getTemplatedJsonStringWithData("table", false);
		String rawHtmlString = getHtmlString("table");

		assertNotNull(jsonTemplatedOutput);
		assertNotNull(rawHtmlString);
		boolean compliancyCheck = htmlTagCompliancyCheck(rawHtmlString, jsonTemplatedOutput);
		assertTrue(compliancyCheck);
		
		assertTrue(generateHTMLFile("table", jsonTemplatedOutput));
	}
	
//	@Test
	public void tableDisplayTest(){
		String jsonTemplatedOutput = getTemplatedJsonStringWithData("table", true);
		String rawHtmlString = getHtmlString("table");

		assertNotNull(jsonTemplatedOutput);
		
		boolean compliancyCheck = htmlTagCompliancyCheck(rawHtmlString, jsonTemplatedOutput);
		assertTrue(compliancyCheck);
		
		assertTrue(generateHTMLFile("tableDisplay", jsonTemplatedOutput));
	}
	
//	@Test
	public void tablePDFTest(){
		String jsonTemplatedOutput = getTemplatedJsonStringWithData("table", true);
		String pdfPath = generatedFilesFolder + "/tablePDF.pdf";
		PDFGenerator.generatePDFfromRawHTML(pdfPath, jsonTemplatedOutput);

		assertNotNull(jsonTemplatedOutput);
		
		assertTrue(generateHTMLFile("tablePDFOutput", jsonTemplatedOutput));
	}
	
	@Test
	public void verticalTableTest(){
		String jsonTemplatedOutput = getTemplatedJsonStringWithData("verticalTable", false);
		String rawHtmlString = getHtmlString("verticalTable");

		assertNotNull(jsonTemplatedOutput);
		
//		boolean compliancyCheck = htmlTagCompliancyCheck(rawHtmlString, jsonTemplatedOutput);
//		assertTrue(compliancyCheck);
		
		assertTrue(generateHTMLFile("verticalTable", jsonTemplatedOutput));
	}
	
//	@Test
	public void verticalTableDisplayTest(){
		String jsonTemplatedOutput = getTemplatedJsonStringWithData("verticalTable", true);
		String rawHtmlString = getHtmlString("verticalTable");

		assertNotNull(jsonTemplatedOutput);
		
//		boolean compliancyCheck = htmlTagCompliancyCheck(rawHtmlString, jsonTemplatedOutput);
//		assertTrue(compliancyCheck);
		
		assertTrue(generateHTMLFile("verticalTableDisplay", jsonTemplatedOutput));
	}
	
	// @Test
	public void verticalTablePDFTest(){
		String jsonTemplatedOutput = getTemplatedJsonStringWithData("verticalTable", true);
		String pdfPath = generatedFilesFolder + "/verticalTablePDF.pdf";
		PDFGenerator.generatePDFfromRawHTML(pdfPath, jsonTemplatedOutput);

		assertNotNull(jsonTemplatedOutput);
		
		assertTrue(generateHTMLFile("verticalTablePDFOutput", jsonTemplatedOutput));
	}
//	
//	@Test
	public void imageTest(){
		String jsonTemplatedOutput = getTemplatedJsonStringWithData("image", false);
		assertNotNull(jsonTemplatedOutput);
		
		assertTrue(generateHTMLFile("image", jsonTemplatedOutput));
	}
	
//	@Test
	public void imagePDFTest(){
		String jsonTemplatedOutput = getTemplatedJsonStringWithData("imagePDF", false);
		
		jsonTemplatedOutput = "<html><head></head><body>"+jsonTemplatedOutput+"</body></html>";
		String pdfPath = generatedFilesFolder + "/imagePDF.pdf";
		String context = "file:///C:/Users/Samuel/workspace/JavaCommons/test-files/test-specific/htmlGenerator/FormInputTemplates_test/res/images/";
		
		PDFGenerator.generatePDFfromRawHTML(pdfPath, jsonTemplatedOutput, context);
		
		assertTrue(generateHTMLFile("imagePDFOutput", jsonTemplatedOutput));
	}
	
//	@Test
	public void signatureTest(){
		String jsonTemplatedOutput = getTemplatedJsonStringWithData("signature", false);
		assertNotNull(jsonTemplatedOutput);
		
		File prefix = new File("./test-files/test-specific/htmlGenerator/signature/prefix.html");
		String prefixString = "";
		try{
			prefixString = FileUtils.readFileToString(prefix);
		}catch(Exception e){
			
		}
		jsonTemplatedOutput = prefixString + jsonTemplatedOutput;
		assertTrue(generateHTMLFile("signature", jsonTemplatedOutput));
	}
	
//	@Test
	public void signatureDisplay(){
		String jsonTemplatedOutput = getTemplatedJsonStringWithData("signature", false);
		assertNotNull(jsonTemplatedOutput);
		
		assertTrue(generateHTMLFile("signatureDisplay", jsonTemplatedOutput));
	}
	
//	@Test
	public void datePickerTest(){
		String jsonTemplatedOutput = getTemplatedJSONString("date", false, true);

		assertNotNull(jsonTemplatedOutput);
		assertTrue(generateHTMLFile("date", jsonTemplatedOutput));
		
		jsonTemplatedOutput = getTemplatedJSONString("date", false, true);
		assertNotNull(jsonTemplatedOutput);
		assertTrue(generateHTMLFile("dateWithData", jsonTemplatedOutput));
		
		jsonTemplatedOutput = getTemplatedJSONString("date", true, true);
		assertNotNull(jsonTemplatedOutput);
		assertTrue(generateHTMLFile("dateDisplay", jsonTemplatedOutput));
	}
	
//	@Test
	public void fullyQualifiedTest(){
		String jsonTemplatedOutput = getTemplatedJSONString("fullyQualified", false, true);
		
		assertNotNull(jsonTemplatedOutput);
		
		assertTrue(generateHTMLFile("fullyQualified", jsonTemplatedOutput));
	}
	
//	@Test
	public void dummy(){
		String test = "client[0].asd_re-csa";
		String result = RegexUtils.removeAllNonAlphaNumeric_allowCommonSeparators(test);
		System.out.println(result);
	}
	
//	@Test
	public void sanityTest(){
		
		Map<String, Object> inputValue = new HashMap<String, Object>();
		List<Map<String, String>> clientList = new ArrayList<Map<String, String>>();
		
		Map<String, String> clientAMap = new HashMap<String, String>();
		clientAMap.put("name", "Someone");
		clientAMap.put("title", "mister");
		clientList.add(clientAMap);
		
		Map<String, String> clientBMap = new HashMap<String, String>();
		clientBMap.put("name", "Someone Else");
		clientBMap.put("title", "doctor");
		clientList.add(clientBMap);
		
		inputValue.put("client", clientList);
		
		
		Object val = getRawValue("client[0].name", inputValue);
		
		System.out.println(val);
	}
	
	protected Object getRawValue(String fieldName, Map<String, Object> valueMap){
		Object val = null;

		
		if(fieldName.contains("&#91;")){
			fieldName.replace("&#91;", "[");
		}
		if(fieldName.contains("&#92;")){
			fieldName.replace("&#92;", "]");
		}
		
		if(valueMap != null && valueMap.containsKey(fieldName)){
			val = valueMap.get(fieldName);
		}
		
		//SINGLE TIER VALUE LOADING HACK!
		//this will allow you to load single tier values - however, it -SHOULDNT- crash if no value is found
		if(val == null){//if val == null, try again by splitting fieldname - THIS IS A HACK HACK HACK
			String[] fieldNameSplit = fieldName.split("\\.");
			String tempString = fieldNameSplit[1];
			if(fieldNameSplit != null && fieldNameSplit.length > 1){
				tempString = fieldNameSplit[1];
			}
			
			if(valueMap != null && valueMap.containsKey(tempString)){
				val = valueMap.get(tempString);
			}
		}
		
		if(val == null){
			//nukenukenuke
			Map<String, Object> fullyQualifiedMap = MapValueConv.toFullyQualifiedKeys(valueMap, "", ".");
			
			if(fullyQualifiedMap != null){
				val = fullyQualifiedMap.get(fieldName);
			}
		}
		//END HACK HACK HACK
		return val;
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

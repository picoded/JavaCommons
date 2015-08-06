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
		Map<String,Object> prefilledData = new HashMap<String, Object>();
		prefilledData.put("title", "Mr");
		prefilledData.put("name", "Samuel");
		
		prefilledData.put("nricpp", "9070462");
		prefilledData.put("nat", "Singaporean PR");
		
		prefilledData.put("CoB", "London");
		prefilledData.put("gender", "Male");
		
		prefilledData.put("marriage", "Single");
		prefilledData.put("dob", "17th May 1990");
		prefilledData.put("isSmoker", "No");
		
		prefilledData.put("address", "Blk 450D Tampines St 42 #04-418");
		prefilledData.put("postcode", "524450");
		prefilledData.put("country", "Singapore");
		
		prefilledData.put("race", "Others");
		prefilledData.put("language", "English");
		
		prefilledData.put("employment", "selfemployed");
		prefilledData.put("EduLvl", "Tertiary and Above");
		
		prefilledData.put("occupation", "Programmer");
		prefilledData.put("employer", "Picoded");
		
		prefilledData.put("contactHome", "67899449");
		prefilledData.put("contactOffice", "-NA-");
		prefilledData.put("contactHandphone", "92724850");
		prefilledData.put("contactFax", "-NA-");
		prefilledData.put("contactEmail", "samuel@socialoctet.com");
		
		prefilledData.put("Income", "Above $15,000");
		prefilledData.put("dueDiligenceYesNo", "No");
		
		return prefilledData;
	}
	
//	@Test
	public void doPDFOutput(){
		String htmlFileString = "./test-files/test-specific/htmlGenerator/pdfReadyHtml.html";
		String pdfFileString = "./test-files/test-specific/htmlGenerator/htmlPDF.pdf";
		picoded.fileUtils.PDFGenerator.generatePDFfromHTMLfile(pdfFileString, htmlFileString);
	}
	
//	@Test
	public void outputPrefilledPDF(){
		File jsonObjectFile = new File("./test-files/test-specific/htmlGenerator/testJSONObject.js");
		assertTrue(jsonObjectFile.canRead());
		String jsonFileString = "";
		try{
			jsonFileString = FileUtils.readFileToString(jsonObjectFile, Charset.defaultCharset());
		} catch (Exception ex){
		}
		Map<String, Object> jsonMap = ConvertJSON.toMap(jsonFileString);
		assertNotNull(jsonMap);
		
		FormNode formNode = new FormNode(jsonMap, getPrefilledData());
		String pdfReadyHtmlString = testObj.generatePDFReadyHTML(formNode);
		
		File pdfReadyHtmlFile = new File("./test-files/test-specific/htmlGenerator/pdfReadyHtml.html");
		
		try{
			FileWriter writer = new FileWriter(pdfReadyHtmlFile);
			writer.write(pdfReadyHtmlString);
			writer.flush();
			writer.close();
		}catch(Exception ex){
			
		}
		
		String pdfFileString = "./test-files/test-specific/htmlGenerator/htmlPDF.pdf";
		picoded.fileUtils.PDFGenerator.generatePDFfromRawHTML(pdfFileString, pdfReadyHtmlString);
	}
	
//	@Test
	public void outputHTMLFromJSONObject(){
		File jsonObjectFile = new File("./test-files/test-specific/htmlGenerator/testJSONObject.js");
		assertTrue(jsonObjectFile.canRead());
		String jsonFileString = "";
		try{
			jsonFileString = FileUtils.readFileToString(jsonObjectFile, Charset.defaultCharset());
		} catch (Exception ex){
			
		}
		
		Map<String, Object> jsonMap = ConvertJSON.toMap(jsonFileString);
		
		assertNotNull(jsonMap);
		
		
		FormNode formNode = new FormNode(jsonMap, getPrefilledData());
		
		String htmlVal = testObj.applyTemplating(formNode);
		File htmlFile = new File("./test-files/test-specific/htmlGenerator/htmlFromJSONObject.html");
		
		try{
			FileWriter writer = new FileWriter(htmlFile);
			writer.write(htmlVal);
			writer.flush();
			writer.close();
		}catch(Exception ex){
			
		}
	}
	
//	@Test
	public void outputHtmlFromJSONArray(){
		File jsonArrayFile = new File("./test-files/test-specific/htmlGenerator/testJSONArray.js");
		assertTrue(jsonArrayFile.canRead());
		String jsonFileString = "";
		try{
			jsonFileString = FileUtils.readFileToString(jsonArrayFile, Charset.defaultCharset());
		} catch (Exception ex){
			
		}
		
		List<FormNode> nodeList = FormNode.createFromJSONString(jsonFileString, getPrefilledData());
		assertNotNull(nodeList);
		
		String htmlVal = testObj.applyTemplating(nodeList);
		File htmlFile = new File("./test-files/test-specific/htmlGenerator/htmlFromJSONArray.html");
		
		try{
			FileWriter writer = new FileWriter(htmlFile);
			writer.write(htmlVal);
			writer.flush();
			writer.close();
		}catch(Exception ex){
			
		}
	}
	
	@Test
	public void testSimpleJSONObject(){
		File jsonObjectFile = new File("./test-files/test-specific/htmlGenerator/simpleJSONObject.js");
		assertTrue(jsonObjectFile.canRead());
		String jsonFileString = "";
		try{
			jsonFileString = FileUtils.readFileToString(jsonObjectFile, Charset.defaultCharset());
		} catch (Exception ex){
		}
		
		Map<String, Object> jsonMap = ConvertJSON.toMap(jsonFileString);
		
		assertNotNull(jsonMap);
		
		List<FormNode> formNodes = FormNode.createFromJSONString(jsonFileString, getPrefilledData());
		
		assertEquals(1, formNodes.get(0).childCount());
		assertEquals(2, formNodes.get(0).children().get(0).childCount());
		
		assertEquals("div", formNodes.get(0).getString("type"));
		assertEquals("title", formNodes.get(0).children().get(0).getString("type"));
		assertEquals("dropdown", formNodes.get(0).children().get(0).children().get(0).getString("type"));
//		assertEquals("text", formNodes.get(0).children().get(0).children().get(1).getString("type"));
		
		//html section
		String htmlVal = testObj.applyTemplating(formNodes);
		File htmlFile = new File("./test-files/test-specific/htmlGenerator/simpleHtmlObject.html");
		
		try{
			FileWriter writer = new FileWriter(htmlFile);
			writer.write(htmlVal);
			writer.flush();
			writer.close();
		}catch(Exception ex){
			
		}
		
		//pdf section
//		String pdfReadyHtmlString = testObj.generatePDFReadyHTML(formNodes);
//		File pdfReadyHtmlFile = new File("./test-files/test-specific/htmlGenerator/simplePDFHtml.html");
//		
//		try{
//			FileWriter writer = new FileWriter(pdfReadyHtmlFile);
//			writer.write(pdfReadyHtmlString);
//			writer.flush();
//			writer.close();
//		}catch(Exception ex){
//			
//		}
//		
//		String pdfFileString = "./test-files/test-specific/htmlGenerator/simplePDF.pdf";
//		picoded.fileUtils.PDFGenerator.generatePDFfromRawHTML(pdfFileString, pdfReadyHtmlString);
	}
	
//	@Test
	public void testDropdownWithOthers(){
		File jsonObjectFile = new File("./test-files/test-specific/htmlGenerator/testDropDownOthers.js");
		assertTrue(jsonObjectFile.canRead());
		String jsonFileString = "";
		try{
			jsonFileString = FileUtils.readFileToString(jsonObjectFile, Charset.defaultCharset());
		} catch (Exception ex){
		}
		Map<String, Object> jsonMap = ConvertJSON.toMap(jsonFileString);
		assertNotNull(jsonMap);
		
		Map<String, Object> dropdownData = new HashMap<String, Object>();
		dropdownData.put("natDropDown", "Malay");
		
		List<FormNode> formNodes = FormNode.createFromJSONString(jsonFileString, dropdownData);
		
		String htmlVal = testObj.applyTemplating(formNodes);
		File htmlFile = new File("./test-files/test-specific/htmlGenerator/dropdownOthers.html");
		
		try{
			FileWriter writer = new FileWriter(htmlFile);
			writer.write(htmlVal);
			writer.flush();
			writer.close();
		}catch(Exception ex){
			
		}
		
		//pdf section
		String pdfReadyHtmlString = testObj.generatePDFReadyHTML(formNodes);
		File pdfReadyHtmlFile = new File("./test-files/test-specific/htmlGenerator/dropdownWithOthersPDF.html");
		
		try{
			FileWriter writer = new FileWriter(pdfReadyHtmlFile);
			writer.write(pdfReadyHtmlString);
			writer.flush();
			writer.close();
		}catch(Exception ex){
			
		}
		
		String pdfFileString = "./test-files/test-specific/htmlGenerator/dropdownWithOthersPDF.pdf";
		picoded.fileUtils.PDFGenerator.generatePDFfromRawHTML(pdfFileString, pdfReadyHtmlString);
	}
}

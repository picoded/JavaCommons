package picodedTests.webTemplateEngines;

// Target test class
import picoded.webTemplateEngines.*;
import picoded.conv.ConvertJSON;



// Test Case include
import org.junit.*;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileReader;
// java includes
import java.util.*;

///
/// Test Case for picoded.struct.CaseInsensitiveHashMap
///
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
	
	@Test
	public void testNodes(){
		FormNode normalHeaderNode = getHeaderNode();
		String headerVal = testObj.applyTemplating(normalHeaderNode);
		assertEquals("<h1 class=\"pf_titleClass\">Title</h1>", headerVal);
		
		FormNode normalSelectNode_map = getSelectNode_usingMap();
		String selectVal_map = testObj.applyTemplating(normalSelectNode_map);
		assertEquals("<select class=\"pf_dropDownClass\"><option value=\"opt1\">Option 1</option><option value=\"opt2\">Option 2</option></select>", selectVal_map);
		
		FormNode normalSelectNode_list = getSelectNode_usingList();
		String selectVal_list = testObj.applyTemplating(normalSelectNode_list);
		assertEquals("<select class=\"pf_dropDownClass\"><option>Option 1</option><option>Option 2</option></select>", selectVal_list);
		
		FormNode normalInputFieldNode = getInputFieldNode();
		String inputFieldVal = testObj.applyTemplating(normalInputFieldNode);
		assertEquals("Input Field: <input class=\"pf_inputFieldClass\" type=text>", inputFieldVal);
	}
	
	@Test
	public void testNodeList(){
		List<FormNode> nodes = getNestedNodes();
		String finalVal = testObj.applyTemplating(nodes);
		System.out.println(finalVal);
		assertEquals("<div class=\"pf_divClass\">"+
					 	"<h1 class=\"customClass\">Title</h1>"+
					 	"<div class=\"pf_divClass\">"+
					 		"Input Field: <input class=\"pf_inputFieldClass\" type=text>"+
				 			"<select class=\"pf_dropDownClass\">"+
			 					"<option value=\"opt1\">Option 1</option>"+
			 					"<option value=\"opt2\">Option 2</option>"+
			 				"</select>"+
		 				"</div>"+
					 "</div>"+
	 				 "<h2 class=\"pf_titleClass\">Second Title</h2>",
				finalVal);
	}
	
	private FormNode getHeaderNode(){
		FormNode node = new FormNode();
		node.put(FormGenerator.htmlTypeKey, "title");
		node.put("number", 1);
		node.put(FormGenerator.htmlTextKey, "Title");
		
		return node;
	}
	
	private FormNode getSelectNode_usingList(){
		FormNode node = new FormNode();
		node.put(FormGenerator.htmlTypeKey, FormGenerator.dropDownListKey);
		
		List<String> vals = new ArrayList<String>();
		vals.add("Option 1");
		vals.add("Option 2");
		node.put("option", vals);
		
		return node;
	}
	
	private FormNode getSelectNode_usingMap(){
		FormNode node = new FormNode();
		node.put(FormGenerator.htmlTypeKey, FormGenerator.dropDownListKey);
		
		Map<String, String> vals = new LinkedHashMap<String, String>();
		vals.put("opt1", "Option 1");
		vals.put("opt2", "Option 2");
		node.put("option", vals);
		
		return node;
	}
	
	private FormNode getInputFieldNode(){
		FormNode node = new FormNode();
		node.put(FormGenerator.htmlTypeKey, FormGenerator.inputFieldKey);
		node.put("inputLabel", "Input Field: ");
		node.put("inputType", "text");
		
		return node;
	}
	
	private List<FormNode> getNestedNodes(){
		List<FormNode> returnVal = new ArrayList<FormNode>();
		
		//Wrapper is the VERY FIRST layer of html
		//anything nested is an input

		/*
		 * <div class="pf_wrapper pf_titleWrapper">  // # This is the prefix output from wrapper
		 *      <!--<div class="pf_label"></div>-->  //   -> The label is not included, as there is no value          
		 * 		<h1 class="pf_title">Title</h1>      // # This is the output from Input
		 * </div>                                    // # This is the suffix output from wrapper
		 */

		/*
		 * <div class="pf_wrapper pf_inputWrapper">            // # This is the prefix output from wrapper
		 *      <div class="pf_label">Input Name</div>         //   -> The label is not included, as there is no value          
		 * 		<input class="pf_inputFieldClass" type=text>   // # This is the output from Input
		 * </div>                                              // # This is the suffix output from wrapper
		 */

		/*
		 * <div class="pf_wrapper pf_selectWrapper">                // # This is the prefix output from wrapper
		 *      <div class="pf_label">Input Name</div>              //   -> The label is not included, as there is no value      
		 * 		<select class="pf_dropDownClass">                   // # This is the output from Input
		 * 			<option value="option1">Option 1</option>       //
		 * 			<option value="option2">Option 2</option>       //
		 * 		</select>                                           //
		 * </div>                                                   // # This is the suffix output from wrapper
		 */

		/*
		 * <div class="pf_wrapper pf_canvasWrapper">                // # This is the prefix output from wrapper
		 *      <div class="pf_label">Input Name</div>              //   -> The label is not included, as there is no value      
		 * 		<div class="pf_canvasClass">                        // # This is the output from Input
		 * 			<canvas id="xyz" onload="xyz()"/>               //
		 * 		</div>                                              //
		 * </div>                                                   // # This is the suffix output from wrapper
		 */
		
		
		/*
		 * <div class="pf_divClass">
		 * 		<h1 class="customClass">Title</h1>
		 * 			<div class="pf_divClass">
		 * 				Input Field: <input class="pf_inputFieldClass" type=text>
		 * 				<select class="pf_dropDownClass">
		 * 					<option value="option1">Option 1</option>
		 * 					<option value="option2">Option 2</option>
		 * 				</select>
		 * 			</div>
		 * </div>
		 * <h2>Second Title</h2>
		 * 
		 */
		
		//create input field and dropdown first
		FormNode inputNode = getInputFieldNode();
		FormNode dropDownNode = getSelectNode_usingMap();
		
		FormNode innerDivNode = new FormNode();
		innerDivNode.put("type", "div");
		
		innerDivNode.addChild(inputNode);
		innerDivNode.addChild(dropDownNode);
		
		//cxreate header
		FormNode headerNode = getHeaderNode();
		headerNode.put("inputClass", "customClass");
		
		FormNode outerDivNode = new FormNode();
		outerDivNode.put("type", "div");
		
		
		outerDivNode.addChild(headerNode);
		outerDivNode.addChild(innerDivNode);
		
		FormNode secondHeader = new FormNode();
		secondHeader.put("type", "title");
		secondHeader.put("number", 2);
		secondHeader.put("text", "Second Title");
		
		returnVal.add(outerDivNode);
		returnVal.add(secondHeader);
		
		return returnVal;
	}
	
//	
////	@Test
//	public void simpleDiv() {
//		assertEquals("<div></div>", FormGenerator.htmlNodeGenerator("div", null, null, null));
//		assertEquals("<h1>hello world</h1>", FormGenerator.htmlNodeGenerator("h1", null, null, "hello world"));
//		
//		assertEquals("<h1 type='hidden'>hello world</h1>", FormGenerator.htmlNodeGenerator("h1", null, ConvertJSON.toMap("{ \"type\":\"hidden\" }"), "hello world"));
//	}
//	
////	@Test
//	public void basicObjectTests() {
//		Map<String,Object> format = new HashMap<String,Object>();
//		
//		format.put("type", "title");
//		assertEquals("<h3 class=\"title\">title</h3>", FormGenerator.templateObjectToHtml(format));
//		
//		format.put("title", "hello world");
//		assertEquals("<h3 class=\"title\">hello world</h3>", FormGenerator.templateObjectToHtml(format));
//		
//	}
}

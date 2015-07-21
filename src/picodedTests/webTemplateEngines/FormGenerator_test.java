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
		FormNode titleNode = getTitleNode();
		String finalVal = testObj.applyTemplating(titleNode);
		assertEquals("<div><h3>Title</h3></div>", finalVal);
		
		FormNode selectNode = getDropDownNode();
		String finalSelectVal = testObj.applyTemplating(selectNode);
		assertEquals("<div><div>Drop Down Title</div><select name=\"dropdowntitle\"><option value=\"option1\">Option 1</option><option value=\"option2\">Option 2</option></select></div>", finalSelectVal);
	}

	private FormNode getTitleNode(){
		FormNode titleNode = new FormNode();
		
		titleNode.put("type", "title");
		titleNode.put("text", "Title");
		titleNode.put("wrapper", "div");
		
		return titleNode; 
	}
	
	private FormNode getDropDownNode(){
		FormNode dropDownNode = new FormNode();
		dropDownNode.put("type", "dropdown");
		dropDownNode.put("label", "Drop Down Title");
		dropDownNode.put("field", "dropdowntitle"); //this field corresponds to the database key - defaults to label lowercased
		List<String> options = Arrays.asList("Option 1", "Option 2");
		dropDownNode.put("options", options); //put map here
		dropDownNode.put("wrapper", "div");
		
		return dropDownNode;
	}
	
	
	
	private List<FormNode> getNestedNodes(){
		List<FormNode> returnVal = new ArrayList<FormNode>();
		
		//wrapper is whatever you define it to be
		//for example, a titlewrapper will wrap a div
		//an inputWrapper will wrap a inputfield
		//each wrapper is a very specific thing

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

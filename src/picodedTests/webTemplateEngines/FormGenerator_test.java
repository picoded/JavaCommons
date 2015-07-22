package picodedTests.webTemplateEngines;

import picoded.webTemplateEngines.*;

import org.junit.*;
import static org.junit.Assert.*;
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
	
	@Test
	public void testTitleNode(){
		FormNode titleNodeWithNoWrapper = getTitleNode("none");
		String titleNodeWithNoWrapper_output = testObj.applyTemplating(titleNodeWithNoWrapper);
		assertEquals("<h3 class=\"pf_inputClass\">Title</h3>", titleNodeWithNoWrapper_output);
		
		FormNode titleNodeWithDefaultWrapper = getTitleNode("default");
		String titleNodeWithDefaultWrapper_output = testObj.applyTemplating(titleNodeWithDefaultWrapper);
		assertEquals("<div class=\"pf_titleClass\"><h3 class=\"pf_inputClass\">Title</h3></div>", titleNodeWithDefaultWrapper_output);
		
		FormNode titleNode_defaultWrapper_customClass = getTitleNode("default");
		titleNode_defaultWrapper_customClass.put("wrapperClass", "customClass");
		titleNode_defaultWrapper_customClass.put("inputClass", "customInputClass");
		String titleNode_defaultWrapper_customClass_output = testObj.applyTemplating(titleNode_defaultWrapper_customClass);
		assertEquals("<div class=\"customClass\"><h3 class=\"customInputClass\">Title</h3></div>", titleNode_defaultWrapper_customClass_output);
		
		//test custom css
		titleNode_defaultWrapper_customClass.put("wrapperCss", "font-size:5; color:red;");
		titleNode_defaultWrapper_customClass.put("inputCss", "font-size:10; blue;");
		String newOutput_withCss = testObj.applyTemplating(titleNode_defaultWrapper_customClass);
		assertEquals("<div class=\"customClass\" style=\"font-size:5; color:red;\"><h3 class=\"customInputClass\" style=\"font-size:10; blue;\">Title</h3></div>",newOutput_withCss);
	}
	
	@Test
	public void testDropDownNode(){
		FormNode dropDownNode_list_noWrapper = getDropDownNode(false, "none");
		dropDownNode_list_noWrapper.put("field", "dropdowntitle");
		String dropDownNode_list_noWrapper_Output = testObj.applyTemplating(dropDownNode_list_noWrapper);
		
		FormNode dropDownNode_list_defaultWrapper = getDropDownNode(false, "default");
		dropDownNode_list_defaultWrapper.put("label", "Drop Down Title");
		String dropDownNode_list_defaultWrapper_Output = testObj.applyTemplating(dropDownNode_list_defaultWrapper);
		
		FormNode dropDownNode_map_noWrapper = getDropDownNode(true, "none");
		dropDownNode_map_noWrapper.put("field", "dropdowntitle");
		String dropDownNode_map_noWrapper_Output = testObj.applyTemplating(dropDownNode_map_noWrapper);
		
		FormNode dropDownNode_map_defaultWrapper = getDropDownNode(true, "default");
		dropDownNode_map_defaultWrapper.put("label", "Drop Down Title");
		String dropDownNode_map_defaultWrapper_Output = testObj.applyTemplating(dropDownNode_map_defaultWrapper);
		
		//check against no wrapper output
		assertEquals("<select class=\"pf_inputClass\" name=\"dropdowntitle\">"
				+ "<option value=\"option1\">Option 1</option>"
				+ "<option value=\"option2\">Option 2</option>"
				+ "<option value=\"option3\">Option 3</option>"
				+ "<option value=\"option4\">Option 4</option>"
				+ "<option value=\"option5\">Option 5</option>"
				+ "</select>", dropDownNode_list_noWrapper_Output);
		
		assertEquals(dropDownNode_list_noWrapper_Output, dropDownNode_map_noWrapper_Output);
		
		//check against default wrapper output
		assertEquals("<div class=\"pf_dropdownClass\"><div class=\"pf_labelClass\">Drop Down Title</div>"
				+ "<select class=\"pf_inputClass\" name=\"dropdowntitle\">"
				+ "<option value=\"option1\">Option 1</option>"
				+ "<option value=\"option2\">Option 2</option>"
				+ "<option value=\"option3\">Option 3</option>"
				+ "<option value=\"option4\">Option 4</option>"
				+ "<option value=\"option5\">Option 5</option>"
				+ "</select></div>", dropDownNode_list_defaultWrapper_Output);
		
		assertEquals(dropDownNode_list_defaultWrapper_Output, dropDownNode_map_defaultWrapper_Output);
		
		//test custom classes
		dropDownNode_map_defaultWrapper.put("wrapperClass", "customWrapperClass");
		dropDownNode_map_defaultWrapper.put("labelClass", "customLabelClass");
		String newOutput = testObj.applyTemplating(dropDownNode_map_defaultWrapper);
		assertEquals("<div class=\"customWrapperClass\"><div class=\"customLabelClass\">Drop Down Title</div>"
				+ "<select class=\"pf_inputClass\" name=\"dropdowntitle\">"
				+ "<option value=\"option1\">Option 1</option>"
				+ "<option value=\"option2\">Option 2</option>"
				+ "<option value=\"option3\">Option 3</option>"
				+ "<option value=\"option4\">Option 4</option>"
				+ "<option value=\"option5\">Option 5</option>"
				+ "</select></div>", newOutput);
	}
	
	@Test
	public void testTextInputNode(){
		FormNode textInputNode_noWrapper = getTextInputNode("none");
		String textInputNode_noWrapper_output = testObj.applyTemplating(textInputNode_noWrapper);
		assertEquals("<input class=\"pf_inputClass\" type=\"text\" name=\"textinputfield\">", textInputNode_noWrapper_output);
		
		
		FormNode textInputNode_defaultWrapper = getTextInputNode("default");
		String textInputNode_defaultWrapper_output = testObj.applyTemplating(textInputNode_defaultWrapper);
		assertEquals("<div class=\"pf_textClass\"><div class=\"pf_labelClass\">Text Input Field: </div><input class=\"pf_inputClass\" type=\"text\" name=\"textinputfield\"></div>", textInputNode_defaultWrapper_output);
		
		//test custom wrapper, label, and input classes
		textInputNode_defaultWrapper.put("wrapperClass", "customWrapperClass");
		textInputNode_defaultWrapper.put("labelClass", "customLabelClass");
		textInputNode_defaultWrapper.put("inputClass", "customInputClass");
		String newOutputWithCustomClass = testObj.applyTemplating(textInputNode_defaultWrapper);
		assertEquals("<div class=\"customWrapperClass\"><div class=\"customLabelClass\">Text Input Field: </div><input class=\"customInputClass\" type=\"text\" name=\"textinputfield\"></div>", newOutputWithCustomClass);
	}
	
	@Test
	public void testNestedNodes(){
		/*
		 * <div class="pf_titleClass">
		 * 		<h3 class="customTitleClass" style="font-size:20; color:yellow;">Title Here</h3>
		 * 		<div class="customDropDownClass">
		 * 			<select class="pf_dropdownClass">
		 * 				<option value="option1">Option 1</option>
		 * 				<option value="option2">Option 2</option>
		 * 				<option value="option3">Option 3</option>
		 * 				<option value="option4">Option 4</option>
		 * 				<option value="option5">Option 5</option>
		 * 			</select>
		 * 		</div>
		 * </div>
		 * <div class="customTextInputClass">
		 * 		<div class="pf_labelClass" style="font-size:15; color:red;">Text Input Field:</div>
		 * 		<input class="pf_inputClass" type="text" name="textinputfield>
		 * </div>
		 */
		
		FormNode titleNode = new FormNode();
		titleNode.put("type", "title");
		titleNode.put("text", "Title Here");
		titleNode.put("inputClass", "customTitleClass");
		titleNode.put("inputCss", "font-size:20; color:yellow;");
		
		FormNode childDropDownNode = getDropDownNode(true, "default");
		childDropDownNode.put("wrapperClass", "customDropDownClass");
		
		titleNode.addChild(childDropDownNode);
		
		FormNode textInputNode = getTextInputNode("default");
		textInputNode.put("wrapperClass", "customTextInputClass");
		textInputNode.put("labelCss", "font-size:15; color:red;");
		textInputNode.put("label", "Text Input Field: ");
		
		ArrayList<FormNode> nodes = new ArrayList<FormNode>();
		nodes.add(titleNode);
		nodes.add(textInputNode);
		
		String finalOutput = testObj.applyTemplating(nodes);
		
		assertEquals("<div class=\"pf_titleClass\">"
		  		+"<h3 class=\"customTitleClass\" style=\"font-size:20; color:yellow;\">Title Here</h3>"
		  		+"<div class=\"customDropDownClass\">"
		  		+"<select class=\"pf_inputClass\">"
		  		+"<option value=\"option1\">Option 1</option>"
		  		+"<option value=\"option2\">Option 2</option>"
		  		+"<option value=\"option3\">Option 3</option>"
		  		+"<option value=\"option4\">Option 4</option>"
		  		+"<option value=\"option5\">Option 5</option>"
		  		+"</select>"
		  		+"</div>"
	  			+"</div>"
  				+"<div class=\"customTextInputClass\">"
		  		+"<div class=\"pf_labelClass\" style=\"font-size:15; color:red;\">Text Input Field: </div>"
		  		+"<input class=\"pf_inputClass\" type=\"text\" name=\"textinputfield\">"
		  		+"</div>", finalOutput);
		
	}

	private FormNode getTitleNode(String wrapperType){
		FormNode titleNode = new FormNode();
		
		titleNode.put("type", "title");
		titleNode.put("text", "Title");
		titleNode.put("wrapper", wrapperType);
		
		return titleNode; 
	}
	
	private FormNode getDropDownNode(boolean useMap, String wrapperType){
		FormNode dropDownNode = new FormNode();
		dropDownNode.put("type", "dropdown");
		//dropDownNode.put("label", "Drop Down Title");
		//dropDownNode.put("field", "dropdowntitle"); //this field corresponds to the database key - defaults to label lowercased and trimmed of whitespace
		
		if(useMap){
			LinkedHashMap<String, String> options = new LinkedHashMap<String, String>();
			options.put("option1", "Option 1");
			options.put("option2", "Option 2");
			options.put("option3", "Option 3");
			options.put("option4", "Option 4");
			options.put("option5", "Option 5");
			dropDownNode.put("options", options);
		}else{
			List<String> options = Arrays.asList("Option 1", "Option 2", "Option 3", "Option 4", "Option 5");
			dropDownNode.put("options", options);
		}
		
		dropDownNode.put("wrapper", wrapperType);
		
		return dropDownNode;
	}
	
	private FormNode getTextInputNode(String wrapperType){
		FormNode textInputNode = new FormNode();
		textInputNode.put("type", "text");
		//textInputNode.put("field", "textinputfield");
		textInputNode.put("label", "Text Input Field: ");
		textInputNode.put("wrapper", wrapperType);
		
		return textInputNode;
	}
}

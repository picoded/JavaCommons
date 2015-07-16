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
	public void importAndReadJSON(){
		
		System.out.println("");
		
		List<FormNode> nodes = new ArrayList<FormNode>();
//		FormNode node = getDoubleLayerTitleNode();
//		nodes.add(node);
		
		FormNode singleNode = getTestingNode();
		nodes.add(singleNode);
		
		String finalVal = FormGenerator.applyTemplating(nodes);
		
		System.out.println(finalVal);
	}
	
	private FormNode getDoubleLayerTitleNode(){
		FormNode firstLayerNode = getRandomTitleNode();
		firstLayerNode.addChild(getRandomTitleNode());
		
		return firstLayerNode;
	}
	
	private FormNode getTestingNode(){
		FormNode divNode = new FormNode();
		divNode.put("type", "div");
		
		FormNode headerNode = new FormNode();
		headerNode.put("type", "title");
		headerNode.put("number", 1);
		headerNode.put("text", "First Header Node");
		headerNode.put("inputCss", "color: red; margin: 0 0 5px 0");
		divNode.addChild(headerNode);
		
		FormNode headerNodeSecond = new FormNode();
		headerNodeSecond.put("type", "title");
		headerNodeSecond.put("number", 2);
		headerNodeSecond.put("text", "Second Header Node");
		headerNodeSecond.put("inputClass", "customClass");
		headerNodeSecond.put("size", 20);
		divNode.addChild(headerNodeSecond);
		
		FormNode innerDivNode = new FormNode();
		innerDivNode.put("type", "div");
		
		FormNode doubleInnerDivNode = new FormNode();
		doubleInnerDivNode.put("type", "div");
		innerDivNode.addChild(doubleInnerDivNode);
		
		FormNode innerHeaderNode = new FormNode();
		innerHeaderNode.put("type", "title");
		innerHeaderNode.put("number", 3);
		innerHeaderNode.put("text", "Inner Header Node");
		innerHeaderNode.put("size", 10);
		innerDivNode.addChild(innerHeaderNode);
		
		divNode.addChild(innerDivNode);
		
		return divNode;
	}
	
	private FormNode getRandomTitleNode(){
		Random rnd = new Random();
		
		FormNode node = new FormNode();
		node.put("type", "title");
		node.put("text", "Title");
		
		if(rnd.nextBoolean()){
			node.put("inputClass", "funkyClass");
		}
		
		if(rnd.nextBoolean()){
			node.put("inputCss", "display: none; color: red; margin: 0 0 5px 0");
		} else {
			Map<String, Object> styles = new HashMap<String, Object>();
			styles.put("display", "none");
			styles.put("color", "red");
			styles.put("margin", "0 0 5px 0");
			node.put("inputCss", styles);
		}
		
		return node;
	}
	
	
//	@Test
	public void simpleDiv() {
		assertEquals("<div></div>", FormGenerator.htmlNodeGenerator("div", null, null, null));
		assertEquals("<h1>hello world</h1>", FormGenerator.htmlNodeGenerator("h1", null, null, "hello world"));
		
		assertEquals("<h1 type='hidden'>hello world</h1>", FormGenerator.htmlNodeGenerator("h1", null, ConvertJSON.toMap("{ \"type\":\"hidden\" }"), "hello world"));
	}
	
//	@Test
	public void basicObjectTests() {
		Map<String,Object> format = new HashMap<String,Object>();
		
		format.put("type", "title");
		assertEquals("<h3 class=\"title\">title</h3>", FormGenerator.templateObjectToHtml(format));
		
		format.put("title", "hello world");
		assertEquals("<h3 class=\"title\">hello world</h3>", FormGenerator.templateObjectToHtml(format));
		
	}
}

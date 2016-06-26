package picodedTests.page.mte;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import picoded.page.mte.*;

// IMPORTANT! Dun just test NULL, and Succesful cases, test expected failure also!
public class MinimalTemplateEngine_test {
	protected MinimalTemplateEngine mteObj;
	
	@Before
	public void setUp() {
		mteObj = new MinimalTemplateEngine();
	}
	
	@After
	public void tearDown() {
		mteObj = null;
	}
	
	@Test
	public void constructor() {
		assertNotNull(mteObj);
	}
	
	public Map<String, Object> simpleVarMap() {
		Map<String, Object> ret = new HashMap<String, Object>();
		
		ret.put("helloMsg", "Hello World, How do you do?");
		ret.put("reply", "Good =)");
		ret.put("falseCond", false);
		
		return ret;
	}
	
	@Test
	public void parseTemplate_helloWorld() {
		assertEquals("<h1>Hello World, How do you do?</h1>", mteObj.parseTemplate("<h1>${helloMsg}</h1>", simpleVarMap()));
		assertEquals("<h1>Hello World, How do you do?</h1>",
			mteObj.parseTemplate("<h1>{{helloMsg}}</h1>", simpleVarMap()));
		assertEquals("<h1>Hello World, How do you do?</h1>",
			mteObj.parseTemplate("<h1>{{{helloMsg}}}</h1>", simpleVarMap()));
	}
	
	@Test
	public void conditionalTemplate_false() {
		assertEquals("<h1>Hello World, How do you do?</h1>", mteObj.parseTemplate("<h1>${helloMsg}</h1>"
			+ "${if falseCond}<h2>${reply}</h2>${end}", simpleVarMap()));
	}
	
	// @Test
	// public void conditionalTemplate_true() {
	// 	mteObj.baseDataModel.put("helloMsg", "Hello World, How do you do?");
	// 	mteObj.baseDataModel.put("reply", "Good =)");
	// 	
	// 	HashMap<String, Object> dataObj = new HashMap<String, Object>();
	// 	
	// 	dataObj.put("cond", "true boolean, or any non null data will do here");
	// 	assertEquals("<h1>Hello World, How do you do?</h1><h2>Good =)</h2>",
	// 	
	// 	mteObj.parseTemplate("<h1>${helloMsg}</h1>" + "${if cond}<h2>${reply}</h2>${end}",
	// 	
	// 	dataObj));
	// }
	// 
	// @Test
	// public void iterationTemplate() {
	// 	mteObj.baseDataModel.put("dataList", Arrays.asList(new String[] { "Good", "Bad", "Ugly" }));
	// 	
	// 	assertEquals("<h2>Good, Bad, Ugly, </h2>",
	// 	
	// 	mteObj.parseTemplate("<h2>${foreach dataList item}${item}, ${end}</h2>"));
	// }
	// 
	// @Test
	// public void hashmapDataFetchTemplate() {
	// 	HashMap<String, String> mObj = new HashMap<String, String>();
	// 	mObj.put("hello", "world");
	// 	mObj.put("world", "ends");
	// 	
	// 	mteObj.baseDataModel.put("dataObj", mObj);
	// 	
	// 	assertEquals("<h2>world ends</h2>", mteObj.parseTemplate("<h2>${dataObj.hello} ${dataObj.world}</h2>"));
	// }
	// 
	// @Test
	// public void listOfHashmapIterationTemplate() {
	// 	ArrayList<HashMap<String, String>> dataList = new ArrayList<HashMap<String, String>>(3);
	// 	HashMap<String, String> mObj;
	// 	
	// 	for (int i = 0; i < 3; ++i) {
	// 		mObj = new HashMap<String, String>();
	// 		mObj.put("m1", "hello");
	// 		mObj.put("m2", ":" + i);
	// 		dataList.add(mObj);
	// 	}
	// 	mteObj.baseDataModel.put("dataList", dataList);
	// 	
	// 	assertEquals("<h2>hello:0</h2><h2>hello:1</h2><h2>hello:2</h2>",
	// 		mteObj.parseTemplate("${foreach dataList mObj}<h2>${mObj.m1}${mObj.m2}</h2>${end}"));
	// }
	// 
	// //@Test
	// public void registerRendererTest() {
	// 	NamedRenderer render = new TempNamedRenderer();
	// 	//assertEquals(mteObj, mteObj.registerRenderer(mte.unixTimeToDisplayTime.class, render));
	// }
	// 
	// @Test
	// public void registerNamedRendererTest() {
	// 	NamedRenderer render = new TempNamedRenderer();
	// 	assertEquals(mteObj, mteObj.registerNamedRenderer(render));
	// }
	// 
	// @Test
	// public void mteEngineTest() {
	// 	assertNotNull(mteObj.mteEngine());
	// }
	// 
	// @Test
	// public void unixTimeToFullStringTest() {
	// 	assertEquals("20/11/2015 10:27:42 UTC", mte.unixTimeToFullString(1448015262));
	// }
	// 
	// @Test
	// public void unixTimeToDisplayTimeTest() {
	// 	assertEquals("20/11/2015 10:27", mte.unixTimeToDisplayTime(1448015262));
	// }
	// 
	// @Test
	// public void unixTimeToFormatTest() {
	// 	assertEquals("20-Nov-2015", mte.unixTimeToFormat(1448015262, "dd-MMM-yyyy"));
	// }
	// 
	// @Test
	// public void readFileToStringTest() throws java.io.IOException {
	// 	assertEquals("mte Test", mte.readFileToString("./test-files/test-specific/conv/mte_Test.txt"));
	// }
	// 
	// @Test
	// public void readFileToStringTestFileInput() throws java.io.IOException {
	// 	assertEquals("mte Test", mte.readFileToString(new File("./test-files/test-specific/conv/mte_Test.txt")));
	// }
	// 
	// @Test
	// public void rawHtmlPartTest() throws java.io.IOException {
	// 	assertEquals("mte Test", mteObj.rawHtmlPart("test-files/test-specific/conv/mte_Test.txt"));
	// }
	// 
	// @Test(expected = java.io.IOException.class)
	// public void rawHtmlPartTest_Exception() throws java.io.IOException {
	// 	assertEquals("mte Test", mteObj.htmlTemplatePart("test-files/test-specific/conv/mteTest.txt"));
	// }
	// 
	// @Test
	// public void rawHtmlPartTestOverloadMethod() throws java.io.IOException {
	// 	HashMap<String, Object> mObj = new HashMap<String, Object>();
	// 	mObj.put("m1", "hello");
	// 	mObj.put("m2", ":" + new Integer(1));
	// 	assertEquals("mte Test", mteObj.htmlTemplatePart("test-files/test-specific/conv/mte_Test.txt", mObj));
	// }
	// 
	// private static class TempNamedRenderer extends mte implements NamedRenderer {
	// 	@Override
	// 	public RenderFormatInfo getFormatInfo() {
	// 		return null;
	// 	}
	// 	
	// 	@Override
	// 	public String getName() {
	// 		return "TempNamedRenderer";
	// 	}
	// 	
	// 	@Override
	// 	public Class<?>[] getSupportedClasses() {
	// 		return new Class<?>[] { long.class, int.class, Number.class };
	// 	}
	// 	
	// 	@Override
	// 	public String render(Object o, String format, Locale locale) {
	// 		DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT, locale);
	// 		return df.format((Date) o);
	// 	}
	// }
	
}

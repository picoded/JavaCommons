package picodedTests.conv;
import static org.junit.Assert.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Arrays;

import picoded.conv.JMTE;

// IMPORTANT! Dun just test NULL, and Succesful cases, test expected failure also!
public class JMTE_test {
	protected JMTE jmteObj;
	
	@Before
	public void setUp() {
		jmteObj = new JMTE();
	}
	
	@After
	public void tearDown() {
		jmteObj = null;
	}
	
	@Test
	public void constructor() {
		assertNotNull( jmteObj );
	}
	
	@Test
	public void template() {
		jmteObj.baseDataModel.put("helloMsg", "Hello World, How do you do?");
		
		assertEquals( "<h1>Hello World, How do you do?</h1>", jmteObj.parseTemplate("<h1>${helloMsg}</h1>"));
	}
	
	@Test
	public void conditionalTemplate_false() {
		jmteObj.baseDataModel.put("helloMsg", "Hello World, How do you do?");
		jmteObj.baseDataModel.put("reply", "Good =)");
		
		HashMap<String,Object> dataObj = new HashMap<String,Object>();
		
		dataObj.put("cond", false);
		assertEquals("<h1>Hello World, How do you do?</h1>",
						 
						 jmteObj.parseTemplate(
													  "<h1>${helloMsg}</h1>"+
													  "${if cond}<h2>${reply}</h2>${end}",
													  
													  dataObj
													  )
						 );
	}
	
	@Test
	public void conditionalTemplate_true() {
		jmteObj.baseDataModel.put("helloMsg", "Hello World, How do you do?");
		jmteObj.baseDataModel.put("reply", "Good =)");
		
		HashMap<String,Object> dataObj = new HashMap<String,Object>();
		
		dataObj.put("cond", "true boolean, or any non null data will do here");
		assertEquals("<h1>Hello World, How do you do?</h1><h2>Good =)</h2>",
						 
						 jmteObj.parseTemplate(
													  "<h1>${helloMsg}</h1>"+
													  "${if cond}<h2>${reply}</h2>${end}",
													  
													  dataObj
													  )
						 );
	}
	
	@Test
	public void iterationTemplate() {
		jmteObj.baseDataModel.put("dataList", Arrays.asList(new String[]{"Good", "Bad", "Ugly"}));
		
		assertEquals("<h2>Good, Bad, Ugly, </h2>",
						 
						 jmteObj.parseTemplate("<h2>${foreach dataList item}${item}, ${end}</h2>")
						 );
	}
	
	@Test
	public void hashmapDataFetchTemplate() {
		HashMap<String,String>mObj = new HashMap<String,String>();
		mObj.put("hello","world");
		mObj.put("world","ends");
		
		jmteObj.baseDataModel.put("dataObj", mObj);
		
		
		assertEquals("<h2>world ends</h2>",
						 jmteObj.parseTemplate("<h2>${dataObj.hello} ${dataObj.world}</h2>")
						 );
	}
	
	@Test
	public void listOfHashmapIterationTemplate() {
		ArrayList< HashMap<String,String> > dataList = new ArrayList< HashMap<String,String> >(3);
		HashMap<String,String>mObj;
		
		for(int i=0; i<3; ++i) {
			mObj = new HashMap<String,String>();
			mObj.put("m1","hello");
			mObj.put("m2",":"+i);
			dataList.add(mObj);
		}
		jmteObj.baseDataModel.put("dataList", dataList);
		
		assertEquals("<h2>hello:0</h2><h2>hello:1</h2><h2>hello:2</h2>",
						 jmteObj.parseTemplate("${foreach dataList mObj}<h2>${mObj.m1}${mObj.m2}</h2>${end}")
						 );
	}
	
	
	
}

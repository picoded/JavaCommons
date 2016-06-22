package picodedTests.page.mte.internal;

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
import picoded.page.mte.internal.*;

// IMPORTANT! Dun just test NULL, and Succesful cases, test expected failure also!
public class SyntaxTreeRoot_test {
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
	
	@Test
	public void parseTemplate_helloWorld() {
		String helloTemplate = "<h1>Hello Wolrd</h1>";
		
		SyntaxTreeRoot root = new SyntaxTreeRoot(mteObj, helloTemplate);
		assertNotNull(root);
		assertEquals(0, root.childNodes.size());
		
		root.processStageOne();
		assertEquals(1, root.childNodes.size());
		
		assertEquals(helloTemplate, root.toString());
	}
	
	@Test
	public void parseTemplate_helloVar() {
		String helloTemplate = "<h1>${helloMsg}</h1>";
		
		SyntaxTreeRoot root = new SyntaxTreeRoot(mteObj, helloTemplate);
		assertNotNull(root);
		assertEquals(0, root.childNodes.size());
		
		root.processStageOne();
		assertEquals(3, root.childNodes.size());
		
		assertEquals(helloTemplate, root.toString());
	}
	
	@Test
	public void parseTemplate_basicIf() {
		String helloTemplate = "<h1>${if hello}${world}${end}</h1>";
		
		SyntaxTreeRoot root = new SyntaxTreeRoot(mteObj, helloTemplate);
		assertNotNull(root);
		assertEquals(0, root.childNodes.size());
		
		root.processStageOne();
		assertEquals(5, root.childNodes.size());
		assertEquals(helloTemplate, root.toString());
		
		root.processStageTwo();
		assertEquals(3, root.childNodes.size());
		assertEquals(helloTemplate, root.toString());
	}
	
}

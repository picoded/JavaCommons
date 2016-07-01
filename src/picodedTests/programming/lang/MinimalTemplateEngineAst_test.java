package picodedTests.programming.lang;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import picoded.programming.lang.MinimalTemplateEngineAst;
import picoded.programming.MultiStageAst.*;

// IMPORTANT! Dun just test NULL, and Succesful cases, test expected failure also!
public class MinimalTemplateEngineAst_test {
	
	@Test
	public void parseTemplate_helloWorld() {
		String helloTemplate = "<h1>Hello Wolrd</h1>";
		
		MinimalTemplateEngineAst node = new MinimalTemplateEngineAst(helloTemplate);
		assertNotNull(node);
		assertEquals(0, node.children.size());
		
		node.applySingleStage(0);
		assertEquals(1, node.children.size());
		
		assertEquals(helloTemplate, node.toString());
	}
	
	@Test
	public void parseTemplate_missingBracketException() {
		String helloTemplate = "<h1>${helloMsg</h1>";
		AstSyntaxException exc = null;
		MinimalTemplateEngineAst node = new MinimalTemplateEngineAst(helloTemplate);
		assertEquals(0, node.children.size());
		
		try {
			node.applySingleStage(0);
		} catch (AstSyntaxException e) {
			exc = e;
		}
		assertNotNull(exc);
	}
	
	@Test
	public void parseTemplate_helloVar() {
		String helloTemplate = "<h1>${helloMsg}</h1>";
		
		MinimalTemplateEngineAst node = new MinimalTemplateEngineAst(helloTemplate);
		assertNotNull(node);
		assertEquals(0, node.children.size());
		
		node.applySingleStage(0);
		assertEquals(3, node.children.size());
		
		assertEquals(helloTemplate, node.toString());
	}
	
	// @Test
	// public void parseTemplate_basicIf() {
	// 	String helloTemplate = "<h1>${if hello}${world}${end}</h1>";
	// 	
	// 	SyntaxTreeRoot root = new SyntaxTreeRoot(mteObj, helloTemplate);
	// 	assertNotNull(root);
	// 	assertEquals(0, root.childNodes.size());
	// 	
	// 	root.processStageOne();
	// 	assertEquals(5, root.childNodes.size());
	// 	assertEquals(helloTemplate, root.toString());
	// 	
	// 	root.processStageTwo();
	// 	assertEquals(3, root.childNodes.size());
	// 	assertEquals(helloTemplate, root.toString());
	// }
	
}

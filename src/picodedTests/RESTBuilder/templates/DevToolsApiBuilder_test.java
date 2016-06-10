package picodedTests.RESTBuilder.templates;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

import picoded.conv.*;
import picoded.JStruct.JStruct;
import picoded.JStruct.MetaObject;
import picoded.JStruct.MetaTable;
import picoded.RESTBuilder.*;
import picoded.RESTBuilder.templates.*; 
import picoded.conv.GUID;

public class DevToolsApiBuilder_test {
	
	// The testing RESTBuilder object
	public static RESTBuilder rbObj = null;
	
	@BeforeClass
	public static void setup() {
		rbObj = new RESTBuilder();
	}
	
	@AfterClass
	public static void tearDown() {
		rbObj = null;
	}
	
	@Test
	public void constructorTest() {
		//not null check
		assertNotNull(rbObj);
	}
	
	@Test
	public void mapTesting() {
		assertEquals("{}", ConvertJSON.fromMap( rbObj.namespaceTree() ) );
		DevToolsApiBuilder.setupRESTBuilder(rbObj, "dev.");
		assertNotEquals("{}", ConvertJSON.fromMap( rbObj.namespaceTree() ) );
	}
}

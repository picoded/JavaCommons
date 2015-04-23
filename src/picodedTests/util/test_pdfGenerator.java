package picodedTests.util;

import static org.junit.Assert.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.io.IOException;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.*;

import picoded.util.pdfGenerator;

/// IMPORTANT! Dun just test NULL, and Succesful cases, test expected failure also!
/// test_pdfGenerator is just test NULL, and Succesful cases, test expected failure also
///
/// As this class , several of its common functionalities are inherited
///
/// ### Example Usage
///
/// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~{.java}
///
/// String outputTest = "./picodedTests/testingFiles/outputTest.pdf";
/// String test = "./picodedTests/testingFiles/test.html";
/// boolean returnVal = pdfGenerator.generatePDFfromHTML(outputTest, test);
/// if returnVal == true then pdfGenerator Succesfully
/// if returnVal == false then pdfGenerator Unsuccesfully
/// assertTrue(returnVal);
///
/// String testPDF = "./picodedTests/testingFiles/testPDF.pdf";
/// String welcome = "./picodedTests/testingFiles/welcome.html";
/// boolean returnVal = pdfGenerator.generatePDFfromRawHTML(welcome, testPDF);
/// if returnVal == true then pdfGenerator Succesfully
/// if returnVal == false then pdfGenerator Unsuccesfully
/// assertTrue(returnVal);
///
/// ~~~~~~~~~~~~~~~~~~~~~~~~~~~Methods~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
///
/// setUp(); setUp create a new instance of pdfGenerator class.
/// tearDown(); tearDown initialize null to pdfGenObj.
/// constructor(); constructor check pdfGenObj is null or not.
/// generatePDFfromHTML(); generatePDFfromHTML just test generate PDF from HTML NULL, and Succesful cases, test expected failure also!
/// generatePDFfromRawHTML(); generatePDFfromRawHTML just test generate PDF from Raw HTML NULL, and Succesful cases, test expected failure also!
///
/// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
///
public class test_pdfGenerator {
	protected pdfGenerator pdfGenObj;

	/// setUp create a new instance of pdfGenerator class.
	@Before
	public void setUp() {
		pdfGenObj = new pdfGenerator(); //"./picodedTests/testingFiles/pdfFolder");
		//pdfGenObj = new pdfGenerator("./picodedTests/testingFiles/welcome.html");
	}

	/// tearDown initialize null to pdfGenObj.
	@After
	public void tearDown() {
		pdfGenObj = null;
	}

	/// constructor check pdfGenObj is null or not.
	@Test
	public void constructor() {
		assertNotNull(pdfGenObj);
	}

	// generatePDFfromHTML just test generate PDF from HTML NULL, and Succesful cases, test expected failure also!
	// Pdf generated Succesful cases: retrun true
	// Pdf generated failure cases: retrun false
	// Test should not catch and print line. Exception should be thrown instead to register as test failure
	@Test
	public void generatePDFfromHTML() throws FileNotFoundException, IOException {
		assertTrue(pdfGenerator.generatePDFfromHTML("./picodedTests/testingFiles/outputTest.pdf",
			"./picodedTests/testingFiles/test.html"));
	}

	// generatePDFfromRawHTML just test generate PDF from Raw HTML NULL, and Succesful cases, test expected failure also!
	// Pdf generated Succesful cases: retrun true
	// Pdf generated failure cases: retrun false
	// Test should not catch and print line. Exception should be thrown instead to register as test failure
	@Test
	public void generatePDFfromRawHTML() throws FileNotFoundException, IOException {
		BufferedReader br = null;
		String everything = "";
		br = new BufferedReader(new FileReader("./picodedTests/testingFiles/welcome.html"));
		// other reading code here
		StringBuilder sb = new StringBuilder();
		String line = br.readLine();
		while (line != null) {
			sb.append(line);
			sb.append("\n");
			line = br.readLine();
		}
		everything = sb.toString();
		br.close();
		assertTrue(pdfGenerator.generatePDFfromRawHTML(everything, "./picodedTests/testingFiles/testPDF.pdf"));
	}
}
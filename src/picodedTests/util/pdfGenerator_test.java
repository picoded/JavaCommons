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

///
/// Test Case for picoded.util.pdfGenerator
///
public class pdfGenerator_test {
	private String outputPdfFile = null;
	private String inputHTMLFile = null;
	
	@Before
	public void setUp() {
		inputHTMLFile = "./test-files/junit-html/test.html";
		outputPdfFile = "./test-files/junit-reports/test.pdf";
	}
	
	@After
	public void tearDown() {
	}
	
	///
	/// Test HTML file conversion to a PDF file
	///
	@Test
	public void generatePDFfromHTML() throws FileNotFoundException, IOException {
		assertTrue(pdfGenerator.generatePDFfromHTML(outputPdfFile, inputHTMLFile));
	}
	
	///
	/// Test HTML raw string conversion to a PDF file
	///
	@Test
	public void generatePDFfromRawHTML() throws FileNotFoundException, IOException {
		assertTrue(pdfGenerator.generatePDFfromRawHTML(readHTMLFile(inputHTMLFile), outputPdfFile));
	}
	
	/// Reads a HTML file in a string and returns
	private String readHTMLFile(String inputHTMLFile) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(inputHTMLFile));
		StringBuilder stringBuilder = new StringBuilder();
		String line = reader.readLine();
		while (line != null) {
			stringBuilder.append(line);
			stringBuilder.append("\n");
			line = reader.readLine();
		}
		reader.close();
		return stringBuilder.toString();
	}
}
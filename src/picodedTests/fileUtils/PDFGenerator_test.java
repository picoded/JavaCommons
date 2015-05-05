package picodedTests.fileUtils;

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

import picoded.fileUtils.PDFGenerator;

///
/// Test Case for picoded.util.PDFGenerator
///
public class PDFGenerator_test {
	private String outputPdfFile = null;
	private String inputHTMLFile = null;
	
	@Before
	public void setUp() {
		inputHTMLFile = "./test-files/fileUtils/PDFGenerator/pdf-generator-html.html";
		outputPdfFile = "./test-files/tmp/fileUtils/PDFGenerator/test.pdf";
		
		// makes the output directory tmporary folder as needed
		(new File("./test-files/tmp/fileUtils/PDFGenerator")).mkdirs();
	}
	
	@After
	public void tearDown() {
	}
	
	///
	/// Test HTML file conversion to a PDF file
	///
	@Test
	public void generatePDFfromHTML() throws FileNotFoundException, IOException {
		assertTrue(PDFGenerator.generatePDFfromHTML(outputPdfFile, inputHTMLFile));
	}
	
	///
	/// Test HTML raw string conversion to a PDF file
	///
	@Test
	public void generatePDFfromRawHTML() throws FileNotFoundException, IOException {
		assertTrue(PDFGenerator.generatePDFfromRawHTML(readHTMLFile(inputHTMLFile), outputPdfFile));
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
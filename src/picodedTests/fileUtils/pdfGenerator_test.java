package picodedTests.fileUtils;

import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import picoded.fileUtils.pdfGenerator;

///
/// Test Case for picoded.util.pdfGenerator
///
public class pdfGenerator_test {
	private String outputPdfFile = null;
	private String inputHTMLFile = null;
	
	@Before
	public void setUp() {
		inputHTMLFile = "/home/action/workspace/javacommons/test-files/fileUtils/pdfGenerator/pdf-generator-html.html";
		outputPdfFile = "/home/action/workspace/javacommons/test-files/tmp/fileUtils/test.pdf";
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
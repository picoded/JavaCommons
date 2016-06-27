package picodedTests.fileUtils;

import static org.junit.Assert.assertTrue;
import java.io.File;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import picoded.fileUtils.PDFGenerator;

///
/// Test Case for picoded.util.PDFGenerator
///
public class PDFGenerator_test {
	private String outputPdfFile = null;
	private String inputHTMLFile = null;

	@Before
	public void setUp() {
		inputHTMLFile = "./test-files/test-specific/fileUtils/PDFGenerator/";
		outputPdfFile = "./test-files/tmp/fileUtils/PDFGenerator/";

		// makes the output directory tmporary folder as needed
		(new File("./test-files/tmp/fileUtils/PDFGenerator")).mkdirs();
	}

	@After
	public void tearDown() {
	}

	// /
	// / Test HTML file conversion to a PDF file
	// / embeded file path (image, css, js etc.) in input html file should be
	// relative to html file path
	// /
	@Test
	public void generatePDFfromHTMLfile() throws FileNotFoundException,
			IOException {
		assertTrue(PDFGenerator.generatePDFfromHTMLfile(outputPdfFile
				+ "test1.pdf", inputHTMLFile + "pdf-generator-1.html"));
	}

	// /
	// / Test HTML raw string conversion to a PDF file
	// / embeded file path (image, css, js etc.) in input html String should be
	// relative to execute location
	// /
	@Test
	public void generatePDFfromRawHTML() throws FileNotFoundException,
			IOException {
		assertTrue(PDFGenerator.generatePDFfromRawHTML(outputPdfFile
				+ "test2.pdf", readHTMLFile(inputHTMLFile
				+ "pdf-generator-2.html")));
	}

	// / Reads a HTML file in a string and returns
	private String readHTMLFile(String inputHTMLFile) throws IOException {
		BufferedReader reader = new BufferedReader(
				new FileReader(inputHTMLFile));
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
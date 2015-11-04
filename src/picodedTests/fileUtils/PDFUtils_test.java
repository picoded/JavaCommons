package picodedTests.fileUtils;

import java.io.InputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import picoded.fileUtils.PDFUtils;

import com.lowagie.text.DocumentException;

public class PDFUtils_test {
	private String inputPDFFileDir = null;
	private String outputPDFFileDir = null;
	private String mergeSourceFile1 = "PDFMerge1.pdf";
	private String mergeSourceFile2 = "PDFMerge2.pdf";
	private String mergeDestinationFile = "PDFMergeOutput.pdf";
	private String subPageSourceFile = "PDFMerge1.pdf";
	private String subPageOutputFile = "PDFSubPageOutput.pdf";
	
	@Before
	public void setUp() {
		inputPDFFileDir = "./test-files/test-specific/fileUtils/PDFUtils/";
		outputPDFFileDir = "./test-files/tmp/fileUtils/PDFUtils/";
		(new File(outputPDFFileDir)).mkdirs();
	}
	
	// 	@After
	// 	public void tearDown() {
	// 	}
	
	///
	/// Test PDF merge two files into single file.
	///
	@Test
	public void mergePDFfile() throws FileNotFoundException, IOException, DocumentException {
		List<InputStream> sourceFiles = new ArrayList<InputStream>();
		sourceFiles.add(new FileInputStream(inputPDFFileDir + mergeSourceFile1));
		sourceFiles.add(new FileInputStream(inputPDFFileDir + mergeSourceFile2));
		OutputStream outputStream = new FileOutputStream(outputPDFFileDir + mergeDestinationFile);
		PDFUtils.mergePDF(sourceFiles, outputStream);
		outputStream.close();
	}
	
	///
	/// Test PDF subPage generates a new PDF file which contains range of pages as per parameter.
	///
	@Test
	public void subPagePDFfile() throws FileNotFoundException, IOException, DocumentException {
		InputStream inputStream = new FileInputStream(inputPDFFileDir + subPageSourceFile);
		OutputStream outputStream = new FileOutputStream(outputPDFFileDir + subPageOutputFile);
		PDFUtils.splitPDF(inputStream, 1, 1, outputStream);
		outputStream.close();
	}
	
	///
	/// Test PDF countPDFPages return number of pages contains the PDF file.
	///
	@Test
	public void countPDFPages() throws FileNotFoundException, IOException, DocumentException {
		InputStream inputStream = new FileInputStream(inputPDFFileDir + mergeSourceFile1);
		int pageCount = PDFUtils.countPDFPages(inputStream);
		assertEquals(3, pageCount);
		inputStream.close();
	}
}
package picodedTests.fileUtils;

import static org.junit.Assert.assertEquals;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import org.junit.Before;
import org.junit.Test;

import picoded.fileUtils.PDFUtils;

import com.lowagie.text.DocumentException;

public class PDFUtils_test {
	private String inputPDFFileDir = null;
	private String outputPDFFileDir = null;
	
	private String mergeSourceFile1 = "PDFMerge1.pdf";
	private String mergeSourceFile2 = "PDFMerge2.pdf";
	
	private String mergeDestinationFile = "PDFMergeOutput.pdf";
	private String mergeDestinationOPFile = "PDFMergeOFile.pdf";
	
	private String subPageSourceFile = "PDFMerge1.pdf";
	private String subPageOutputFile = "PDFSubPageOutput.pdf";
	private String subPageOutputOPFile = "PDFSubPageOFile.pdf";

	private String pdfToImageSourceFile = "PDFToImageFile.pdf";
	private String pdfToImageOutputOPFile = "PDFToImageOFile.jpg";

	@Before
	public void setUp() {
		inputPDFFileDir = "./test-files/test-specific/fileUtils/PDFUtils/";
		outputPDFFileDir = "./test-files/tmp/fileUtils/PDFUtils/";
		(new File(outputPDFFileDir)).mkdirs();
	}
	
	// @After
	// public void tearDown() {
	// }
	
	///
	/// Test PDF merge two files into single file.
	///
	@Test
	public void mergePDFfile() throws FileNotFoundException, IOException, DocumentException {
		List<InputStream> sourceFiles = new ArrayList<InputStream>();
		sourceFiles.add(new FileInputStream(inputPDFFileDir + mergeSourceFile1));
		sourceFiles.add(new FileInputStream(inputPDFFileDir + mergeSourceFile2));
		int pageCount1 = PDFUtils.countPDFPages(new File(inputPDFFileDir + mergeSourceFile1));
		int pageCount2 = PDFUtils.countPDFPages(new File(inputPDFFileDir + mergeSourceFile2));
		OutputStream outputStream = new FileOutputStream(outputPDFFileDir + mergeDestinationFile);
		PDFUtils.mergePDF(sourceFiles, outputStream);
		outputStream.close();
		int pageCount = PDFUtils.countPDFPages(new File(outputPDFFileDir + mergeDestinationFile));
		assertEquals(pageCount, pageCount1 + pageCount2);
	}
	
	///
	/// Test PDF subPage generates a new PDF file which contains range of page
	// as per parameter.
	///
	@Test
	public void subPagePDFfile() throws FileNotFoundException, IOException, DocumentException {
		InputStream inputStream = new FileInputStream(inputPDFFileDir + subPageSourceFile);
		OutputStream outputStream = new FileOutputStream(outputPDFFileDir + subPageOutputFile);
		PDFUtils.splitPDF(inputStream, 1, 1, outputStream);
		outputStream.close();
		int pageCount = PDFUtils.countPDFPages(new File(outputPDFFileDir + subPageOutputFile));
		assertEquals(pageCount, 1);
	}
	
	///
	/// Test PDF countPDFPages return number of page contains the PDF file.
	///
	@Test
	public void countPDFPages() throws FileNotFoundException, IOException, DocumentException {
		InputStream inputStream = new FileInputStream(inputPDFFileDir + mergeSourceFile1);
		int pageCount = PDFUtils.countPDFPages(inputStream);
		inputStream.close();
		assertEquals(3, pageCount);
	}
	
	///
	/// Test PDF countPDFPages return number of page contains the PDF file.
	///
	@Test
	public void countPDFPagesFileInput() throws FileNotFoundException, IOException, DocumentException {
		File file = new File(inputPDFFileDir + mergeSourceFile1);
		int pageCount = PDFUtils.countPDFPages(file);
		assertEquals(3, pageCount);
	}
	
	///
	/// Test PDF merge two files into single file.
	///
	@Test
	public void mergePDFfileInputFile() throws FileNotFoundException, IOException, DocumentException {
		List<File> sourceFiles = new ArrayList<File>();
		sourceFiles.add(new File(inputPDFFileDir + mergeSourceFile1));
		sourceFiles.add(new File(inputPDFFileDir + mergeSourceFile2));
		// count page in sourced files
		int pageCount1 = PDFUtils.countPDFPages(new File(inputPDFFileDir + mergeSourceFile1));
		int pageCount2 = PDFUtils.countPDFPages(new File(inputPDFFileDir + mergeSourceFile2));
		// merge the files
		File file = new File(outputPDFFileDir + mergeDestinationOPFile);
		PDFUtils.mergePDF(sourceFiles, file);
		// count page in merged file
		int pageCount = PDFUtils.countPDFPages(new File(outputPDFFileDir + mergeDestinationOPFile));
		
		assertEquals(pageCount, pageCount1 + pageCount2);
	}
	
	///
	/// Test PDF subPage generates a new PDF file which contains range of page
	// as per parameter.
	///
	@Test
	public void subPagePDFInputFile() throws FileNotFoundException, IOException, DocumentException {
		File inputFile = new File(inputPDFFileDir + subPageSourceFile);
		File outputFile = new File(outputPDFFileDir + subPageOutputOPFile);
		PDFUtils.splitPDF(inputFile, 1, 1, outputFile);
		int pageCount = PDFUtils.countPDFPages(new File(outputPDFFileDir + subPageOutputOPFile));
		assertEquals(pageCount, 1);
	}
	
	///
	/// Test PDF byte array to image byte array conversion 
	/// as per parameter. PDF byte array and number of page
	/// page number in the pdf doc to return, page start from 0
	///
	@Test
	@SuppressWarnings("resource")
	public void testToJPEG() throws IOException {
		FileInputStream stream = new FileInputStream(inputPDFFileDir + pdfToImageSourceFile); 
		byte[] buffer = new byte[1000];
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		int bytesRead;
		while ((bytesRead = stream.read(buffer)) != -1) {
			baos.write(buffer, 0, bytesRead);
		}
		// made call without page number 
		byte[] jpgByteArray = PDFUtils.toJPEG(baos.toByteArray());
		BufferedImage imag = ImageIO.read(new ByteArrayInputStream(jpgByteArray));
		ImageIO.write(imag, "jpg", new File(outputPDFFileDir + pdfToImageOutputOPFile));
		
		// made call with page number 
		jpgByteArray = PDFUtils.toJPEG(baos.toByteArray(), 0);
		imag = ImageIO.read(new ByteArrayInputStream(jpgByteArray));
		ImageIO.write(imag, "jpg", new File(outputPDFFileDir + 1+"_"+pdfToImageOutputOPFile));
	}
}
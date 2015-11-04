package picoded.fileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfImportedPage;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfWriter;
import java.util.regex.Matcher;

/**
 * PDFUtils is a utility class to merge multile pdf files , count number
 * of pages in pdf and extract out a segment of the PDF into another PDF.
 * 
 * @author Dheeraj
 *
 */
public class PDFUtils {
	
	/**
	 * Merge multiple pdf into one pdf
	 * 
	 * @param fileList List<File>
	 * @param file File
	 * 
	 */
	public static void mergePDF(List<File> fileList, File outFile) throws DocumentException, IOException {
		List<InputStream> inputStreams = new ArrayList<InputStream>();
		OutputStream outputStream = null;
		try {
			for (File file : fileList) {
				inputStreams.add(new FileInputStream(file));
			}
			outputStream = new FileOutputStream(outFile);
			mergePDF(inputStreams, outputStream);
		} finally {
			for (InputStream inputStream : inputStreams) {
				if (inputStream != null) {
					inputStream.close();
				}
			}
			if (outputStream != null) {
				outputStream.close();
			}
		}
	}
	
	/**
	 * Merge multiple pdf into one pdf
	 * 
	 * @param fileList List<InputStream>
	 * @param outputStream OutputStream
	 * 
	 */
	public static void mergePDF(List<InputStream> fileList, OutputStream outputStream) throws DocumentException,
		IOException {
		Document document = null;
		try {
			document = new Document();
			PdfWriter writer = PdfWriter.getInstance(document, outputStream);
			document.open();
			PdfContentByte cb = writer.getDirectContent();
			
			for (InputStream in : fileList) {
				PdfReader reader = new PdfReader(in);
				for (int i = 1; i <= reader.getNumberOfPages(); i++) {
					document.newPage();
					//import the page from source pdf
					PdfImportedPage page = writer.getImportedPage(reader, i);
					//add the page to the destination pdf
					cb.addTemplate(page, 0, 0);
				}
			}
			
		} finally {
			if (document.isOpen())
				document.close();
			try {
				if (outputStream != null) {
					outputStream.flush();
					outputStream.close();
				}
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}
	}
	
	/**
	 * Count number of pages in pdf document
	 * 
	 * @param pdfFileName InputStream
	 * 
	 */
	public static int countPDFPages(File pdfFileName) throws IOException {
		InputStream inputStream = new FileInputStream(pdfFileName);
		int pageCount = countPDFPages(inputStream);
		inputStream.close();
		return pageCount;
	}
	
	/**
	 * Count number of pages in pdf document
	 * 
	 * @param pdfFileName InputStream
	 * 
	 */
	public static int countPDFPages(InputStream pdfFileName) throws IOException {
		PdfReader reader = new PdfReader(pdfFileName);
		return reader.getNumberOfPages();
	}
	
	/**
	 * This method create new PDF file and add specified range of pages from source file.
	 * 
	 * @param inputFile File
	 * @param fromPage int
	 * @param toPage int
	 * @param outputFile File
	 * 
	 */
	public static void splitPDF(File inputFile, int fromPage, int toPage, File outputFile) throws DocumentException,
		IOException {
		InputStream inputStream = null;
		OutputStream outputStream = null;
		try {
			inputStream = new FileInputStream(inputFile);
			outputStream = new FileOutputStream(outputFile);
			splitPDF(inputStream, fromPage, toPage, outputStream);
			outputStream.flush();
		} finally {
			if (inputStream != null) {
				inputStream.close();
			}
			if (outputStream != null) {
				outputStream.close();
			}
		}
	}
	
	/**
	 * This method create new PDF file and add specified range of pages from source file.
	 * 
	 * @param inputStream InputStram
	 * @param fromPage int
	 * @param toPage int
	 * @param outputStream OutputStream
	 * 
	 */
	public static void splitPDF(InputStream inputStream, int fromPage, int toPage, OutputStream outputStream)
		throws DocumentException, IOException {
		Document document = new Document();
		if (fromPage <= 0) {
			fromPage = 1;
		}
		if (toPage <= 0) {
			toPage = 1;
		}
		if (fromPage > toPage) {
			fromPage = toPage;
		}
		try {
			PdfReader pdfReader = new PdfReader(inputStream);
			if (toPage > pdfReader.getNumberOfPages()) {
				toPage = pdfReader.getNumberOfPages();
			}
			PdfWriter writer = PdfWriter.getInstance(document, outputStream);
			document.open();
			PdfContentByte content = writer.getDirectContent();
			PdfImportedPage page;
			while (fromPage <= toPage) {
				document.newPage();
				page = writer.getImportedPage(pdfReader, fromPage);
				content.addTemplate(page, 0, 0);
				fromPage++;
			}
			outputStream.flush();
		} finally {
			if (document.isOpen()) {
				document.close();
			}
		}
	}
	
}
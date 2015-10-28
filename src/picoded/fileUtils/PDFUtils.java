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
	 * @param fileList
	 * @param outputStream
	 * 
	 */
	public void mergePDF(List<InputStream> fileList, OutputStream outputStream) {
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
			
		} catch (Exception e) {
			e.printStackTrace();
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
	 * @param pdfFileName
	 * 
	 */
	public int countPDFPages(String pdfFileName) {
		PdfReader reader = null;
		try {
			reader = new PdfReader(pdfFileName);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return reader.getNumberOfPages();
	}
	
	private static void createOutputFolder(String outputFilePath) throws IOException {
		String outputFolderpath = outputFilePath.substring(0, outputFilePath.lastIndexOf(File.separator));
		File outputFolder = new File(outputFolderpath);
		
		if (!outputFolder.exists()) {
			outputFolder.mkdirs();
		}
	}
	
	/**
	 * 
	 * 
	 * @param inputStream
	 * @param fromPage
	 * @param toPage
	 * 
	 */
	public void splitPDF(InputStream inputStream, int fromPage, int toPage) {
		Document document = new Document();
		if (fromPage > toPage) {
			fromPage = toPage;
		}
		String pdfFile = "subPage.pdf";
		OutputStream outputStream = null;
		try {
			createOutputFolder(pdfFile.replaceAll("[/\\\\]+",
				Matcher.quoteReplacement(System.getProperty("file.separator"))));
			PdfReader pdfReader = new PdfReader(inputStream);
			if (toPage > pdfReader.getNumberOfPages()) {
				toPage = pdfReader.getNumberOfPages();
			}
			outputStream = new FileOutputStream(pdfFile);
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
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (document.isOpen())
				document.close();
			try {
				if (outputStream != null)
					outputStream.close();
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}
	}
	
}
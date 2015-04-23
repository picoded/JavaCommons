package picoded.util;

import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/// pdfGenerator is a utility class to covert either a HTML string or a HTML file to a PDF file
///
/// ### Example Usage
///
/// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~{.java}
///
/// // covert a HTML file to a pdf file
///
/// String pdfOutputFile = //picodedTests/testingFiles/pdfFile.pdf
/// String htmlInputFile = //picodedTests/testingFiles/test.html
/// pdfGenerator.generatePDFfromHTML(pdfOutputFile, htmlInputFile);
///
/// // covert a HTML string to a pdf file
///
/// String pdfOutputFile = //picodedTests/testingFiles/pdfFile.pdf
/// String htmlString = "<table><tr><th>Cell A</th></tr><tr><td>Cell Data</td></tr></table>"
/// pdfGenerator.generatePDFfromRawHTML(pdfOutputFile, htmlString);
///
/// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
///
public class pdfGenerator {
	
	/// Generates a pdf file given the HTML file path
	///
	/// @param   pdfFile         pdf file path string
	/// @param   htmlFilePath    HTML file path string
	///
	/// @returns  true if the HTML file is converted and saved in a pdf file
	public static boolean generatePDFfromHTML(String pdfFile, String htmlFilePath) {
		OutputStream outputStream = null;
		try {
			String url2 = new File(htmlFilePath).toURI().toString();
			outputStream = new FileOutputStream(pdfFile);
			ITextRenderer renderer = new ITextRenderer();
			//renderer.setDocument(new File(url2).toURI().toString());
			renderer.setDocument(new File(htmlFilePath));
			renderer.writeNextDocument();
			renderer.layout();
			
			// generate pdf
			renderer.createPDF(outputStream);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			if (outputStream != null) {
				try {
					outputStream.close();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		}
		return true;
	}
	
	/// Generates a pdf file given the RAW html string
	///
	/// @param   rawHtml          raw HTML string
	/// @param   outputpdfpath    pdf file path string
	///
	/// @returns  true if the HTML raw string is converted and saved in a pdf file.
	public static boolean generatePDFfromRawHTML(String rawHtml, String outputpdfpath) {
		OutputStream outputStream = null;
		try {
			outputStream = new FileOutputStream(outputpdfpath);
			ITextRenderer renderer = new ITextRenderer();
			renderer.setDocumentFromString(rawHtml);
			renderer.layout();
			
			// generate pdf
			renderer.createPDF(outputStream);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			if (outputStream != null) {
				try {
					outputStream.close();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		}
		return true;
	}
}
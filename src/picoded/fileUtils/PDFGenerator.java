package picoded.fileUtils;

import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import java.util.regex.Matcher;

/// pdfGenerator is a utility class to covert either a HTML string or a HTML file to a PDF file
///
/// ### Example Usage
///
/// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~{.java}
///
/// // covert a HTML file to a pdf file
///
/// String pdfOutputFile = //test-files/temp/fileUtils//pdfFile.pdf
/// String htmlInputFile = //test-files/fileUtils/PDFGenerator/pdf-generator-html.html
/// PDFGenerator.generatePDFfromHTML(pdfOutputFile, htmlInputFile);
///
/// // covert a HTML string to a pdf file
///
/// String pdfOutputFile = //test-files/temp/fileUtils//pdfFile.pdf
/// String htmlString = "<table><tr><th>Cell A</th></tr><tr><td>Cell Data</td></tr></table>"
/// PDFGenerator.generatePDFfromRawHTML(pdfOutputFile, htmlString);
///
/// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
///
public class PDFGenerator {
	
	/// Generates a pdf file given the HTML file path
	/// embeded file path (image, css, js etc.) in input html file should be relative to html file path
	///
	/// @param   pdfFile         pdf file path string
	/// @param   htmlFilePath    HTML file path string
	///
	/// @returns  true if the HTML file is converted and saved in a pdf file
	public static boolean generatePDFfromHTMLfile(String pdfFile, String htmlFilePath) {
		OutputStream outputStream = null;
		try {
			pdfFile = pdfFile.replaceAll("[/\\\\]+", Matcher.quoteReplacement(System.getProperty("file.separator")));
			
			createOutputFolder(pdfFile);
			
			outputStream = new FileOutputStream(pdfFile);
			
			htmlFilePath = htmlFilePath.replaceAll("[/\\\\]+", Matcher.quoteReplacement(System
				.getProperty("file.separator")));
			
			ITextRenderer renderer = new ITextRenderer();
			renderer.setDocument(new File(htmlFilePath));
			// renderer.writeNextDocument();
			renderer.layout();
			
			// generate pdf
			renderer.createPDF(outputStream);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (Exception e) {
			e.printStackTrace();
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
	
	private static void createOutputFolder(String outputFilePath) throws IOException {
		String outputFolderpath = outputFilePath.substring(0, outputFilePath.lastIndexOf(File.separator));
		File outputFolder = new File(outputFolderpath);
		
		if (!outputFolder.exists()) {
			outputFolder.mkdirs();
		}
	}
	
	/// Generates a pdf file given the RAW html string
	/// embeded file path (image, css, js etc.) in input html String should be relative to execute location
	///
	/// @param   outputpdfpath    pdf file path string
	/// @param   rawHtml          raw HTML string
	///
	/// @returns  true if the HTML raw string is converted and saved in a pdf file.
	public static boolean generatePDFfromRawHTML(String outputpdfpath, String rawHtml) {
		OutputStream outputStream = null;
		try {
			outputpdfpath = outputpdfpath.replaceAll("[/\\\\]+", Matcher.quoteReplacement(System
				.getProperty("file.separator")));
			
			createOutputFolder(outputpdfpath);
			
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
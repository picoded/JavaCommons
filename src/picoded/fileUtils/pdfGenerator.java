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
/// String htmlInputFile = //test-files/fileUtils/pdfGenerator/pdf-generator-html.html
/// pdfGenerator.generatePDFfromHTML(pdfOutputFile, htmlInputFile);
///
/// // covert a HTML string to a pdf file
///
/// String pdfOutputFile = //test-files/temp/fileUtils//pdfFile.pdf
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
			pdfFile = pdfFile.replaceAll("[/\\\\]+", Matcher.quoteReplacement(System.getProperty("file.separator")));
			
			htmlFilePath = htmlFilePath.replaceAll("[/\\\\]+", Matcher.quoteReplacement(System
				.getProperty("file.separator")));
			
			String outputFolderpath = pdfFile.substring(0, pdfFile.lastIndexOf(File.separator));
			String outputFilename = pdfFile.substring(pdfFile.lastIndexOf(File.separator) + 1);
			File outputFolder = new File(outputFolderpath);
			File outputFile = new File(outputFolder, outputFilename);
			
			if (!outputFolder.exists()) {
				outputFolder.mkdirs();
			}
			outputStream = new FileOutputStream(outputFile);
			
			String inputFolderpath = htmlFilePath.substring(0, htmlFilePath.lastIndexOf(File.separator));
			String inputFilename = htmlFilePath.substring(htmlFilePath.lastIndexOf(File.separator) + 1);
			
			File inputFolder = new File(inputFolderpath);
			if (!inputFolder.exists()) {
				throw new IOException("HTML file path is not valid.");
			}
			File inputFile = new File(inputFolder, inputFilename);
			if (!inputFile.exists()) {
				throw new IOException("HTML file does not exist.");
			}
			
			ITextRenderer renderer = new ITextRenderer();
			renderer.setDocument(inputFile);
			//          renderer.writeNextDocument();
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
package picoded.util;

import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/// pdfGenerator is a component class to do HTML to PDF file conversion & saving
///
/// As this class , several of its common functionalities are inherited
///
/// ### Example Usage
///
/// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~{.java}
///
/// String pdfFile = ./picodedTests/testingFiles/pdfFile.pdf
/// OutputStream os = new FileOutputStream(pdfFile);
///
/// String htmlFilePath =./picodedTests/testingFiles/test.html
/// String url2 = new File(htmlFilePath).toURI().toString();
///
/// String rawHtml = ./picodedTests/testingFiles/welcome.html
/// String url2 = rawHtml.toString();
///
/// String outputpdfpath = ./picodedTests/testingFiles/outputpdfpath.pdf
/// OutputStream os = new FileOutputStream(outputpdfpath);
///
/// ~~~~~~~~~~~~~~~~~~~~~~~~~~~Methods~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
/// generatePDFfromHTML( String pdfFile, String htmlFilePath );
///
/// generatePDFfromRawHTML( String rawHtml, String outputpdfpath );
///
/// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
public class pdfGenerator {

	/// Generates a pdf given the HTML file path
	///
	/// @param   pdfFile         pdfFile string with which the specified pdf File
	/// @param   htmlFilePath    htmlFilePath string with which the specified html file path
	///
	/// @returns  generatePDFfromHTML() return the boolean value (true, false) based on generate PDF has been successfully or
	/// not if pdf successfully generated then return true otherwaise return false.
	/// (A fasle return can also indicate that the generate PDF not successfully. @param pdfFile and htmlFilePath values are incorrect).
	public static boolean generatePDFfromHTML(String pdfFile, String htmlFilePath) {
		try {
			String url2 = new File(htmlFilePath).toURI().toString();
			OutputStream os = new FileOutputStream(pdfFile);
			ITextRenderer renderer = new ITextRenderer();

			renderer.setDocument(url2);
			renderer.writeNextDocument();
			renderer.layout();
			renderer.createPDF(os);

			os.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return true;
	}

	/// Generates a pdf given the RAW html string
	///
	/// @param   rawHtml     rawHtml string with which the specified raw html
	/// @param   outputpdfpath    outputpdfpath string with which the specified output pdf path
	///
	/// @returns  generatePDFfromRawHTML() return the boolean value (true, false) based on generate PDF has been successfully or
	/// not if pdf successfully generated then return true otherwaise return false.
	/// (A fasle return can also indicate that the generate PDF not successfully. @param rawHtml and outputpdfpath values are incorrect).
	public static boolean generatePDFfromRawHTML(String rawHtml, String outputpdfpath) {
		try {
			OutputStream os = new FileOutputStream(outputpdfpath);
			ITextRenderer renderer = new ITextRenderer();
			
			String url2 = rawHtml.toString();
			//renderer.setDocument(url2);
			renderer.setDocumentFromString(url2);
			renderer.layout();
			renderer.createPDF(os);
			os.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return true;
	}
}
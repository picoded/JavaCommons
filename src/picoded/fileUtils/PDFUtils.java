package picoded.fileUtils;

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

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.ColumnText;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfCopy;
import com.lowagie.text.pdf.PdfImportedPage;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfWriter;

/**
 * PDFUtils is a utility class to merge multile pdf files , count number of
 * page in pdf and extract out a segment of the PDF into another PDF.
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
			PdfCopy copy = new PdfCopy(document, outputStream);
			document.open();
			
			for (InputStream in : fileList) {
				PdfReader reader = new PdfReader(in);
				for (int page = 1; page <= reader.getNumberOfPages(); ++page) {
					copy.addPage(copy.getImportedPage(reader, page));
				}
				copy.freeReader(reader);
				reader.close();
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
	
	// /
	/// Taken from http://itextpdf.com/examples/iia.php?id=128
	/// Takes an input pdf, and outputs to a different location with page
	// numbering
	// /
	public static void numberPDFPages(File pdfToNumber, File outputFile) throws DocumentException, IOException,
		FileNotFoundException {
		Document document = new Document();
		PdfCopy copy = new PdfCopy(document, new FileOutputStream(outputFile));
		document.open();
		
		PdfReader reader = new PdfReader(pdfToNumber.getAbsolutePath());
		int totalPages = reader.getNumberOfPages();
		
		try {
			PdfImportedPage page;
			PdfCopy.PageStamp stamp;
			for (int i = 0; i < totalPages; ++i) {
				page = copy.getImportedPage(reader, i + 1);
				stamp = copy.createPageStamp(page);
				
				ColumnText.showTextAligned(stamp.getUnderContent(), Element.ALIGN_CENTER,
					new Phrase(String.format("Page %d of %d", i + 1, totalPages)), 297.5f, 28, 0);
				
				stamp.alterContents();
				copy.addPage(page);
			}
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		} finally {
			document.close();
			reader.close();
		}
	}
	
	/**
	 * Count number of page in pdf document
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
	 * Count number of page in pdf document
	 * 
	 * @param pdfFileName InputStream
	 * 
	 */
	public static int countPDFPages(InputStream pdfFileName) throws IOException {
		PdfReader reader = new PdfReader(pdfFileName);
		return reader.getNumberOfPages();
	}
	
	/**
	 * This method create new PDF file and add specified range of page from
	 * source file.
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
	 * This method create new PDF file and add specified range of page from
	 * source file.
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
	
	///
	/// Convert first page of PDF doc to image byte array
	///
	/// @param byte[] pdfData pdf byte array
	/// @Returns the image byet array of the first page in pdf doc
	///
	public static byte[] toJPEG(byte[] pdfData) throws IOException {
		return toJPEG(pdfData, 0);
	}
	
	///
	/// Convert specified page of PDF doc to image byte array
	///
	/// @param byte[] pdfData pdf byte array
	/// @param int page, page number in the pdf doc to return, page start from 0
	/// @Returns the image byet array of the specified page in pdf doc.
	/// @exception throws IndexOutOfBoundsException if the page number is out of range (page < 0 || page >= total pages)
	///
	public static byte[] toJPEG(byte[] pdfData, int page) throws IOException  {
		System.setProperty("sun.java2d.cmm", "sun.java2d.cmm.kcms.KcmsServiceProvider");
		// create document object from byte[]
		PDDocument document = PDDocument.load( new ByteArrayInputStream(pdfData));
		PDFRenderer pdfRenderer = new PDFRenderer(document);
		// throw exeception if invalid page 
		if(page < 0 || page >= document.getNumberOfPages()){
			throw new IndexOutOfBoundsException("page number is out of range");
		}
		// create buffered image for the specified page of pdf
		BufferedImage bufferedImage = pdfRenderer.renderImageWithDPI(page, 300, ImageType.RGB);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();            
		ImageIO.write(bufferedImage, "jpeg", baos);
		document.close();
		
		return baos.toByteArray();
	}
}
package picoded.fileUtils;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.File;

import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfStamper;
import com.lowagie.text.pdf.PdfWriter;

import de.idyl.winzipaes.AesZipFileEncrypter;
import de.idyl.winzipaes.impl.AESEncrypter;
import de.idyl.winzipaes.impl.AESEncrypterBC;

///
/// Provides a simple file encryption library for excel file or PDF files
///
public class FileEncrypt {
	
	//-------------------------------------------------------------------
	//
	// PDF encryption
	//
	//-------------------------------------------------------------------
	
	///
	/// Conversion of PDF Input Stream to PDF Encrypted Output Stream
	///
	/// @params  OutputStream to directly write to
	/// @params  InputStream to read the data from
	/// @params  The PDF password to use
	///
	/// @returns the OutputStream used
	///
	public static OutputStream pdf(OutputStream baos, InputStream is, String pdfPassword) {
		PdfReader reader = null;
		PdfStamper stamper = null;
		
		try {
			/// Reads the input stream for the pdf data
			reader = new PdfReader(is, null);
			
			/// PDF output stream with encryption password
			stamper = new PdfStamper(reader, baos);
			stamper.setEncryption(pdfPassword.getBytes(), pdfPassword.getBytes(), PdfWriter.ALLOW_PRINTING,
				PdfWriter.ENCRYPTION_AES_128 | PdfWriter.DO_NOT_ENCRYPT_METADATA);
			
			/// Stream close
			stamper.close();
			reader.close();
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			
			/// Stream clean up
			if (stamper != null) {
				try {
					stamper.close();
				} catch (Exception e) {
				}
			}
			if (reader != null) {
				try {
					reader.close();
				} catch (Exception e) {
				}
			}
			stamper = null;
			reader = null;
		}
		
		return baos;
	}
	
	///
	/// Conversion of PDF Input Stream to PDF Encrypted Output Stream
	///
	/// @params  InputStream to read the data from
	/// @params  The PDF password to use
	///
	/// @returns the generated ByteArrayOutputStream
	///
	public static ByteArrayOutputStream pdf(InputStream is, String pdfPassword) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		pdf(baos, is, pdfPassword);
		return baos;
	}
	
	///
	/// Conversion of PDF Input Stream to PDF Encrypted Output Stream
	///
	/// @params  InputStream to read the data from
	/// @params  The PDF password to use
	///
	/// @returns the generated ByteArrayOutputStream
	///
	public static ByteArrayOutputStream pdf(File inFile, String pdfPassword) {
		try {
			return pdf(new FileInputStream(inFile), pdfPassword);
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
	}
	
	//-------------------------------------------------------------------
	//
	// ZIP encryption
	//
	//-------------------------------------------------------------------
	
	///
	/// Encrypting the given input stream as a file, inside a zip file
	///
	/// @params  OutputStream to directly write to
	/// @params  InputStream to read the data from
	/// @params  filename of the inner file (to be zipped) that is given by InputStream
	/// @params  The ZIP password to use
	///
	/// @returns the OutputStream used
	///
	public static OutputStream zip(OutputStream os, InputStream is, String fileName, String zipPassword) {
		// Preparing the zip encryptor
		AesZipFileEncrypter ze = null;
		try {
			AESEncrypter aesEncrypter = new AESEncrypterBC();
			aesEncrypter.init(zipPassword, 0);
			ze = new AesZipFileEncrypter(os, aesEncrypter);
			
			// Runs the encryption, and outputs it
			ze.add(fileName, is, zipPassword);
			ze.close();
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			if (ze != null) {
				try {
					ze.close();
				} catch (Exception e) {
				}
			}
			ze = null;
		}
		return os;
	}
	
	///
	/// Encrypting the given input stream as a file, inside a zip file
	///
	/// @params  InputStream to read the data from
	/// @params  filename of the inner file (to be zipped) that is given by InputStream
	/// @params  The ZIP password to use
	///
	/// @returns the generated ByteArrayOutputStream
	///
	public static ByteArrayOutputStream zip(InputStream is, String fileName, String zipPassword) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		zip(baos, is, fileName, zipPassword);
		return baos;
	}
	
	///
	/// Encrypting the given input stream as a file, inside a zip file
	///
	/// @params  InputStream to read the data from
	/// @params  filename of the inner file (to be zipped) that is given by InputStream
	/// @params  The ZIP password to use
	///
	/// @returns the generated ByteArrayOutputStream
	///
	public static ByteArrayOutputStream zip(File inFile, String fileName, String zipPassword) {
		try {
			return zip(new FileInputStream(inFile), fileName, zipPassword);
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
	}
	
}
package picodedTests;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.*;
import org.junit.runner.*;
import org.junit.runner.notification.Failure;

import picoded.fileUtils.PDFGenerator;

public class BaseTestClass {
	
	protected String baseRootFolder = "";
	protected String resourceFolder = "";
	protected String generatedFolder = "";
	
	private String packageRoot = "picodedTests.";
	
	public BaseTestClass() {
		baseRootFolder = getTestClassRootFolder();
		
		resourceFolder = baseRootFolder + "/res";
		File resourceFile = new File(resourceFolder);
		if (!resourceFile.exists()) {
			resourceFile.mkdirs();
		}
		
		generatedFolder = baseRootFolder + "/generated";
		File generatedFile = new File(generatedFolder);
		if (!generatedFile.exists()) {
			generatedFile.mkdirs();
		}
	}
	
	protected String getTestClassRootFolder() {
		String ret = "./test-files/test-specific/";
		
		Class<?> thisClass = this.getClass();
		String packageName = thisClass.getName();
		
		if (packageName != null && packageName.startsWith(packageRoot)) {
			packageName = packageName.substring(packageRoot.length(), packageName.length());
			
			String[] packageNameSplit = packageName.split("\\.");
			for (int count = 0; count < packageNameSplit.length; ++count) {
				if (count == packageNameSplit.length - 1) {
					if (packageNameSplit[count].contains("_test")) {
						packageNameSplit[count] = packageNameSplit[count].replace("_test", "");
					}
				}
				
				ret += packageNameSplit[count];
				
				if (count < packageNameSplit.length - 1) {
					ret += "/";
				}
			}
		}
		
		return ret;
	}
	
	protected boolean writeStringToGeneratedFile(String input, String fileName) {
		File generatedFile = new File(generatedFolder + "/" + fileName);
		
		try {
			FileWriter fw = new FileWriter(generatedFile);
			fw.write(input);
			fw.flush();
			fw.close();
		} catch (Exception ex) {
			return false;
		}
		
		return true;
	}
	
	protected boolean writeHTMLStringToGeneratedPDF(String input, String fileName) {
		String outputFileString = generatedFolder + "/" + fileName;
		return PDFGenerator.generatePDFfromRawHTML(outputFileString, input);
	}
	
	protected String readStringFromResourceFile(String fileName) {
		String ret = "";
		
		File resourceFile = new File(resourceFolder + "/" + fileName);
		
		if (!resourceFile.exists()) {
			return null;
		}
		
		try {
			ret = FileUtils.readFileToString(resourceFile);
		} catch (Exception ex) {
			return null;
		}
		
		return ret;
	}
}

package picoded.fileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/// Replace with apache StringUtils?
import com.mysql.jdbc.StringUtils;

///
/// Small extension of apache FileUtils, for some additional features that we needed
///
/// @See https://commons.apache.org/proper/commons-io/javadocs/api-2.5/org/apache/commons/io/FileUtils.html
///
public class FileUtils extends org.apache.commons.io.FileUtils {
	
	///
	/// List only the folders inside a folder
	///
	/// @param folder to scan
	///
	/// @return Collection of folders within the current folder
	///
	public static Collection<File> listDirs(File inFile) {
		List<File> ret = new ArrayList<File>();
		
		for (File f : inFile.listFiles()) {
			if (f.isDirectory()) {
				ret.add(f);
			}
		}
		
		return ret;
	}
	
	///
	/// Extends the readFileToString to include a "fallback" default value,
	/// which is used if the file does not exists / is not readable / is not a
	/// file
	///
	/// @param file to read
	/// @param encoding mode
	/// @param fallback return value if file is invalid
	///
	/// @returns the file value if possible, else returns the fallback value
	///
	public static String readFileToString_withFallback(File inFile, String encoding, String fallback) {
		if (inFile == null || !inFile.exists() || !inFile.isFile() || !inFile.canRead()) {
			return fallback;
		}
		
		try {
			return readFileToString(inFile, encoding);
		} catch (IOException e) {
			return fallback;
		}
	}
	
	///
	/// Write to file only if it differs
	///
	/// @param file to write
	/// @param value to write
	/// @param encoding mode
	///
	/// @returns the boolean indicating true if file was written to
	///
	public static boolean writeStringToFile_ifDifferant(File inFile, String encoding, String data) throws IOException {
		String original = readFileToString_withFallback(inFile, encoding, "");
		if (original.equals(data)) {
			return false;
		}
		
		writeStringToFile(inFile, data, encoding);
		return true;
	}
	
	///
	/// Recursively copy all directories, and files only if the file content is different
	///
	/// @TODO : Implmenent the existing file checks, ignoring if it has same content
	///
	/// @param folder to scan and copy from
	///
	public static void copyDirectory_ifDifferent(File inDir, File outDir) throws IOException {
		copyDirectory(inDir, outDir);
	}
	
	///
	/// Recursively copy all directories, and files only if the file content is different
	///
	/// @TODO : Implmenent the existing file checks, ignoring if it has same content
	///
	/// @param file to scan and copy from
	///
	public static void copyFile_ifDifferent(File inFile, File outFile) throws IOException {
		copyFile(inFile, outFile);
	}
	
	//------------------------------------------------------------------------------------------------------------------
	//
	// Items below here, requires cleanup : not considered stable
	//
	//------------------------------------------------------------------------------------------------------------------
	
	// @TODO: Sam, documentation =.= on what this is for?
	public static List<String> getFileNamesFromFolder(File inFile, String separator, String rootFolderName) {
		List<String> keyList = new ArrayList<String>();
		
		if (StringUtils.isNullOrEmpty(rootFolderName)) {
			rootFolderName = "";
		}
		
		if (StringUtils.isNullOrEmpty(separator)) {
			separator = "/";
		}
		
		if (inFile.isDirectory()) {
			File[] innerFiles = inFile.listFiles();
			for (File innerFile : innerFiles) {
				if (innerFile.isDirectory()) {
					String parentFolderName = innerFile.getName();
					if (!rootFolderName.isEmpty()) {
						parentFolderName = rootFolderName + separator + parentFolderName;
					}
					keyList.addAll(getFileNamesFromFolder(innerFile, parentFolderName, separator));
				} else {
					keyList.addAll(getFileNamesFromFolder(innerFile, rootFolderName, separator));
				}
			}
		} else {
			String fileName = inFile.getName();
			fileName = fileName.substring(0, fileName.lastIndexOf('.'));
			String prefix = "";
			if (!rootFolderName.isEmpty()) {
				prefix += rootFolderName + separator;
			}
			
			keyList.add(prefix + fileName);
		}
		
		return keyList;
	}
	
}

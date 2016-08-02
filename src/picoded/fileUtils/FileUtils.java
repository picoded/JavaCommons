package picoded.fileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/// Replace with apache StringUtils?
import com.mysql.jdbc.StringUtils;

///
/// Small extension of apache FileUtils, for some additional features that we needed.
/// Additionally several FilenameUtils is made avaliable here
///
/// @See https://commons.apache.org/proper/commons-io/javadocs/api-2.5/org/apache/commons/io/FileUtils.html
///
public class FileUtils extends org.apache.commons.io.FileUtils {
	
	//------------------------------------------------------------------------------------------------------------------
	//
	// JavaCommons extensions
	//
	//------------------------------------------------------------------------------------------------------------------
	
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
	
	/// Overwrites null encoding with US-ASCII
	public static String readFileToString(File inFile) throws IOException {
		return picoded.fileUtils.FileUtils.readFileToString(inFile, (String)null);
	}
	
	/// Overwrites null encoding with US-ASCII
	public static String readFileToString(File inFile, String encoding) throws IOException {
		if(encoding == null) {
			encoding = "US-ASCII";
		} 
		return org.apache.commons.io.FileUtils.readFileToString(inFile, encoding);
	}
	
	/// Overwrites null encoding with US-ASCII
	public static void writeStringToFile(File inFile, String data) throws IOException {
		picoded.fileUtils.FileUtils.writeStringToFile(inFile, data, (String)null);
	}
	
	/// Overwrites null encoding with US-ASCII
	public static void writeStringToFile(File inFile, String data, String encoding) throws IOException {
		if(encoding == null) {
			encoding = "US-ASCII";
		} 
		org.apache.commons.io.FileUtils.writeStringToFile(inFile, data, encoding);
	}
	
	///
	/// Extends the readFileToString to include a "fallback" default value,
	/// which is used if the file does not exists / is not readable / is not a
	/// file
	///
	/// @param file to read
	/// @param fallback return value if file is invalid
	/// @param encoding mode
	///
	/// @returns the file value if possible, else returns the fallback value
	///
	public static String readFileToString_withFallback(File inFile, String fallback) {
		return picoded.fileUtils.FileUtils.readFileToString_withFallback(inFile, fallback, null);
	}
	
	///
	/// Extends the readFileToString to include a "fallback" default value,
	/// which is used if the file does not exists / is not readable / is not a
	/// file
	///
	/// @param file to read
	/// @param fallback return value if file is invalid
	/// @param encoding mode
	///
	/// @returns the file value if possible, else returns the fallback value
	///
	public static String readFileToString_withFallback(File inFile, String fallback, String encoding) {
		if (inFile == null || !inFile.exists() || !inFile.isFile() || !inFile.canRead()) {
			return fallback;
		}
		
		try {
			return picoded.fileUtils.FileUtils.readFileToString(inFile, encoding);
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
	public static boolean writeStringToFile_ifDifferant(File inFile, String data, String encoding) throws IOException {
		String original = readFileToString_withFallback(inFile, "", encoding);
		if (original.equals(data)) {
			return false;
		}
		
		writeStringToFile(inFile, data, encoding);
		return true;
	}
	
	///
	/// Write to file only if it differs
	///
	/// @param file to write
	/// @param value to write
	///
	/// @returns the boolean indicating true if file was written to
	///
	public static boolean writeStringToFile_ifDifferant(File inFile, String data) throws IOException {
		return picoded.fileUtils.FileUtils.writeStringToFile_ifDifferant(inFile, data, null);
	}
	
	///
	/// Recursively copy all directories, and files only if the file content is different
	///
	/// @TODO : Implmenent the existing file checks, ignoring if it has same content
	///
	/// @param folder to scan and copy from
	///
	public static void copyDirectory_ifDifferent(File inDir, File outDir) throws IOException {
		if(inDir ==null || outDir == null){
			new IOException("Invalid directory");
		}
		File[] dir_inDir = inDir.listFiles();
		for (int i = 0; i < dir_inDir.length; i++) {
			File infile = dir_inDir[i];
			if (infile.isFile()) {
				File outfile =new File(outDir, infile.getName());
				copyFile_ifDifferent(infile, outfile);
			} else if (infile.isDirectory()) {
				File newOutDir= new File(outDir.getAbsolutePath()+File.separator+ infile.getName());
				newOutDir.mkdir();
				copyDirectory_ifDifferent(infile, newOutDir);
			}
		}
	}
	
	///
	/// Recursively copy all directories, and files only if the file content is different
	///
	/// @TODO : Implmenent the existing file checks, ignoring if it has same content
	///
	/// @param file to scan and copy from
	///
	public static void copyFile_ifDifferent(File inFile, File outFile) throws IOException {
		if (!FileUtils.contentEqualsIgnoreEOL(inFile, outFile, null)) {
			copyFile(inFile, outFile);
		}
	}
	
	//------------------------------------------------------------------------------------------------------------------
	//
	// FilenameUtils functions
	//
	//------------------------------------------------------------------------------------------------------------------
	
	/// @see https://commons.apache.org/proper/commons-io/javadocs/api-2.5/org/apache/commons/io/FilenameUtils.html#getBaseName(java.lang.String)
	/// @param raw file name/path
	/// @return filename only without the the type extension
	public static String getBaseName(String filename) { 
		return org.apache.commons.io.FilenameUtils.getBaseName(filename);
	}
	
	/// @see https://commons.apache.org/proper/commons-io/javadocs/api-2.5/org/apache/commons/io/FilenameUtils.html#getExtension(java.lang.String)
	/// @param raw file name/path
	/// @return filename type extension
	public static String getExtension(String filename) { 
		return org.apache.commons.io.FilenameUtils.getExtension(filename);
	}
	
	/// @see https://commons.apache.org/proper/commons-io/javadocs/api-2.5/org/apache/commons/io/FilenameUtils.html#getFullPath(java.lang.String)
	/// @param raw file name/path
	/// @return full resolved path with ending / for directories
	public static String getFullPath(String filename) { 
		return org.apache.commons.io.FilenameUtils.getFullPath(filename);
	}
	
	/// @see https://commons.apache.org/proper/commons-io/javadocs/api-2.5/org/apache/commons/io/FilenameUtils.html#getFullPathNoEndSeparator(java.lang.String)
	/// @param raw file name/path
	/// @return full resolved path without ending / for directories
	public static String getFullPathNoEndSeparator(String filename) { 
		return org.apache.commons.io.FilenameUtils.getFullPathNoEndSeparator(filename);
	}
	
	/// @see https://commons.apache.org/proper/commons-io/javadocs/api-2.5/org/apache/commons/io/FilenameUtils.html#getName(java.lang.String)
	/// @param raw file name/path
	/// @return filename without the path
	public static String getName(String filename) { 
		return org.apache.commons.io.FilenameUtils.getName(filename);
	}
	
	/// @see https://commons.apache.org/proper/commons-io/javadocs/api-2.5/org/apache/commons/io/FilenameUtils.html#getPath(java.lang.String)
	/// @param raw file name/path
	/// @return full resolved path with ending / for directories
	public static String getPath(String filename) { 
		return org.apache.commons.io.FilenameUtils.getPath(filename);
	}
	
	/// @see https://commons.apache.org/proper/commons-io/javadocs/api-2.5/org/apache/commons/io/FilenameUtils.html#getPathNoEndSeparator(java.lang.String)
	/// @param raw file name/path
	/// @return full resolved path without ending / for directories
	public static String getPathNoEndSeparator(String filename) { 
		return org.apache.commons.io.FilenameUtils.getPathNoEndSeparator(filename);
	}
	
	/// @see https://commons.apache.org/proper/commons-io/javadocs/api-2.5/org/apache/commons/io/FilenameUtils.html#normalize(java.lang.String)
	/// @param raw file name/path
	/// @return full resolved path with ending / for directories
	public static String normalize(String filename) { 
		return org.apache.commons.io.FilenameUtils.normalize(filename);
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

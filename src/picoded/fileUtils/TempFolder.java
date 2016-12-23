package picoded.fileUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.UUID;
import java.util.function.BiFunction;

/// TempFile is a class which is create and cleanup temp files.
///
/// ### Example Usage
///
/// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~{.java}
///
/// // Create a temp file with specified name with '.tmp' extension.
///
/// // temp files are given randomly generated file names 'base58 guid' or user specific file name.
/// String generatedFileNames = ce30cd34-bc07-4c39-9952-dcb7200555d9.tmp
/// or
/// String userFileNames = test.tmp
///
/// // Created directory to strore all created temp files.
/// String createTempFile = /var/tmp/pjcTemp
///
/// // Cleanup function for files older then X (default to 48 hours) can be specified or user specific hours.
/// String fileCleanupPath = /var/tmp/pjcTemp
///
/// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
///

public class TempFolder {
	private interface fileWriterInterface extends BiFunction<File, Object, Boolean> {
		public Boolean apply(File file, Object input);
	}
	
	/// default to 48 hours i.e. 172800 seconds
	private final static int file_outdated_hours = 48;
	
	/// temp folder name to be appened in system default temp folder
	//	private final static String tempFolder = "piJCTemp";
	
	/// default temp file extension
	//	private final static String fileExtension = ".tmp";
	
	private final static String _javaTmpDir = "java.io.tmpdir";
	
	private static String _piJCTempFolderName = "piJCTemp";
	
	private String _tempFolderName = "";
	
	private File _tempFolder = null;
	
	public TempFolder(String tempFolderName) {
		createFolder(tempFolderName);
	}
	
	public TempFolder() {
		createFolder(null);
	}
	
	public File createFolder(String tempFolderName) {
		if (tempFolderName == null || tempFolderName.trim().isEmpty()) {
			_tempFolderName = getRandomUUID();
		} else {
			_tempFolderName = tempFolderName;
		}
		
		try {
			getTempFolder();
		} catch (IOException ioex) {
			throw new RuntimeException("createFolder() -> " + ioex.getMessage());
		}
		
		return _tempFolder;
	}
	
	public File createChildFile(String childFileName) {
		if (childFileName == null || childFileName.trim().isEmpty()) {
			return null;
		}
		
		File newFile = new File(_tempFolder.getPath() + "/" + childFileName);
		if (!newFile.exists()) {
			newFile.mkdir();
		}
		
		return newFile;
	}
	
	public boolean writeToChildFile(String childFileName, Object data) {
		if (childFileName == null || childFileName.trim().isEmpty()) {
			return false;
		}
		
		// File childFile = getChildFile(childFileName);
		File childFile = new File(_tempFolder.getPath() + File.separator + childFileName);
		if (data instanceof String) {
			return writeString.apply(childFile, data);
		} else if (data instanceof byte[]) {
			return writeByteArray.apply(childFile, data);
		} else {
			throw new RuntimeException(
				"Unable to process data other than String or byte[]. Please extend if needed");
		}
	}
	
	// ///////////////////////////////////////
	//
	// HELPER FUNCSSSSSSSSSSSSS
	//
	// ///////////////////////////////////////
	
	public File getChildFile(String childFileName) {
		File newFile = new File(_tempFolder.getPath() + "/" + childFileName);
		if (!newFile.exists()) {
			newFile.mkdir();
		}
		
		return newFile;
	}
	
	/// Create "piJCTemp" folder under the system temp folder if not already
	// exists.
	/// @returns File object; system temp folder path i.e. /var/tmp/piJCTemp
	public File getTempFolder() throws IOException {
		if (_tempFolder == null) {
			String tmpDir = getSystemTempRootPath();
			_tempFolder = new File(tmpDir + File.separator + _tempFolderName);
			
			boolean createdSuccessfully = true; // will only become false if
			// failed to create a folder
			if (!_tempFolder.exists()) {
				createdSuccessfully = _tempFolder.mkdirs();
			} else {
				return _tempFolder;
			}
			
			if (!createdSuccessfully) {
				throw new RuntimeException("getTempFolder() -> Failed to create temp folder");
			}
		}
		return _tempFolder;
	}
	
	public static String getSystemTempRootPath() {
		try {
			String tmpDir = System.getProperty(_javaTmpDir);
			return tmpDir + File.separator + _piJCTempFolderName;
		} catch (SecurityException sex) { // hehehehe
			throw new RuntimeException("getTempDir() -> Unable to access tmp dir property: "
				+ sex.getMessage());
		} catch (Exception ex) {
			throw new RuntimeException("getTempDir() -> Error while trying to get tmp dir property: "
				+ ex.getMessage());
		}
	}
	
	/// Temp files randomly generated names (base58 guid)
	private static String getRandomUUID() {
		return UUID.randomUUID().toString();
	}
	
	private fileWriterInterface writeString = (file, input) -> {
		String inputString = (String) input;
		
		try {
			FileWriter fw = new FileWriter(file);
			fw.write(inputString);
			fw.flush();
			fw.close();
			
			return true;
		} catch (Exception e) {
			return false;
		}
	};
	
	private fileWriterInterface writeByteArray = (file, input) -> {
		byte[] inputString = (byte[]) input;
		
		try {
			FileOutputStream fos = new FileOutputStream(file);
			fos.write(inputString);
			fos.flush();
			fos.close();
			
			return true;
		} catch (Exception e) {
			return false;
		}
	};
	
	// ///////////////////////////////////////
	//
	// TO REFACTOR
	//
	// ///////////////////////////////////////
	
	/// Delete files from the temp folder older than default hours
	public void cleanupNow() {
		cleanupNow(file_outdated_hours);
	}
	
	/// Delete files from the temp folder older than specified hours. If hours
	/// is <=0 then default hours are considered
	///
	/// Before Cleanup check user hours is '< 0 or > 0' if '< 0' then hous
	/// should be default to 48 hours.
	///
	/// @param hours hours is user specific hours (1 hour or 2 ).
	public void cleanupNow(int hours) {
		if (hours <= 0) {
			hours = file_outdated_hours;
		}
		long currentTimeMillis = (System.currentTimeMillis() / 1000);
		try {
			File[] files = getTempFolder().listFiles();
			if (files != null) {
				for (File file : files) {
					if (file.isFile()) {
						long fileCreateDatetime = (file.lastModified() / 1000);
						if ((currentTimeMillis - fileCreateDatetime) >= (60 * 60 * hours)) {
							file.delete();
						}
					}
				}
			}
		} catch (Exception e) {
			throw new RuntimeException("cleanupNow() -> " + e.getMessage());
		}
	}
}

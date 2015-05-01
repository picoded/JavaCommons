package picoded.fileUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

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

public class tempFile {
	
	/// default to 48 hours i.e. 172800 seconds
	private final static int file_outdated_hours = 48;
	
	/// temp folder name to be appened in system default temp folder
	private final static String tempFolder = "pjcTemp";
	
	/// default temp file extension
	private final static String fileExtension = ".tmp";
	
	/// Temp files randomly generated names (base58 guid)
	public static String getRandomUUID() {
		return UUID.randomUUID().toString();
	}
	
	/// Create a temp file with randomly generated file name 'base58 guid' to system default tmp folder suffix "pjcTemp" i.e. /var/tmp/pjcTemp
	public static void createTempFile() throws IOException {
		createTempFile(null);
	}
	
	/// Create a temp file with specified file name. If file name is NULL / BLANK then file name is randomly generated.
	/// The file is created at the system default temp folder suffix with "pjcTemp" i.e. /var/tmp/pjcTemp
	/// @param   tempFileName         temp File Name is user specific file name (test.tmp).
	/// Throws IOException if file is already exists
	public static void createTempFile(String tempFileName) throws IOException {
		if (tempFileName == null || tempFileName.trim().length() == 0) {
			tempFileName = getRandomUUID();
		}
		// check file is exits or not in the directory
		File folder = new File(createTempDir().getPath());
		File file = new File(folder, tempFileName + fileExtension);
		if (file.isFile()) {
			throw new IOException("File already exists.");
		}
		File temp = File.createTempFile(tempFileName, fileExtension, createTempDir());
	}
	
	/// Create "pjcTemp" folder under the system temp folder if not already exists.
	/// @returns  File object; system temp folder path i.e. /var/tmp/pjcTemp
	public static File createTempDir() {
		File tempDir = new File(System.getProperty("java.io.tmpdir") + File.separator + tempFolder);
		// check directory is exits
		if (tempDir.exists() == false) {
			tempDir.mkdir();
		}
		return tempDir;
	}
	
	/// Delete files from the temp folder older than default hours
	public static void cleanupNow() {
		cleanupNow(file_outdated_hours);
	}
	
	/// Delete files from the temp folder older than specified hours. If hours is <=0 then default hours are considered
	/// @param   hours         hours is user specific hours (1 hour or 2 ).
	/// Before Cleanup check user hours is '< 0 or > 0' if '< 0' then hous should be default to 48 hours.
	public static void cleanupNow(int hours) {
		if (hours <= 0) {
			hours = file_outdated_hours;
		}
		long currentTimeMillis = (System.currentTimeMillis() / 1000);
		File[] files = createTempDir().listFiles();
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
	}
	
	//TODO: TBI
	public static void debouncedCleanup() {
		
	}
}
package picoded.util.file;

import java.io.File;

/**
 * Scans a directory and delete all files older then a specified age.
 * This works using modified timestamp, and its main usage is the clearing of temporary files inside a java servlet.
 *
 * @TODO : File name / type filtering by regexp
 *
 *
 * ### Example Usage
 *
 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~{.java}
 *
 * // The following was extracted from the unit test case
 * // picodedTests.fileUtils.DeleteFilesByAge_test.
 *
 * String testDir = "./test/tmp/fileUtils/DeleteFilesByAge";
 *
 * pWriter = new PrintWriter(testDir+"/olderFile.txt", "UTF-8");
 * pWriter.println("olderFile");
 * pWriter.close();
 *
 * //Pause for 2 seconds
 * Thread.sleep(2000);
 *
 * pWriter = new PrintWriter(testDir+"/newerFile.txt", "UTF-8");
 * pWriter.println("newerFile");
 * pWriter.close();
 *
 * assertTrue( (new File(testDir+"/olderFile.txt")).isFile() );
 * assertTrue( (new File(testDir+"/newerFile.txt")).isFile() );
 *
 * DeleteFilesByAge.olderThenGivenAgeInSeconds(testDir, 1 );
 *
 * assertFalse( (new File(testDir+"/olderFile.txt")).isFile() ); //File is gone
 * assertTrue ( (new File(testDir+"/newerFile.txt")).isFile() ); //File survived
 *
 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 **/
public class DeleteFilesByAge {
	
	/**
	 * Delete files modified timestamp, found older then the given time stamp
	 *
	 * @param fileObj File object, that is scanned as a directory / deleted as
	 * a file
	 * @param olderThenUnixTimestamp Unix-timestamp in which files older or
	 * equal to it, are deleted
	 **/
	static public void olderThenUnixTimestamp(File fileObj, long olderThenUnixTimestamp) {
		if (fileObj.isDirectory()) {
			if (fileObj.listFiles().length == 0 && fileObj.lastModified() <= olderThenUnixTimestamp) {
				fileObj.delete();
			} else {
				for (File f : fileObj.listFiles()) {
					olderThenUnixTimestamp(f, olderThenUnixTimestamp);
				}
			}
		} else if (fileObj != null && (fileObj.lastModified() / 1000L) <= olderThenUnixTimestamp) { // actual
			// deleting?
			fileObj.delete();
		}
	}
	
	/**
	 * Delete files modified timestamp, older then given age
	 *
	 * @param fileObj File object, that is scanned as a directory / deleted as
	 * a file
	 * @param ageInSeconds The age in seconds, that the file must be to be
	 * deleted
	 **/
	static public void olderThenGivenAgeInSeconds(File fileObj, long ageInSeconds) {
		DeleteFilesByAge.olderThenUnixTimestamp(fileObj,
			((System.currentTimeMillis() / 1000L) - ageInSeconds));
	}
	
	/**
	 * Delete files modified timestamp, older then given age
	 *
	 * @param filePath The file path of the directory / file to scan
	 * @param ageInSeconds The age in seconds, that the file must be to be
	 * deleted
	 **/
	static public void olderThenGivenAgeInSeconds(String filePath, long ageInSeconds) {
		DeleteFilesByAge.olderThenGivenAgeInSeconds(new File(filePath), ageInSeconds);
	}
}

package picoded.util.file;

// java incldues
import java.io.*;
import java.math.BigInteger;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.*;
import java.util.zip.*;

// apache includes
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.FalseFileFilter;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.io.output.NullOutputStream;

/**
 * Direct 1-to-1 replacement to apache FileUtils. With one major "catch",
 *
 * All documentations here is copied from apache commons, trimmed down where necessary.
 * All deprecated functions are removed, to break things.
 *
 * And: There are no IOExceptions, they are all reduced to RuntimeException.
 *
 * Blasphamy!
 *
 * Note: This is suppose to be tagged to the latest apache.commons.io implementation, however as we (picoded)
 *       will only make the updated changes, when we actually finally need that updated function.
 *       Any changes to sync the libraries to latest will pull requests is welcomed =)
 *
 * @See https://commons.apache.org/proper/commons-io/javadocs/api-2.5/org/apache/commons/io/FileUtil.html
 **/
public class FileUtilBase {
	
	/**
	 * The number of bytes in a kilobyte.
	 **/
	public static final long ONE_KB = FileUtils.ONE_KB;
	
	/**
	 * The number of bytes in a kilobyte.
	 **/
	public static final BigInteger ONE_KB_BI = FileUtils.ONE_KB_BI;
	
	/**
	 * The number of bytes in a megabyte.
	 **/
	public static final long ONE_MB = FileUtils.ONE_MB;
	
	/**
	 * The number of bytes in a megabyte.
	 **/
	public static final BigInteger ONE_MB_BI = FileUtils.ONE_MB_BI;
	
	/**
	 * The number of bytes in a gigabyte.
	 **/
	public static final long ONE_GB = FileUtils.ONE_GB;
	
	/**
	 * The number of bytes in a gigabyte.
	 **/
	public static final BigInteger ONE_GB_BI = FileUtils.ONE_GB_BI;
	
	/**
	 * The number of bytes in a terabyte.
	 **/
	public static final long ONE_TB = FileUtils.ONE_TB;
	
	/**
	 * The number of bytes in a terabyte.
	 **/
	public static final BigInteger ONE_TB_BI = FileUtils.ONE_TB_BI;
	
	/**
	 * The number of bytes in a petabyte.
	 **/
	public static final long ONE_PB = FileUtils.ONE_PB;
	
	/**
	 * The number of bytes in a petabyte.
	 **/
	public static final BigInteger ONE_PB_BI = FileUtils.ONE_PB_BI;
	
	/**
	 * The number of bytes in an exabyte.
	 **/
	public static final long ONE_EB = FileUtils.ONE_EB;
	
	/**
	 * The number of bytes in an exabyte.
	 **/
	public static final BigInteger ONE_EB_BI = FileUtils.ONE_EB_BI;
	
	/**
	 * The number of bytes in a zettabyte.
	 **/
	public static final BigInteger ONE_ZB = FileUtils.ONE_ZB;
	
	/**
	 * The number of bytes in a yottabyte.
	 **/
	public static final BigInteger ONE_YB = FileUtils.ONE_YB;
	
	/**
	 * An empty array of type <code>File</code>.
	 **/
	public static final File[] EMPTY_FILE_ARRAY = FileUtils.EMPTY_FILE_ARRAY;
	
	/**
	 * Construct a file from the set of name elements.
	 * @param directory the parent directory
	 * @param names the name elements
	 * @return the file
	 **/
	public static File getFile(final File directory, final String... names) {
		return FileUtils.getFile(directory, names);
	}
	
	/**
	 * Construct a file from the set of name elements.
	 * @param names the name elements
	 * @return the file
	 **/
	public static File getFile(final String... names) {
		return FileUtils.getFile(names);
	}
	
	/**
	 * Returns the path to the system temporary directory.
	 * @return the path to the system temporary directory.
	 **/
	public static String getTempDirectoryPath() {
		return FileUtils.getTempDirectoryPath();
	}
	
	/**
	 * Returns a {@link File} representing the system temporary directory.
	 * @return the system temporary directory.
	 **/
	public static File getTempDirectory() {
		return FileUtils.getTempDirectory();
	}
	
	/**
	 * Returns the path to the user's home directory.
	 * @return the path to the user's home directory.
	 **/
	public static String getUserDirectoryPath() {
		return FileUtils.getUserDirectoryPath();
	}
	
	/**
	 * Returns a {@link File} representing the user's home directory.
	 * @return the user's home directory.
	 **/
	public static File getUserDirectory() {
		return FileUtils.getUserDirectory();
	}
	
	/**
	 * Opens a {@link FileInputStream} for the specified file, providing better
	 * error messages than simply calling <code>new FileInputStream(file)</code>.
	 *
	 * At the end of the method either the stream will be successfully opened,
	 * or an exception will have been thrown.
	 *
	 * An exception is thrown if the file does not exist.
	 * An exception is thrown if the file object exists but is a directory.
	 * An exception is thrown if the file exists but cannot be read.
	 *
	 * @param file the file to open for input, must not be {@code null}
	 * @return a new {@link FileInputStream} for the specified file
	 **/
	public static FileInputStream openInputStream(final File file) {
		try {
			return FileUtils.openInputStream(file);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Opens a {@link FileOutputStream} for the specified file, checking and
	 * creating the parent directory if it does not exist.
	 *
	 * At the end of the method either the stream will be successfully opened,
	 * or an exception will have been thrown.
	 *
	 * The parent directory will be created if it does not exist.
	 * The file will be created if it does not exist.
	 * An exception is thrown if the file object exists but is a directory.
	 * An exception is thrown if the file exists but cannot be written to.
	 * An exception is thrown if the parent directory cannot be created.
	 *
	 * @param file the file to open for output, must not be {@code null}
	 * @return a new {@link FileOutputStream} for the specified file
	 **/
	public static FileOutputStream openOutputStream(final File file) {
		try {
			return FileUtils.openOutputStream(file);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Opens a {@link FileOutputStream} for the specified file, checking and
	 * creating the parent directory if it does not exist.
	 *
	 * At the end of the method either the stream will be successfully opened,
	 * or an exception will have been thrown.
	 *
	 * The parent directory will be created if it does not exist.
	 * The file will be created if it does not exist.
	 * An exception is thrown if the file object exists but is a directory.
	 * An exception is thrown if the file exists but cannot be written to.
	 * An exception is thrown if the parent directory cannot be created.
	 *
	 * @param file   the file to open for output, must not be {@code null}
	 * @param append if {@code true}, then bytes will be added to the
	 *               end of the file rather than overwriting
	 * @return a new {@link FileOutputStream} for the specified file
	 **/
	public static FileOutputStream openOutputStream(final File file, final boolean append) {
		try {
			return FileUtils.openOutputStream(file, append);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Returns a human-readable version of the file size, where the input represents a specific number of bytes.
	 *
	 * If the size is over 1GB, the size is returned as the number of whole GB, i.e. the size is rounded down to the
	 * nearest GB boundary.
	 *
	 * Similarly for the 1MB and 1KB boundaries.
	 *
	 * @param size the number of bytes
	 * @return a human-readable display value (includes units - EB, PB, TB, GB, MB, KB or bytes)
	 * @see <a href="https://issues.apache.org/jira/browse/IO-226">IO-226 - should the rounding be changed?</a>
	 **/
	public static String byteCountToDisplaySize(final BigInteger size) {
		try {
			return FileUtils.byteCountToDisplaySize(size);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Returns a human-readable version of the file size, where the input represents a specific number of bytes.
	 *
	 * If the size is over 1GB, the size is returned as the number of whole GB, i.e. the size is rounded down to the
	 * nearest GB boundary.
	 *
	 * Similarly for the 1MB and 1KB boundaries.
	 *
	 * @param size the number of bytes
	 * @return a human-readable display value (includes units - EB, PB, TB, GB, MB, KB or bytes)
	 **/
	public static String byteCountToDisplaySize(final long size) {
		try {
			return FileUtils.byteCountToDisplaySize(size);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Implements the same behaviour as the "touch" utility on Unix. It creates
	 * a new file with size 0 or, if the file exists already, it is opened and
	 * closed without modifying it, but updating the file date and time.
	 *
	 * NOTE: As from v1.3, this method throws an IOException if the last
	 * modified date of the file cannot be set. Also, as from v1.3 this method
	 * creates parent directories if they do not exist.
	 *
	 * @param file the File to touch
	 **/
	public static void touch(final File file) {
		try {
			FileUtils.touch(file);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Converts a Collection containing java.io.File instanced into array
	 * representation. This is to account for the difference between
	 * File.listFiles() and FileUtils.listFiles().
	 *
	 * @param files a Collection containing java.io.File instances
	 * @return an array of java.io.File
	 **/
	public static File[] convertFileCollectionToFileArray(final Collection<File> files) {
		try {
			return FileUtils.convertFileCollectionToFileArray(files);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Finds files within a given directory (and optionally its
	 * subdirectories). All files found are filtered by an IOFileFilter.
	 *
	 * If your search should recurse into subdirectories you can pass in
	 * an IOFileFilter for directories. You don't need to bind a
	 * DirectoryFileFilter (via logical AND) to this filter. This method does
	 * that for you.
	 *
	 * An example: If you want to search through all directories called
	 * "temp" you pass in <code>FileFilterUtils.NameFileFilter("temp")</code>
	 *
	 * Another common usage of this method is find files in a directory
	 * tree but ignoring the directories generated CVS. You can simply pass
	 * in <code>FileFilterUtils.makeCVSAware(null)</code>.
	 *
	 * @param directory  the directory to search in
	 * @param fileFilter filter to apply when finding files.
	 * @param dirFilter  optional filter to apply when finding subdirectories.
	 *                   If this parameter is {@code null}, subdirectories will not be included in the
	 *                   search. Use TrueFileFilter.INSTANCE to match all directories.
	 * @return an collection of java.io.File with the matching files
	 **/
	public static Collection<File> listFiles(final File directory, final IOFileFilter fileFilter,
		final IOFileFilter dirFilter) {
		try {
			return FileUtils.listFiles(directory, fileFilter, dirFilter);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Finds files within a given directory (and optionally its
	 * subdirectories). All files found are filtered by an IOFileFilter.
	 *
	 * The resulting collection includes the starting directory and
	 * any subdirectories that match the directory filter.
	 *
	 * @param directory  the directory to search in
	 * @param fileFilter filter to apply when finding files.
	 * @param dirFilter  optional filter to apply when finding subdirectories.
	 *                   If this parameter is {@code null}, subdirectories will not be included in the
	 *                   search. Use TrueFileFilter.INSTANCE to match all directories.
	 * @return an collection of java.io.File with the matching files
	 **/
	public static Collection<File> listFilesAndDirs(final File directory,
		final IOFileFilter fileFilter, final IOFileFilter dirFilter) {
		return FileUtils.listFilesAndDirs(directory, fileFilter, dirFilter);
	}
	
	/**
	 * Allows iteration over the files in given directory (and optionally
	 * its subdirectories).
	 *
	 * All files found are filtered by an IOFileFilter. This method is
	 * based on {@link #listFiles(File, IOFileFilter, IOFileFilter)},
	 * which supports Iterable ('foreach' loop).
	 *
	 * @param directory  the directory to search in
	 * @param fileFilter filter to apply when finding files.
	 * @param dirFilter  optional filter to apply when finding subdirectories.
	 *                   If this parameter is {@code null}, subdirectories will not be included in the
	 *                   search. Use TrueFileFilter.INSTANCE to match all directories.
	 * @return an iterator of java.io.File for the matching files
	 **/
	public static Iterator<File> iterateFiles(final File directory, final IOFileFilter fileFilter,
		final IOFileFilter dirFilter) {
		return FileUtils.iterateFiles(directory, fileFilter, dirFilter);
	}
	
	/**
	 * Allows iteration over the files in given directory (and optionally
	 * its subdirectories).
	 *
	 * All files found are filtered by an IOFileFilter. This method is
	 * based on {@link #listFilesAndDirs(File, IOFileFilter, IOFileFilter)},
	 * which supports Iterable ('foreach' loop).
	 *
	 * The resulting iterator includes the subdirectories themselves.
	 *
	 * @param directory  the directory to search in
	 * @param fileFilter filter to apply when finding files.
	 * @param dirFilter  optional filter to apply when finding subdirectories.
	 *                   If this parameter is {@code null}, subdirectories will not be included in the
	 *                   search. Use TrueFileFilter.INSTANCE to match all directories.
	 * @return an iterator of java.io.File for the matching files
	 **/
	public static Iterator<File> iterateFilesAndDirs(final File directory,
		final IOFileFilter fileFilter, final IOFileFilter dirFilter) {
		return FileUtils.iterateFilesAndDirs(directory, fileFilter, dirFilter);
	}
	
	/**
	 * Finds files within a given directory (and optionally its subdirectories)
	 * which match an array of extensions.
	 *
	 * @param directory  the directory to search in
	 * @param extensions an array of extensions, ex. {"java","xml"}. If this
	 *                   parameter is {@code null}, all files are returned.
	 * @param recursive  if true all subdirectories are searched as well
	 * @return an collection of java.io.File with the matching files
	 **/
	public static Collection<File> listFiles(final File directory, final String[] extensions,
		final boolean recursive) {
		return FileUtils.listFiles(directory, extensions, recursive);
	}
	
	/**
	 * Allows iteration over the files in a given directory (and optionally
	 * its subdirectories) which match an array of extensions. This method
	 * is based on {@link #listFiles(File, String[], boolean)},
	 * which supports Iterable ('foreach' loop).
	 *
	 * @param directory  the directory to search in
	 * @param extensions an array of extensions, ex. {"java","xml"}. If this
	 *                   parameter is {@code null}, all files are returned.
	 * @param recursive  if true all subdirectories are searched as well
	 * @return an iterator of java.io.File with the matching files
	 * @since 1.2
	 **/
	public static Iterator<File> iterateFiles(final File directory, final String[] extensions,
		final boolean recursive) {
		return FileUtils.iterateFiles(directory, extensions, recursive);
	}
	
	/**
	 * Compares the contents of two files to determine if they are equal or not.
	 *
	 * This method checks to see if the two files are different lengths
	 * or if they point to the same file, before resorting to byte-by-byte
	 * comparison of the contents.
	 *
	 * Code origin: Avalon
	 *
	 * @param file1 the first file
	 * @param file2 the second file
	 * @return true if the content of the files are equal or they both don't
	 * exist, false otherwise
	 **/
	public static boolean contentEquals(final File file1, final File file2) {
		try {
			return FileUtils.contentEquals(file1, file2);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Compares the contents of two files to determine if they are equal or not.
	 *
	 * This method checks to see if the two files point to the same file,
	 * before resorting to line-by-line comparison of the contents.
	 *
	 *
	 * @param file1       the first file
	 * @param file2       the second file
	 * @param charsetName the character encoding to be used.
	 *                    May be null, in which case the platform default is used
	 * @return true if the content of the files are equal or neither exists,
	 * false otherwise
	 **/
	public static boolean contentEqualsIgnoreEOL(final File file1, final File file2,
		final String charsetName) {
		try {
			return FileUtils.contentEqualsIgnoreEOL(file1, file2, charsetName);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Convert from a <code>URL</code> to a <code>File</code>.
	 *
	 * From version 1.1 this method will decode the URL.
	 * Syntax such as <code>file:*my%20docs/file.txt</code> will be
	 * correctly decoded to <code>/my docs/file.txt</code>. Starting with version
	 * 1.5, this method uses UTF-8 to decode percent-encoded octets to characters.
	 * Additionally, malformed percent-encoded octets are handled leniently by
	 * passing them through literally.
	 *
	 * @param url the file URL to convert, {@code null} returns {@code null}
	 * @return the equivalent <code>File</code> object, or {@code null}
	 * if the URL's protocol is not <code>file</code>
	 **/
	public static File toFile(final URL url) {
		return FileUtils.toFile(url);
	}
	
	/**
	 * Converts each of an array of <code>URL</code> to a <code>File</code>.
	 *
	 * Returns an array of the same size as the input.
	 * If the input is {@code null}, an empty array is returned.
	 * If the input contains {@code null}, the output array contains {@code null} at the same
	 * index.
	 *
	 * This method will decode the URL.
	 * Syntax such as <code>file:*my%20docs/file.txt</code> will be
	 * correctly decoded to <code>/my docs/file.txt</code>.
	 *
	 * @param urls the file URLs to convert, {@code null} returns empty array
	 * @return a non-{@code null} array of Files matching the input, with a {@code null} item
	 * if there was a {@code null} at that index in the input array
	 **/
	public static File[] toFiles(final URL[] urls) {
		return FileUtils.toFiles(urls);
	}
	
	/**
	 * Converts each of an array of <code>File</code> to a <code>URL</code>.
	 *
	 * Returns an array of the same size as the input.
	 *
	 * @param files the files to convert, must not be {@code null}
	 * @return an array of URLs matching the input
	 **/
	public static URL[] toURLs(final File[] files) {
		try {
			return FileUtils.toURLs(files);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Copies a file to a directory preserving the file date.
	 *
	 * This method copies the contents of the specified source file
	 * to a file of the same name in the specified destination directory.
	 * The destination directory is created if it does not exist.
	 * If the destination file exists, then this method will overwrite it.
	 *
	 * <strong>Note:</strong> This method tries to preserve the file's last
	 * modified date/times using {@link File#setLastModified(long)}, however
	 * it is not guaranteed that the operation will succeed.
	 * If the modification operation fails, no indication is provided.
	 *
	 * @param srcFile an existing file to copy, must not be {@code null}
	 * @param destDir the directory to place the copy in, must not be {@code null}
	 **/
	public static void copyFileToDirectory(final File srcFile, final File destDir) {
		try {
			FileUtils.copyFileToDirectory(srcFile, destDir);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Copies a file to a directory optionally preserving the file date.
	 *
	 * This method copies the contents of the specified source file
	 * to a file of the same name in the specified destination directory.
	 * The destination directory is created if it does not exist.
	 * If the destination file exists, then this method will overwrite it.
	 *
	 * <strong>Note:</strong> Setting <code>preserveFileDate</code> to
	 * {@code true} tries to preserve the file's last modified
	 * date/times using {@link File#setLastModified(long)}, however it is
	 * not guaranteed that the operation will succeed.
	 * If the modification operation fails, no indication is provided.
	 *
	 * @param srcFile          an existing file to copy, must not be {@code null}
	 * @param destDir          the directory to place the copy in, must not be {@code null}
	 * @param preserveFileDate true if the file date of the copy
	 *                         should be the same as the original
	 **/
	public static void copyFileToDirectory(final File srcFile, final File destDir,
		final boolean preserveFileDate) {
		try {
			FileUtils.copyFileToDirectory(srcFile, destDir, preserveFileDate);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Copies a file to a new location preserving the file date.
	 *
	 * This method copies the contents of the specified source file to the
	 * specified destination file. The directory holding the destination file is
	 * created if it does not exist. If the destination file exists, then this
	 * method will overwrite it.
	 *
	 * <strong>Note:</strong> This method tries to preserve the file's last
	 * modified date/times using {@link File#setLastModified(long)}, however
	 * it is not guaranteed that the operation will succeed.
	 * If the modification operation fails, no indication is provided.
	 *
	 * @param srcFile  an existing file to copy, must not be {@code null}
	 * @param destFile the new file, must not be {@code null}
	 *
	 * @throws NullPointerException if source or destination is {@code null}
	 **/
	public static void copyFile(final File srcFile, final File destFile) {
		try {
			FileUtils.copyFile(srcFile, destFile);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Copies a file to a new location.
	 *
	 * This method copies the contents of the specified source file
	 * to the specified destination file.
	 * The directory holding the destination file is created if it does not exist.
	 * If the destination file exists, then this method will overwrite it.
	 *
	 * <strong>Note:</strong> Setting <code>preserveFileDate</code> to
	 * {@code true} tries to preserve the file's last modified
	 * date/times using {@link File#setLastModified(long)}, however it is
	 * not guaranteed that the operation will succeed.
	 * If the modification operation fails, no indication is provided.
	 *
	 * @param srcFile          an existing file to copy, must not be {@code null}
	 * @param destFile         the new file, must not be {@code null}
	 * @param preserveFileDate true if the file date of the copy
	 *                         should be the same as the original
	 **/
	public static void copyFile(final File srcFile, final File destFile,
		final boolean preserveFileDate) {
		try {
			FileUtils.copyFile(srcFile, destFile, preserveFileDate);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Copy bytes from a <code>File</code> to an <code>OutputStream</code>.
	 *
	 * This method buffers the input internally, so there is no need to use a <code>BufferedInputStream</code>.
	 *
	 * @param input  the <code>File</code> to read from
	 * @param output the <code>OutputStream</code> to write to
	 * @return the number of bytes copied
	 **/
	public static long copyFile(final File input, final OutputStream output) {
		try {
			return FileUtils.copyFile(input, output);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Copies a directory to within another directory preserving the file dates.
	 *
	 * This method copies the source directory and all its contents to a
	 * directory of the same name in the specified destination directory.
	 *
	 * The destination directory is created if it does not exist.
	 * If the destination directory did exist, then this method merges
	 * the source with the destination, with the source taking precedence.
	 *
	 * <strong>Note:</strong> This method tries to preserve the files' last
	 * modified date/times using {@link File#setLastModified(long)}, however
	 * it is not guaranteed that those operations will succeed.
	 * If the modification operation fails, no indication is provided.
	 *
	 * @param srcDir  an existing directory to copy, must not be {@code null}
	 * @param destDir the directory to place the copy in, must not be {@code null}
	public static void copyDirectoryToDirectory(final File srcDir, final File destDir) {
		try {
			FileUtils.copyDirectoryToDirectory(srcDir, destDir);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	 * Copies a whole directory to a new location preserving the file dates.
	 *
	 * This method copies the specified directory and all its child
	 * directories and files to the specified destination.
	 * The destination is the new location and name of the directory.
	 *
	 * The destination directory is created if it does not exist.
	 * If the destination directory did exist, then this method merges
	 * the source with the destination, with the source taking precedence.
	 *
	 * <strong>Note:</strong> This method tries to preserve the files' last
	 * modified date/times using {@link File#setLastModified(long)}, however
	 * it is not guaranteed that those operations will succeed.
	 * If the modification operation fails, no indication is provided.
	 *
	 * @param srcDir  an existing directory to copy, must not be {@code null}
	 * @param destDir the new directory, must not be {@code null}
	 **/
	public static void copyDirectory(final File srcDir, final File destDir) {
		try {
			FileUtils.copyDirectory(srcDir, destDir);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Copies a whole directory to a new location.
	 *
	 * This method copies the contents of the specified source directory
	 * to within the specified destination directory.
	 *
	 * The destination directory is created if it does not exist.
	 * If the destination directory did exist, then this method merges
	 * the source with the destination, with the source taking precedence.
	 *
	 * <strong>Note:</strong> Setting <code>preserveFileDate</code> to
	 * {@code true} tries to preserve the files' last modified
	 * date/times using {@link File#setLastModified(long)}, however it is
	 * not guaranteed that those operations will succeed.
	 * If the modification operation fails, no indication is provided.
	 *
	 * @param srcDir           an existing directory to copy, must not be {@code null}
	 * @param destDir          the new directory, must not be {@code null}
	 * @param preserveFileDate true if the file date of the copy
	 *                         should be the same as the original
	 **/
	public static void copyDirectory(final File srcDir, final File destDir,
		final boolean preserveFileDate) {
		try {
			FileUtils.copyDirectory(srcDir, destDir, preserveFileDate);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Copies a filtered directory to a new location preserving the file dates.
	 *
	 * This method copies the contents of the specified source directory
	 * to within the specified destination directory.
	 *
	 * The destination directory is created if it does not exist.
	 * If the destination directory did exist, then this method merges
	 * the source with the destination, with the source taking precedence.
	 *
	 * <strong>Note:</strong> This method tries to preserve the files' last
	 * modified date/times using {@link File#setLastModified(long)}, however
	 * it is not guaranteed that those operations will succeed.
	 * If the modification operation fails, no indication is provided.
	 *
	 * <h3>Example: Copy directories only</h3>
	 * <pre>
	 *  // only copy the directory structure
	 *  FileUtils.copyDirectory(srcDir, destDir, DirectoryFileFilter.DIRECTORY);
	 *  </pre>
	 *
	 * <h3>Example: Copy directories and txt files</h3>
	 * <pre>
	 *  // Create a filter for ".txt" files
	 *  IOFileFilter txtSuffixFilter = FileFilterUtils.suffixFileFilter(".txt");
	 *  IOFileFilter txtFiles = FileFilterUtils.andFileFilter(FileFileFilter.FILE, txtSuffixFilter);
	 *
	 *  // Create a filter for either directories or ".txt" files
	 *  FileFilter filter = FileFilterUtils.orFileFilter(DirectoryFileFilter.DIRECTORY, txtFiles);
	 *
	 *  // Copy using the filter
	 *  FileUtils.copyDirectory(srcDir, destDir, filter);
	 *  </pre>
	 *
	 * @param srcDir  an existing directory to copy, must not be {@code null}
	 * @param destDir the new directory, must not be {@code null}
	 * @param filter  the filter to apply, null means copy all directories and files
	 *                should be the same as the original
	 **/
	public static void copyDirectory(final File srcDir, final File destDir, final FileFilter filter) {
		try {
			FileUtils.copyDirectory(srcDir, destDir, filter);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Copies a filtered directory to a new location.
	 *
	 * This method copies the contents of the specified source directory
	 * to within the specified destination directory.
	 *
	 * The destination directory is created if it does not exist.
	 * If the destination directory did exist, then this method merges
	 * the source with the destination, with the source taking precedence.
	 *
	 * <strong>Note:</strong> Setting <code>preserveFileDate</code> to
	 * {@code true} tries to preserve the files' last modified
	 * date/times using {@link File#setLastModified(long)}, however it is
	 * not guaranteed that those operations will succeed.
	 * If the modification operation fails, no indication is provided.
	 *
	 * <h3>Example: Copy directories only</h3>
	 * <pre>
	 *  // only copy the directory structure
	 *  FileUtils.copyDirectory(srcDir, destDir, DirectoryFileFilter.DIRECTORY, false);
	 *  </pre>
	 *
	 * <h3>Example: Copy directories and txt files</h3>
	 * <pre>
	 *  // Create a filter for ".txt" files
	 *  IOFileFilter txtSuffixFilter = FileFilterUtils.suffixFileFilter(".txt");
	 *  IOFileFilter txtFiles = FileFilterUtils.andFileFilter(FileFileFilter.FILE, txtSuffixFilter);
	 *
	 *  // Create a filter for either directories or ".txt" files
	 *  FileFilter filter = FileFilterUtils.orFileFilter(DirectoryFileFilter.DIRECTORY, txtFiles);
	 *
	 *  // Copy using the filter
	 *  FileUtils.copyDirectory(srcDir, destDir, filter, false);
	 *  </pre>
	 *
	 * @param srcDir           an existing directory to copy, must not be {@code null}
	 * @param destDir          the new directory, must not be {@code null}
	 * @param filter           the filter to apply, null means copy all directories and files
	 * @param preserveFileDate true if the file date of the copy
	 *                         should be the same as the original
	 **/
	public static void copyDirectory(final File srcDir, final File destDir, final FileFilter filter,
		final boolean preserveFileDate) {
		try {
			FileUtils.copyDirectory(srcDir, destDir, filter, preserveFileDate);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Copies bytes from the URL <code>source</code> to a file
	 * <code>destination</code>. The directories up to <code>destination</code>
	 * will be created if they don't already exist. <code>destination</code>
	 * will be overwritten if it already exists.
	 *
	 * Warning: this method does not set a connection or read timeout and thus
	 * might block forever. Use {@link #copyURLToFile(URL, File, int, int)}
	 * with reasonable timeouts to prevent this.
	 *
	 * @param source      the <code>URL</code> to copy bytes from, must not be {@code null}
	 * @param destination the non-directory <code>File</code> to write bytes to
	 *                    (possibly overwriting), must not be {@code null}
	 **/
	public static void copyURLToFile(final URL source, final File destination) {
		try {
			FileUtils.copyURLToFile(source, destination);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Copies bytes from the URL <code>source</code> to a file
	 * <code>destination</code>. The directories up to <code>destination</code>
	 * will be created if they don't already exist. <code>destination</code>
	 * will be overwritten if it already exists.
	 *
	 * @param source            the <code>URL</code> to copy bytes from, must not be {@code null}
	 * @param destination       the non-directory <code>File</code> to write bytes to
	 *                          (possibly overwriting), must not be {@code null}
	 * @param connectionTimeout the number of milliseconds until this method
	 *                          will timeout if no connection could be established to the <code>source</code>
	 * @param readTimeout       the number of milliseconds until this method will
	 *                         timeout if no data could be read from the <code>source</code>
	 **/
	public static void copyURLToFile(final URL source, final File destination,
		final int connectionTimeout, final int readTimeout) {
		try {
			FileUtils.copyURLToFile(source, destination, connectionTimeout, readTimeout);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Copies bytes from an {@link InputStream} <code>source</code> to a file
	 * <code>destination</code>. The directories up to <code>destination</code>
	 * will be created if they don't already exist. <code>destination</code>
	 * will be overwritten if it already exists.
	 * The {@code source} stream is closed.
	 *
	 * @param source      the <code>InputStream</code> to copy bytes from, must not be {@code null}, will be closed
	 * @param destination the non-directory <code>File</code> to write bytes to
	 *                    (possibly overwriting), must not be {@code null}
	 **/
	public static void copyInputStreamToFile(final InputStream source, final File destination) {
		try {
			FileUtils.copyInputStreamToFile(source, destination);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Deletes a directory recursively.
	 *
	 * @param directory directory to delete
	 **/
	public static void deleteDirectory(final File directory) {
		try {
			FileUtils.deleteDirectory(directory);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Deletes a file, never throwing an exception. If file is a directory, delete it and all sub-directories.
	 *
	 * The difference between File.delete() and this method are:
	 * <ul>
	 * <li>A directory to be deleted does not have to be empty.</li>
	 * <li>No exceptions are thrown when a file or directory cannot be deleted.</li>
	 * </ul>
	 *
	 * @param file file or directory to delete, can be {@code null}
	 * @return {@code true} if the file or directory was deleted, otherwise {@code false}
	 **/
	public static boolean deleteQuietly(final File file) {
		return FileUtils.deleteQuietly(file);
	}
	
	/**
	 * Determines whether the {@code parent} directory contains the {@code child} element (a file or directory).
	 *
	 * Files are normalized before comparison.
	 *
	 * Edge cases:
	 * <ul>
	 * <li>A {@code directory} must not be null: if null, throw IllegalArgumentException</li>
	 * <li>A {@code directory} must be a directory: if not a directory, throw IllegalArgumentException</li>
	 * <li>A directory does not contain itself: return false</li>
	 * <li>A null child file is not contained in any parent: return false</li>
	 * </ul>
	 *
	 * @param directory the file to consider as the parent.
	 * @param child     the file to consider as the child.
	 * @return true is the candidate leaf is under by the specified composite. False otherwise.
	 **/
	public static boolean directoryContains(final File directory, final File child) {
		try {
			return FileUtils.directoryContains(directory, child);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Waits for NFS to propagate a file creation, imposing a timeout.
	 *
	 * This method repeatedly tests {@link File#exists()} until it returns
	 * true up to the maximum time specified in seconds.
	 *
	 * @param file    the file to check, must not be {@code null}
	 * @param seconds the maximum time in seconds to wait
	 * @return true if file exists
	 **/
	public static boolean waitFor(final File file, final int seconds) {
		return FileUtils.waitFor(file, seconds);
	}
	
	/**
	 * Reads the contents of a file into a String.
	 * The file is always closed.
	 *
	 * @param file     the file to read, must not be {@code null}
	 * @param encoding the encoding to use, {@code null} means platform default
	 * @return the file contents, never {@code null}
	 **/
	public static String readFileToString(final File file, final Charset encoding) {
		try {
			return FileUtils.readFileToString(file, encoding);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Reads the contents of a file into a String. The file is always closed.
	 *
	 * @param file     the file to read, must not be {@code null}
	 * @param encoding the encoding to use, {@code null} means platform default
	 * @return the file contents, never {@code null}
	 **/
	public static String readFileToString(final File file, final String encoding) {
		try {
			return FileUtils.readFileToString(file, encoding);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Reads the contents of a file into a byte array.
	 * The file is always closed.
	 *
	 * @param file the file to read, must not be {@code null}
	 * @return the file contents, never {@code null}
	 **/
	public static byte[] readFileToByteArray(final File file) {
		try {
			return FileUtils.readFileToByteArray(file);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Reads the contents of a file line by line to a List of Strings.
	 * The file is always closed.
	 *
	 * @param file     the file to read, must not be {@code null}
	 * @param encoding the encoding to use, {@code null} means platform default
	 * @return the list of Strings representing each line in the file, never {@code null}
	 **/
	public static List<String> readLines(final File file, final Charset encoding) {
		try {
			return FileUtils.readLines(file, encoding);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Reads the contents of a file line by line to a List of Strings. The file is always closed.
	 *
	 * @param file     the file to read, must not be {@code null}
	 * @param encoding the encoding to use, {@code null} means platform default
	 * @return the list of Strings representing each line in the file, never {@code null}
	 **/
	public static List<String> readLines(final File file, final String encoding) {
		try {
			return FileUtils.readLines(file, encoding);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Returns an Iterator for the lines in a <code>File</code>.
	 *
	 * This method opens an <code>InputStream</code> for the file.
	 * When you have finished with the iterator you should close the stream
	 * to free internal resources. This can be done by calling the
	 * {@link LineIterator#close()} or
	 * {@link LineIterator#closeQuietly(LineIterator)} method.
	 *
	 * The recommended usage pattern is:
	 * <pre>
	 * LineIterator it = FileUtils.lineIterator(file, "UTF-8");
	 * try {
	 *   while (it.hasNext()) {
	 *     String line = it.nextLine();
	 *     * do something with line
	 *   }
	 * } finally {
	 *   LineIterator.closeQuietly(iterator);
	 * }
	 * </pre>
	 *
	 * If an exception occurs during the creation of the iterator, the
	 * underlying stream is closed.
	 *
	 * @param file     the file to open for input, must not be {@code null}
	 * @param encoding the encoding to use, {@code null} means platform default
	 * @return an Iterator of the lines in the file, never {@code null}
	 **/
	public static LineIterator lineIterator(final File file, final String encoding) {
		try {
			return FileUtils.lineIterator(file, encoding);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Returns an Iterator for the lines in a <code>File</code> using the default encoding for the VM.
	 *
	 * @param file the file to open for input, must not be {@code null}
	 * @return an Iterator of the lines in the file, never {@code null}
	 * @see #lineIterator(File, String)
	 **/
	public static LineIterator lineIterator(final File file) {
		try {
			return FileUtils.lineIterator(file);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Writes a String to a file creating the file if it does not exist.
	 *
	 * NOTE: As from v1.3, the parent directories of the file will be created
	 * if they do not exist.
	 *
	 * @param file     the file to write
	 * @param data     the content to write to the file
	 * @param encoding the encoding to use, {@code null} means platform default
	 **/
	public static void writeStringToFile(final File file, final String data, final Charset encoding) {
		try {
			FileUtils.writeStringToFile(file, data, encoding);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Writes a String to a file creating the file if it does not exist.
	 *
	 * NOTE: As from v1.3, the parent directories of the file will be created
	 * if they do not exist.
	 *
	 * @param file     the file to write
	 * @param data     the content to write to the file
	 * @param encoding the encoding to use, {@code null} means platform default
	 **/
	public static void writeStringToFile(final File file, final String data, final String encoding) {
		try {
			FileUtils.writeStringToFile(file, data, encoding);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Writes a String to a file creating the file if it does not exist.
	 *
	 * @param file     the file to write
	 * @param data     the content to write to the file
	 * @param encoding the encoding to use, {@code null} means platform default
	 * @param append   if {@code true}, then the String will be added to the
	 *                 end of the file rather than overwriting
	 **/
	public static void writeStringToFile(final File file, final String data, final Charset encoding,
		final boolean append) {
		try {
			FileUtils.writeStringToFile(file, data, encoding, append);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Writes a String to a file creating the file if it does not exist.
	 *
	 * @param file     the file to write
	 * @param data     the content to write to the file
	 * @param encoding the encoding to use, {@code null} means platform default
	 * @param append   if {@code true}, then the String will be added to the
	 *                 end of the file rather than overwriting
	 *
	 **/
	public static void writeStringToFile(final File file, final String data, final String encoding,
		final boolean append) {
		try {
			FileUtils.writeStringToFile(file, data, encoding, append);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Writes a CharSequence to a file creating the file if it does not exist.
	 *
	 * @param file     the file to write
	 * @param data     the content to write to the file
	 * @param encoding the encoding to use, {@code null} means platform default
	 **/
	public static void write(final File file, final CharSequence data, final Charset encoding) {
		try {
			FileUtils.write(file, data, encoding);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Writes a CharSequence to a file creating the file if it does not exist.
	 *
	 * @param file     the file to write
	 * @param data     the content to write to the file
	 * @param encoding the encoding to use, {@code null} means platform default
	 **/
	public static void write(final File file, final CharSequence data, final String encoding) {
		try {
			FileUtils.write(file, data, encoding);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Writes a CharSequence to a file creating the file if it does not exist.
	 *
	 * @param file     the file to write
	 * @param data     the content to write to the file
	 * @param encoding the encoding to use, {@code null} means platform default
	 * @param append   if {@code true}, then the data will be added to the
	 *                 end of the file rather than overwriting
	 **/
	public static void write(final File file, final CharSequence data, final Charset encoding,
		final boolean append) {
		try {
			FileUtils.write(file, data, encoding, append);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Writes a CharSequence to a file creating the file if it does not exist.
	 *
	 * @param file     the file to write
	 * @param data     the content to write to the file
	 * @param encoding the encoding to use, {@code null} means platform default
	 * @param append   if {@code true}, then the data will be added to the
	 *                 end of the file rather than overwriting
	 **/
	public static void write(final File file, final CharSequence data, final String encoding,
		final boolean append) {
		try {
			FileUtils.write(file, data, encoding, append);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Writes a byte array to a file creating the file if it does not exist.
	 *
	 * NOTE: As from v1.3, the parent directories of the file will be created
	 * if they do not exist.
	 *
	 * @param file the file to write to
	 * @param data the content to write to the file
	 **/
	public static void writeByteArrayToFile(final File file, final byte[] data) {
		try {
			FileUtils.writeByteArrayToFile(file, data);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Writes a byte array to a file creating the file if it does not exist.
	 *
	 * @param file   the file to write to
	 * @param data   the content to write to the file
	 * @param append if {@code true}, then bytes will be added to the
	 *               end of the file rather than overwriting
	 **/
	public static void writeByteArrayToFile(final File file, final byte[] data, final boolean append) {
		try {
			FileUtils.writeByteArrayToFile(file, data, append);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Writes the <code>toString()</code> value of each item in a collection to
	 * the specified <code>File</code> line by line.
	 * The specified character encoding and the default line ending will be used.
	 *
	 * NOTE: As from v1.3, the parent directories of the file will be created
	 * if they do not exist.
	 *
	 * @param file     the file to write to
	 * @param encoding the encoding to use, {@code null} means platform default
	 * @param lines    the lines to write, {@code null} entries produce blank lines
	 **/
	public static void writeLines(final File file, final String encoding, final Collection<?> lines) {
		try {
			FileUtils.writeLines(file, encoding, lines);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Writes the <code>toString()</code> value of each item in a collection to
	 * the specified <code>File</code> line by line, optionally appending.
	 * The specified character encoding and the default line ending will be used.
	 *
	 * @param file     the file to write to
	 * @param encoding the encoding to use, {@code null} means platform default
	 * @param lines    the lines to write, {@code null} entries produce blank lines
	 * @param append   if {@code true}, then the lines will be added to the
	 *                 end of the file rather than overwriting
	 **/
	public static void writeLines(final File file, final String encoding, final Collection<?> lines,
		final boolean append) {
		try {
			FileUtils.writeLines(file, encoding, lines, append);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Writes the <code>toString()</code> value of each item in a collection to
	 * the specified <code>File</code> line by line.
	 * The default VM encoding and the default line ending will be used.
	 *
	 * @param file  the file to write to
	 * @param lines the lines to write, {@code null} entries produce blank lines
	 **/
	public static void writeLines(final File file, final Collection<?> lines) {
		try {
			FileUtils.writeLines(file, lines);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Writes the <code>toString()</code> value of each item in a collection to
	 * the specified <code>File</code> line by line.
	 * The default VM encoding and the default line ending will be used.
	 *
	 * @param file   the file to write to
	 * @param lines  the lines to write, {@code null} entries produce blank lines
	 * @param append if {@code true}, then the lines will be added to the
	 *               end of the file rather than overwriting
	 **/
	public static void writeLines(final File file, final Collection<?> lines, final boolean append) {
		try {
			FileUtils.writeLines(file, lines, append);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Writes the <code>toString()</code> value of each item in a collection to
	 * the specified <code>File</code> line by line.
	 * The specified character encoding and the line ending will be used.
	 *
	 * NOTE: As from v1.3, the parent directories of the file will be created
	 * if they do not exist.
	 *
	 * @param file       the file to write to
	 * @param encoding   the encoding to use, {@code null} means platform default
	 * @param lines      the lines to write, {@code null} entries produce blank lines
	 * @param lineEnding the line separator to use, {@code null} is system default
	 **/
	public static void writeLines(final File file, final String encoding, final Collection<?> lines,
		final String lineEnding) {
		try {
			FileUtils.writeLines(file, encoding, lines, lineEnding);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Writes the <code>toString()</code> value of each item in a collection to
	 * the specified <code>File</code> line by line.
	 * The specified character encoding and the line ending will be used.
	 *
	 * @param file       the file to write to
	 * @param encoding   the encoding to use, {@code null} means platform default
	 * @param lines      the lines to write, {@code null} entries produce blank lines
	 * @param lineEnding the line separator to use, {@code null} is system default
	 * @param append     if {@code true}, then the lines will be added to the
	 *                   end of the file rather than overwriting
	 **/
	public static void writeLines(final File file, final String encoding, final Collection<?> lines,
		final String lineEnding, final boolean append) {
		try {
			FileUtils.writeLines(file, encoding, lines, lineEnding, append);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Writes the <code>toString()</code> value of each item in a collection to
	 * the specified <code>File</code> line by line.
	 * The default VM encoding and the specified line ending will be used.
	 *
	 * @param file       the file to write to
	 * @param lines      the lines to write, {@code null} entries produce blank lines
	 * @param lineEnding the line separator to use, {@code null} is system default
	 **/
	public static void writeLines(final File file, final Collection<?> lines, final String lineEnding) {
		try {
			FileUtils.writeLines(file, lines, lineEnding);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Writes the <code>toString()</code> value of each item in a collection to
	 * the specified <code>File</code> line by line.
	 * The default VM encoding and the specified line ending will be used.
	 *
	 * @param file       the file to write to
	 * @param lines      the lines to write, {@code null} entries produce blank lines
	 * @param lineEnding the line separator to use, {@code null} is system default
	 * @param append     if {@code true}, then the lines will be added to the
	 *                   end of the file rather than overwriting
	 **/
	public static void writeLines(final File file, final Collection<?> lines,
		final String lineEnding, final boolean append) {
		try {
			FileUtils.writeLines(file, lines, lineEnding, append);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Deletes a file. If file is a directory, delete it and all sub-directories.
	 *
	 * The difference between File.delete() and this method are:
	 * <ul>
	 * <li>A directory to be deleted does not have to be empty.</li>
	 * <li>You get exceptions when a file or directory cannot be deleted.
	 * (java.io.File methods returns a boolean)</li>
	 * </ul>
	 *
	 * @param file file or directory to delete, must not be {@code null}
	 **/
	public static void forceDelete(final File file) {
		try {
			FileUtils.forceDelete(file);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Schedules a file to be deleted when JVM exits.
	 * If file is directory delete it and all sub-directories.
	 *
	 * @param file file or directory to delete, must not be {@code null}
	 **/
	public static void forceDeleteOnExit(final File file) {
		try {
			FileUtils.forceDeleteOnExit(file);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Makes a directory, including any necessary but nonexistent parent
	 * directories. If a file already exists with specified name but it is
	 * not a directory then an IOException is thrown.
	 * If the directory cannot be created (or does not already exist)
	 * then an IOException is thrown.
	 *
	 * @param directory directory to create, must not be {@code null}
	 **/
	public static void forceMkdir(final File directory) {
		try {
			FileUtils.forceMkdir(directory);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Returns the size of the specified file or directory. If the provided
	 * {@link File} is a regular file, then the file's length is returned.
	 * If the argument is a directory, then the size of the directory is
	 * calculated recursively. If a directory or subdirectory is security
	 * restricted, its size will not be included.
	 *
	 * Note that overflow is not detected, and the return value may be negative if
	 * overflow occurs. See {@link #sizeOfAsBigInteger(File)} for an alternative
	 * method that does not overflow.
	 *
	 * @param file the regular file or directory to return the size
	 *             of (must not be {@code null}).
	 *
	 * @return the length of the file, or recursive size of the directory,
	 * provided (in bytes).
	 **/
	public static long sizeOf(final File file) {
		return FileUtils.sizeOf(file);
	}
	
	/**
	 * Returns the size of the specified file or directory. If the provided
	 * {@link File} is a regular file, then the file's length is returned.
	 * If the argument is a directory, then the size of the directory is
	 * calculated recursively. If a directory or subdirectory is security
	 * restricted, its size will not be included.
	 *
	 * @param file the regular file or directory to return the size
	 *             of (must not be {@code null}).
	 *
	 * @return the length of the file, or recursive size of the directory,
	 * provided (in bytes).
	 **/
	public static BigInteger sizeOfAsBigInteger(final File file) {
		return FileUtils.sizeOfAsBigInteger(file);
	}
	
	/**
	 * Counts the size of a directory recursively (sum of the length of all files).
	 *
	 * @param directory directory to inspect, must not be {@code null}
	 * @return size of directory in bytes, 0 if directory is security restricted.
	 **/
	public static BigInteger sizeOfDirectoryAsBigInteger(final File directory) {
		return FileUtils.sizeOfDirectoryAsBigInteger(directory);
	}
	
	/**
	 * Tests if the specified <code>File</code> is newer than the reference
	 * <code>File</code>.
	 *
	 * @param file      the <code>File</code> of which the modification date must
	 *                  be compared, must not be {@code null}
	 * @param reference the <code>File</code> of which the modification date
	 *                  is used, must not be {@code null}
	 * @return true if the <code>File</code> exists and has been modified more
	 * recently than the reference <code>File</code>
	 **/
	public static boolean isFileNewer(final File file, final File reference) {
		try {
			return FileUtils.isFileNewer(file, reference);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Tests if the specified <code>File</code> is newer than the specified
	 * <code>Date</code>.
	 *
	 * @param file the <code>File</code> of which the modification date
	 *             must be compared, must not be {@code null}
	 * @param date the date reference, must not be {@code null}
	 * @return true if the <code>File</code> exists and has been modified
	 * after the given <code>Date</code>.
	 **/
	public static boolean isFileNewer(final File file, final Date date) {
		try {
			return FileUtils.isFileNewer(file, date);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Tests if the specified <code>File</code> is newer than the specified
	 * time reference.
	 *
	 * @param file       the <code>File</code> of which the modification date must
	 *                   be compared, must not be {@code null}
	 * @param timeMillis the time reference measured in milliseconds since the
	 *                   epoch (00:00:00 GMT, January 1, 1970)
	 * @return true if the <code>File</code> exists and has been modified after
	 * the given time reference.
	 **/
	public static boolean isFileNewer(final File file, final long timeMillis) {
		return FileUtils.isFileNewer(file, timeMillis);
	}
	
	/**
	 * Tests if the specified <code>File</code> is older than the reference
	 * <code>File</code>.
	 *
	 * @param file      the <code>File</code> of which the modification date must
	 *                  be compared, must not be {@code null}
	 * @param reference the <code>File</code> of which the modification date
	 *                  is used, must not be {@code null}
	 * @return true if the <code>File</code> exists and has been modified before
	 * the reference <code>File</code>
	 **/
	public static boolean isFileOlder(final File file, final File reference) {
		return FileUtils.isFileOlder(file, reference);
	}
	
	/**
	 * Tests if the specified <code>File</code> is older than the specified
	 * <code>Date</code>.
	 *
	 * @param file the <code>File</code> of which the modification date
	 *             must be compared, must not be {@code null}
	 * @param date the date reference, must not be {@code null}
	 * @return true if the <code>File</code> exists and has been modified
	 * before the given <code>Date</code>.
	 **/
	public static boolean isFileOlder(final File file, final Date date) {
		return FileUtils.isFileOlder(file, date);
	}
	
	/**
	 * Tests if the specified <code>File</code> is older than the specified
	 * time reference.
	 *
	 * @param file       the <code>File</code> of which the modification date must
	 *                   be compared, must not be {@code null}
	 * @param timeMillis the time reference measured in milliseconds since the
	 *                   epoch (00:00:00 GMT, January 1, 1970)
	 * @return true if the <code>File</code> exists and has been modified before
	 * the given time reference.
	 **/
	public static boolean isFileOlder(final File file, final long timeMillis) {
		return FileUtils.isFileOlder(file, timeMillis);
	}
	
	/**
	 * Computes the checksum of a file using the CRC32 checksum routine.
	 * The value of the checksum is returned.
	 *
	 * @param file the file to checksum, must not be {@code null}
	 * @return the checksum value
	 **/
	public static long checksumCRC32(final File file) {
		try {
			return FileUtils.checksumCRC32(file);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Computes the checksum of a file using the specified checksum object.
	 * Multiple files may be checked using one <code>Checksum</code> instance
	 * if desired simply by reusing the same checksum object.
	 * For example:
	 * <pre>
	 *   long csum = FileUtils.checksum(file, new CRC32()).getValue();
	 * </pre>
	 *
	 * @param file     the file to checksum, must not be {@code null}
	 * @param checksum the checksum object to be used, must not be {@code null}
	 * @return the checksum specified, updated with the content of the file
	 **/
	public static Checksum checksum(final File file, final Checksum checksum) {
		try {
			return FileUtils.checksum(file, checksum);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Moves a directory.
	 *
	 * When the destination directory is on another file system, do a "copy and delete".
	 *
	 * @param srcDir  the directory to be moved
	 * @param destDir the destination directory
	 **/
	public static void moveDirectory(final File srcDir, final File destDir) {
		try {
			FileUtils.moveDirectory(srcDir, destDir);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Moves a directory to another directory.
	 *
	 * @param src           the file to be moved
	 * @param destDir       the destination file
	 * @param createDestDir If {@code true} create the destination directory,
	 *                      otherwise if {@code false} throw an IOException
	 **/
	public static void moveDirectoryToDirectory(final File src, final File destDir,
		final boolean createDestDir) {
		try {
			FileUtils.moveDirectoryToDirectory(src, destDir, createDestDir);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Moves a file.
	 *
	 * When the destination file is on another file system, do a "copy and delete".
	 *
	 * @param srcFile  the file to be moved
	 * @param destFile the destination file
	 **/
	public static void moveFile(final File srcFile, final File destFile) {
		try {
			FileUtils.moveFile(srcFile, destFile);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Moves a file to a directory.
	 *
	 * @param srcFile       the file to be moved
	 * @param destDir       the destination file
	 * @param createDestDir If {@code true} create the destination directory,
	 *                      otherwise if {@code false} throw an IOException
	 **/
	public static void moveFileToDirectory(final File srcFile, final File destDir,
		final boolean createDestDir) {
		try {
			FileUtils.moveFileToDirectory(srcFile, destDir, createDestDir);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Moves a file or directory to the destination directory.
	 *
	 * When the destination is on another file system, do a "copy and delete".
	 *
	 * @param src           the file or directory to be moved
	 * @param destDir       the destination directory
	 * @param createDestDir If {@code true} create the destination directory,
	 *                      otherwise if {@code false} throw an IOException
	 **/
	public static void moveToDirectory(final File src, final File destDir,
		final boolean createDestDir) {
		try {
			FileUtils.moveToDirectory(src, destDir, createDestDir);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Determines whether the specified file is a Symbolic Link rather than an actual file.
	 *
	 * Will not return true if there is a Symbolic Link anywhere in the path,
	 * only if the specific file is.
	 *
	 * When using jdk1.7, this method delegates to {@code boolean java.nio.file.Files.isSymbolicLink(Path path)}
	 *
	 * <b>Note:</b> the current implementation always returns {@code false} if running on
	 * jkd1.6 and the system is detected as Windows using {@link FilenameUtils#isSystemWindows()}
	 *
	 * For code that runs on Java 1.7 or later, use the following method instead:
	 *
	 * {@code boolean java.nio.file.Files.isSymbolicLink(Path path)}
	 * @param file the file to check
	 * @return true if the file is a Symbolic Link
	 **/
	public static boolean isSymlink(final File file) {
		try {
			return FileUtils.isSymlink(file);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
}

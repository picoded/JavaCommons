package picoded.dstack.module.vfs;

import java.util.*;

import picoded.dstack.*;
import picoded.util.file.FileUtil;

import picoded.core.conv.*;

/**
 * VirtualFileObject using DataTable implmentation
 * This replicates most of the file system requirements of Uilicious workspace
 *
 * This represents a single file / folder
 **/
public class VirtualFileObject {
	
	//--------------------------------------------------------------------------
	//
	// Constructor variables, and Constructor
	//
	//--------------------------------------------------------------------------
	
	/**
	 * The parent VirtualFileSystem
	 **/
	protected VirtualFileSystem vfs = null;
	
	/**
	 * The file / folder meta object
	 **/
	protected DataObject mObj = null;
	
	/**
	 * File object mode
	 **/
	protected String type = null;
	
	/**
	 * [Internal] blank constructor
	 **/
	protected VirtualFileObject() {
		// does nothing
	}
	
	/**
	 * [Internal] Constructor for the VirtualFileObject
	 * This should only be constructed by VirtualFileSystem
	 * Avoid direct constructor use.
	 *
	 * @param   Original file system
	 * @param   DataObject representing either a file or folder
	 * @param   DataObject type, being either a "FILE" or "FOLDER"
	 **/
	protected VirtualFileObject(VirtualFileSystem inVFS, DataObject inMObj, String inType) {
		vfs = inVFS;
		mObj = inMObj;
		type = inType;
	}
	
	//--------------------------------------------------------------------------
	//
	// helper functions
	//
	//--------------------------------------------------------------------------
	
	/**
	 * Normalize a path string (reduce amount of edge cases error)
	 * and return its split path array format
	 *
	 * @param  String of file path to normalize and split
	 *
	 * @return The splitted string path to iterate later
	 **/
	protected String[] normalizeToSplitPath(String path) {
		// Null means blank string path
		if (path == null) {
			return ArrayConv.EMPTY_STRING_ARRAY;
		}
		
		// Normalize the paths, removing double // to single slash
		// and a few other things things as well
		// (See: https://commons.apache.org/proper/commons-io/javadocs/api-2.5/org/apache/commons/io/FilenameUtils.html#normalize(java.lang.String))
		String normalized = path.trim();
		normalized = FileUtil.normalize(path);
		
		// This is blank
		if (normalized.length() <= 0) {
			return ArrayConv.EMPTY_STRING_ARRAY;
		}
		
		// Split it as a single slash
		return normalized.split("/");
	}
	
	protected String[] normalizeToSplitPath_withBlankCheck(String path) {
		String[] splitPath = normalizeToSplitPath(path);
		if (splitPath.length == 0) {
			throw new RuntimeException("Unable to create Directory for NULL path");
		}
		return splitPath;
	}
	
	/**
	 * @return the current folder ID, used internally
	 **/
	protected String getDirectoryID() {
		assertIsDirectory();
		
		if (type == "ROOT") {
			return "ROOT";
		}
		return mObj._oid();
	}
	
	/**
	 * Extract various names from an array of DataObject
	 *
	 * @param   array of meta objects
	 *
	 * @return   String array of names, or null (if array is null)
	 **/
	protected String[] extractDataObjectNames(DataObject[] inObjs) {
		// Null safety check
		if (inObjs == null) {
			return null;
		}
		
		// Return array
		String[] ret = new String[inObjs.length];
		for (int i = 0; i < inObjs.length; ++i) {
			ret[i] = inObjs[i].getString("name");
		}
		return ret;
	}
	
	//--------------------------------------------------------------------------
	//
	// DataObject helper functions
	//
	//--------------------------------------------------------------------------
	
	/**
	 * Fetch the DataObject given the directoryName and parent object
	 * if it exists, else return null
	 *
	 * @param   The parent GUID, null defaults to ROOT
	 * @param   The Directory name as a path step
	 *
	 * @return  The Directory meta object, if it exsits
	 **/
	protected DataObject getDirectoryDataObject(String parentID, String directoryName) {
		// Normalize the parent ID
		if (parentID == null) {
			parentID = "ROOT";
		}
		
		//checking if the Directory already exists
		DataObject[] directoryList = vfs.directories.query("parentID = ? AND name = ?", new String[] {
			parentID, directoryName });
		
		// Gets the Directory meta object
		if (directoryList.length > 0) {
			return directoryList[0];
		}
		return null;
	}
	
	/**
	 * Fetch the DataObject given the directoryName and parent object
	 * if it exists, else return null
	 *
	 * @param   The parent GUID, null defaults to ROOT
	 * @param   The file name as a path step
	 *
	 * @return  The file meta object, if it exists
	 **/
	protected DataObject getFileDataObject(String parentID, String fileName) {
		// Normalize the parent ID
		if (parentID == null) {
			parentID = "ROOT";
		}
		
		//checking if the file already exists
		DataObject[] fileList = vfs.files.query("parentID = ? AND name = ?", new String[] { parentID,
			fileName });
		
		// Gets the file meta object
		if (fileList.length > 0) {
			return fileList[0];
		}
		return null;
	}
	
	//--------------------------------------------------------------------------
	//
	// Is checks
	//
	//--------------------------------------------------------------------------
	
	/**
	 * @return  true if its a file object
	 **/
	public boolean isFile() {
		return type.equals("FILE");
	}
	
	/**
	 * @return  true if its a file object
	 **/
	public boolean isDirectory() {
		return type.equals("DIRECTORY") || type.equals("ROOT");
	}
	
	/**
	 * Throws an exception if not a file
	 **/
	protected void assertIsFile() {
		if (!isFile()) {
			throw new RuntimeException("VirtualFileObject is not a FILE");
		}
	}
	
	/**
	 * Throws an exception if not a directory
	 **/
	protected void assertIsDirectory() {
		if (!isDirectory()) {
			throw new RuntimeException("VirtualFileObject is not a DIRECTORY");
		}
	}
	
	//--------------------------------------------------------------------------
	//
	// File / Directory listing
	//
	//--------------------------------------------------------------------------
	
	/**
	 * List the files found inside this folder
	 * Throws an exception if its not a folder
	 *
	 * @return  List of files
	 **/
	public String[] listFileNames() {
		return extractDataObjectNames(vfs.files.query("parentID = ?",
			new String[] { getDirectoryID() }));
	};
	
	/**
	 * List the files found inside this folder
	 * Throws an exception if its not a folder
	 *
	 * @return  List of files
	 **/
	
	public String[] listDirectoryNames() {
		return extractDataObjectNames(vfs.directories.query("parentID = ?",
			new String[] { getDirectoryID() }));
	};
	
	//--------------------------------------------------------------------------
	//
	// File manipulation
	//
	//--------------------------------------------------------------------------
	
	/**
	 * Save the byte[]
	 **/
	public void saveData(byte[] data) {
		assertIsFile();
		
		mObj.put("data", data);
		mObj.saveDelta();
	}
	
	/**
	 * Get the byte[]
	 **/
	public byte[] getData() {
		assertIsFile();
		return (byte[]) mObj.get("data");
	}
	
	/**
	 * Create a file and return the VirtualFileObject
	 *
	 * @param   The full path name to create at
	 * @param   The byte array to store the data (if given, ignored if null)
	 *
	 * @return  The VirtualFileObject representing a file
	 **/
	public VirtualFileObject saveFile(String pathName, byte[] data) {
		// You cant save a file inside a file
		assertIsDirectory();
		
		// Path parts normalized
		String[] splitPath = normalizeToSplitPath_withBlankCheck(pathName);
		if (splitPath.length > 1) {
			throw new RuntimeException("Function does not support nested directories (yet)");
		}
		
		// Get the file DataObject, or generates a new one if needed.
		DataObject newDataObject = null;
		
		// The parentID to use
		String parentID = getDirectoryID();
		
		// Get an exisitng file object
		newDataObject = getFileDataObject(parentID, splitPath[0]);
		if (newDataObject != null) {
			throw new RuntimeException("There already is a file with this name");
		}
		
		// Get an exisisting folder object
		newDataObject = getDirectoryDataObject(parentID, splitPath[0]);
		if (newDataObject != null) {
			throw new RuntimeException("There already is a folder with this name");
		}
		
		// Else create a new file
		if (newDataObject == null) {
			newDataObject = vfs.files.newEntry();
		}
		
		// Save required information
		newDataObject.put("parentID", parentID);
		newDataObject.put("name", splitPath[0]);
		newDataObject.saveDelta();
		
		// Generate the return object
		VirtualFileObject ret = new VirtualFileObject(vfs, newDataObject, "FILE");
		
		// There is data to save
		if (data != null) {
			ret.saveData(data);
		}
		
		// Return the VirtualFileObject
		return ret;
	}
	
	/**
	 * Create a file and return the VirtualFileObject
	 *
	 * @param   The full path name to create at
	 *
	 * @return  The VirtualFileObject representing a file
	 **/
	public VirtualFileObject getFile(String pathName) {
		// You cant save a file inside a file
		assertIsDirectory();
		
		// Path parts normalized
		String[] splitPath = normalizeToSplitPath_withBlankCheck(pathName);
		if (splitPath.length > 1) {
			throw new RuntimeException("Function does not support nested directories (yet)");
		}
		
		// The parentID to use
		String parentID = getDirectoryID();
		
		// Get an exisitng file object
		DataObject newDataObject = getFileDataObject(parentID, splitPath[0]);
		
		// Return null, if no file obj
		if (newDataObject == null) {
			return null;
		}
		
		// Generate the return object
		return new VirtualFileObject(vfs, newDataObject, "FILE");
	}
	
	/**
	 *Delete a file
	 *
	 *@param The full path name to delete
	 **/
	public boolean deleteFile(String pathName) {
		//
		assertIsDirectory();
		
		//Path parts normalised
		String[] splitPath = normalizeToSplitPath_withBlankCheck(pathName);
		if (splitPath.length > 1) {
			throw new RuntimeException("Function does not support nested directories(yet)");
		}
		//the parentID to use
		String parentID = getDirectoryID();
		
		//Get the exisiting file
		DataObject fileToDelete = getFileDataObject(parentID, splitPath[0]);
		
		if (fileToDelete != null) {
			vfs.files.remove(fileToDelete._oid());
		}
		return false;
	}
	
	/**
	 *Move a file
	 **/
	public VirtualFileObject moveFile(String pathName, String newPathName) {
		
		//
		assertIsDirectory();
		
		//Path parts normalised
		String[] splitPath = normalizeToSplitPath_withBlankCheck(pathName);
		if (splitPath.length > 1) {
			throw new RuntimeException("Function does not support nested directories(yet)");
		}
		//the parentID to use
		String parentID = getDirectoryID();
		
		//Get the exisiting file
		DataObject fileToMove = getFileDataObject(parentID, splitPath[0]);
		
		if (fileToMove != null) {
			fileToMove.put("name", newPathName);
			fileToMove.saveDelta();
		}
		// Generate the return object
		VirtualFileObject ret = new VirtualFileObject(vfs, fileToMove, "FILE");
		
		// Return the VirtualFileObject
		return ret;
	}
	
	//--------------------------------------------------------------------------
	//
	// Folder manipulation
	//
	//--------------------------------------------------------------------------
	
	/**
	 * Create a folder and return the VirtualFileObject
	 *
	 * @param   The full path name to create at
	 *
	 * @return  The VirtualFileObject representing a file
	 **/
	public VirtualFileObject makeDirectory(String pathName) {
		// You cant save a file inside a file
		assertIsDirectory();
		
		// Path parts normalized
		String[] splitPath = normalizeToSplitPath_withBlankCheck(pathName);
		if (splitPath.length > 1) {
			throw new RuntimeException("Function does not support nested directories(yet)");
		}
		
		// Get the folder DataObject, or generates a new one if needed.
		DataObject newDataObject = null;
		
		// The parentID to use
		String parentID = getDirectoryID();
		
		// Get an exisitng directory object
		newDataObject = getDirectoryDataObject(parentID, splitPath[0]);
		if (newDataObject != null) {
			throw new RuntimeException("The folder name already exixts");
		}
		
		// Get an exisisting file object
		newDataObject = getFileDataObject(parentID, splitPath[0]);
		if (newDataObject != null) {
			throw new RuntimeException("This name already exists for a file");
		}
		
		// Else create a new directory
		if (newDataObject == null) {
			newDataObject = vfs.directories.newEntry();
		}
		
		// Save required information
		newDataObject.put("parentID", parentID);
		newDataObject.put("name", splitPath[0]);
		newDataObject.saveDelta();
		
		// Generate the return object
		VirtualFileObject ret = new VirtualFileObject(vfs, newDataObject, "DIRECTORY");
		
		// Return the VirtualFileObject
		return ret;
	}
	
	/**
	 * Create a folder and return the VirtualFileObject
	 *
	 * @param   The full path name to create at
	 *
	 * @return  The VirtualFileObject representing a directory
	 **/
	public VirtualFileObject getDirectory(String pathName) {
		// You cant save a file inside a file
		assertIsDirectory();
		
		// Path parts normalized
		String[] splitPath = normalizeToSplitPath_withBlankCheck(pathName);
		if (splitPath.length > 1) {
			throw new RuntimeException("Function does not support nested directories (yet)");
		}
		// The parentID to use
		String parentID = getDirectoryID();
		
		// Get an exisitng file object
		DataObject newDataObject = getDirectoryDataObject(parentID, splitPath[0]);
		
		// Return null, if no file obj
		if (newDataObject == null) {
			return null;
		}
		
		// Generate the return object
		return new VirtualFileObject(vfs, newDataObject, "DIRECTORY");
	}
	
	/**
	 * Delete all files and folder in the current directory
	 **/
	public void emptyDirectory() {
		// Delete call can only be done inside a directory
		assertIsDirectory();
		
		// Get the list of folders in this directory to delete
		String[] dirNames = listDirectoryNames();
		for (int pt = 0; pt < dirNames.length; ++pt) {
			deleteDirectory(dirNames[pt]);
		}
		
		// Get the list of files in this directory to delete
		String[] fileNames = listFileNames();
		for (int pt = 0; pt < fileNames.length; ++pt) {
			deleteFile(fileNames[pt]);
		}
	}
	
	/**
	 * Delete a folder
	 *
	 * @param The full path name to delete
	 **/
	public boolean deleteDirectory(String pathName) {
		// Delete call can only be done inside a directory
		assertIsDirectory();
		
		// Path parts normalised
		String[] splitPath = normalizeToSplitPath_withBlankCheck(pathName);
		if (splitPath.length > 1) {
			throw new RuntimeException("Function does not support nested directories(yet)");
		}
		
		// Get the directory to delete
		VirtualFileObject directoryToDelete = getDirectory(splitPath[0]);
		
		// Directory (to delete) does not exist, terminate
		if (directoryToDelete == null) {
			return false;
		}
		
		// Empties the directory
		directoryToDelete.emptyDirectory();
		
		// Now actually delete the directory
		vfs.directories.remove(directoryToDelete.mObj._oid());
		
		return true;
	}
	
	/**
	 *Move a file
	 **/
	public VirtualFileObject moveDirectory(String pathName, String newPathName) {
		
		//
		assertIsDirectory();
		
		//Path parts normalised
		String[] splitPath = normalizeToSplitPath_withBlankCheck(pathName);
		if (splitPath.length > 1) {
			throw new RuntimeException("Function does not support nested directories(yet)");
		}
		//the parentID to use
		String parentID = getDirectoryID();
		
		//Get the exisiting file
		DataObject directoryToMove = getDirectoryDataObject(parentID, splitPath[0]);
		
		if (directoryToMove != null) {
			directoryToMove.put("name", newPathName);
			directoryToMove.saveDelta();
		}
		// Generate the return object
		VirtualFileObject ret = new VirtualFileObject(vfs, directoryToMove, "DIRECTORY");
		
		// Return the VirtualFileObject
		return ret;
	}
}

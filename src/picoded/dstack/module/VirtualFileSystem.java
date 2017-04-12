package picoded.dstack.module;

import picoded.dstack.*;
import picoded.file.FileUtil;

import javax.print.DocFlavor.BYTE_ARRAY;

import picoded.conv.*;

///
/// VirtualFileSystem using MetaTable implmentation
/// This replicates most of the file system requirements of Uilicious workspace
///
public class VirtualFileSystem {
	
	//--------------------------------------------------------------------------
	//
	// Constructor variables, and Constructor
	//
	//--------------------------------------------------------------------------
	
	/// Folder structure and meta data
	protected MetaTable folders = null;
	
	/// Actual data files
	protected MetaTable files = null;
	
	/// Constructor with folder, and file table
	///
	/// @param Folder table
	/// @param File table
	public VirtualFileSystem(MetaTable inFolder, MetaTable inFile) {
		folders = inFolder;
		files = inFile;
	}
	
	//--------------------------------------------------------------------------
	//
	// Internal helper functions
	//
	//--------------------------------------------------------------------------
	
	/// Normalize a path string (reduce amount of edge cases error)
	/// and return its split path array format
	///
	/// @param  String of file path to normalize and split
	///
	/// @return The splitted string path to iterate later
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
			throw new RuntimeException("Unable to create folder for NULL path");
		}
		return splitPath;
	}
	
	/// Fetch the metaobject given the foldername and parent object
	/// if it exists, else return null
	///
	/// @param   The parent GUID, null defaults to ROOT
	/// @param   The folder name as a path step
	protected MetaObject getFolderMetaObject(String parentID, String folderName) {
		// Normalize the parent ID
		if (parentID == null) {
			parentID = "ROOT";
		}
		
		//checking if the folder already exists
		MetaObject[] folderList = folders.query("parentID = ? AND name = ?", new String[] { parentID,
			folderName });
		
		// Gets the folder meta object
		if (folderList.length > 0) {
			return folderList[0];
		}
		return null;
	}
	
	protected MetaObject getNestedFolderMetaObject(String[] splitPath) {
		MetaObject folderStep = null;
		
		for (int i = 0; i < splitPath.length; ++i) {
			
			// if( parentFolder != null ) {}
			//    parentID = parentFolder._oid();
			// } else {
			// 	  parentID = null;
			// }
			String parentID = (folderStep != null) ? folderStep._oid() : null;
			
			// Gets the next folder nested
			folderStep = getFolderMetaObject(parentID, splitPath[i]);
			//Failed to get folder
			if (folderStep == null) {
				return null;
			}
			
		}
		return folderStep;
	}
	
	/// Fetch the metaobject given the foldername and parent object
	/// if it does not exists, create, save it, and return it
	///
	/// @param   The parent GUID, null defaults to ROOT
	/// @param   The folder name as a path step
	protected MetaObject ensureFolderMetaObject(String parentID, String folderName) {
		// Normalize the parent ID
		if (parentID == null) {
			parentID = "ROOT";
		}
		
		// Get and return existing result
		MetaObject result = getFolderMetaObject(parentID, folderName);
		if (result != null) {
			return result;
		}
		
		// Time to create the folder, save it, return it
		result = folders.newObject();
		result.put("name", folderName);
		result.put("parentID", parentID);
		result.saveDelta();
		return result;
	}
	
	protected MetaObject ensureFileMetaObject(String parentID, String fileName) {
		
		//normalize the parent ID
		if (parentID == null) {
			parentID = "ROOT";
		}
		
		MetaObject result = null;
		
		/// return the file if it already exists
		// MetaObject result = getFileMetaObject(parentID, fileName, fileContent);
		// if(result != null) {
		// 	return result;
		// }
		
		//Time to create a new file , save it , return it
		result = files.newObject();
		result.put("parentID", parentID);
		result.put("name", fileName);
		//result.put("content", fileContent);
		result.saveDelta();
		return result;
	}
	
	protected MetaObject getFileMetaObject(String fileName, byte[] fileContent) {
		
		//checking if the file already exists
		MetaObject[] fileList = files.query("name = ?", new String[] { fileName });
		
		// Gets the file meta object
		if (fileList.length > 0) {
			return fileList[0];
			
		}
		return null;
	}
	
	//--------------------------------------------------------------------------
	//
	// Public functions
	//
	//--------------------------------------------------------------------------
	
	/**
	 * + List
	 * + Add File / Folder
	 * + Move 
	 * + Rename
	 * + Delete
	 * + Update file value
	 */
	
	/// get a list of folders
	public String[] listFolderNames(String path) {
		String[] splitPath = normalizeToSplitPath(path);
		
		// For this case you will need to find the folder, then put inside the folder
		if (splitPath.length >= 1) {
			throw new UnsupportedOperationException("@TODO");
		}
		
		// Getting the list of folders in root
		MetaObject[] listOfFolders = folders.query("parentID = ?", new String[] { "ROOT" });
		String[] result = new String[listOfFolders.length];
		
		// Getting the list of folder names
		for (int i = 0; i < listOfFolders.length; ++i) {
			result[i] = listOfFolders[i].getString("name");
		}
		return result;
	}
	
	///check if the folder already exists
	protected boolean hasFolder(String[] splitPath) {
		return getNestedFolderMetaObject(splitPath) != null;
	}
	
	///check if the folder already exists
	public boolean hasFolder(String path) {
		return hasFolder(normalizeToSplitPath(path));
	}
	
	///Create folder
	public boolean createFolder(String path) {
		String[] splitPath = normalizeToSplitPath_withBlankCheck(path);
		
		// Current parent MetaObject
		MetaObject parentFolder = null;
		
		// Find each folder step by step, and make them if needed
		// if (splitPath.length > 1) {
		for (int i = 0; i < splitPath.length; ++i) {
			
			// if( parentFolder != null ) {}
			//    parentID = parentFolder._oid();
			// } else {
			// 	  parentID = null;
			// }
			String parentID = (parentFolder != null) ? parentFolder._oid() : null;
			parentFolder = ensureFolderMetaObject(parentID, splitPath[i]);
		}
		return true;
	}
	
	///create file with content
	public boolean saveFile(String path, byte[] fileContent) {
		String[] splitPath = normalizeToSplitPath_withBlankCheck(path);
		//current file MetaObject
		MetaObject file = null;
		
		for (int i = 0; i < splitPath.length; i++) {
			String parentID = null;
			file = ensureFileMetaObject(parentID, splitPath[i]);
			file.put("data", fileContent);
			file.saveDelta();
		}
		
		return true;
	}
	
	///get file with content
	public byte[] getFile(String path) {
		String[] splitPath = normalizeToSplitPath_withBlankCheck(path);
		//current file metaObject
		MetaObject file = null;
		
		for (int i = 0; i < splitPath.length; i++) {
			//file = getFileMetaObject(splitPath[i], fileContent);
			//file = files.query("name = ?", new String[] { fileName });
		}
		if (file != null) {
			return (byte[]) file.get("data");
		}
		return null;
	}
	
	///check if a file already exists
	// public boolean hasFile(String parentID, String fileName) {
	// 	return getFileMetaObject(parentID, fileName) != null;
	// }
	
	// public boolean createFileContent( String parentID , String fileName , String fileContentName) {
	
	// //	String parentID = "ROOT";
	// //	String fileName = "firstFile";
	
	// 	//check for the filename to exists
	//  MetaObject result = getFileMetaObject(parentID, fileName , fileContent);
	// 	if(result == null) {
	// 		 addFileContent(parentID, fileName, fileContentName);
	// 	}
	// 	return true;
	// }
	
	// public boolean hasFileContent(String parentID , String fileName , String fileContentName) {
	// 	return getFileContent(parentID,fileName,fileContentName) != null;
	// }
}
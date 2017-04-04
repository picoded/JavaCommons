package picoded.dstack.module;

import picoded.dstack.*;
import picoded.file.FileUtil;
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
		// and a few other things (See: https://commons.apache.org/proper/commons-io/javadocs/api-2.5/org/apache/commons/io/FilenameUtils.html#normalize(java.lang.String))
		String normalized = path.trim();
		normalized = FileUtil.normalize(path);

		// This is blank
		if(normalized.length() <= 0) {
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
		MetaObject[] listOfFolders = folders.query("parentID = ?", new String[] { "ROOT" } );
		String[] result = new String[ listOfFolders.length ];

		// Getting the list of folder names
		for(int i=0; i<listOfFolders.length; ++i) {
			result[i] = listOfFolders[i].getString("name");
		}
		return result;
	}

	protected boolean hasFolder(String[] splitPath) {
		// For this case you will need to find the folder, and check nested
		if (splitPath.length > 1) {
			throw new UnsupportedOperationException("@TODO");
		}

		//checking if the folder already exists
		MetaObject[] folderList = folders.query("parentID = ? AND name = ?", new String[] { "ROOT" , splitPath[0] } );
		
		//geting the folder name
		return folderList.length > 0;
	}

	///check if the folder already exists
	public boolean hasFolder(String path) {
		return hasFolder( normalizeToSplitPath(path) );
	}
	
	/// create folder
	public boolean createFolder(String path) {
		return createFolder(path, false);
	}
	
	public boolean createFolder(String path, boolean strict) {
		String[] splitPath = normalizeToSplitPath_withBlankCheck(path);
		
		// For this case you will need to find the folder, then put inside the folder
		if (splitPath.length > 1) {
			throw new UnsupportedOperationException("@TODO");
		}
		
		///Folder already exists checks
		 if( hasFolder(splitPath) ) {
			 throw new RuntimeException("Folder already exists");
		 }
		
		// Time to create the folder
		String folderName = splitPath[0];
		MetaObject newFolder = folders.newObject();
		newFolder.put("name", folderName);
		newFolder.put("parentID", "ROOT");
		newFolder.saveDelta();
		
		return true;
	}
	
	//  ///rename a folader
	//  public String renameFolder(String name) {
	// 	 return new String(name) ;
	//  }
	
	//  ///move folders
	//  public boolean moveFolder(String path) {
	// 	 return true;
	//  }
	
	//  ///delete folders
	//  public boolean deleteFolders(String path) {
	// 	 return true;
	//  }
}
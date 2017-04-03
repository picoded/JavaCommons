package picoded.dstack.module;

import picoded.dstack.*;
import picoded.file.FileUtil;
import sun.rmi.runtime.NewThreadAction;
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
		if(path == null) {
			return ArrayConv.EMPTY_STRING_ARRAY;
		}
		String normalized = FileUtil.normalize(path);
		return normalized.split("/");
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
		 return new String[] {};
	 }
     
	
     
	//  /// get a folder by name
	//  public boolean getFolderByName(String name) {
	// 	 return true;
	//  }

	  /// create folder
	 public boolean createFolder(String path) {
		return createFolder(path, false);
	 }
	 public boolean createFolder(String path, boolean strict) {
		 String[] splitPath = normalizeToSplitPath(path);
		 if(splitPath.length == 0) {
			 throw new RuntimeException("Unable to create folder for NULL path");
		 }

		 // For this case you will need to find the folder, then put inside the folder
		 if(splitPath.length > 1) {
			 throw new UnsupportedOperationException("@TODO");
		 }

		 // Folder already exists checks
		//  if( hasFolder(splitPath) ) {
		// 	 if( strict ) {
		// 		 return true;
		// 	 }
		// 	 throw new RuntimeException("Folder already exists");
		//  }

		 // Time to create the folder



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
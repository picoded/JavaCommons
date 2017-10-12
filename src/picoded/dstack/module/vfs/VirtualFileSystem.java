package picoded.dstack.module.vfs;

import picoded.dstack.*;
import picoded.util.file.FileUtil;

import java.util.*;

import picoded.core.conv.*;

/**
 * VirtualFileSystem using DataTable implmentation
 * This replicates most of the file system requirements of Uilicious workspace
 **/
public class VirtualFileSystem extends VirtualFileObject {
	
	//--------------------------------------------------------------------------
	//
	// Constructor variables, and Constructor
	//
	//--------------------------------------------------------------------------
	
	/**
	 * Directory structure and meta data
	 **/
	protected DataTable directories = null;
	
	/**
	 * Actual data files
	 **/
	protected DataTable files = null;
	
	/**
	 * Constructor with Directory, and file table
	 *
	 * @param Directory table
	 * @param File table
	 **/
	public VirtualFileSystem(DataTable inDirectory, DataTable inFile) {
		super();
		
		// The meta table involved
		directories = inDirectory;
		files = inFile;
		
		// Setting up root VirtualFileObject1
		type = "ROOT";
		vfs = this;
	}
	//	/**
	// * Gets and return the VirtualFileObject if either Directory / file
	// * With the respective name inside the parent ID exists.
	// *
	// * @param   The parent Directory ID (can be NULL / ROOT)
	// * @param   The sub path name to fetch
	// **/
	// protected VirtualFileObject getVirtualFileObject(String parentID, String pathName) {
	// 	// Normalize the parent ID
	// 	if (parentID == null) {
	// 		parentID = "ROOT";
	// 	}
	//
	// 	// The meta object to fetch
	// 	DataObject mObj = null;
	//
	// 	mObj = getDirectoryDataObject(parentID, pathName);
	// 	if( mObj != null ) {
	// 		return new VirtualFileObject(this, mObj, "DIRECTORY");
	// 	}
	//
	// 	mObj = getFileDataObject(parentID, pathName);
	// 	if ( mObj != null ) {
	// 		return new VirtualFileObject(this, mObj, "FILE");
	// 	}
	//
	// 	// Neither file nor Directory
	// 	return null;
	// }
	//
	// //--------------------------------------------------------------------------
	// //
	// // VirtualFileObject internal commands
	// //
	// //--------------------------------------------------------------------------
	//	/**
	// * Get and return the VirtualFileObject if it exists
	// *
	// * @param   The full path name to fetch
	// *
	// * @return  The VirtualFileObject representing either a file / Directory if found
	// *          Otherwise it will return null
	// **/
	// public VirtualFileObject getVirtualFileObject(String pathName) {
	// 	// Path parts normalized
	// 	String[] splitPath = normalizeToSplitPath_withBlankCheck(pathName);
	//
	// 	// Let us implment for ROOT only for now
	// 	return getVirtualFileObject(null, splitPath[0]);
	// }
	
}

package picoded.webTemplateEngines.JSML;

import java.io.*;
import java.util.*;

import picoded.struct.*;
import picoded.servlet.*;
import picoded.servletUtils.*;
import picoded.fileUtils.PDFGenerator;
import picoded.webTemplateEngines.FormGenerator.*;

///
/// JSML Form set, which handles all the various utility and servlet functionality
///
public class JSMLFormSet extends UnsupportedDefaultMap<String, JSMLForm> {
	
	///////////////////////////////////////////////////////
	//
	// Instance variables 
	//
	///////////////////////////////////////////////////////
	
	/// The form set folder
	protected File formSetFolder = null;
	
	/// The form set URI base
	protected String formSetURI = null;
	
	///////////////////////////////////////////////////////
	//
	// Constructor
	//
	///////////////////////////////////////////////////////
	
	/// Blank constructor (will crash if not configured properly)
	public JSMLFormSet() {
		//Does nothing
	}
	
	/// Constructor with formSetFolder, and formSetURI
	public JSMLFormSet(File folderPath, String folderUri) {
		formSetFolder = forlderPath;
		formSetURI = folderUri;
		// Validate
		validateFormSetFolder();
	}
	
	/// Constructor with formSetFolder string path, and formSetURI 
	public JSMLFormSet(String folderPath, String folderUri) {
		formSetFolder = new File(folderPath);
		formSetURI = folderUri;
		// Validate
		validateFormSetFolder();
	}
	
	/// Utility function called by init, or paramtirized startup. To validate basePath
	public void validateFormSetFolder() {
		// Validate base path.
		if(formSetFolder == null) {
			throw new RuntimeException("JSMLFormSet init param 'formSetFolder' is required.");
		}
		
		if (!formSetFolder.exists()) {
			throw new RuntimeException(
				"JSMLFormSet init param 'formSetFolder' (" + 
				formSetFolder.toString() + ") does actually not exist in file system."
			);
		}
		
		if (!formSetFolder.isDirectory()) {
			throw new RuntimeException(
				"JSMLFormSet init param 'formSetFolder' value (" + 
				formSetFolder.toString() + ") is actually not a directory in file system."
			);
		} 
		
		if (!formSetFolder.canRead()) {
			throw new RuntimeException(
				"JSMLFormSet init param 'formSetFolder' value (" + 
				formSetFolder.toString() + ") is actually not readable in file system."
			);
		}
		
		// Use provided file, to extract filepath
		if( basePath == null ) {
			basePath = formSetFolder.toString();
		}
	}
	
	///////////////////////////////////////////////////////
	//
	// Utility functions
	//
	///////////////////////////////////////////////////////
	
	protected List<File> foldersInFormSet() {
		if( formSetFolder == null ) {
			validateFormSetFolder();
		}
		List<File> ret = new ArrayList<File>();
		
		for(File entry : formSetFolder.listFiles()) {
			if( entry.getName().startsWith('.') ) {
				continue; //skip private folders
			}
			
			if( entry.isDirectory() ) {
				ret.add(entry);
			}
		}
		return ret;
	}
	
	/// Cache for nameList
	protected _folderNameListCache = null;
	
	/// Returns the list of names, found in the set. This is equivalent to .keySet()
	public List<String> nameList() {
		if(_folderNameListCache != null) {
			return _folderNameListCache;
		}
		
		List<String> ret = new ArrayList<String>();
		
		for(File entry : foldersInFormSet()) {
			return entry.getName();
		}
		
		return (_folderNameListCache = ret);
	}
	
	///////////////////////////////////////////////////////
	//
	// Map operation functions
	//
	///////////////////////////////////////////////////////
	
	/// Returns the set of JSMLForm names
	public Set<String> keySet() {
		return new HashSet<String>(nameList());
	}
	
	/// Gets the JSMLForm 
	public JSMLForm get(Object key) {
		if( !(nameList().contains(key)) ) {
			return null;
		}
		String keyStr = key.toString();
		new JSMLForm( new File(formSetFolder, keyStr), formSetURI+keyStr, new File(formSetFolder, keyStr+"/tmp") );
	}
	
	///////////////////////////////////////////////////////
	//
	// Servlet Utility functions
	//
	///////////////////////////////////////////////////////
	
	/// Returns the File servlet
	public FileServer resourseFileServlet() {
		if( formSetFolder == null ) {
			validateFormSetFolder();
		}
		
		return (new FileServlet(formSetFolder));
	}
	
	/// Process the file serving request for resoruces
	public void processResourcesFileRequest( //
		HttpServletRequest servletRequest, //
		HttpServletResponse servletResponse, //
		boolean headersOnly //
	) {
		resourseFileServlet(servletRequest, servletResponse, headersOnly);
	}
}

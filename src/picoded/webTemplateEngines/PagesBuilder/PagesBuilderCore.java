package picoded.webTemplateEngines.PagesBuilder;

import java.io.*;
import java.util.*;

import picoded.enums.*;
import picoded.conv.*;
import picoded.struct.*;
import picoded.fileUtils.*;
import picoded.servlet.*;
import picoded.servletUtils.*;

///
/// Core class that handle the conversion and copying process.
///
/// NOT the folder iteration
///
/// @TODO
/// + Minify the rawPageName/index.html
///
public class PagesBuilderCore {
	
	////////////////////////////////////////////////////////////
	//
	// Local variables
	//
	////////////////////////////////////////////////////////////
	
	/// The folder to get the various page definition from
	public File pagesFolder = null;
	
	/// The output folder to process the output to (if given)
	public File outputFolder = null;
	
	/// The local JMTE reference
	public JMTE jmteObj = null;
	
	/// The URI root context for built files
	public String uriRootPrefix = "./";
	
	/// LESS compiler
	protected LessToCss less = new LessToCss();
	
	////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////
	
	///
	/// Constructor, with the folders defined
	///
	/// @param The various pages definition folder
	///
	public PagesBuilderCore(File inPagesFolder) {
		pagesFolder = inPagesFolder;
	}
	
	///
	/// Constructor, with the folders defined
	///
	/// @param The various pages definition folder
	///
	public PagesBuilderCore(String inPagesFolder) {
		this(new File(inPagesFolder));
	}
	
	///
	/// Constructor, with the folders defined
	///
	/// @param The various pages definition folder
	/// @param The target folder to build the result into
	///
	public PagesBuilderCore(File inPagesFolder, File inOutputFolder) {
		pagesFolder = inPagesFolder;
		outputFolder = inOutputFolder;
	}
	
	///
	/// Constructor, with the folders defined
	///
	/// @param The various pages definition folder
	/// @param The target folder to build the result into
	///
	public PagesBuilderCore(String inPagesFolder, String inOutputFolder) {
		this(new File(inPagesFolder), new File(inOutputFolder));
	}
	
	////////////////////////////////////////////////////////////
	//
	// Public vars access
	//
	////////////////////////////////////////////////////////////
	
	/// @returns Gets the protected JMTE object, used internally.
	///          This is autocreated if not set
	public JMTE getJMTE() {
		if (jmteObj == null) {
			jmteObj = new JMTE();
		}
		return jmteObj;
	}
	
	/// Overides the default (if loaded) JMTE object.
	public void setJMTE(JMTE set) {
		jmteObj = set;
	}
	
	/// @returns Gets the protected uriRootPrefix, used internally
	public String getUriRootPrefix() {
		return uriRootPrefix;
	}
	
	/// Overides the uriRootPrefix. 
	public void setUriRootPrefix(String set) {
		if (set == null || set.length() <= 0) {
			set = "/";
		}
		
		if (!set.endsWith("/")) {
			set = set + "/";
		}
		
		uriRootPrefix = set;
	}
	
	////////////////////////////////////////////////////////////
	//
	// Utility functions
	//
	////////////////////////////////////////////////////////////
	
	/// Utility to get path safe rawPageName
	///
	/// @param  rawPageName used to generate the vars
	///
	/// @returns  The rawPageName which is subpath safe
	protected String safePageName(String rawPageName) {
		if (rawPageName.endsWith("/")) {
			rawPageName = rawPageName.substring(0, rawPageName.length() - 1);
		}
		return rawPageName.replaceAll("/", "_");
	}
	
	/// Utility to get page frame ID
	///
	/// @param  rawPageName used to generate the vars
	///
	/// @returns  The page frame ID used (that is character safe?)
	protected String pageFrameID(String rawPageName) {
		return "pageFrame_" + safePageName(rawPageName);
	}
	
	/// Generates the needed map string template for the respective page
	///
	/// @param  rawPageName used to generate the vars
	///
	/// @returns The data map used inside JMTE
	protected Map<String, Object> pageJMTEvars(String rawPageName) {
		HashMap<String, Object> ret = new HashMap<String, Object>();
		
		// Initialize to root if not previously set
		if (uriRootPrefix == null) {
			uriRootPrefix = "./";
		}
		
		// Removes trailing /, unless its the only character
		if (uriRootPrefix.length() > 1 && uriRootPrefix.endsWith("/")) {
			uriRootPrefix = uriRootPrefix.substring(0, uriRootPrefix.length() - 1);
		} /*else {
		  uriRootPrefix = "";
		  }*/
		
		//
		String pageURI = uriRootPrefix + "/" + rawPageName;
		while (pageURI.startsWith("//")) {
			pageURI = pageURI.substring(1);
		}
		
		ret.put("PagesRootURI", uriRootPrefix);
		ret.put("PageURI", pageURI);
		ret.put("PageNameRaw", rawPageName);
		ret.put("PageName", safePageName(rawPageName));
		ret.put("PageFrameID", pageFrameID(rawPageName));
		
		return ret;
	}
	
	///
	/// Gets the requested page prefix / suffix from the following priority order
	///
	/// 1) The page path itself
	/// 2) The common folder
	/// 3) The index folder (legacy support, do not use)
	///
	protected String getCommonPrefixOrSuffixHtml(String rawPageName, String fixType) {
		return getCommonFile(rawPageName, fixType + ".html");
	}
	
	protected String getCommonFile(String rawPageName, String fileName) {
		// Get from the rawPageName folder itself (v2)
		String res = FileUtils.readFileToString_withFallback(new File(pagesFolder, rawPageName + "/" + fileName),
			"UTF-8", null);
		
		// Get from the common folder (v2)
		if (res == null) {
			res = FileUtils.readFileToString_withFallback(new File(pagesFolder, "common/" + fileName), "UTF-8", null);
		}
		
		// Legacy support (v1) get from index folder
		if (res == null) {
			res = FileUtils.readFileToString_withFallback(new File(pagesFolder, "index/" + fileName), "UTF-8", null);
		}
		
		// Fallbacks to blank
		if (res == null) {
			return "";
		}
		return res;
	}
	
	////////////////////////////////////////////////////////////
	//
	// HTML handling
	//
	////////////////////////////////////////////////////////////
	
	/// Gets the prefix
	public String prefixHTML(String rawPageName) {
		return getJMTE().parseTemplate(getCommonPrefixOrSuffixHtml(rawPageName, "prefix"), pageJMTEvars(rawPageName));
	}
	
	/// Gets the prefix
	public String suffixHTML(String rawPageName) {
		return getJMTE().parseTemplate(getCommonPrefixOrSuffixHtml(rawPageName, "suffix"), pageJMTEvars(rawPageName));
	}
	
	/// Gets and returns a page frame string, with its respective JMTE input vars?
	public String buildPageInnerHTML(String rawPageName, Map<String, Object> jmteTemplate) {
		String indexFileStr = FileUtils.readFileToString_withFallback(new File(pagesFolder, rawPageName + "/"
			+ safePageName(rawPageName) + ".html"), "UTF-8", null);
		if (indexFileStr == null) {
			indexFileStr = FileUtils.readFileToString_withFallback(new File(pagesFolder, rawPageName + "/index.html"),
				"UTF-8", "");
		}
		
		if ((indexFileStr = indexFileStr.trim()).length() == 0) {
			if (hasPageFile(rawPageName)) { //has file
				return ""; //this is a blank HTML file
			}
			return null;
		}
		
		return getJMTE().parseTemplate(indexFileStr.toString(), jmteTemplate);
	}
	
	/// Get the page frame div header, this is used to do a "search replace" for script / css injection
	protected String pageFrameHeaderDiv(String rawPageName) {
		return "<div class='pageFrame " + pageFrameID(rawPageName) + "' id='" + pageFrameID(rawPageName) + "'>\n";
	}
	
	/// Builds a rawPageName HTML frame
	public String buildPageFrame(String rawPageName) {
		return buildPageFrame(rawPageName, null);
	}
	
	/// Builds a rawPageName HTML frame
	public String buildPageFrame(String rawPageName, String injectionStr) {
		String innerHTML = buildPageInnerHTML(rawPageName, pageJMTEvars(rawPageName));
		if (innerHTML == null) {
			return null;
		}
		
		StringBuilder frame = new StringBuilder();
		frame.append(pageFrameHeaderDiv(rawPageName));
		if (injectionStr != null) {
			frame.append(injectionStr);
		}
		
		frame.append(innerHTML);
		frame.append("\n</div>\n");
		return frame.toString();
	}
	
	/// Builds the FULL rawPageName HTML, with prefix and suffix
	public StringBuilder buildFullPageFrame(String rawPageName) {
		return buildFullPageFrame(rawPageName, null);
	}
	
	/// Builds the FULL rawPageName HTML, with prefix and suffix
	public StringBuilder buildFullPageFrame(String rawPageName, String injectionStr) {
		String frameHTML = buildPageFrame(rawPageName, injectionStr);
		if (frameHTML != null) {
			StringBuilder ret = new StringBuilder();
			ret.append(prefixHTML(rawPageName));
			ret.append(frameHTML);
			ret.append(suffixHTML(rawPageName));
			return ret;
		}
		return null;
	}
	
	////////////////////////////////////////////////////////////
	//
	// Page Building
	//
	////////////////////////////////////////////////////////////
	
	/// Enum of file types that is processed
	protected enum PageFileType {
		js, jsons, jsons_to_js, less, less_to_css, html
	}
	
	/// Indicate if the page definition FOLDER exists
	///
	/// @param PageName to build
	///
	/// @return  boolean true if pageName has folder
	public boolean hasPageFolder(String pageName) {
		try {
			File definitionFolder = new File(pagesFolder, pageName);
			return definitionFolder.exists() && definitionFolder.isDirectory();
		} catch (Exception e) {
			// Failed?
		}
		return false;
	}
	
	/// Indicates if the page definition FILE exists
	///
	/// @param PageName to build
	///
	/// @return  boolean true if pageName is a folder
	public boolean hasPageFile(String pageName) {
		try {
			File definitionFolder = new File(pagesFolder, pageName);
			return ((new File(definitionFolder, "index.html")).exists() || (new File(definitionFolder,
				safePageName(pageName) + ".html")).exists());
		} catch (Exception e) {
			// Failed?
		}
		return false;
	}
	
	/// Process the file, according to its type, and outputs it into the respective file
	///
	/// @param  filetype enum
	/// @param  Source file (returns false if not exists)
	/// @param  Target file (written if source file exists)
	/// @param  The page name used, used in certain type logic
	/// @param  The JMTE variable map to use
	///
	/// @return true, if a file was processed and written
	public boolean processPageFile(PageFileType type, File input, File output, String rawPageName,
		Map<String, Object> jmteVarMap) throws IOException {
		if (input.exists() && input.isFile() && input.canRead()) {
			// Gets its string value, and process only if not blank
			String fileVal = FileUtils.readFileToString(input);
			if ((fileVal = fileVal.trim()).length() > 0) {
				
				// Does a JMTE filter
				fileVal = getJMTE().parseTemplate(fileVal, jmteVarMap);
				
				// Does specific conversions
				if (type == PageFileType.jsons_to_js) {
					// Adds the script object wrapper
					fileVal = "window.pageFrames = window.pageFrames || {}; window.pageFrames." + safePageName(rawPageName)
						+ " = (" + fileVal + ");";
				} else if (type == PageFileType.less_to_css) {
					
					/// Add the config .less file
					String lessPrefix = getCommonFile(rawPageName, "prefix.less");
					String lessSuffix = getCommonFile(rawPageName, "suffix.less");
					
					/// Does an outer wrap, if its not index page (which applies style to 'all')
					if (!rawPageName.equalsIgnoreCase("index") && !rawPageName.equalsIgnoreCase("common")) {
						fileVal = "." + pageFrameID(rawPageName) + " { \n" + fileVal + "\n } \n";
					}
					
					// Ensure prefix, and suffix are added
					fileVal = (lessPrefix + "\n" + fileVal + "\n" + lessSuffix).trim();
					
					// Less to css conversion
					fileVal = less.compile(fileVal);
				}
				
				// Write to file if it differ
				FileUtils.writeStringToFile_ifDifferant(output, "UTF-8", fileVal);
				
				// Indicate file is "deployed"
				return true;
			}
		}
		return false;
	}
	
	/// Varient of processPageFile, where it iterates an array set of input files till a valid file is found
	///
	/// @param  filetype enum
	/// @param  Source file (returns false if not exists)
	/// @param  Target file (written if source file exists)
	/// @param  The page name used, used in certain type logic
	/// @param  The JMTE variable map to use
	///
	/// @return true, if a file was processed and written
	public boolean processPageFile(PageFileType type, File[] inputArr, File output, String rawPageName,
		Map<String, Object> jmteVarMap) throws IOException {
		for (File input : inputArr) {
			if (processPageFile(type, input, output, rawPageName, jmteVarMap)) {
				return true;
			}
		}
		return false;
	}
	
	///
	/// Builds all the assets for a single page
	///
	/// @param rawPageName to build
	///
	/// @return boolean true, if page had content to be built
	///
	public boolean buildAndOutputPage(String rawPageName) {
		
		// rawPageName here assumes NO "/" suffix
		if (rawPageName.endsWith("/")) {
			rawPageName = rawPageName.substring(0, rawPageName.length() - 1);
		}
		
		// System.out allowed here, because LESS does a system out ANYWAY.
		// Help to make more "sense" of the done output
		System.out.print("> PageBuilder[Core].buildPage(\'" + rawPageName + "\'): ");
		
		// Output folder validitiy check
		if (outputFolder == null) {
			throw new RuntimeException("Missing output folder, unable to generate : " + rawPageName);
		}
		
		// Future extension, possible loop hole abuse. Im protecting against it early
		if (rawPageName.startsWith(".")) {
			throw new RuntimeException("Unable to load page name, starting with '.' : " + rawPageName);
		}
		if (rawPageName.indexOf("..") >= 0) {
			throw new RuntimeException("Unable to load page name, containing '..' : " + rawPageName);
		}
		if (rawPageName.toLowerCase().indexOf("web-inf") >= 0) {
			throw new RuntimeException("Unable to load page name, that may bypass WEB-INF : " + rawPageName);
		}
		
		try {
			
			// Prepares output and definition FILE objects, and JMTE map
			//-------------------------------------------------------------------
			File outputPageFolder = new File(outputFolder, rawPageName);
			File definitionFolder = new File(pagesFolder, rawPageName);
			Map<String, Object> jmteVarMap = pageJMTEvars(rawPageName);
			String pageName_safe = safePageName(rawPageName);
			
			// Create the output folder as needed
			//-------------------------------------------------------------------
			if (!outputPageFolder.exists()) {
				outputPageFolder.mkdirs();
			}
			
			// Copy the page assets folder
			//
			// @TODO: Optimize this to only copy IF newer
			//-------------------------------------------------------------------
			
			// Folder to copy from
			boolean hasAssets = false;
			File pageAssetsFolder = new File(definitionFolder, "assets");
			if (pageAssetsFolder.exists() && pageAssetsFolder.isDirectory()) {
				// Copy if folder to target
				FileUtils.copyDirectory(pageAssetsFolder, new File(outputPageFolder, "assets"));
				hasAssets = true;
			}
			
			// Process the JS script (if provided)
			//-------------------------------------------------------------------
			boolean hasJsFile = processPageFile(PageFileType.js, new File[] { new File(definitionFolder, "index.js"),
				new File(definitionFolder, pageName_safe + ".js") }, new File(outputPageFolder, pageName_safe + ".js"),
				rawPageName, jmteVarMap);
			
			// Build the JSONS script (if provided)
			//-------------------------------------------------------------------
			boolean hasJsonsFile = processPageFile(PageFileType.jsons_to_js, new File[] {
				new File(definitionFolder, "index.jsons"), new File(definitionFolder, pageName_safe + ".jsons") },
				new File(outputPageFolder, pageName_safe + ".jsons.js"), rawPageName, jmteVarMap);
			
			// Build the LESS script (if provided)
			//-------------------------------------------------------------------
			boolean hasLessFile = processPageFile(PageFileType.less_to_css, new File[] {
				new File(definitionFolder, "index.less"), new File(definitionFolder, pageName_safe + ".less") }, new File(
				outputPageFolder, pageName_safe + ".css"), rawPageName, jmteVarMap);
			
			// Build the html page
			//-------------------------------------------------------------------
			
			// The HTML output (if valid)
			StringBuilder indexStrBuilder = buildFullPageFrame(rawPageName);
			if (indexStrBuilder == null) {
				return hasAssets || hasJsFile || hasJsonsFile || hasLessFile;
			}
			String indexStr = indexStrBuilder.toString();
			
			// Build the injector code for this page (before </head>)
			//-------------------------------------------------------------------
			StringBuilder injectorStrBuilder = new StringBuilder();
			
			if (hasLessFile) {
				if (indexStr.indexOf(rawPageName + "/" + pageName_safe + ".css") > 0) {
					// Skips injection if already included
				} else {
					injectorStrBuilder.append("<link rel='stylesheet' type='text/css' href='" + uriRootPrefix + "/"
						+ rawPageName + "/" + pageName_safe + ".css'></link>\n");
				}
			}
			if (hasJsFile) {
				if (indexStr.indexOf(rawPageName + "/" + pageName_safe + ".js") > 0) {
					// Skips injection if already included
				} else {
					injectorStrBuilder.append("<script src='" + uriRootPrefix + "/" + rawPageName + "/" + pageName_safe
						+ ".js'></script>\n");
				}
			}
			if (hasJsonsFile) {
				if (indexStr.indexOf(rawPageName + "/" + pageName_safe + ".jsons.js") > 0) {
					// Skips injection if already included
				} else {
					injectorStrBuilder.append("<script src='" + uriRootPrefix + "/" + rawPageName + "/" + pageName_safe
						+ ".jsons.js'></script>\n");
				}
			}
			
			String injectorStr = injectorStrBuilder.toString();
			
			// Ammend the HTML output
			//-------------------------------------------------------------------
			
			// Apply injector code if any
			if (injectorStr.length() > 0) {
				// Rebuild with injection
				indexStr = buildFullPageFrame(rawPageName, injectorStr).toString();
			}
			
			// HTML minify
			//-------------------------------------------------------------------
			
			// Apply a simplistic compression (so avoid inline JS with line comments for nuts)
			//
			// @TODO: A proper minifier library integration, like:
			// https://code.google.com/p/htmlcompressor
			// indexStr = indexStr.trim().replaceAll("\\s+", " ");
			// indexStr = indexStr.trim().replaceAll("\\>\\s\\<", "><");
			
			// Write out to file
			//-------------------------------------------------------------------
			
			// Write to file if it differ
			FileUtils.writeStringToFile_ifDifferant(new File(outputPageFolder, "index.html"), "UTF-8", indexStr);
			
			// Returns success
			return true;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
		// End and returns failure
		//-------------------------------------------------------------------
		// return false;
	}
	
	///
	/// Builds all pages (NOT including itself) inside a page folder
	///
	/// @param rawPageName to build
	///
	/// @return boolean true, if page had content to be built
	///
	public boolean buildPageFolder(String rawPageName) {
		boolean res = false;
		if (rawPageName == null || rawPageName.equalsIgnoreCase("/")) {
			rawPageName = "";
		}
		
		// The current folder to scan
		File folder = new File(pagesFolder, rawPageName);
		
		// Possible page pathing error fix
		if (rawPageName.length() > 0 && !rawPageName.endsWith("/")) {
			rawPageName = rawPageName + "/";
		}
		
		// Scan for subdirectories ONLY if this is a directory
		if (folder.isDirectory()) {
			// For each sub directory, build it as a page
			for (File pageDefine : FileUtils.listDirs(folder)) {
				// Build each page
				String subPageName = pageDefine.getName();
				
				// Scan for sub pages
				if (subPageName.equalsIgnoreCase("assets") || subPageName.equalsIgnoreCase("common")
					|| subPageName.equalsIgnoreCase("index") || subPageName.equalsIgnoreCase("web-inf")) {
					// ignoring certain reserved folders
				} else {
					// Build the page
					buildAndOutputPage(rawPageName + subPageName);
					// Recursive iterate
					res = buildPageFolder(rawPageName + subPageName + "/") || true;
				}
			}
		}
		
		return res;
	}
	
	///
	/// Builds all pages (INCLUDING itself, if possible) inside a page folder
	///
	/// @param rawPageName to build
	///
	/// @return boolean true, if page had content to be built
	///
	public boolean buildPageFolder_includingSelf(String rawPageName) {
		boolean res = false;
		if (hasPageFile(rawPageName)) {
			res = buildAndOutputPage(rawPageName) || res;
		}
		return buildPageFolder(rawPageName) || res;
	}
}

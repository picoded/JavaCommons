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
/// + Minify the pagename/index.html
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
	
	/// Utility to get path safe pageName
	///
	/// @param  pageName used to generate the vars
	///
	/// @returns  The pageName which is subpath safe
	protected String safePageName(String pageName) {
		return pageName.replaceAll("/","_");
	}
	
	/// Utility to get page frame ID
	///
	/// @param  pageName used to generate the vars
	///
	/// @returns  The page frame ID used (that is character safe?)
	protected String pageFrameID(String pageName) {
		return "pageFrame_"+safePageName(pageName);
	}
	
	/// Generates the needed map string template for the respective page
	///
	/// @param  pageName used to generate the vars
	///
	/// @returns The data map used inside JMTE
	protected Map<String, Object> pageJMTEvars(String pageName) {
		HashMap<String, Object> ret = new HashMap<String, Object>();
		
		// Initialize to root if not previously set
		if(uriRootPrefix == null) {
			uriRootPrefix = "./";
		}
		
		// Removes trailing /, unless its the only character
		if (uriRootPrefix.length() > 1 && uriRootPrefix.endsWith("/")) {
			uriRootPrefix = uriRootPrefix.substring(0, uriRootPrefix.length() - 1);
		} /*else {
			uriRootPrefix = "";
		}*/
		
		//
		String pageURI = uriRootPrefix + "/" + pageName;
		while (pageURI.startsWith("//")) {
			pageURI = pageURI.substring(1);
		}
		
		ret.put("PagesRootURI", uriRootPrefix);
		ret.put("PageURI", pageURI);
		ret.put("PageNameRaw", pageName);
		ret.put("PageName", safePageName(pageName));
		ret.put("PageFrameID", pageFrameID(pageName));
		
		return ret;
	}
	
	///
	/// Gets the requested page prefix / suffix from the following priority order
	///
	/// 1) The page path itself
	/// 2) The common folder
	/// 3) The index folder (legacy support, do not use)
	///
	protected String getCommonPrefixOrSuffixHtml(String pageName, String fixType) {
		// Get from the pageName folder itself (v2)
		String res = FileUtils.readFileToString_withFallback(new File(pagesFolder, pageName+"/"+fixType+".html"), "UTF-8", null);
		
		// Get from the common folder (v2)
		if(res == null) {
			res = FileUtils.readFileToString_withFallback(new File(pagesFolder, "common/"+fixType+".html"), "UTF-8", null);
		}
		
		// Legacy support (v1) get from index folder
		if(res == null) {
			res = FileUtils.readFileToString_withFallback(new File(pagesFolder, "index/"+fixType+".html"), "UTF-8", null);
		}
		
		// Fallbacks to blank
		if(res == null) {
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
	public String prefixHTML(String pageName) {
		return getJMTE().parseTemplate(getCommonPrefixOrSuffixHtml(pageName,"prefix"), pageJMTEvars(pageName));
	}
	
	/// Gets the prefix
	public String suffixHTML(String pageName) {
		return getJMTE().parseTemplate(getCommonPrefixOrSuffixHtml(pageName,"suffix"), pageJMTEvars(pageName));
	}
	
	/// Builds a pagename HTML frame
	public String buildPageFrame(String pageName) {
		return buildPageFrame(pageName, false);
	}
	
	/// Builds a pagename HTML frame, with the hidden property if applicable
	public String buildPageFrame(String pageName, boolean isHidden) {
		String indexFileStr = FileUtils.readFileToString_withFallback(new File(pagesFolder, pageName + "/" + safePageName(pageName) + ".html"),
			"UTF-8", null);
		if( indexFileStr == null ) {
			indexFileStr = FileUtils.readFileToString_withFallback(new File(pagesFolder, pageName + "/index.html"),
				"UTF-8", "");
		}
		
		if( indexFileStr.trim().length() == 0 ) {
			return null;
		}
		
		StringBuilder frame = new StringBuilder();
		frame.append("<div class='pageFrame "+pageFrameID(pageName)+"' id='"+pageFrameID(pageName)+"'");
		if (isHidden) {
			frame.append(" style='display:none;'");
		}
		frame.append(">\n");
		frame.append(indexFileStr);
		frame.append("\n</div>\n");
		return getJMTE().parseTemplate(frame.toString(), pageJMTEvars(pageName));
	}
	
	/// Builds the FULL pagename HTML, with prefix and suffix
	public StringBuilder buildFullPageFrame(String pageName) {
		String frameHTML = buildPageFrame(pageName);
		
		if(frameHTML != null) {
			StringBuilder ret = new StringBuilder();
			ret.append(prefixHTML(pageName));
			ret.append(frameHTML);
			ret.append(suffixHTML(pageName));
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
		js,
		jsons,
		jsons_to_js,
		less,
		less_to_css,
		html
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
	public boolean processPageFile(PageFileType type, File input, File output, String pageName, Map<String, Object> jmteVarMap) throws IOException {
		if (input.exists() && input.isFile() && input.canRead()) {
			// Gets its string value, and process only if not blank
			String fileVal = FileUtils.readFileToString(input, "UTF-8");
			if ((fileVal = fileVal.trim()).length() > 0) {
				
				// Does a JMTE filter
				fileVal = getJMTE().parseTemplate(fileVal, jmteVarMap);
				
				// Does specific conversions
				if( type == PageFileType.jsons_to_js ) {
					// Adds the script object wrapper
					fileVal = "window.pageFrames = window.pageFrames || {}; window.pageFrames." + safePageName(pageName) + " = ("
						+ fileVal + ");";
				} else if( type == PageFileType.less_to_css ) {
					/// Does an outer wrap, if its not index page (which applies style to 'all')
					if (!pageName.equalsIgnoreCase("index") && !pageName.equalsIgnoreCase("common")) {
						fileVal = "." + pageFrameID(pageName) + " { \n" + fileVal + "\n } \n";
					}
					
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
	public boolean processPageFile(PageFileType type, File[] inputArr, File output, String pageName, Map<String, Object> jmteVarMap) throws IOException {
		for(File input : inputArr) {
			if( processPageFile(type, input, output, pageName, jmteVarMap) ) {
				return true;
			}
		}
		return false;
	}
	
	///
	/// Builds all the assets for a single page
	///
	/// @param PageName to build
	///
	public boolean buildAndOutputPage(String pageName) {
		
		// System.out allowed here, because LESS does a system out ANYWAY.
		// Help to make more "sense" of the done output
		System.out.print("> PageBuilder[Core].buildPage(\'" + pageName + "\'): ");
		
		// Output folder validitiy check
		if( outputFolder == null ) {
			throw new RuntimeException("Missing output folder, unable to generate : " + pageName);
		}
		
		// Future extension, possible loop hole abuse. Im protecting against it early
		if (pageName.startsWith(".")) {
			throw new RuntimeException("Unable to load page name, starting with '.' : " + pageName);
		}
		if (pageName.indexOf("..") >= 0) {
			throw new RuntimeException("Unable to load page name, containing '..' : " + pageName);
		}
		if (pageName.toLowerCase().indexOf("web-inf") >= 0) {
			throw new RuntimeException("Unable to load page name, that may bypass WEB-INF : " + pageName);
		}
		
		try {
			
			// Prepares output and definition FILE objects, and JMTE map
			//-------------------------------------------------------------------
			File outputPageFolder = new File(outputFolder, pageName);
			File definitionFolder = new File(pagesFolder, pageName);
			Map<String, Object> jmteVarMap = pageJMTEvars(pageName);
			String pageName_safe = safePageName(pageName);
			
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
			File pageAssetsFolder = new File(definitionFolder, "assets");
			if (pageAssetsFolder.exists() && pageAssetsFolder.isDirectory()) {
				// Copy if folder to target
				FileUtils.copyDirectory(pageAssetsFolder, new File(outputPageFolder, "assets"));
			}
			
			// Process the JS script (if provided)
			//-------------------------------------------------------------------
			boolean hasJsFile = processPageFile(
				PageFileType.js,
				new File[] {
					new File(definitionFolder, "index.js"),
					new File(definitionFolder, pageName_safe + ".js")
				},
				new File(outputPageFolder, pageName_safe + ".js"),
				pageName,
				jmteVarMap
			);
			
			// Build the JSONS script (if provided)
			//-------------------------------------------------------------------
			boolean hasJsonsFile = processPageFile(
				PageFileType.jsons_to_js,
				new File[] {
					new File(definitionFolder, "index.jsons"),
					new File(definitionFolder, pageName_safe + ".jsons")
				},
				new File(outputPageFolder, pageName_safe + ".jsons.js"),
				pageName,
				jmteVarMap
			);
			
			// Build the LESS script (if provided)
			//-------------------------------------------------------------------
			boolean hasLessFile = processPageFile(
				PageFileType.jsons_to_js,
				new File[] {
					new File(definitionFolder, "index.less"),
					new File(definitionFolder, pageName_safe + ".less")
				},
				new File(outputPageFolder, pageName_safe + ".css"),
				pageName,
				jmteVarMap
			);
			
			// Build the html page
			//-------------------------------------------------------------------
			
			// The HTML output (if valid)
			StringBuilder indexStrBuilder = buildFullPageFrame(pageName);
			if( indexStrBuilder == null ) {
				return false;
			}
			String indexStr = indexStrBuilder.toString();
			
			// Build the injector code for this page (before </head>)
			//-------------------------------------------------------------------
			StringBuilder injectorStrBuilder = new StringBuilder();
			String injectorStr = injectorStrBuilder.toString();
			
			if( hasLessFile ) {
				if( injectorStr.indexOf(pageName+"/"+pageName_safe+".css") > 0 ) {
					// Skips injection if already included
				} else {
					injectorStrBuilder.append("<link rel='stylesheet' type='text/css' href='"+uriRootPrefix+""+pageName+"/"+pageName_safe+".css'/>\n");
				}
			}
			if( hasJsFile ) {
				if( injectorStr.indexOf(pageName+"/"+pageName_safe+".js") > 0 ) {
					// Skips injection if already included
				} else {
					injectorStrBuilder.append("<script src='"+uriRootPrefix+""+pageName+"/"+pageName_safe+".js'/>\n");
				}
			}
			if( hasJsonsFile ) {
				if( injectorStr.indexOf(pageName+"/"+pageName_safe+".jsons.js") > 0 ) {
					// Skips injection if already included
				} else {
					injectorStrBuilder.append("<script src='"+uriRootPrefix+""+pageName+"/"+pageName_safe+".jsons.js'/>\n");
				}
			}
			
			// Ammend the HTML output
			//-------------------------------------------------------------------
			
			// Apply injector code if any
			if( injectorStr.length() > 0 ) {
				indexStr = indexStr.replaceAll("\\<\\/head\\>", injectorStr+"</head>");
			}
			
			// HTML minify
			//-------------------------------------------------------------------
			
			// Apply a simplistic compression (so avoid inline JS with line comments for nuts)
			//
			// @TODO: A proper minifier library integration, like:
			// https://code.google.com/p/htmlcompressor
			indexStr = indexStr.trim().replaceAll("\\s+", " ");
			indexStr = indexStr.trim().replaceAll("\\>\\s\\<", "><");
			
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
	
}

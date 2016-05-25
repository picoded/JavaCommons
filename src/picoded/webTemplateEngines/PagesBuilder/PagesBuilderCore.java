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
	// Protected vars access
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
	
	////////////////////////////////////////////////////////////
	//
	// Utility functions
	//
	////////////////////////////////////////////////////////////
	
	/// Utility to get page frame ID
	protected String pageFrameID(String pageName) {
		return "pageFrame_"+pageName.replaceAll("/","_");
	}
	
	/// Generates the needed map string template for the respective page
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
		ret.put("PageName", pageName);
		ret.put("PageFrameID", pageFrameID(pageName));
		
		return ret;
	}
	
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
	
	/// Get pageFrame
	public String buildPageFrame(String pageName) {
		return buildPageFrame(pageName, false);
	}
	
	public String buildPageFrame(String pageName, boolean isHidden) {
		String[] pageNamePathSet = pageName.split("/");
		String indexFileStr = FileUtils.readFileToString_withFallback(new File(pagesFolder, pageName + "/" + pageNamePathSet[pageNamePathSet.length - 1] + ".html"),
			"UTF-8", null);
		if( indexFileStr == null ) {
			indexFileStr = FileUtils.readFileToString_withFallback(new File(pagesFolder, pageName + "/index.html"),
				"UTF-8", "");
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
	
	/// HTML builder
	public StringBuilder buildFullPageFrame(String pageName) {
		StringBuilder ret = new StringBuilder();
		ret.append(prefixHTML(pageName));
		ret.append(buildPageFrame(pageName));
		ret.append(suffixHTML(pageName));
		return ret;
	}
	
	////////////////////////////////////////////////////////////
	//
	// Page Building
	//
	////////////////////////////////////////////////////////////
	
	///
	/// Builds all the assets for a single page
	///
	/// @param PageName to build
	///
	public void buildAndOutputPage(String pageName) {
		
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
			
			// Indicates if there is a JS script
			boolean hasJsFile = false;
			
			// The JS file source location
			File jsFile = new File(definitionFolder, pageName + ".js");
			
			// Process file only if its readable
			if (jsFile.exists() && jsFile.isFile() && jsFile.canRead()) {
				
				// Gets its string value, and process only if not blank
				String jsString = FileUtils.readFileToString(jsFile, "UTF-8");
				if ((jsString = jsString.trim()).length() > 0) {
					
					// Does a JMTE filter
					jsString = getJMTE().parseTemplate(jsString, jmteVarMap);
					
					// Write to file if it differ
					FileUtils.writeStringToFile_ifDifferant(new File(outputPageFolder, pageName + ".js"), "UTF-8", jsString);
					
					// Indicate file is "deployed"
					hasJsFile = true;
				}
			}
			
			// Build the JSONS script (if provided)
			//-------------------------------------------------------------------
			
			// Indicates if there is a JSONS script
			boolean hasJsonsFile = false;
			
			// The JSONS file source location
			File jsonsFile = new File(definitionFolder, pageName + ".jsons");
			
			// Process file only if its readable
			if (jsonsFile.exists() && jsonsFile.isFile() && jsonsFile.canRead()) {
				
				// Gets its string value, and process only if not blank
				String jsonsString = FileUtils.readFileToString(jsonsFile, "UTF-8");
				if ((jsonsString = jsonsString.trim()).length() > 0) {
					
					// Adds the script object wrapper
					jsonsString = "window.pageFrames = window.pageFrames || {}; window.pageFrames." + pageName + " = ("
						+ jsonsString + ");";
					
					// Does a JMTE filter
					jsonsString = getJMTE().parseTemplate(jsonsString, jmteVarMap);
					
					// Write to file if it differ
					FileUtils.writeStringToFile_ifDifferant(new File(outputPageFolder, pageName + ".jsons.js"), "UTF-8",
						jsonsString);
					
					// Indicate file is "deployed"
					hasJsFile = true;
				}
			}
			
			// Build the LESS script
			//-------------------------------------------------------------------
			
			// Indicates if there is a LESS script
			boolean hasLessFile = false;
			
			// The LESS file source location
			File lessFile = new File(definitionFolder, pageName + ".less");
			
			// Process file only if its readable
			if (lessFile.exists() && lessFile.isFile() && lessFile.canRead()) {
				
				// Gets its string value, and process only if not blank
				String lessString = FileUtils.readFileToString(lessFile, "UTF-8");
				if ((lessString = lessString.trim()).length() > 0) {
					
					/// Does an outer wrap, if its not index page (which applies style to 'all')
					if (!pageName.equalsIgnoreCase("index") && !pageName.equalsIgnoreCase("common")) {
						lessString = ".pageFrame_" + pageName + " { \n" + lessString + "\n } \n";
					}
					
					// Does a JMTE filter
					lessString = getJMTE().parseTemplate(lessString, jmteVarMap);
					
					/// Write to map file if differ
					FileUtils.writeStringToFile_ifDifferant(new File(outputPageFolder, pageName + ".css.map"), "UTF-8",
						lessString);
					
					/// Convert LESS to CSS
					String cssString = less.compile(lessString);
					
					/// Does a simplistic compression
					cssString = cssString.trim().replaceAll("\\s+", " ");
					
					// Write to file if it differ
					FileUtils.writeStringToFile_ifDifferant(new File(outputPageFolder, pageName + ".css"), "UTF-8",
						cssString);
					
					// Indicate file is "deployed"
					hasLessFile = true;
				}
			}
			
			// Build the html page
			//-------------------------------------------------------------------
			
			// The HTML output
			String indexStr = buildFullPageFrame(pageName).toString();
			
			// Build the injector code for this page (before </head>)
			//-------------------------------------------------------------------
			// StringBuilder injectorStrBuilder = new StringBuilder();
			// String injectorStr = injectorStrBuilder.toString();
			// 
			// if( hasLessFile ) {
			// 	if( injectorStr.indexOf(pageName+"/"+pageName+".css") > 0 ) {
			// 		// Skips injection if already included
			// 	} else {
			// 		injectorStrBuilder.append("<link rel='stylesheet' type='text/css' href='"+uriRootPrefix+""+pageName+"/"+pageName+".css'/>\n");
			// 	}
			// }
			// if( hasJsFile ) {
			// 	if( injectorStr.indexOf(pageName+"/"+pageName+".js") > 0 ) {
			// 		// Skips injection if already included
			// 	} else {
			// 		injectorStrBuilder.append("<script src='"+uriRootPrefix+""+pageName+"/"+pageName+".js'/>\n");
			// 	}
			// }
			
			// Ammend the HTML output
			//-------------------------------------------------------------------
			
			// Apply injector code if any
			// if( injectorStr.length() > 0 ) {
			// 	indexStr = indexStr.replace("</head>", injectorStr+"</head>");
			// }
			
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
			
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
		// End and returns self
		//-------------------------------------------------------------------
		return this;
	}
	
}

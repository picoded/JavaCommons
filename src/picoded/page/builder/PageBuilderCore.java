package picoded.page.builder;

import java.io.*;
import java.util.*;

// JMTE inner functions add-on
import com.floreysoft.jmte.*;

// Sub modules useds
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
public class PageBuilderCore {

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

	/// Dependency chain tracking
	protected GenericConvertListSet<String> dependencyTracker = new GenericConvertListSet<String>();

	/// Components filter utility
	protected PageComponentFilter componentsFilter = null;

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
	public PageBuilderCore(File inPagesFolder) {
		pagesFolder = inPagesFolder;
		componentsFilter = new PageComponentFilter(this);
	}

	///
	/// Constructor, with the folders defined
	///
	/// @param The various pages definition folder
	///
	public PageBuilderCore(String inPagesFolder) {
		this(new File(inPagesFolder));
	}

	///
	/// Constructor, with the folders defined
	///
	/// @param The various pages definition folder
	/// @param The target folder to build the result into
	///
	public PageBuilderCore(File inPagesFolder, File inOutputFolder) {
		pagesFolder = inPagesFolder;
		outputFolder = inOutputFolder;
		componentsFilter = new PageComponentFilter(this);
	}

	///
	/// Constructor, with the folders defined
	///
	/// @param The various pages definition folder
	/// @param The target folder to build the result into
	///
	public PageBuilderCore(String inPagesFolder, String inOutputFolder) {
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
			setJMTE(new JMTE());
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
		return rawPageName.replaceAll("/", "-");
	}

	/// Utility to get page frame ID
	///
	/// @param  rawPageName used to generate the vars
	///
	/// @returns  The page frame ID used (that is character safe?)
	protected String pageFrameID(String rawPageName) {
		return "page-" + safePageName(rawPageName);
	}

	/// Utility to get page frame ID : legacy format specific to IFAM (to phase out)
	///
	/// @param  rawPageName used to generate the vars
	///
	/// @returns  The page frame ID used (that is character safe?)
	protected String pageFrameID_ifamLegacy(String rawPageName) {
		return "pageFrame_" + safePageName(rawPageName);
	}

	///
	/// Gets the requested page template.json from the following priority order
	///
	/// 1) The page path itself
	/// 2) Any parent path folder
	/// 3) The root folder
	///
	protected Map<String,Object> getTemplateJson(String rawPageName) {
		String resStr = null;
		String fileName = "template.json";
		List<Map<String,Object>> templateList = new ArrayList<Map<String,Object>>();

		// Page path itself
		templateList.add(
			GenericConvert.toStringMap(
				FileUtils.readFileToString_withFallback(new File(pagesFolder, rawPageName + "/" + fileName), null /*"UTF-8"*/, null), null
			)
		);

		// Gets the parent paths (if valid)
		String[] splitNames = splitPageName(rawPageName);
		if (splitNames.length <= 1) {
			// There were no parent folders, skip the parent paths checks
		} else {
			// Go one "directory" parent upward
			splitNames = ArrayConv.subarray(splitNames, 0, splitNames.length - 1);

			// Breaks once root directory is reached / or result found
			while (splitNames.length > 0) {
				// Join the name path, and get the file
				templateList.add(
					GenericConvert.toStringMap(
						FileUtils.readFileToString_withFallback(new File(pagesFolder, String.join("/", splitNames) + "/" + fileName), null /*"UTF-8"*/, null), null
					)
				);

				// Go one "directory" parent upward
				splitNames = ArrayConv.subarray(splitNames, 0, splitNames.length - 1);
			}
		}

		// Get from the root folder (v2)
		templateList.add(
			GenericConvert.toStringMap(
				FileUtils.readFileToString_withFallback(new File(pagesFolder, fileName), null /*"UTF-8"*/, null), null
			)
		);

		// Flips the list
		Collections.reverse(templateList);

		Map<String,Object> res = new HashMap<String,Object>();
		for(Map<String,Object> template : templateList) {
			if(template != null) {
				res.putAll(template);
			}
		}
		return res;
	}

	/// Gets and extract out a page specific configuration. In its respective page.json file
	///
	/// @param  rawPageName used to generate the vars
	///
	/// @returns The data hash map
	protected Map<String,Object> pageConfigFetch(String rawPageName) {
		// Basic filter safety
		rawPageName = filterRawPageName(rawPageName);

		// Get the config from rawPageName folder itself
		return GenericConvert.toStringMap( FileUtils.readFileToString_withFallback(new File(pagesFolder, rawPageName + "/page.json" ), null /*"UTF-8"*/, "{}") );
	}


	/// Generates the needed map string template for the respective page
	///
	/// @param  rawPageName used to generate the vars
	///
	/// @returns The data map used inside JMTE
	protected Map<String, Object> pageJMTEvars(String rawPageName) {
		// Basic filter safety
		rawPageName = filterRawPageName(rawPageName);

		// Initialize to root if not previously set
		if (uriRootPrefix == null) {
			uriRootPrefix = ".";
		}

		// Removes trailing /, unless its the only character
		if (uriRootPrefix.length() > 1 && uriRootPrefix.endsWith("/")) {
			uriRootPrefix = uriRootPrefix.substring(0, uriRootPrefix.length() - 1);
		} /*else {
		  uriRootPrefix = "";
		  }*/

		// Uri slash fixing (in case of double slash)
		String pageURI = uriRootPrefix + "/" + rawPageName;
		while (pageURI.indexOf("//") >= 0) {
			pageURI = pageURI.replaceAll("//", "/");
		}

		// Get the template json stack
		Map<String, Object> ret = getTemplateJson(rawPageName);

		ret.put("PagesRootURI", uriRootPrefix);
		ret.put("PageRootURI", uriRootPrefix); //because FAportal and orgeva
		ret.put("PageURI", pageURI);

		ret.put("PageNameRaw", rawPageName);
		ret.put("PageName", safePageName(rawPageName));
		ret.put("PageClass", pageFrameID(rawPageName));

		ret.put("Page", buildPageComponentMap());
		ret.put("PageConfig", pageConfigFetch(rawPageName));

		// Legacy to phase out
		// @TODO : Phase Out
		//-----------------------------
		ret.put("PageFrameID", pageFrameID_ifamLegacy(rawPageName));
		ret.put("PageComponent", buildPageComponentMap());

		return ret;
	}

	///
	/// HTML specific version of getCommonFile
	///
	protected String getCommonPrefixOrSuffixHtml(String rawPageName, String fixType) {
		return getCommonFile(rawPageName, fixType + ".html");
	}

	///
	/// Filters the rawPageName, into its valid form (remove any pre/suf-fix of slashes)
	///
	protected String filterRawPageName(String rawPageName) {
		if(rawPageName == null) {
			rawPageName = "";
		}

		rawPageName = rawPageName.trim();
		rawPageName = rawPageName.replaceAll("\\.","/");

		while (rawPageName.indexOf("//") >= 0) {
			rawPageName = rawPageName.replaceAll("//", "/");
		}
		while (rawPageName.startsWith("/")) {
			rawPageName = rawPageName.substring(1);
		}
		while (rawPageName.endsWith("/")) {
			rawPageName = rawPageName.substring(0, rawPageName.length());
		}

		return rawPageName;
	}

	///
	/// Takes a pagename, and split in its pathing.
	/// Used to find parent folders
	///
	protected String[] splitPageName(String rawPageName) {
		return filterRawPageName(rawPageName).split("/");
	}

	///
	/// Gets the requested page prefix / suffix from the following priority order
	///
	/// 1) The page path itself
	/// 2) Any parent path folder
	/// 3) The root folder (v3)
	/// 4) The common folder (v2)
	/// 5) The index folder (legacy support, do not use)
	///
	protected String getCommonFile(String rawPageName, String fileName) {
		String res = null;

		// Get from the rawPageName folder itself (v2)
		res = FileUtils.readFileToString_withFallback(new File(pagesFolder, rawPageName + "/" + fileName), null /*"UTF-8"*/, null);

		// Gets the parent paths (if valid)
		if (res == null) {
			String[] splitNames = splitPageName(rawPageName);
			if (splitNames.length <= 1) {
				// There were no parent folders, skip the parent paths checks
			} else {
				// Go one "directory" parent upward
				splitNames = ArrayConv.subarray(splitNames, 0, splitNames.length - 1);

				// Breaks once root directory is reached / or result found
				while (res == null && splitNames.length > 0) {
					// Join the name path, and get the file
					res = FileUtils.readFileToString_withFallback(new File(pagesFolder, String.join("/", splitNames) + "/"
						+ fileName), null /*"UTF-8"*/, null);

					// Go one "directory" parent upward
					splitNames = ArrayConv.subarray(splitNames, 0, splitNames.length - 1);
				}
			}
		}

		// Get from the root folder (v2)
		if (res == null) {
			res = FileUtils.readFileToString_withFallback(new File(pagesFolder, fileName), null /*"UTF-8"*/, null);
		}

		// Get from the common folder (v2)
		if (res == null) {
			res = FileUtils.readFileToString_withFallback(new File(pagesFolder, "common/" + fileName), null /*"UTF-8"*/, null);
		}

		// Legacy support (v1) get from index folder
		if (res == null) {
			res = FileUtils.readFileToString_withFallback(new File(pagesFolder, "index/" + fileName), null /*"UTF-8"*/, null);
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

	/// Gets and returns a page frame raw string without going through the JMTE parser
	public String buildPageInnerRawHTML(String rawPageName) {
		// Depenency chain tracking
		rawPageName = filterRawPageName(rawPageName);
		dependencyTracker.add(rawPageName);

		String indexFileStr = FileUtils.readFileToString_withFallback(new File(pagesFolder, rawPageName + "/"
			+ safePageName(rawPageName) + ".html"), null /*"UTF-8"*/, null);
		if (indexFileStr == null) {
			indexFileStr = FileUtils.readFileToString_withFallback(new File(pagesFolder, rawPageName + "/index.html"),
				null /*"UTF-8"*/, "");
		}

		if ((indexFileStr = indexFileStr.trim()).length() == 0) {
			if (hasPageFile(rawPageName)) { //has file
				return ""; //this is a blank HTML file
			}
			return null;
		}

		return indexFileStr.toString();
	}

	/// Gets and returns a page frame string, with its respective JMTE input vars?
	public String buildPageInnerHTML(String rawPageName) {
		return buildPageInnerHTML(rawPageName, null);
	}

	/// Gets and returns a page frame string, with its respective JMTE input vars?
	public String buildPageInnerHTML(String rawPageName, Map<String, Object> jmteTemplate) {
		String indexFileStr = FileUtils.readFileToString_withFallback(new File(pagesFolder, rawPageName + "/"
			+ safePageName(rawPageName) + ".html"), null /*"UTF-8"*/, null);
		if (indexFileStr == null) {
			indexFileStr = FileUtils.readFileToString_withFallback(new File(pagesFolder, rawPageName + "/index.html"),
				null /*"UTF-8"*/, "");
		}

		if ((indexFileStr = indexFileStr.trim()).length() == 0) {
			if (hasPageFile(rawPageName)) { //has file
				return ""; //this is a blank HTML file
			}
			return null;
		}

		if( jmteTemplate == null ) {
			jmteTemplate = pageJMTEvars(rawPageName);
		}

		return getJMTE().parseTemplate(indexFileStr.toString(), jmteTemplate);
	}

	/// Get the page frame div header, this is used to do a "search replace" for script / css injection
	protected String pageFrameHeaderDiv(String rawPageName) {
		return "<div class='pageFrame " + pageFrameID(rawPageName) + " "+ pageFrameID_ifamLegacy(rawPageName)+ "' id='" + pageFrameID_ifamLegacy(rawPageName) + "'>\n";
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
	// Subpages searching handling
	//
	////////////////////////////////////////////////////////////

	///
	/// Sub page name listing
	///
	/// @param rawPageName Parent page to search from
	///
	/// @return Collection of sub pages name from the parent page
	///
	public Set<String> subPagesList(String rawPageName) {
		rawPageName = filterRawPageName(rawPageName);
		HashSet<String> res = new HashSet<String>();

		// The current folder to scan
		File folder = new File(pagesFolder, rawPageName);

		// Scan for subdirectories ONLY if this is a directory
		if (folder.isDirectory()) {
			// For each sub directory, build it as a page
			for (File pageDefine : FileUtils.listDirs(folder)) {
				// Get sub page name
				String subPageName = pageDefine.getName();

				// Common and index zone only if its top layer
				if (subPageName.equalsIgnoreCase("common") || subPageName.equalsIgnoreCase("index")) {
					if (rawPageName.length() <= 0) {
						res.add(subPageName);
					}
				} else if (subPageName.equalsIgnoreCase("assets") || subPageName.equalsIgnoreCase("web-inf")) {
					// ignoring certain reserved folders
				} else {
					res.add(subPageName);
				}
			}
		}

		// Return result set
		return res;
	}

	///
	/// Build and returns the page components map
	///
	public PageComponentMap buildPageComponentMap() {
		return new PageComponentMap(this, "");
	}

	////////////////////////////////////////////////////////////
	//
	// Page Building parts
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

				// Does specific conversions
				if (type == PageFileType.jsons_to_js) {

					// Does a JMTE filter
					fileVal = getJMTE().parseTemplate(fileVal, jmteVarMap);

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

					// Does a JMTE filter
					fileVal = getJMTE().parseTemplate(fileVal, jmteVarMap);

					// Less to css conversion
					fileVal = less.compile(fileVal);
				} else {
					// Does a JMTE filter
					fileVal = getJMTE().parseTemplate(fileVal, jmteVarMap);
				}

				// Write to file if it differ
				FileUtils.writeStringToFile_ifDifferant(output, null /*"UTF-8"*/, fileVal);

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

	////////////////////////////////////////////////////////////
	//
	// Full Page Building
	//
	////////////////////////////////////////////////////////////
	
	///
	/// Builds all the assets for a single page
	/// Copies out the various files, and does direct conversion on them if needed.
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
				FileUtils.copyDirectory_ifDifferent(pageAssetsFolder, new File(outputPageFolder, "assets"));
				hasAssets = true;
			}
			
			// Copy out all the various other files
			//
			// @TODO: Optimize this to only copy IF newer
			//
			// Ignores: index files (as of now)
			//-------------------------------------------------------------------
			
			// For each sub directory, build it as a page
			for (File inFile : FileUtils.listFiles(definitionFolder, null, false)) {
				String fileName = inFile.getName();
				String[] splitFileName = fileName.split(".");
				
				// Ignore index based files
				if(splitFileName[0].equalsIgnoreCase("index")) {
					continue;
				}
				
				// File to output to
				File outFile = new File(outputPageFolder, fileName);
				
				// Copy over the file
				FileUtils.copyFile_ifDifferent(inFile, outFile);
				
				String fileExt = (splitFileName.length > 1)? splitFileName[ splitFileName.length - 1 ] : "";
				if(fileExt.equalsIgnoreCase("less")) {
					
				} else if(fileExt.equalsIgnoreCase("es6")) {
					
				}
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

			// Components resolution
			//-------------------------------------------------------------------
			indexStr = componentsFilter.resolve(indexStr);

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
			FileUtils.writeStringToFile_ifDifferant(new File(outputPageFolder, "index.html"), null /*"UTF-8"*/, indexStr);

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
		if (rawPageName != null) {
			rawPageName = rawPageName.trim();
		}

		if (rawPageName.equalsIgnoreCase("/")) {
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
				if (subPageName.equalsIgnoreCase("common") || subPageName.equalsIgnoreCase("index")) {
					buildAndOutputPage(rawPageName + subPageName);
					if (rawPageName.length() <= 0) {
						//buildAndOutputPage(rawPageName + subPageName);
					} else {
						System.out.print("> PageBuilder[Core].buildPageFolder - WARNING, common / index nested build (\'"
							+ rawPageName + "\', \'" + subPageName + "\'): ");
					}
				} else if (subPageName.equalsIgnoreCase("assets") || subPageName.equalsIgnoreCase("web-inf") || subPageName.equalsIgnoreCase("build")) {
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

	////////////////////////////////////////////////////////////
	//
	// Dependency chain management
	//
	////////////////////////////////////////////////////////////

	/// Dependency chain tracking
	public GenericConvertListSet<String> dependencyTracker() {
		return dependencyTracker();
	}

	/// Reset the Dependency tracking
	public void dependencyTrackerReset() {
		dependencyTracker.clear();
	}

	/// Recursively get the sub depencies, and do the full tracking
	protected GenericConvertListSet<String> fullDependencyTracker() {
		// @TODO Recursive pulls
		return dependencyTracker();
	}

	/// Build the depency for a raw file
	protected String dependencyBuildFile(String filename) {
		StringBuilder res = new StringBuilder();
		for(String name : dependencyTracker) {
			String pageData = FileUtils.readFileToString_withFallback(new File(pagesFolder, name + "/"+filename), null /*"UTF-8"*/, null);
			if( pageData != null ) {
				res.append(pageData);
			}
		}
		return res.toString();
	}

	/// Builds the LESS from the depency chain
	public String dependencyLess() {
		return dependencyBuildFile("depend.less");
	}

	/// Builds the CSS from the depency chain
	public String dependencyCss() {
		return less.compile(dependencyLess());
	}
}

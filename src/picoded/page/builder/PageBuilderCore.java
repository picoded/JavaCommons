package picoded.page.builder;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import picoded.conv.ArrayConv;
import picoded.conv.CompileES6;
import picoded.conv.GenericConvert;
import picoded.conv.JMTE;
import picoded.conv.LessToCss;
import picoded.file.FileUtil;
import picoded.struct.GenericConvertListSet;
import picoded.struct.GenericConvertMap;
// JMTE inner functions add-on
// Sub modules useds

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
	public File pageFolder = null;
	
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
	
	/// Component filter utility
	protected PageComponentFilter componentFilter = null;
	
	/// Utility functions
	protected PageBuilderUtil util = null;
	
	////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////
	
	///
	/// Constructor, with the folders defined
	///
	/// @param The various page definition folder
	///
	public PageBuilderCore(File inPageFolder) {
		this(inPageFolder, (File) null);
	}
	
	///
	/// Constructor, with the folders defined
	///
	/// @param The various page definition folder
	///
	public PageBuilderCore(String inPageFolder) {
		this(new File(inPageFolder));
	}
	
	///
	/// Constructor, with the folders defined
	///
	/// @param The various page definition folder
	/// @param The target folder to build the result into
	///
	public PageBuilderCore(File inPageFolder, File inOutputFolder) {
		pageFolder = inPageFolder;
		outputFolder = inOutputFolder;
		componentFilter = new PageComponentFilter(this);
		util = new PageBuilderUtil(this);
	}
	
	///
	/// Constructor, with the folders defined
	///
	/// @param The various page definition folder
	/// @param The target folder to build the result into
	///
	public PageBuilderCore(String inPageFolder, String inOutputFolder) {
		this(new File(inPageFolder), new File(inOutputFolder));
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
	
	///
	/// Internal centralized point (for the class) to process JMTE tempaltes
	///
	/// Used to centralizes the handling of "filterRawTemplateForRelativeURImode" and etc.
	///
	/// @param  
	///
	///
	protected String processJMTE(String template, Map<String, Object> templateObj,
		String relativeRawPageName) {
		if (relativeRawPageName != null) {
			templateObj = overwriteTemplateObjectForRleativePathIfNeeded(templateObj,
				relativeRawPageName);
		}
		
		return getJMTE().parseTemplate(filterRawTemplateForRelativeURImode(template), templateObj);
	}
	
	////////////////////////////////////////////////////////////
	//
	// Utility functions
	// @TODO : Migrate all these functions over to PageBuilderUtil class
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
	/// @TODO Drop eventually after everything is migrated into the new system
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
	protected Map<String, Object> getTemplateJson(String rawPageName) throws IOException {
		String resStr = null;
		String fileName = "page.json";
		List<Map<String, Object>> templateList = new ArrayList<Map<String, Object>>();
		
		// Page path itself
		templateList.add(GenericConvert.toStringMap(FileUtil.readFileToString_withFallback(new File(
			pageFolder, rawPageName + "/" + fileName), null), null));
		
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
				templateList
					.add(GenericConvert.toStringMap(
						FileUtil.readFileToString_withFallback(
							new File(pageFolder, String.join("/", splitNames) + "/" + fileName), null),
						null));
				
				// Go one "directory" parent upward
				splitNames = ArrayConv.subarray(splitNames, 0, splitNames.length - 1);
			}
		}
		
		// Get from the root folder (v2)
		templateList.add(GenericConvert.toStringMap(
			FileUtil.readFileToString_withFallback(new File(pageFolder, fileName), null), null));
		
		// Flips the list
		Collections.reverse(templateList);
		
		Map<String, Object> res = new HashMap<String, Object>();
		for (Map<String, Object> template : templateList) {
			if (template != null) {
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
	protected Map<String, Object> pageConfigFetch(String rawPageName) throws IOException {
		// Basic filter safety
		rawPageName = filterRawPageName(rawPageName);
		
		// Get the config from rawPageName folder itself
		return GenericConvert.toStringMap(FileUtil.readFileToString_withFallback(new File(pageFolder,
			rawPageName + "/page.json"), "{}"));
	}
	
	/// Generates the needed map string template for the respective page
	///
	/// @param  rawPageName used to generate the vars
	///
	/// @returns The data map used inside JMTE
	protected Map<String, Object> pageJMTEvars(String rawPageName) throws IOException {
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
		
		ret.put("PageRootURI", uriRootPrefix);
		ret.put("ApiRootURI", uriRootPrefix + "/api");
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
		ret.put("PagesRootURI", uriRootPrefix); //because FAportal and orgeva //@TODO: Remove this
		
		return ret;
	}
	
	///
	/// HTML specific version of getCommonFile
	///
	protected String getCommonPrefixOrSuffixHtml(String rawPageName, String fixType)
		throws IOException {
		return getCommonFile(rawPageName, fixType + ".html");
	}
	
	///
	/// Filters the rawPageName, into its valid form (remove any pre/suf-fix of slashes)
	///
	public String filterRawPageName(String rawPageName) {
		if (rawPageName == null) {
			rawPageName = "";
		}
		
		rawPageName = rawPageName.trim();
		rawPageName = rawPageName.replaceAll("\\.", "/");
		rawPageName = rawPageName.replaceAll("\\-", "/");
		
		// Double slashses are normalized here as well, complex relative paths are condensed
		rawPageName = FileUtil.normalize(rawPageName).trim();
		
		// Remove starting slashes
		while (rawPageName.startsWith("/")) {
			rawPageName = rawPageName.substring(1);
		}
		
		// Remove ending slashes
		while (rawPageName.endsWith("/")) {
			rawPageName = rawPageName.substring(0, rawPageName.length());
		}
		
		return rawPageName.trim();
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
	protected String getCommonFile(String rawPageName, String fileName) throws IOException {
		String res = null;
		
		// Get from the rawPageName folder itself (v2)
		res = FileUtil.readFileToString_withFallback(new File(pageFolder, rawPageName + "/"
			+ fileName), null);
		
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
					res = FileUtil.readFileToString_withFallback(
						new File(pageFolder, String.join("/", splitNames) + "/" + fileName), null);
					
					// Go one "directory" parent upward
					splitNames = ArrayConv.subarray(splitNames, 0, splitNames.length - 1);
				}
			}
		}
		
		// Get from the root folder (v2)
		if (res == null) {
			res = FileUtil.readFileToString_withFallback(new File(pageFolder, fileName), null);
		}
		
		// Get from the common folder (v2)
		if (res == null) {
			res = FileUtil.readFileToString_withFallback(new File(pageFolder, "common/" + fileName),
				null);
		}
		
		// Legacy support (v1) get from index folder
		if (res == null) {
			res = FileUtil.readFileToString_withFallback(new File(pageFolder, "index/" + fileName),
				null);
		}
		
		// Fallbacks to blank
		if (res == null) {
			return "";
		}
		return res;
	}
	
	////////////////////////////////////////////////////////////
	//
	// Relative path handling (Phabricator T605)
	//
	////////////////////////////////////////////////////////////
	
	/// Boolean switch to enable RelativeURI mode (or not)
	public boolean enableRelativeURI = false;
	
	/// The full API to use, if null. It fallsback to ${PageRootURI}/api
	public String fullApiRootPath = null;
	
	/// Filters the template in preperation for RelativeURI mode
	///
	/// AKA: replace_PageRootURI_to_ApiRootURI_whereApplicable
	protected String filterRawTemplateForRelativeURImode(String input) {
		// Also normalizes the PagesRootURI to PageRootURI
		return input.replaceAll("\\$\\{PagesRootURI\\}", "\\${PageRootURI}").replaceAll(
			"\\$\\{PageRootURI\\}/api", "\\${ApiRootURI}");
	}
	
	/// Process and overwrite the template object for the relative path mode
	/// 
	/// @param  Template object to overwrite
	/// @param  The raw page name (used to process the relative pathing)
	///
	/// @return The overwritten template object
	protected Map<String, Object> overwriteTemplateObjectForRleativePathIfNeeded(
		Map<String, Object> templateObj, String rawPageName) {
		
		// Skip if enableRelativeURI is disabled (no processing)
		if (!enableRelativeURI) {
			return templateObj;
		}
		
		// Tabulate the root path
		String relativeRoot = tabulateRelativePath(rawPageName);
		
		// Does the actual substitution
		templateObj.put("PageRootURI", relativeRoot);
		
		if (fullApiRootPath != null) {
			templateObj.put("ApiRootURI", fullApiRootPath);
		} else {
			//
			// NOTE: Relative API path is a VERY complex problem,
			// as it will be passed around as a string in JS, losing much contextual infromation.
			// 
			templateObj.put("ApiRootURI", relativeRoot + "/api");
		}
		
		// Return template object
		return templateObj;
	}
	
	///
	/// Derive the relative path using the rawPageName after filtering it
	///
	/// The following is the expected input, and its output
	///
	/// | input string    | Relative ROOT path    |
	/// |-----------------|-----------------------|
	/// | (blank)         | .                     |
	/// | abc             | ./..                  |
	/// | /abc/           | ./..                  |
	/// | abc/xyz         | ./../..               |
	/// | abc/xyz/        | ./../..               |
	///
	protected String tabulateRelativePath(String rawPageName) {
		// Processing relative path mode =x
		rawPageName = filterRawPageName(rawPageName);
		
		// Check for blank
		if (rawPageName == null || rawPageName.length() <= 0) {
			return "./";
		}
		
		// Split the raw page name
		String[] splitPageName = rawPageName.split("/");
		
		// Return the path in accordance to the length
		StringBuilder ret = new StringBuilder(".");
		for (int i = 0; i < splitPageName.length; ++i) {
			ret.append("/..");
		}
		
		// Return the result
		return ret.toString();
	}
	
	////////////////////////////////////////////////////////////
	//
	// HTML handling
	//
	////////////////////////////////////////////////////////////
	
	/// Gets the prefix
	public String prefixHTML(String rawPageName) throws IOException {
		return processJMTE(getCommonPrefixOrSuffixHtml(rawPageName, "prefix"),
			pageJMTEvars(rawPageName), rawPageName);
	}
	
	/// Gets the prefix
	public String suffixHTML(String rawPageName) throws IOException {
		return processJMTE(getCommonPrefixOrSuffixHtml(rawPageName, "suffix"),
			pageJMTEvars(rawPageName), rawPageName);
	}
	
	/// Gets and returns a page frame raw string without going through the JMTE parser
	public String buildPageInnerRawHTML(String rawPageName) throws IOException {
		// Depenency chain tracking
		rawPageName = filterRawPageName(rawPageName);
		addDependencyTracking(rawPageName);
		
		String indexFileStr = FileUtil.readFileToString_withFallback(new File(pageFolder, rawPageName
			+ "/" + safePageName(rawPageName) + ".html"), null);
		if (indexFileStr == null) {
			indexFileStr = FileUtil.readFileToString_withFallback(new File(pageFolder, rawPageName
				+ "/index.html"), "");
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
	public String buildPageInnerHTML(String rawPageName) throws IOException {
		return buildPageInnerHTML(rawPageName, null);
	}
	
	/// Gets and returns a page frame string, with its respective JMTE input vars?
	public String buildPageInnerHTML(String rawPageName, Map<String, Object> jmteTemplate)
		throws IOException {
		String indexFileStr = FileUtil.readFileToString_withFallback(new File(pageFolder, rawPageName
			+ "/" + safePageName(rawPageName) + ".html"), null);
		if (indexFileStr == null) {
			indexFileStr = FileUtil.readFileToString_withFallback(new File(pageFolder, rawPageName
				+ "/index.html"), "");
		}
		
		if ((indexFileStr = indexFileStr.trim()).length() == 0) {
			if (hasPageFile(rawPageName)) { //has file
				return ""; //this is a blank HTML file
			}
			return null;
		}
		
		if (jmteTemplate == null) {
			jmteTemplate = pageJMTEvars(rawPageName);
		}
		
		return processJMTE(indexFileStr.toString(), jmteTemplate, rawPageName);
	}
	
	/// Get the page frame div header, this is used to do a "search replace" for script / css injection
	protected String pageFrameHeaderDiv(String rawPageName) {
		return "<div class='pageFrame " + pageFrameID(rawPageName) + " "
			+ pageFrameID_ifamLegacy(rawPageName) + "' id='" + pageFrameID_ifamLegacy(rawPageName)
			+ "'>\n";
	}
	
	/// Builds a rawPageName HTML frame
	public String buildPageFrame(String rawPageName) throws IOException {
		return buildPageFrame(rawPageName, null);
	}
	
	/// Builds a rawPageName HTML frame
	public String buildPageFrame(String rawPageName, String injectionStr) throws IOException {
		Map<String, Object> templateVars = pageJMTEvars(rawPageName);
		String innerHTML = buildPageInnerHTML(rawPageName, templateVars);
		if (innerHTML == null) {
			return null;
		}
		
		StringBuilder frame = new StringBuilder();
		frame.append(pageFrameHeaderDiv(rawPageName));
		if (injectionStr != null) {
			injectionStr = processJMTE(injectionStr, templateVars, rawPageName);
			frame.append(injectionStr);
		}
		
		frame.append(innerHTML);
		frame.append("\n</div>\n");
		return frame.toString();
	}
	
	/// Builds the FULL rawPageName HTML, with prefix and suffix
	public StringBuilder buildFullPageFrame(String rawPageName) throws IOException {
		return buildFullPageFrame(rawPageName, null);
	}
	
	/// Builds the FULL rawPageName HTML, with prefix and suffix
	public StringBuilder buildFullPageFrame(String rawPageName, String injectionStr)
		throws IOException {
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
	// Subpage searching handling
	//
	////////////////////////////////////////////////////////////
	
	///
	/// Sub page name listing
	///
	/// @param rawPageName Parent page to search from
	///
	/// @return Collection of sub page name from the parent page
	///
	public Set<String> subPageList(String rawPageName) {
		rawPageName = filterRawPageName(rawPageName);
		HashSet<String> res = new HashSet<String>();
		
		// The current folder to scan
		File folder = new File(pageFolder, rawPageName);
		
		// Scan for subdirectories ONLY if this is a directory
		if (folder.isDirectory()) {
			// For each sub directory, build it as a page
			for (File pageDefine : FileUtil.listDirs(folder)) {
				// Get sub page name
				String subPageName = pageDefine.getName();
				
				// Could be hidden, or invalid folder format
				if (subPageName.startsWith(".")
					|| !subPageName.equalsIgnoreCase(FileUtil.getBaseName(subPageName))) {
					continue;
				}
				
				// Common and index zone only if its top layer
				if (subPageName.equalsIgnoreCase("common") || subPageName.equalsIgnoreCase("index")) {
					if (rawPageName.length() <= 0) {
						res.add(subPageName);
					}
				} else if (subPageName.equalsIgnoreCase("assets")
					|| subPageName.equalsIgnoreCase("web-inf")) {
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
	/// Build and returns the page component map
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
			File definitionFolder = new File(pageFolder, pageName);
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
			File definitionFolder = new File(pageFolder, pageName);
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
	/// @param  The page name used, used in deriving relative file path logic
	/// @param  The JMTE variable map to use
	///
	/// @return true, if a file was processed and written
	public boolean processPageFile(PageFileType type, File input, File output, String rawPageName,
		String relativeRawPageName, Map<String, Object> jmteVarMap) throws IOException {
		if (input.exists() && input.isFile() && input.canRead()) {
			// Gets its string value, and process only if not blank
			String fileVal = FileUtil.readFileToString(input);
			if ((fileVal = fileVal.trim()).length() > 0) {
				
				// Does specific conversions
				if (type == PageFileType.jsons_to_js) {
					
					// Does a JMTE filter
					fileVal = processJMTE(fileVal, jmteVarMap, relativeRawPageName);
					
					// Adds the script object wrapper
					fileVal = "window.PageComponent = window.PageComponent || {}; window.PageComponent."
						+ safePageName(rawPageName) + " = (" + fileVal + ");";
				} else if (type == PageFileType.less_to_css) {
					
					/// Add the config .less file
					String lessPrefix = getCommonFile(rawPageName, "prefix.less");
					String lessSuffix = getCommonFile(rawPageName, "suffix.less");
					
					///
					/// Outerwrap class isolation, is now deprecated 
					///
					
					// /// Does an outer wrap, if its not index page (which applies style to 'all')
					// if (!rawPageName.equalsIgnoreCase("index") && !rawPageName.equalsIgnoreCase("common")) {
					// 	fileVal = "." + pageFrameID(rawPageName) + " { \n" + fileVal + "\n } \n";
					// }
					
					// Ensure prefix, and suffix are added
					fileVal = (lessPrefix + "\n" + fileVal + "\n" + lessSuffix).trim();
					
					// Does a JMTE filter
					fileVal = processJMTE(fileVal, jmteVarMap, relativeRawPageName);
					
					// Less to css conversion
					fileVal = less.compile(fileVal);
				} else {
					// Does a JMTE filter
					fileVal = processJMTE(fileVal, jmteVarMap, relativeRawPageName);
				}
				
				// Write to file if it differ
				FileUtil.writeStringToFile_ifDifferant(output, fileVal);
				
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
	/// @param  The page name used, used in deriving relative file path logic
	/// @param  The JMTE variable map to use
	///
	/// @return true, if a file was processed and written
	public boolean processPageFile(PageFileType type, File[] inputArr, File output,
		String rawPageName, String relativeRawPageName, Map<String, Object> jmteVarMap)
		throws IOException {
		for (File input : inputArr) {
			if (processPageFile(type, input, output, rawPageName, relativeRawPageName, jmteVarMap)) {
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
			throw new RuntimeException("Unable to load page name, that may bypass WEB-INF : "
				+ rawPageName);
		}
		
		try {
			
			// Prepares output and definition FILE objects, and JMTE map
			//-------------------------------------------------------------------
			File outputPageFolder = new File(outputFolder, rawPageName);
			File definitionFolder = new File(pageFolder, rawPageName);
			Map<String, Object> jmteVarMap = pageJMTEvars(rawPageName);
			String pageName_safe = safePageName(rawPageName);
			
			// Create the output folder as needed
			//-------------------------------------------------------------------
			if (!outputPageFolder.exists()) {
				outputPageFolder.mkdirs();
			}
			
			// Copy the page assets folder
			//-------------------------------------------------------------------
			
			// Folder to copy from
			boolean hasAssets = false;
			File pageAssetsFolder = new File(definitionFolder, "assets");
			if (pageAssetsFolder.exists() && pageAssetsFolder.isDirectory()) {
				// Copy if folder to target
				FileUtil.copyDirectory_ifDifferent(pageAssetsFolder, new File(outputPageFolder,
					"assets"), true, false);
				hasAssets = true;
			}
			
			// Copy out all the various other files
			//
			// Ignores: index files (as of now)
			//-------------------------------------------------------------------
			
			// For each sub file, clone it
			for (File inFile : FileUtil.listFiles(definitionFolder, null, false)) {
				String fileName = inFile.getName();
				
				// Ignore hidden files, or index based files
				String baseName = FileUtil.getBaseName(fileName);
				String fileExtn = FileUtil.getExtension(fileName);
				if (baseName == null || baseName.length() <= 0 || baseName.equalsIgnoreCase("index") /* || baseName.equalsIgnoreCase("depend") || baseName.equalsIgnoreCase("component") || baseName.equalsIgnoreCase("page") */) {
					continue;
				}
				
				// File to output to
				File outFile = new File(outputPageFolder, fileName);
				
				// Copy over the file
				FileUtil.copyFile_ifDifferent(inFile, outFile, true, false);
				
				// Ignore conversions on certain same filepath - name (legacy files)
				if (rawPageName.equalsIgnoreCase(baseName)) {
					continue;
				}
				
				// Less and es6 specific conversion
				if (fileExtn.equalsIgnoreCase("less")) {
					util.compileFileIfNewer(fileExtn, inFile, new File(outputPageFolder, baseName
						+ ".css"));
				} else if (fileExtn.equalsIgnoreCase("es6")) {
					util.compileFileIfNewer(fileExtn, inFile, new File(outputPageFolder, baseName
						+ ".js"));
				}
			}
			
			// For each sub file, clone it
			for (File inFile : FileUtil.listDirs(definitionFolder)) {
				
				// Ignore hidden files, or index based files
				String fileName = inFile.getName();
				
				// Ignore protected folders, ignore already copied assets folder
				if (fileName.startsWith(".") || fileName.equalsIgnoreCase("WEB-INF")
					|| fileName.equalsIgnoreCase("assets")) {
					continue;
				}
				
				// Clone files, these are not processed recursively
				if (fileName.indexOf(".") >= 0 || fileName.indexOf("-") >= 0) {
					// File to output to
					File outFile = new File(outputPageFolder, fileName);
					
					// Copy over the file
					FileUtil.copyDirectory_ifDifferent(inFile, outFile, true, false);
				}
			}
			
			// Process the JS script (if provided)
			//-------------------------------------------------------------------
			boolean hasJsFile = processPageFile(PageFileType.js, new File[] {
				new File(definitionFolder, "index.js"),
				new File(definitionFolder, pageName_safe + ".js") }, new File(outputPageFolder,
				pageName_safe + ".js"), rawPageName, rawPageName, jmteVarMap);
			
			// Build the JSONS script (if provided)
			//-------------------------------------------------------------------
			boolean hasJsonsFile = processPageFile(PageFileType.jsons_to_js, new File[] {
				new File(definitionFolder, "index.jsons"),
				new File(definitionFolder, pageName_safe + ".jsons") }, new File(outputPageFolder,
				pageName_safe + ".jsons.js"), rawPageName, rawPageName, jmteVarMap);
			
			// Build the LESS script (if provided)
			//-------------------------------------------------------------------
			boolean hasLessFile = processPageFile(PageFileType.less_to_css, new File[] {
				new File(definitionFolder, "index.less"),
				new File(definitionFolder, pageName_safe + ".less") }, new File(outputPageFolder,
				pageName_safe + ".css"), rawPageName, rawPageName, jmteVarMap);
			
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
					injectorStrBuilder
						.append("<link rel='stylesheet' type='text/css' href='${PageRootURI}/"
							+ rawPageName + "/" + pageName_safe + ".css'></link>\n");
				}
			}
			if (hasJsFile) {
				if (indexStr.indexOf(rawPageName + "/" + pageName_safe + ".js") > 0) {
					// Skips injection if already included
				} else {
					injectorStrBuilder.append("<script src='${PageRootURI}/" + rawPageName + "/"
						+ pageName_safe + ".js'></script>\n");
				}
			}
			if (hasJsonsFile) {
				if (indexStr.indexOf(rawPageName + "/" + pageName_safe + ".jsons.js") > 0) {
					// Skips injection if already included
				} else {
					injectorStrBuilder.append("<script src='${PageRootURI}/" + rawPageName + "/"
						+ pageName_safe + ".jsons.js'></script>\n");
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
			
			// Component resolution
			//-------------------------------------------------------------------
			indexStr = componentFilter.resolve(indexStr, rawPageName);
			
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
			FileUtil.writeStringToFile_ifDifferant(new File(outputPageFolder, "index.html"), indexStr);
			
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
	/// Builds all page (NOT including itself) inside a page folder
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
		File folder = new File(pageFolder, rawPageName);
		
		// Possible page pathing error fix
		if (rawPageName.length() > 0 && !rawPageName.endsWith("/")) {
			rawPageName = rawPageName + "/";
		}
		
		// Scan for subdirectories ONLY if this is a directory
		if (folder.isDirectory()) {
			// For each sub directory, build it as a page
			for (File pageDefine : FileUtil.listDirs(folder)) {
				// Build each page
				String subPageName = pageDefine.getName();
				
				// Could be hidden, or invalid folder format
				if (subPageName.startsWith(".")
					|| !subPageName.equalsIgnoreCase(FileUtil.getBaseName(subPageName))) {
					continue;
				}
				
				// these are not processed recursively, avoid pathing ambiguity
				if (subPageName.indexOf(".") >= 0 || subPageName.indexOf("-") >= 0) {
					continue;
				}
				
				// Scan for sub page
				if (subPageName.equalsIgnoreCase("common") || subPageName.equalsIgnoreCase("index")) {
					buildAndOutputPage(rawPageName + subPageName);
					if (rawPageName.length() <= 0) {
						//buildAndOutputPage(rawPageName + subPageName);
					} else {
						System.out
							.print("> PageBuilder[Core].buildPageFolder - WARNING, common / index nested build (\'"
								+ rawPageName + "\', \'" + subPageName + "\'): ");
					}
				} else if (subPageName.equalsIgnoreCase("assets")
					|| subPageName.equalsIgnoreCase("web-inf") || subPageName.equalsIgnoreCase("build")) {
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
	/// Builds all page (INCLUDING itself, if possible) inside a page folder
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
		return dependencyTracker;
	}
	
	/// Reset the Dependency tracking
	public void dependencyTrackerReset() throws IOException {
		dependencyTracker.clear();
		
		addDependencyTracking("");
		addDependencyTracking("component");
		addDependencyTracking("common");
		addDependencyTracking("index");
	}
	
	/// Gets the dependency map for path
	protected GenericConvertMap<String, Object> depenencyConfig(String rawPageName)
		throws IOException {
		String dependJson = FileUtil.readFileToString_withFallback(new File(pageFolder,
			filterRawPageName(rawPageName) + "/depend.json"), "{}");
		return GenericConvert.toGenericConvertStringMap(dependJson, "{}");
	}
	
	/// Normalize case sensitivity of path
	protected String normalizeDependencyPath(String dependPath) {
		return componentFilter.normalizeComponentPath(dependPath);
	}
	
	/// Recursively pull add in the depency of a existing module, if its not already on the list
	protected void addDependencyTracking(String dependPath) throws IOException {
		// Normalize the path
		dependPath = filterRawPageName(dependPath);
		
		// Check if it already exists
		if (dependencyTracker.contains(dependPath)) {
			return;
		}
		
		// Normalize the path
		dependPath = normalizeDependencyPath(dependPath);
		
		// Check if it already exists after normalizing
		if (dependencyTracker.contains(dependPath)) {
			return;
		}
		
		// Add to track (prevent circular reference conflict)
		dependencyTracker.add(dependPath);
		
		// Get submodules
		String[] sublist = depenencyConfig(dependPath).getStringArray("component", "[]");
		for (String submodule : sublist) {
			addDependencyTracking(submodule);
		}
		
		// Actual add (at the end), remove from position first
		dependencyTracker.remove(dependPath);
		dependencyTracker.add(dependPath);
	}
	
	/// Get dependency file into a string builder utility
	protected StringBuilder dependencyGetSingleFile(StringBuilder res, String pageFilePath,
		String checkType) throws IOException {
		String fileData = FileUtil.readFileToString_withFallback(new File(pageFolder, pageFilePath),
			null);
		if (fileData != null) {
			if (checkType.equalsIgnoreCase("es6")) {
				if (fileData.indexOf("${PageRootURI}") >= 0 || fileData.indexOf("${ApiRootURI}") >= 0) {
					throw new RuntimeException(
						"("
							+ pageFilePath
							+ ") Do not use ${Page/ApiRootURI} inside depend.es6 scripts as it does not resolve properly. Use PageComponent.page/apiRootURI instead");
				}
			}
			
			res.append("/** file:" + pageFilePath + " **/\n"); //Added file name reference
			res.append(fileData);
			res.append("\n");
		}
		return res;
	}
	
	/// Dependency build a single set, no recursion calls
	protected StringBuilder dependencyBuiltPagePart(StringBuilder res, String rawPageName,
		String type) throws IOException {
		//
		// Get pathing, and does safety checks
		//
		String path = filterRawPageName(rawPageName);
		if (!(type.equalsIgnoreCase("less") || type.equalsIgnoreCase("es6"))) {
			throw new RuntimeException("Unexpected dpendency type : " + type);
		}
		
		//
		// Process JMTE filtered scripts
		// 
		Map<String, Object> jmteVars = pageJMTEvars(path);
		StringBuilder toJMTEParse = new StringBuilder();
		if (type.equalsIgnoreCase("less")) {
			dependencyGetSingleFile(toJMTEParse, path + "/depend.less", type);
			dependencyGetSingleFile(toJMTEParse, path + "/depend.css", type);
		} else {
			dependencyGetSingleFile(toJMTEParse, path + "/depend.es6", type);
			dependencyGetSingleFile(toJMTEParse, path + "/depend.js", type);
		}
		
		//
		// Process JMTE scripts
		//
		String[] fileList = depenencyConfig(path).getStringArray(type + "_jmte", "[]");
		for (String filePath : fileList) {
			// Parse any templates if need be
			filePath = processJMTE(filePath, jmteVars, path);
			
			// Changes relative path to fixed path
			if (!filePath.startsWith("/")) {
				filePath = FileUtil.normalize(rawPageName + "/" + filePath);
			}
			
			// Get and inject, without JMTE parsing
			dependencyGetSingleFile(toJMTEParse, filePath, type);
		}
		
		String toJMTEParseString = toJMTEParse.toString();
		res.append(processJMTE(toJMTEParseString, jmteVars, "build"));
		
		//
		// Process non fitlered scripts
		//
		fileList = depenencyConfig(path).getStringArray(type, "[]");
		for (String filePath : fileList) {
			// Parse any templates if need be
			filePath = processJMTE(filePath, jmteVars, path);
			
			// Changes relative path to fixed path
			if (!filePath.startsWith("/")) {
				filePath = FileUtil.normalize(rawPageName + "/" + filePath);
			}
			
			// Get and inject, without JMTE parsing
			dependencyGetSingleFile(res, filePath, type);
		}
		return res;
	}
	
	/// Build the whole dependency file for a single type
	protected String dependencyBuildFile(String type) throws IOException {
		StringBuilder res = new StringBuilder();
		
		res.append("/**** " + type + " dependencies for the following component ****\n\n");
		for (String path : dependencyTracker) {
			res.append("+ '" + path + "'\n");
		}
		res.append("\n*******************************************************/\n\n");
		
		for (String path : dependencyTracker) {
			dependencyBuiltPagePart(res, path, type);
		}
		return res.toString();
	}
	
	/// Builds the LESS from the depency chain
	public String dependencyLess() throws IOException {
		return dependencyBuildFile("less");
	}
	
	/// Builds the CSS from the depency chain
	public String dependencyCss() throws IOException {
		return less.compile(dependencyLess());
	}
	
	/// Builds the ES6 from the depency chain
	public String dependencyES6() throws IOException {
		return dependencyBuildFile("es6");
	}
	
	/// Builds the CSS from the depency chain
	public String dependencyJS() throws IOException {
		// System.out.println("dependencyBuildFile - es6");
		// System.out.println(dependencyBuildFile("depend.es6", "depend.js"));
		// System.out.println("dependencyBuildFile - es6 + JMTE");
		// System.out.println(dependencyES6());
		return CompileES6.compile(dependencyES6(), "depend.es6");
	}
}

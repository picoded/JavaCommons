package picoded.page.builder;

import java.io.*;
import java.util.*;
import javax.servlet.http.HttpServletResponse;

import picoded.enums.*;
import picoded.conv.*;
import picoded.struct.*;
import picoded.fileUtils.*;
import picoded.servlet.*;
import picoded.servletUtils.*;

///
/// Rapid pages prototyping, support single and multipage mode, caching, etc.
/// Basically lots of good stuff =)
///
/// This is extended from the templating format previously used in ServletCommons
///
public class PageBuilder extends PageBuilderCore {
	
	////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////
	
	///
	/// Constructor, with the folders defined
	///
	/// @param The various pages definition folder
	/// @param The target folder to build the result into
	///
	public PageBuilder(File inPagesFolder, File inOutputFolder) {
		super(inPagesFolder, inOutputFolder);
		validateAndSetup();
	}
	
	///
	/// Constructor, with the folders defined
	///
	/// @param The various pages definition folder
	/// @param The target folder to build the result into
	///
	public PageBuilder(String inPagesFolder, String inOutputFolder) {
		this(new File(inPagesFolder), new File(inOutputFolder));
	}
	
	/// Does some basic validation for the constructor
	protected void validateAndSetup() {
		//
		// NULL check 
		//
		if (pagesFolder == null) {
			throw new RuntimeException("Pages definition folder is not set (null)");
		}
		if (outputFolder == null) {
			throw new RuntimeException("Output folder is not set (null)");
		}
		
		//
		// Exists checks
		//
		if (!pagesFolder.exists()) {
			throw new RuntimeException("Pages definition folder does not exists: " + pagesFolder.getPath());
		}
		if (!outputFolder.exists()) {
			throw new RuntimeException("Output folder does not exists: " + outputFolder.getPath());
		}
		
		//
		// IsDir checks
		//
		if (!pagesFolder.isDirectory()) {
			throw new RuntimeException("Pages definition folder path is not a 'directory': " + pagesFolder.getPath());
		}
		if (!outputFolder.isDirectory()) {
			throw new RuntimeException("Output folder path is not a 'directory': " + outputFolder.getPath());
		}
		
		//
		// HTML files handling
		//
		// html = new PageBuilderCore(pagesFolder, outputFolder);
	}
	
	////////////////////////////////////////////////////////////
	//
	// Public function
	//
	////////////////////////////////////////////////////////////
	
	/// Build depenecy files, this should only be called after all the various standard pages are built
	protected void buildDependency() {
		try {
			FileUtils.writeStringToFile_ifDifferant(new File(outputFolder, "build/depend.less"), null /*"UTF-8"*/, dependencyLess());
			FileUtils.writeStringToFile_ifDifferant(new File(outputFolder, "build/depend.css"), null /*"UTF-8"*/, dependencyCss());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/// Scans and builds all the pages
	///
	/// @return  Returns itself
	public PageBuilder buildAllPages() {
		// Recusively build all pages
		buildPageFolder("");
		
		// Build the depency
		buildDependency();
		
		// End and returns self
		return this;
	}
	
	////////////////////////////////////////////////////////////
	//
	// Servlet building utility
	//
	////////////////////////////////////////////////////////////
	
	/// Cached FileServlet
	protected FileServlet _outputFileServlet = null;
	
	/// Returns the File servlet
	public FileServlet outputFileServlet() {
		if (_outputFileServlet != null) {
			return _outputFileServlet;
		}
		return (_outputFileServlet = new FileServlet(outputFolder));
	}
	
	/// Process the full PageBuilder servlet request
	public void processPageBuilderServlet(BasePage page) {
		processPageBuilderServlet(page, page.requestWildcardUriArray());
	}
	
	/// Process the full PageBuilder servlet request
	public void processPageBuilderServlet(BasePage page, String[] requestWildcardUri) {
		try {
			if (requestWildcardUri == null) {
				requestWildcardUri = new String[] {};
			}
			
			// Load base page name (developer mode rebuilds an entire sub-dir, to optimize in future?)
			String basePageName = "index";
			if (requestWildcardUri.length >= 1 && requestWildcardUri[0].length() > 0) {
				basePageName = requestWildcardUri[0];
			}
			
			String itemName = "index.html";
			// Check if there is an item request
			if (requestWildcardUri.length >= 2) {
				String suffixName = requestWildcardUri[requestWildcardUri.length - 1];
				if (suffixName.indexOf(".") > 0) {
					itemName = suffixName; //Update as item name
				}
			}
			
			boolean isDeveloperMode = (page.JConfig().getBoolean("developersMode.enabled", true) && page.JConfig()
				.getBoolean("developersMode.PageBuilder", true));
			if (isDeveloperMode) {
				
				PageBuilder servletPageBuilder = page.PageBuilder();
				
				// Load the respective page, only on main page load
				if (itemName.equals("index.html") && servletPageBuilder.hasPageFolder(basePageName + "/")) {
					servletPageBuilder.buildPageFolder_includingSelf(basePageName + "/");
					
					// Build index / common, skip if its a duplicate build request
					if (!basePageName.equalsIgnoreCase("index") && servletPageBuilder.hasPageFolder("index")) {
						servletPageBuilder.buildAndOutputPage("index");
					}
					if (!basePageName.equalsIgnoreCase("common") && servletPageBuilder.hasPageFolder("common")) {
						servletPageBuilder.buildAndOutputPage("common");
					}
				}
			}
			
			String reqStr = String.join("/", requestWildcardUri);
			if (reqStr == null) {
				reqStr = "";
			}
			if (!reqStr.endsWith(itemName)) {
				reqStr = reqStr + "/" + itemName;
			}
			String reqStr_lowerCase = reqStr.toLowerCase();
			
			// Security measure? against possible tmp folder access
			if (reqStr_lowerCase.contains("/tmp") || reqStr_lowerCase.contains("/web-inf")) {
				// 404 error if file not found
				page.getHttpServletResponse().sendError(HttpServletResponse.SC_NOT_FOUND);
				return;
			}
			
			if (reqStr.isEmpty()) {
				// 404 error if file not found
				page.getHttpServletResponse().sendError(HttpServletResponse.SC_NOT_FOUND);
				return;
			}
			
			File requestedFile = (new File(page.getPagesOutputPath(), reqStr));
			if (!requestedFile.exists() || requestedFile.isDirectory()) {
				// 404 error if file does not exists
				page.getHttpServletResponse().sendError(HttpServletResponse.SC_NOT_FOUND);
				return;
			}
			
			// Fallsback into File Servlet
			outputFileServlet().processRequest( //
				page.getHttpServletRequest(), //
				page.getHttpServletResponse(), //
				page.requestType() == HttpRequestType.HEAD, //
				reqStr);
			
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}

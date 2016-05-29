package picoded.webTemplateEngines.PagesBuilder;

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
public class PagesBuilder extends PagesBuilderCore {
	
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
	public PagesBuilder(File inPagesFolder, File inOutputFolder) {
		super(inPagesFolder, inOutputFolder);
		validateAndSetup();
	}
	
	///
	/// Constructor, with the folders defined
	///
	/// @param The various pages definition folder
	/// @param The target folder to build the result into
	///
	public PagesBuilder(String inPagesFolder, String inOutputFolder) {
		this(new File(inPagesFolder), new File(inOutputFolder));
	}
	
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
		// html = new PagesBuilderCore(pagesFolder, outputFolder);
	}
	
	////////////////////////////////////////////////////////////
	//
	// Public function
	//
	////////////////////////////////////////////////////////////
	
	/// 
	/// Indicates if the page definition folder exists
	///
	public boolean hasPageDefinition(String pageName) {
		try {
			File definitionFolder = new File(pagesFolder, pageName);
			return definitionFolder.exists() && definitionFolder.isDirectory();
		} catch (Exception e) {
			// Failed?
		}
		return false;
	}
	
	///
	/// Builds all the assets for a single page
	///
	/// @param PageName to build
	///
	public PagesBuilder buildPage(String pageName) {
		buildAndOutputPage(pageName);
		return this;
	}
	
	///
	/// Builds all the pages
	///
	public PagesBuilder buildAllPages() {
		buildAllPages_internal("");
		// End and returns self
		return this;
	}
	
	///
	/// The recursive internal function varient
	///
	public void buildAllPages_internal(String prefixPath) {
		// For each directory, build it as a page
		for (File pageDefine : FileUtils.listDirs(new File(pagesFolder, prefixPath))) {
			// Build each page
			String subPageName = pageDefine.getName();
			buildPage(prefixPath+subPageName);
			
			// Scan for sub pages
			if( 
				subPageName.equalsIgnoreCase("assets") || 
				subPageName.equalsIgnoreCase("common") || 
				subPageName.equalsIgnoreCase("index") 
			) {
				// ignoring certain reserved folders
			} else {
				buildAllPages_internal(prefixPath+subPageName+"/");
			}
		}
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
				requestWildcardUri = new String[0];
			}
			
			// Load page name
			String pageName = "index";
			if (requestWildcardUri.length >= 1) {
				pageName = requestWildcardUri[0];
			}
			
			// Load page name
			String itemName = "index.html";
			if (requestWildcardUri.length >= 2) {
				itemName = requestWildcardUri[1];
			} else {
				requestWildcardUri = new String[] { pageName, itemName };
			}
			
			boolean isDeveloperMode = (page.JConfig().getBoolean("developersMode.enabled", true) && page.JConfig()
				.getBoolean("developersMode.PagesBuilder", true));
			if (isDeveloperMode) {
				// Load the respective page
				if (requestWildcardUri.length == 2 && itemName.equals("index.html")) {
					PagesBuilder servletPagesBuilder = page.PagesBuilder();
					
					// Load the index and common pages (if applicable)
					if (servletPagesBuilder.hasPageDefinition("index")) {
						servletPagesBuilder.buildPage("index");
					}
					if (servletPagesBuilder.hasPageDefinition("common")) {
						servletPagesBuilder.buildPage("common");
					}
					
					if (servletPagesBuilder.hasPageDefinition(pageName)) {
						servletPagesBuilder.buildPage(pageName);
					}
				}
			}
			
			String reqStr = String.join("/", requestWildcardUri);
			// Security measure?
			// if( reqStr != null && reqStr.contains("/tmp") ) {
			// 	// 404 error if file not found
			// 	page.getHttpServletResponse().sendError(HttpServletResponse.SC_NOT_FOUND);
			// 	return;
			// }
			
			if (reqStr == null || reqStr.isEmpty()) {
				// 404 error if file not found
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

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
public class PagesBuilder {
	
	////////////////////////////////////////////////////////////
	//
	// Local variables
	//
	////////////////////////////////////////////////////////////
	
	/// The folder to get the various page definition from
	protected File pagesFolder = null;
	
	/// The output folder to process the output to
	protected File outputFolder = null;
	
	/// The local JMTE reference
	protected JMTE jmteObj = null;
	
	/// The pagesHTML handler
	protected PagesHTML html = null;
	
	/// LESS compiler
	protected LessToCss less = new LessToCss();
	
	/// The URI root context for built files
	protected String uriRootPrefix = "./";
	
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
		pagesFolder = inPagesFolder;
		outputFolder = inOutputFolder;
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
	
	////////////////////////////////////////////////////////////
	//
	// Public vars access
	//
	////////////////////////////////////////////////////////////
	
	/// @returns Gets the protected JMTE object, used internally
	public JMTE getJMTE() {
		if(jmteObj == null) {
			jmteObj = new JMTE();
		}
		return jmteObj;
	}
	
	/// Overides the default (if loaded) JMTE object. 
	public void setJMTE(JMTE set) {
		jmteObj = set;
		if(html != null) {
			html.setJMTE(jmteObj);
		}
	}
	
	/// @returns Gets the protected uriRootPrefix, used internally
	public String getUriRootPrefix() {
		return uriRootPrefix;
	}
	
	/// Overides the uriRootPrefix. 
	public void setUriRootPrefix(String set) {
		if( !set.endsWith("/") ) {
			set = set+"/";
		}
		
		uriRootPrefix = set;
		if(html != null) {
			html.uriRootPrefix = uriRootPrefix;
		}
	}
	
	////////////////////////////////////////////////////////////
	//
	// Protected inner vars / functions
	//
	////////////////////////////////////////////////////////////
	
	protected PagesHTML html() {
		if(html != null) {
			return html;
		}
		html = new PagesHTML(pagesFolder);
		html.setJMTE( getJMTE() );
		html.uriRootPrefix = uriRootPrefix;
		return html;
	}
	
	////////////////////////////////////////////////////////////
	//
	// Utility functions
	//
	////////////////////////////////////////////////////////////
	
	protected void validateAndSetup() {
		//
		// NULL check
		//
		if( pagesFolder == null ) {
			throw new RuntimeException("Pages definition folder is not set (null)");
		}
		if( outputFolder == null ) {
			throw new RuntimeException("Output folder is not set (null)");
		}
		
		//
		// Exists checks
		//
		if( !pagesFolder.exists() ) {
			throw new RuntimeException("Pages definition folder does not exists: "+pagesFolder.getPath());
		}
		if( !outputFolder.exists() ) {
			throw new RuntimeException("Output folder does not exists: "+outputFolder.getPath());
		}
		
		//
		// IsDir checks
		//
		if( !pagesFolder.isDirectory() ) {
			throw new RuntimeException("Pages definition folder path is not a 'directory': "+pagesFolder.getPath());
		}
		if( !outputFolder.isDirectory() ) {
			throw new RuntimeException("Output folder path is not a 'directory': "+outputFolder.getPath());
		}
		
		//
		// HTML files handling
		//
		html = new PagesHTML(pagesFolder);
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
		} catch(Exception e) {
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
		
		// Future extension, possible loop hole abuse. Im protecting against it early
		if( pageName.startsWith(".") ) {
			throw new RuntimeException("Unable to load page name, starting with '.' : "+pageName);
		}
		if( pageName.toLowerCase().indexOf("web-inf") >= 0 ) {
			throw new RuntimeException("Unable to load page name, that may bypass WEB-INF : "+pageName);
		}
		
		try {
			
			// Prepares output and definition FILE objects, and JMTE map
			//-------------------------------------------------------------------
			File outputPageFolder = new File(outputFolder, pageName);
			File definitionFolder = new File(pagesFolder, pageName);
			Map<String,Object> jmteVarMap = html().pageJMTEvars(pageName);
			
			// Create the output folder as needed
			//-------------------------------------------------------------------
			if(!outputPageFolder.exists()) {
				outputPageFolder.mkdirs();
			}
			
			// Copy the page assets folder
			//
			// @TODO: Optimize this to only copy IF newer
			//-------------------------------------------------------------------
			
			// Folder to copy from
			File pageAssetsFolder = new File(definitionFolder, "assets");
			if( pageAssetsFolder.exists() && pageAssetsFolder.isDirectory() ) {
				// Copy if folder to target
				FileUtils.copyDirectory( pageAssetsFolder, new File(outputPageFolder, "assets") );
			}
			
			// Process the JS script (if provided)
			//-------------------------------------------------------------------
			
			// Indicates if there is a JS script
			boolean hasJsFile = false;
			
			// The JS file source location
			File jsFile = new File(definitionFolder, pageName+".js");
			
			// Process file only if its readable
			if( jsFile.exists() && jsFile.isFile() && jsFile.canRead() ) {
				
				// Gets its string value, and process only if not blank
				String jsString = FileUtils.readFileToString(jsFile, "UTF-8");
				if( (jsString = jsString.trim()).length() > 0 ) {
					
					// Does a JMTE filter
					jsString = getJMTE().parseTemplate(jsString, jmteVarMap);
					
					// Write to file if it differ
					FileUtils.writeStringToFile_ifDifferant( new File(outputPageFolder, pageName+".js"), "UTF-8", jsString );
					
					// Indicate file is "deployed"
					hasJsFile = true;
				}
			}
			
			// Build the JSONS script (if provided)
			//-------------------------------------------------------------------
			
			// Indicates if there is a JSONS script
			boolean hasJsonsFile = false;
			
			// The JSONS file source location
			File jsonsFile = new File(definitionFolder, pageName+".jsons");
			
			// Process file only if its readable
			if( jsonsFile.exists() && jsonsFile.isFile() && jsonsFile.canRead() ) {
				
				// Gets its string value, and process only if not blank
				String jsonsString = FileUtils.readFileToString(jsonsFile, "UTF-8");
				if( (jsonsString = jsonsString.trim()).length() > 0 ) {
					
					// Adds the script object wrapper
					jsonsString = "window.pages = window.pages | {}; window.pages."+pageName+" = ("+jsonsString+");";
					
					// Does a JMTE filter
					jsonsString = getJMTE().parseTemplate(jsonsString, jmteVarMap);
					
					// Write to file if it differ
					FileUtils.writeStringToFile_ifDifferant( new File(outputPageFolder, pageName+".jsons.js"), "UTF-8", jsonsString );
					
					// Indicate file is "deployed"
					hasJsFile = true;
				}
			}
			
			// Build the LESS script
			//-------------------------------------------------------------------
			
			// Indicates if there is a LESS script
			boolean hasLessFile = false;
			
			// The LESS file source location
			File lessFile = new File(definitionFolder, pageName+".less");
			
			// Process file only if its readable
			if( lessFile.exists() && lessFile.isFile() && lessFile.canRead() ) {
				
				// Gets its string value, and process only if not blank
				String lessString = FileUtils.readFileToString(lessFile, "UTF-8");
				if( (lessString = lessString.trim()).length() > 0 ) {
					
					/// Does an outer wrap, if its not index page (which applies style to 'all')
					if( !pageName.equalsIgnoreCase("index") && !pageName.equalsIgnoreCase("common") ) {
						lessString = ".pageFrame_"+pageName+" { \n"+lessString+"\n } \n";
					}
					
					// Does a JMTE filter
					lessString = getJMTE().parseTemplate(lessString, jmteVarMap);
					
					/// Write to map file if differ
					FileUtils.writeStringToFile_ifDifferant( new File(outputPageFolder, pageName+".css.map"), "UTF-8", lessString );
					
					/// Convert LESS to CSS
					String cssString = less.compile(lessString);
					
					/// Does a simplistic compression
					cssString = cssString.trim().replaceAll("\\s+", " ");
					
					// Write to file if it differ
					FileUtils.writeStringToFile_ifDifferant( new File(outputPageFolder, pageName+".css"), "UTF-8", cssString );
					
					// Indicate file is "deployed"
					hasLessFile = true;
				}
			}
			
			// Build the html page
			//-------------------------------------------------------------------
			
			// The HTML output
			String indexStr = html().buildFullPageFrame(pageName).toString();
			
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
			FileUtils.writeStringToFile_ifDifferant( new File(outputPageFolder, "index.html"), "UTF-8", indexStr );
			
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
		// End and returns self
		//-------------------------------------------------------------------
		return this;
	}
	
	///
	/// Builds all the pages
	///
	public PagesBuilder buildAllPages() {
		// For each directory, build it as a page
		for( File pageDefine : FileUtils.listDirs(pagesFolder) ) {
			buildPage( pageDefine.getName() );
		}
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
		if( _outputFileServlet != null ) {
			return _outputFileServlet;
		}
		return (_outputFileServlet = new FileServlet(outputFolder));
	}
	
	/// Process the full PageBuilder servlet request
	public void processPageBuilderServlet( BasePage page ) {
		processPageBuilderServlet( page, page.requestWildcardUriArray() );
	}
	
	/// Process the full PageBuilder servlet request
	public void processPageBuilderServlet( BasePage page, String[] requestWildcardUri ) {
		try {
			if( requestWildcardUri == null ) {
				requestWildcardUri = new String[0];
			}
			
			// Load page name
			String pageName = "index";
			if(requestWildcardUri.length >= 1 ) {
				pageName = requestWildcardUri[0];
			}
			
			// Load page name
			String itemName = "index.html";
			if(requestWildcardUri.length >= 2 ) {
				itemName = requestWildcardUri[1];
			} else {
				requestWildcardUri = new String[] { pageName, itemName };
			}
			
			boolean isDeveloperMode = page.JConfig().getBoolean("sys.developersMode.enabled", true);
			if( isDeveloperMode ) {
				PagesBuilder servletPagesBuilder =  page.PagesBuilder();
				
				// Load the index and common pages (if applicable)
				if( servletPagesBuilder.hasPageDefinition( "index" ) ) {
					servletPagesBuilder.buildPage( "index" );
				}
				if( servletPagesBuilder.hasPageDefinition( "common" ) ) {
					servletPagesBuilder.buildPage( "common" );
				}
				
				// Load the respective page
				if( requestWildcardUri.length == 2 && itemName.equals("index.html") ) {
					if( servletPagesBuilder.hasPageDefinition( pageName ) ) {
						servletPagesBuilder.buildPage( pageName );
					}
				}
			}
			
			String reqStr = String.join("/",requestWildcardUri);
			// Security measure?
			// if( reqStr != null && reqStr.contains("/tmp") ) {
			// 	// 404 error if file not found
			// 	page.getHttpServletResponse().sendError(HttpServletResponse.SC_NOT_FOUND);
			// 	return;
			// }
			
			if( reqStr == null || reqStr.isEmpty() ) {
				// 404 error if file not found
				page.getHttpServletResponse().sendError(HttpServletResponse.SC_NOT_FOUND);
				return;
			}
			
			// Fallsback into File Servlet
			outputFileServlet().processRequest( //
				page.getHttpServletRequest(), //
				page.getHttpServletResponse(), //
				page.requestType() == HttpRequestType.HEAD, //
				reqStr
			);
			
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
}

package picoded.servlet;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.net.URL;
import java.lang.String;
import java.io.File;
import java.io.IOException;

import java.nio.ByteBuffer;
import java.nio.charset.Charset; //import java.nio.charset.StandardCharsets;
import java.util.*;

import java.lang.Number;
import java.lang.System;
import java.io.StringWriter;
import java.util.logging.*;

import java.lang.RuntimeException;
import java.lang.IllegalArgumentException;
import java.io.IOException;
import java.lang.Throwable;
import java.lang.Exception;

// Objects used
import java.util.HashMap;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.lang.Class;
import java.lang.RuntimeException;
import java.lang.Exception;

// Sub modules useds
import picoded.JStack.*;
import picoded.JSql.*;

/**
 * Extends the corePage functionality, and implements file directory listing, JStack usage, and config files
 *
 * ---------------------------------------------------------------------------------------------------------
 *
 * ##[TODO]
 *  + unit tests
 */
public class JStackPage extends CorePage {
	
	/////////////////////////////////////////////////////////////////////////////////////////////
	//
	// Static variables
	//
	/////////////////////////////////////////////////////////////////////////////////////////////
	
	// Common not so impt stuff
	//-------------------------------------------
	
	// Serialize version ID
	static final long serialVersionUID = 1L;
	
	/////////////////////////////////////////////////////////////////////////////////////////////
	//
	// Internal variables, can be overwritten. Else it is auto "filled" when needed
	//
	/////////////////////////////////////////////////////////////////////////////////////////////
	
	public String _contextPath = null;
	public String _webInfPath = null;
	public String _classesPath = null;
	public String _libraryPath = null;
	public String _configsPath = null;
	public String _pagesTemplatePath = null;
	public String _pagesOutputPath = null;
	
	/////////////////////////////////////////////////////////////////////////////////////////////
	//
	// Path variables, according to standard WAR package convention
	//
	/////////////////////////////////////////////////////////////////////////////////////////////
	
	/// Gets and returns the context path / application folder path
	public String getContextPath() {
		if (_contextPath != null) {
			return _contextPath;
		} else {
<<<<<<< HEAD
			return (_contextPath = (super.getServletContext()).getRealPath("/") + "/");
=======
			return (_contextPath = (httpRequest.getServletContext()).getRealPath("/") + "/");
>>>>>>> 1725b1dbb3d2905c94f475aa6658ae062ebe23f3
		}
	}
	
	public String getWebInfPath() {
		return (_webInfPath != null) ? _webInfPath : (_webInfPath = getContextPath() + "WEB-INF/");
	}
	
	public String getClassesPath() {
		return (_classesPath != null) ? _classesPath : (_classesPath = getWebInfPath() + "classes/");
	}
	
	public String getLibraryPath() {
		return (_libraryPath != null) ? _libraryPath : (_libraryPath = getWebInfPath() + "lib/");
	}
	
	public String getConfigsPath() {
<<<<<<< HEAD
		return (_libraryPath != null) ? _libraryPath : (_libraryPath = getWebInfPath() + "configs/");
	}
	
	public String getPagesPath() {
		return (_pagesPath != null) ? _pagesPath : (_pagesPath = getWebInfPath() + "pages/");
=======
		return (_configsPath != null) ? _configsPath : (_configsPath = getWebInfPath() + "configs/");
	}
	
	public String getPagesTemplatePath() {
		return (_pagesTemplatePath != null) ? _pagesTemplatePath : (_pagesTemplatePath = getWebInfPath() + "pages/");
	}
	
	public String getPagesOutputPath() {
		return (_pagesOutputPath != null) ? _pagesOutputPath : (_pagesOutputPath = getContextPath() + "pages/");
>>>>>>> 1725b1dbb3d2905c94f475aa6658ae062ebe23f3
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////////
	//
	// Config file handling
	//
	/////////////////////////////////////////////////////////////////////////////////////////////
	
	protected JConfig JConfigObj = null;
	
	public JConfig JConfig() {
		if (JConfigObj != null) {
			return JConfigObj;
		}
		
		JConfigObj = new JConfig();
		JConfigObj.addConfigSet(getConfigsPath(), null);
		// Intentionally DOES NOT setup the JStack config connections?
		
		return JConfigObj;
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////////
	//
	// JStack auto load handling
	//
	/////////////////////////////////////////////////////////////////////////////////////////////
	
	protected JStack JStackObj = null;
	
	/// Returns the JStack object
	/// @TODO Actual JStack config loading, now it just loads a blank sqlite file =(
	public JStack JStack() {
		if (JStackObj != null) {
			return JStackObj;
		}
		
		JStackObj = new JStack(JSql.sqlite(getWebInfPath() + "sqlite.db"));
		
		return JStackObj;
	}
	
	////////////////////////////////////////////////////
	// tableSetup calls for various jSql based modules
	////////////////////////////////////////////////////
	public void stackSetup() throws JStackException {
		JStack().stackSetup();
	}
	
	/// Called once when initialized, to purge all existing data.
	public void stackTeardown() throws JStackException {
		JStack().teardown();
	}
	
}

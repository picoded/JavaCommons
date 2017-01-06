package picoded.servlet.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Closeable;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;

import javax.servlet.Servlet;
import javax.servlet.http.HttpServlet;

import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.Wrapper;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.webresources.DirResourceSet;
import org.apache.catalina.webresources.StandardRoot;
import org.apache.catalina.core.StandardServer;
import org.apache.catalina.core.AprLifecycleListener;

import picoded.conv.GUID;

///
/// EmbeddedServlet class, provides a means of self executing any of the JavaCommons packages
/// Without having the need to use of a parent tomcat : YAYS!
///
/// On the flip side, this assumes only 1 context per deployment.
///
public class EmbeddedServlet implements Closeable {
	
	///////////////////////////////////////////////////////
	//
	// Core instance variables
	//
	///////////////////////////////////////////////////////
	
	/// The tomcat instance, this does the actual implmentation
	Tomcat tomcat = null;
	
	/// Context for the server, as mentioned above, EmbeddedServlet only support 1 context
	Context context = null;
	
	/// Context name to be used, if not provided it defaults to "ROOT"
	String contextName = "ROOT";
	
	/// The context path used, derived from context name
	String contextPath = "/";
	
	///////////////////////////////////////////////////////
	//
	// Constructor, and basic server start/stop
	//
	///////////////////////////////////////////////////////
	
	///
	/// The simplest consturctor, just point to the web application folder
	/// and run it as "ROOT" on port 8080
	///
	/// @param   File representing the 
	///
	public EmbeddedServlet(File webappPath) {
		// initTomcatInstance();
		// addWebapp("ROOT", webappPath);
		// startup();
		
		String mWorkingDir = System.getProperty("java.io.tmpdir");
		tomcat = new Tomcat();
		tomcat.setPort(8080);
		tomcat.setBaseDir(mWorkingDir);
		tomcat.getHost().setAppBase(mWorkingDir);
		tomcat.getHost().setAutoDeploy(true);
		tomcat.getHost().setDeployOnStartup(true);
		
		tomcat.setHostname("localhost");
		tomcat.enableNaming();
		
		// // Alternatively, you can specify a WAR file as last parameter in the following call e.g. "C:\\Users\\admin\\Desktop\\app.war"
		Context appContext = tomcat.addWebapp(tomcat.getHost(), "/", webappPath.getAbsolutePath());
		// LOGGER.info("Deployed " + appContext.getBaseName() + " as " + appContext.getBaseName());
		// 
		// tomcat.getServer().await();
		
		try {
			tomcat.start();
		} catch (LifecycleException e) {
			//LOGGER.severe("Tomcat could not be started.");
			e.printStackTrace();
		}
		//LOGGER.info("Tomcat started on " + tomcat.getHost());
		
	}
	
	/// Destroy the tomcat instance and removes it, if it exists
	public void close() {
		try {
			if( tomcat != null ) {
				tomcat.destroy();
				tomcat = null;
			}
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/// Finalize cleanup, does the tomcat destroy call if needed
	///
	/// This is to ensure proper closure on "Garbage Collection"
	protected void finalize() throws Throwable {
		close();
		super.finalize();
	}
	
	///////////////////////////////////////////////////////
	//
	// Await thread handling
	//
	///////////////////////////////////////////////////////
	
	/// Thread await for the server to keep running
	public void await() {
		tomcat.getServer().await();
	}
	
	///////////////////////////////////////////////////////
	//
	// Initializing and starting, internal functions
	//
	///////////////////////////////////////////////////////
	
	/// Does the basic default tomcat instance setup
	protected void initTomcatInstance() {
		try {
			initTomcatInstance( Files.createTempDirectory( GUID.base58() ).toAbsolutePath().toString(), 8080);
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/// Does the basic default tomcat instance setup
	///
	/// @param  Temp base directory to use
	/// @param  Port number to use
	protected void initTomcatInstance(File tempPath, int port) {
		initTomcatInstance( tempPath.getAbsolutePath(), port );
	}
	
	/// Does the basic default tomcat instance setup
	///
	/// @param  Temp base directory to use
	/// @param  Port number to use
	protected void initTomcatInstance(String tempPath, int port) {
		// Setup tomcat instance
		tomcat = new Tomcat();
		
		// Default port is 8080
		tomcat.setPort(port);
		
		// Get default base dir to current executing directory "java_tmp/random-guid"
		//
		// It is important to note that in the Tomcat API documentation this is considered 
		// a security risk. Mainly due to cross application attacks
		tomcat.setBaseDir( tempPath );
		
		// Add AprLifecycleListener
		StandardServer server = (StandardServer)tomcat.getServer();
		AprLifecycleListener listener = new AprLifecycleListener();
		server.addLifecycleListener(listener);
	}
	
	/// Add a webapplication
	///
	/// @param  ContextPath to use
	/// @param  Webapplication File path to use
	protected void addWebapp(String inContextName, File webappPath) {
		addWebapp(inContextName, webappPath.getAbsolutePath() );
	}
	
	/// Add a webapplication
	///
	/// @param  ContextPath to use
	/// @param  Webapplication File path to use
	protected void addWebapp(String inContextName, String webappPath) {
		try {
			// Load context name and path
			contextName = inContextName;
			if( contextName == null || contextName.isEmpty() || contextName.equalsIgnoreCase("ROOT") ) {
				contextPath = "/";
			} else {
				contextPath = "/"+contextName+"/";
			}
			
			// Load context
			context = tomcat.addWebapp(contextPath, webappPath);
			
			// Normalize context resources
			WebResourceRoot resources = new StandardRoot(context);
			context.setResources(resources);
			
			// Wrapper defaultServlet = context.createWrapper();
			// defaultServlet.setName("default");
			// defaultServlet.setServletClass("org.apache.catalina.servlets.DefaultServlet");
			// defaultServlet.addInitParameter("debug", "0");
			// defaultServlet.addInitParameter("listings", "false");
			// defaultServlet.setLoadOnStartup(1);
			// 
			// context.addChild(defaultServlet);
			// context.addServletMapping("/","default");
			
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/// Startup the tomcat instance 
	protected void startup() {
		try {
			tomcat.init();
			tomcat.start();
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	///////////////////////////////////////////////////////
	//
	// Old stuff
	//
	///////////////////////////////////////////////////////
	
	// private Context _context = null;
	// private 
	// private List<String> _cachedServletsAdded = null;
	// 
	// //Users are forced to provide a context root folder
	// public EmbeddedServlet(String contextRootName, File contextRootFolder) {
	// 	_cachedServletsAdded = new ArrayList<String>();
	// 	
	// 	initTomcatInstance();
	// 	withContextRoot(contextRootName, contextRootFolder);
	// 	initDefaultServlet();
	// }
	// 
	// 
	// //Create a default servlet to handle serving static files
	// private void initDefaultServlet() {
	// 	Wrapper defaultServlet = _context.createWrapper();
	// 	defaultServlet.setName("default");
	// 	defaultServlet.setServletClass("org.apache.catalina.servlets.DefaultServlet");
	// 	defaultServlet.addInitParameter("debug", "0");
	// 	defaultServlet.addInitParameter("listings", "false");
	// 	defaultServlet.setLoadOnStartup(1);
	// 	
	// 	_context.addChild(defaultServlet);
	// 	_context.addServletMapping("/", "default");
	// 	_cachedServletsAdded.add("default");
	// }
	// 
	// //Should this be in its own thread?
	// public void awaitServer() {
	// 	if (_tomcat != null) {
	// 		_tomcat.getServer().await();
	// 	} else {
	// 		System.out.println("Tomcat instance is null");
	// 	}
	// }
	// 
	// public void stop() throws LifecycleException {
	// 	if (_tomcat != null) {
	// 		_tomcat.stop();
	// 	} else {
	// 		System.out.println("Tomcat instance is null");
	// 	}
	// }
	// 
	// public EmbeddedServlet withPort(int portNum) {
	// 	if (_tomcat != null) {
	// 		_tomcat.setPort(portNum);
	// 		_port = portNum;
	// 	} else {
	// 		System.out.println("Tomcat instance is null");
	// 	}
	// 	
	// 	return this;
	// }
	// 
	// public int getPort() {
	// 	return _port;
	// }
	// 
	// public EmbeddedServlet withContextRoot(String contextRootName, File contextRootFolder) {
	// 	if (_tomcat != null) {
	// 		_context = _tomcat.addContext(contextRootName, contextRootFolder.getAbsolutePath());
	// 	} else {
	// 		System.out.println("Tomcat instance is null");
	// 	}
	// 	
	// 	return this;
	// }
	// 
	// public EmbeddedServlet withBaseDirectory(File baseDir) {
	// 	if (baseDir != null) {
	// 		_tomcat.setBaseDir(baseDir.getAbsolutePath());
	// 	} else {
	// 		System.out.println("Paramater baseDir is null");
	// 	}
	// 	
	// 	return this;
	// }
	// 
	// public EmbeddedServlet withServlet(String servletURLName, String servletName, String servletClassName) {
	// 	if (_tomcat != null && _context != null) {
	// 		if (!_cachedServletsAdded.contains(servletName)) {
	// 			_tomcat.addServlet(_context.getPath(), servletName, servletClassName);
	// 			_cachedServletsAdded.add(servletName);
	// 		}
	// 		_context.addServletMapping(servletURLName, servletName);
	// 	} else {
	// 		System.out.println("Tomcat instance is null");
	// 	}
	// 	
	// 	return this;
	// }
	// 
	// public EmbeddedServlet withServlet(String servletURLName, String servletName, Servlet servlet) {
	// 	if (_tomcat != null) {
	// 		if (!_cachedServletsAdded.contains(servletName)) {
	// 			_tomcat.addServlet(_context.getPath(), servletName, servlet);
	// 			_cachedServletsAdded.add(servletName);
	// 		}
	// 		_context.addServletMapping(servletURLName, servletName);
	// 	} else {
	// 		System.out.println("Tomcat instance is null");
	// 	}
	// 	
	// 	return this;
	// }
}

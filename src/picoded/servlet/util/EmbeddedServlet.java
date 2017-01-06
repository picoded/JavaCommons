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
import org.apache.catalina.startup.ContextConfig;
import org.apache.tomcat.util.scan.StandardJarScanner;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleListener;

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
	String contextPath = "";
	
	///////////////////////////////////////////////////////
	//
	// Constructor, and basic server start/stop
	//
	///////////////////////////////////////////////////////
	
	///
	/// The simplest consturctor, just point to the web application folder
	/// and run it as "ROOT" on a specified port
	///
	/// @param   Port to run the embedded servlet on, -1 defaults to 8080
	/// @param   File representing either the folder, or the war file to deploy 
	///
	public EmbeddedServlet(int port, File webappPath) {
		initTomcatInstance("", port);
		addWebapp("ROOT", webappPath);
		startup();
	}
	
	/// Destroy the tomcat instance and removes it, if it exists
	public void close() {
		try {
			if( tomcat != null ) {
				tomcat.stop();
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
		initTomcatInstance("", -1);
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
		try {
			// Setup tomcat instance
			tomcat = new Tomcat();
			
			// Port setup
			tomcat.setPort(port > 0? port : 8080);
			
			// Get default base dir to current executing directory "java_tmp/random-guid"
			// If needed of course
			//
			// It is important to note that in the Tomcat API documentation this is considered 
			// a security risk. Mainly due to cross application attacks
			//
			// Files.createTempDirectory - is used in place of java.io.tmpdir
			// String mWorkingDir = System.getProperty("java.io.tmpdir");
			if( tempPath == null || tempPath.isEmpty() ) {
				tempPath = Files.createTempDirectory( GUID.base58() ).toAbsolutePath().toString();
			}
			tomcat.setBaseDir(tempPath);
			
			// Possible things that may change in the future
			//--------------------------------------------------------------------------
			//tomcat.getHost().setAppBase(mWorkingDir);
			//tomcat.getHost().setAutoDeploy(true);
			//tomcat.getHost().setDeployOnStartup(true);
			//tomcat.setHostname("localhost");
			//tomcat.enableNaming();
			
			// Do not add ContextConfig at the server level, but at the webapp level
			// This somehow fails despite it being in accordance to some documentation
			//--------------------------------------------------------------------------
			// ContextConfig contextConfig = new ContextConfig() {
			// 	private boolean invoked = false;
			// 	@Override
			// 	public void lifecycleEvent(LifecycleEvent event) {
			// 		if (!invoked) {
			// 			StandardJarScanner scanner = new StandardJarScanner();
			// 			scanner.setScanBootstrapClassPath(true);
			// 			scanner.setScanClassPath(false);
			// 			scanner.setScanManifest(true);
			// 			((Context) event.getLifecycle()).setJarScanner(scanner);
			// 			invoked = true;
			// 		}
			// 		super.lifecycleEvent(event);
			// 	}
			// };
			// 
			// StandardServer server = (StandardServer)tomcat.getServer();
			// server.addLifecycleListener(contextConfig);
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
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
			//
			// Load context name and path
			//
			contextName = inContextName;
			if( contextName == null || contextName.isEmpty() || contextName.equalsIgnoreCase("ROOT") ) {
				contextPath = "";
			} else {
				contextPath = "/"+contextName;
			}
			
			//
			// This helps disable the default parent class path scanning
			// And help reduce the classpathing warning errors.
			//
			// In the context JavaCommons webappllication, each deployment
			// will have more then enough Jars to provide on their own 
			//
			ContextConfig contextConfig = new ContextConfig() {
				private boolean invoked = false;
				@Override
				public void lifecycleEvent(LifecycleEvent event) {
					if (!invoked) {
						StandardJarScanner scanner = new StandardJarScanner();
						scanner.setScanBootstrapClassPath(true);
						scanner.setScanClassPath(false);
						scanner.setScanManifest(true);
						((Context) event.getLifecycle()).setJarScanner(scanner);
						invoked = true;
					}
					super.lifecycleEvent(event);
				}
			};
			
			//
			// Loads the application with the custom contextConfig
			//
			context = tomcat.addWebapp(tomcat.getHost(), contextPath, webappPath, (LifecycleListener)contextConfig );
			
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/// Startup the tomcat instance, does any final initialization prior to startup if needed
	protected void startup() {
		try {
			//tomcat.init();
			tomcat.start();
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
}

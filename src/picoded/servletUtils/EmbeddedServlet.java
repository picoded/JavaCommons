package picoded.servletUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.Servlet;
import javax.servlet.http.HttpServlet;

import org.apache.catalina.Context; //import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.Wrapper;
import org.apache.catalina.startup.Tomcat;

//might want to check against Lifecycle event for more fine grain checking
public class EmbeddedServlet {
	
	private Tomcat _tomcat = null;
	private Context _context = null;
	private int _port = -1; //tomcats property port is protected, so i keep this as a way to know what the port is
	private List<String> _cachedServletsAdded = null;
	
	//Users are forced to provide a context root folder
	public EmbeddedServlet(String contextRootName, File contextRootFolder) {
		_cachedServletsAdded = new ArrayList<String>();
		
		initTomcatInstance();
		withContextRoot(contextRootName, contextRootFolder);
		initDefaultServlet();
	}
	
	private void initTomcatInstance() {
		_tomcat = new Tomcat();
		
		//set default base dir to java temp dir
		File baseDir = new File(System.getProperty("java.io.tmpdir"));
		_tomcat.setBaseDir(baseDir.getAbsolutePath());
	}
	
	//Create a default servlet to handle serving static files
	private void initDefaultServlet() {
		Wrapper defaultServlet = _context.createWrapper();
		defaultServlet.setName("default");
		defaultServlet.setServletClass("org.apache.catalina.servlets.DefaultServlet");
		defaultServlet.addInitParameter("debug", "0");
		defaultServlet.addInitParameter("listings", "false");
		defaultServlet.setLoadOnStartup(1);
		
		_context.addChild(defaultServlet);
		_context.addServletMapping("/", "default");
		_cachedServletsAdded.add("default");
	}
	
	public void start() throws LifecycleException {
		if (_tomcat != null) {
			_tomcat.start();
		} else {
			System.out.println("Tomcat instance is null");
		}
	}
	
	//Should this be in its own thread?
	public void awaitServer() {
		if (_tomcat != null) {
			_tomcat.getServer().await();
		} else {
			System.out.println("Tomcat instance is null");
		}
	}
	
	public void stop() throws LifecycleException {
		if (_tomcat != null) {
			_tomcat.stop();
		} else {
			System.out.println("Tomcat instance is null");
		}
	}
	
	public EmbeddedServlet withPort(int portNum) {
		if (_tomcat != null) {
			_tomcat.setPort(portNum);
			_port = portNum;
		} else {
			System.out.println("Tomcat instance is null");
		}
		
		return this;
	}
	
	public int getPort() {
		return _port;
	}
	
	public EmbeddedServlet withContextRoot(String contextRootName, File contextRootFolder) {
		if (_tomcat != null) {
			_context = _tomcat.addContext(contextRootName, contextRootFolder.getAbsolutePath());
		} else {
			System.out.println("Tomcat instance is null");
		}
		
		return this;
	}
	
	public EmbeddedServlet withBaseDirectory(File baseDir) {
		if (baseDir != null) {
			_tomcat.setBaseDir(baseDir.getAbsolutePath());
		} else {
			System.out.println("Paramater baseDir is null");
		}
		
		return this;
	}
	
	public EmbeddedServlet withServlet(String servletURLName, String servletName, String servletClassName) {
		if (_tomcat != null && _context != null) {
			if (!_cachedServletsAdded.contains(servletName)) {
				_tomcat.addServlet(_context.getPath(), servletName, servletClassName);
				_cachedServletsAdded.add(servletName);
			}
			_context.addServletMapping(servletURLName, servletName);
		} else {
			System.out.println("Tomcat instance is null");
		}
		
		return this;
	}
	
	public EmbeddedServlet withServlet(String servletURLName, String servletName, Servlet servlet) {
		if (_tomcat != null) {
			if (!_cachedServletsAdded.contains(servletName)) {
				_tomcat.addServlet(_context.getPath(), servletName, servlet);
				_cachedServletsAdded.add(servletName);
			}
			_context.addServletMapping(servletURLName, servletName);
		} else {
			System.out.println("Tomcat instance is null");
		}
		
		return this;
	}
}

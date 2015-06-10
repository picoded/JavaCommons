package picoded.embedded;

import java.io.File;

import javax.servlet.Servlet;





import org.apache.catalina.Context;
//import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.Wrapper;
import org.apache.catalina.startup.Tomcat;

//might want to check against Lifecycle event for more fine grain checking
public class EmbeddedServlet {

	private Tomcat _tomcat = null;
	private Context _context = null;
	
	//Users are forced to provide a context root folder
	public EmbeddedServlet(String contextRootName, File contextRootFolder){
		initTomcatInstance();
		withContextRoot(contextRootName, contextRootFolder);
		initDefaultServlet();
	}

	private void initTomcatInstance(){
		_tomcat = new Tomcat();
		
		//set default base dir to java temp dir
		File baseDir = new File(System.getProperty("java.io.tmpdir"));
		_tomcat.setBaseDir(baseDir.getAbsolutePath());
	}
	
	//Create a default servlet to handle serving static files
	private void initDefaultServlet(){
		Wrapper defaultServlet = _context.createWrapper();
		defaultServlet.setName("default");
		defaultServlet.setServletClass("org.apache.catalina.servlets.DefaultServlet");
		defaultServlet.addInitParameter("debug", "0");
		defaultServlet.addInitParameter("listings", "false");
		defaultServlet.setLoadOnStartup(1);
		
		_context.addChild(defaultServlet);
		_context.addServletMapping("/", "default");
	}
	
	public void start() throws LifecycleException {
		if(_tomcat != null){
			_tomcat.start();
		} else {
			System.out.println("Tomcat instance is null");
		}
	}
	
	//Should this be in its own thread?
	public void awaitServer(){
		if(_tomcat != null){
			_tomcat.getServer().await();
		} else {
			System.out.println("Tomcat instance is null");
		}
	}
	
	public void stop()throws LifecycleException {
		if(_tomcat != null){
			_tomcat.stop();
		} else {
			System.out.println("Tomcat instance is null");
		}
	}
	
	public EmbeddedServlet withPort(int portNum){
		if(_tomcat != null){
			_tomcat.setPort(portNum);
		} else {
			System.out.println("Tomcat instance is null");
		}
		
		return this;
	}
	
	public EmbeddedServlet withContextRoot(String contextRootName, File contextRootFolder){
		if(_tomcat != null){
			_context = _tomcat.addContext(contextRootName, contextRootFolder.getAbsolutePath());
		} else {
			System.out.println("Tomcat instance is null");
		}
		
		return this;
	}
	
	public EmbeddedServlet withBaseDirectory(File baseDir){
		if(baseDir != null){
			_tomcat.setBaseDir(baseDir.getAbsolutePath());
		} else {
			System.out.println("Paramater baseDir is null");
		}
		
		return this;
	}
	
	public EmbeddedServlet withServlet(String servletURLName, String servletName, String servletClassName){
		if(_tomcat != null && _context != null){
			_tomcat.addServlet(_context.getPath(), servletName, servletClassName);
			_context.addServletMapping(servletURLName, servletName);
		} else {
			System.out.println("Tomcat instance is null");
		}
		
		return this;
	}
	
	public void addServlet(String contextPath, String servletName, Servlet servlet){
		if(_tomcat != null){
			_tomcat.addServlet(contextPath, servletName, servlet);
		} else {
			System.out.println("Tomcat instance is null");
		}
	}
}

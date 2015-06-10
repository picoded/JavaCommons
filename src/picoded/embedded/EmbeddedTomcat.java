package picoded.embedded;

import javax.servlet.Servlet;


//import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;

public class EmbeddedTomcat {

	private Tomcat _tomcat = null;
	
	public EmbeddedTomcat()
	{
		_tomcat = new Tomcat();
	}
	
	public void start() throws LifecycleException {
		if(_tomcat != null){
			_tomcat.start();
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
	
	public void setPort(int portNum){
		if(_tomcat != null){
			_tomcat.setPort(portNum);
		} else {
			System.out.println("Tomcat instance is null");
		}
	}
	
	public void addServlet(String contextPath, String servletName, String servletClass){
		if(_tomcat != null){
			_tomcat.addServlet(contextPath, servletName, servletClass);
		} else {
			System.out.println("Tomcat instance is null");
		}
	}
	
	public void addServlet(String contextPath, String servletName, Servlet servlet){
		if(_tomcat != null){
			_tomcat.addServlet(contextPath, servletName, servlet);
		} else {
			System.out.println("Tomcat instance is null");
		}
	}
	
	public void ran() {
		//addServlet("./", "test", new CorePage() {

		//});
	}

}

package picoded.servlet;

// Java Serlvet requirments
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletContextEvent;

// Net, io, NIO
import java.net.URL;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
//import java.nio.charset.StandardCharsets;

// Exceptions used
import javax.servlet.ServletException;
import java.io.IOException;

// Objects used
import java.util.*;
import java.io.PrintWriter;

// JMTE inner functions add-on
import com.floreysoft.jmte.*;

// Sub modules useds
import picoded.conv.JMTE;
import picoded.JStack.*;
import picoded.JStruct.*;
import picoded.RESTBuilder.*;
import picoded.RESTBuilder.templates.*;
import picoded.webTemplateEngines.JSML.*;
import picoded.webTemplateEngines.PagesBuilder.*;

///
/// Does all the standard USER API, Pages, and forms setup
///
public class CommonsPage extends BasePage {
	
	/// Authenticate the user, or redirects to login page if needed, this is not applied to API page
	@Override
	public boolean doAuth(Map<String,Object> templateData) throws Exception {
		
		// Gets the wildcard URI
		String[] wildcardUri = requestWildcardUriArray();
		if( wildcardUri == null ) {
			wildcardUri = new String[] {};
		}
		
		// Exempt login page from auth
		if( wildcardUri.length >= 1 && ( wildcardUri[0].equalsIgnoreCase("login") ) ) {
			return true;
		}
		
		// Exempt login API from auth
		if( wildcardUri.length >= 1 && 
			( wildcardUri[0].equalsIgnoreCase("api") ) //&& 
			//( wildcardUri[1].equalsIgnoreCase("account") ) && 
			//( wildcardUri[2].equalsIgnoreCase("login") ) 
			) {
			return true;
		}
		
		// Redirect to login, if current login is not valid
		if( currentAccount() == null ) {
			sendRedirect( (getContextURI()+"/login").replaceAll("//", "/") );
			return false;
		}
		
		// Blank wildcard redirects to "home" for valid users
		if( wildcardUri.length <= 0 || wildcardUri[0].length() <= 0 || wildcardUri[0].equals("/") ) {
			sendRedirect( (getContextURI()+"/home").replaceAll("//", "/") );
			return false;
		}
		
		return true;
	}
	
	/// Set the request mode to JSON, for API pages
	@Override
	public boolean isJsonRequest() {
		// Gets the wildcard URI
		String[] wildcardUri = requestWildcardUriArray();
		
		// Indicates its API for API page
		if( wildcardUri != null && wildcardUri.length >= 1 && wildcardUri[0].equalsIgnoreCase("api") ) {
			return true;
		}
		
		// Default behaviour
		return super.isJsonRequest();
	}
	
	/// Does the output processing, this is after do(Post/Get/Put/Delete)Request
	@Override
	public boolean outputRequest(Map<String,Object> templateData, PrintWriter output) throws Exception {
		// Gets the wildcard URI
		String[] wildcardUri = requestWildcardUriArray();
		
		// Indicates if its a API.JS request, and returns the JS file
		if( wildcardUri != null && wildcardUri.length >= 1 && //
			wildcardUri[0].equalsIgnoreCase("api.js")
			) {
			getHttpServletResponse().setContentType("application/javascript");
			output.println( restBuilder().generateJS( "api", getContextURI() ) );
			return true;
		}
		
		// Indicates if its a JSML form usage
		if( wildcardUri != null && wildcardUri.length >= 1 && //
			wildcardUri[0].equalsIgnoreCase("form") && //
			wildcardUri[0].equalsIgnoreCase("jsml") 
			) {
			JSMLFormSet().processJSMLFormCollectionServlet( this, Arrays.copyOfRange(wildcardUri, 1, wildcardUri.length) );
			return true;
		}
		
		//generateJS
		
		// Pages builder redirect (default)
		PagesBuilder().processPageBuilderServlet( this );
		return true;
	}
	
	@Override
	public void restBuilderSetup( RESTBuilder rbObj ) {
		AccountLogin.setupRESTBuilder( rbObj, accountAuthTable(), "account." );
	}
	
	/// Does the actual final json object to json string output, with contentType "application/javascript"
	@Override
	public boolean outputJSON(Map<String,Object> outputData, Map<String,Object> templateData, PrintWriter output) throws Exception {
		// Gets the wildcard URI
		String[] wildcardUri = requestWildcardUriArray();
		
		// Does the API call
		if( wildcardUri.length >= 1 && (wildcardUri[0].equalsIgnoreCase("api")) ) {
			restBuilder().servletCall( this, outputData, String.join(".",Arrays.copyOfRange(wildcardUri, 1, wildcardUri.length)) );
		}
		return super.outputJSON(outputData, templateData, output);
	}
	
}

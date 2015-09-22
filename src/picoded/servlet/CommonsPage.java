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
import picoded.fileUtils.FileUtils;
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
	
	/// Enable or disable commons page Auth redirection
	public boolean enableCommonWildcardAuthRedirection() {
		return true;
	}
	
	/// Authenticate the user, or redirects to login page if needed, this is not applied to API page
	@Override
	public boolean doAuth(Map<String,Object> templateData) throws Exception {
		
		// Gets the wildcard URI
		String[] wildcardUri = requestWildcardUriArray();
		if( wildcardUri == null ) {
			wildcardUri = new String[] {};
		}
		
		boolean enableCommonWildcardAuth = enableCommonWildcardAuthRedirection();
		if( enableCommonWildcardAuth && wildcardUri.length >= 1 ) {
			// Exempt login page from auth
			if( wildcardUri[0].equalsIgnoreCase("login") ) {
				String logout = requestParameters().getString("logout");
				
				// Handle page logout event
				if( logout != null && (logout.equals("1") || logout.equals("true")) ) {
					accountAuthTable().logoutAccount( getHttpServletRequest(), getHttpServletResponse() );
				}
				
				return true;
			}
			// Exempt common / index page from auth
			if( wildcardUri[0].equalsIgnoreCase("common") || wildcardUri[0].equalsIgnoreCase("index") ) {
				return true;
			}
			// Exempt login API from auth
			if( wildcardUri[0].equalsIgnoreCase("api") ) {
				if(wildcardUri.length >= 3 &&
					wildcardUri[1].equalsIgnoreCase("account") && 
					wildcardUri[2].equalsIgnoreCase("login")) {
					return true;
				} 
				
				// Throw a login error
				if( currentAccount() == null ) {
					
					getHttpServletResponse().setContentType("application/javascript");
					getWriter().println( "{ \"error\" : \"Missing User Login\" }" );
					
					return false;
				}
			}
			
			// File exemptions
			String fileStr = wildcardUri[ wildcardUri.length - 1 ].toLowerCase();
			String[] fileStrArr = fileStr.split("\\.");
			String fileExt = fileStrArr[ fileStrArr.length - 1 ];
			
			// Allow common asset files types
			if( //
				fileExt.equalsIgnoreCase("html") || //
				fileExt.equalsIgnoreCase("js") || //
				fileExt.equalsIgnoreCase("css") || //
				fileExt.equalsIgnoreCase("png") || //
				fileExt.equalsIgnoreCase("jpg") || //
				fileExt.equalsIgnoreCase("jpeg") || //
				fileExt.equalsIgnoreCase("svg") || //
				fileExt.equalsIgnoreCase("pdf") //
				) {
				return true;
			}
			
		}
		
		// Redirect to login, if current login is not valid
		if( currentAccount() == null ) {
			sendRedirect( (getContextURI()+"/login").replaceAll("//", "/") );
			return false;
		}
		
		// Blank wildcard redirects to "home" for valid users
		if( enableCommonWildcardAuth && (wildcardUri.length <= 0 || wildcardUri[0].length() <= 0 || wildcardUri[0].equals("/")) ) {
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
			wildcardUri[0].equalsIgnoreCase("api.js") && //api.js request 
			JConfig().getBoolean("sys.developersMode.enabled", true) //developerMode
			) {
			String apiJS = restBuilder().generateJS( "api", (getContextURI()+"/api").replaceAll("//", "/") );
			FileUtils.writeStringToFile_ifDifferant( new File(getContextPath()+"/api.js"), "UTF-8", apiJS );
			getHttpServletResponse().setContentType("application/javascript");
			output.println( apiJS );
			return true;
		}
		
		// Indicates if its a JSML form usage
		//
		// @TODO unit test, and fix it, somehow it isnt working =(
		//
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

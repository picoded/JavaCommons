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
import java.lang.reflect.Constructor;

// JMTE inner functions add-on
import com.floreysoft.jmte.*;

// Sub modules useds
import picoded.conv.JMTE;
import picoded.enums.*;
import picoded.fileUtils.FileUtils;
import picoded.JStack.*;
import picoded.JStruct.*;
import picoded.ServletLogging.*;
import picoded.RESTBuilder.*;
import picoded.RESTBuilder.template.core.*;
import picoded.webUtils.*;
import picoded.webTemplateEngines.JSML.*;
import picoded.page.builder.*;
import picoded.page.jsml.*;

///
/// Does all the standard USER API, Page, and forms setup
///
public class CommonsPage extends BasePage {
	
	/// Authenticate the user, or redirects to login page if needed, this is not applied to API page
	@Override
	public boolean doAuth(Map<String, Object> templateData) throws Exception {
		
		// Gets the wildcard URI
		String[] wildcardUri = requestWildcardUriArray();
		
		//
		// Blank wildcard redirects to "home" for root page request
		// COMPULSORY as we do not support root page as of now
		//
		if (wildcardUri.length <= 0 || wildcardUri[0].trim().length() <= 0 || wildcardUri[0].equals("/")) {
			sendRedirect((getContextURI() + "/" + JConfig().getString("sys.CommonsPage.rootPageRedirect", "home") ).replaceAll("//", "/"));
			return false;
		}
		
		// Enable or disable commons page Auth redirection
		boolean commonWildcardAuth = JConfig().getBoolean("sys.CommonsPage.commonWildcardAuth", true);
		boolean annoymousApiAccess = JConfigObj.getBoolean("sys.CommonsPage.annoymousApiAccess", false);
		boolean publicSiteMode = JConfigObj.getBoolean("sys.CommonsPage.publicSiteMode", false);
		boolean squashErrorMsg = JConfigObj.getBoolean("sys.CommonsPage.squashErrorMsg", false);
		
		//
		// WEB-INF security
		//
		if (wildcardUri.length >= 1) {
			
			//
			// File name and extension extraction for future processing
			//
			String fileName = wildcardUri[wildcardUri.length - 1].toLowerCase();
			String fileExt = FileUtils.getExtension(fileName);
			
			//
			// Always deny WEB-INF path, and potential invisibles (safety)
			//
			if (wildcardUri[0].equalsIgnoreCase("WEB-INF") || wildcardUri[0].startsWith(".")) {
				return false;
			}
			
			//
			// Common wildcard pattern
			//
			if (commonWildcardAuth) {
				
				//
				// Public Site file mode - allow all outside "reserved keywords"
				//
				if( publicSiteMode ) {
					if( !wildcardUri[0].equalsIgnoreCase("api") && !wildcardUri[0].equalsIgnoreCase("formset") ) {
						return true;
					}
				}
				
				// 
				// Public index.html? files, grab it direct
				//
				if (fileName.equalsIgnoreCase("index.html") && (new File(getContextPath(), requestWildcardUri())).canRead()) {
					return true;
				}
				
				//
				// Exempt login page from auth
				//
				if (wildcardUri[0].equalsIgnoreCase("login")) {
					//
					// Handle page logout event
					//
					String logout = requestParameters().getString("logout");
					if (logout != null && (logout.equalsIgnoreCase("1") || logout.equalsIgnoreCase("true"))) {
						accountAuthTable().logoutAccount(getHttpServletRequest(), getHttpServletResponse());
						sendRedirect((getContextURI() + "/login?logout_status=1").replaceAll("//", "/"));
						return false;
					}
					
					return true;
				}
				
				//
				// Exempt common / index / build / login page from auth
				//
				if (wildcardUri[0].equalsIgnoreCase("common") || wildcardUri[0].equalsIgnoreCase("index")
					|| wildcardUri[0].equalsIgnoreCase("login") || wildcardUri[0].equalsIgnoreCase("build")) {
					return true;
				}
				
				//
				// API based control
				//
				if (wildcardUri[0].equalsIgnoreCase("api")) {
					//
					// Allow annoymous API access
					//
					if( annoymousApiAccess ) {
						return true;
					}
					
					//
					// Exempt login API from auth
					//
					if (wildcardUri.length >= 3 && wildcardUri[1].equalsIgnoreCase("account")
						&& wildcardUri[2].equalsIgnoreCase("login")) {
						return true;
					}
					
					//
					// Throw a login error - requires login object
					//
					if (currentAccount() == null) {
						
						getHttpServletResponse().setContentType("application/javascript");
						getWriter().println("{ \"error\" : \"Missing User Login\" }");
						
						return false;
					}
				}
				
				//
				// Public site mode : Allow generated files outside protected zones
				//
				
				// // Allow common asset files types
				// if ( //
				// 	  //
				// 	  // HTML, JS, CSS
				// 	  //
				// fileExt.equalsIgnoreCase("html") || //
				// 	fileExt.equalsIgnoreCase("js") || //
				// 	fileExt.equalsIgnoreCase("css") || //
				// 	fileExt.equalsIgnoreCase("less") || //
				// 	fileExt.equalsIgnoreCase("scss") || //
				// 	fileExt.equalsIgnoreCase("es6") ||
				// 	//
				// 	// Images
				// 	//
				// 	fileExt.equalsIgnoreCase("png") || //
				// 	fileExt.equalsIgnoreCase("jpg") || //
				// 	fileExt.equalsIgnoreCase("jpeg") || //
				// 	fileExt.equalsIgnoreCase("gif") || //
				// 	fileExt.equalsIgnoreCase("svg") || //
				// 	//
				// 	// PDF
				// 	//
				// 	fileExt.equalsIgnoreCase("pdf") || //
				// 	//
				// 	// Markdown, text
				// 	//
				// 	fileExt.equalsIgnoreCase("md") || //
				// 	fileExt.equalsIgnoreCase("txt") || //
				// 	//
				// 	// Fonts
				// 	//
				// 	fileExt.equalsIgnoreCase("otf") || //
				// 	fileExt.equalsIgnoreCase("eot") || //
				// 	fileExt.equalsIgnoreCase("ttf") || //
				// 	fileExt.equalsIgnoreCase("woff") || //
				// 	fileExt.equalsIgnoreCase("woff2") || //
				// 	//
				// 	// Others?
				// 	//
				// 	false) {
				// 	return true;
				// }
			}
		}
		
		//
		// Redirect to login, if current login is not valid - and its not public mode
		//
		if (!publicSiteMode && currentAccount() == null) {
			sendRedirect((getContextURI() + "/" + JConfigObj.getString("sys.CommonsPage.loginPage", "login") ).replaceAll("//", "/"));
			return false;
		}
		
		return true;
	}
	
	/// Set the request mode to JSON, for API page
	@Override
	public boolean isJsonRequest() {
		// Gets the wildcard URI
		String[] wildcardUri = requestWildcardUriArray();
		
		// Indicates its API for API page
		if (wildcardUri != null && wildcardUri.length >= 1 && wildcardUri[0].equalsIgnoreCase("api")) {
			return true;
		}
		
		// Default behaviour
		return super.isJsonRequest();
	}
	
	/// Does the output processing, this is after do(Post/Get/Put/Delete)Request
	@Override
	public boolean outputRequest(Map<String, Object> templateData, PrintWriter output) throws Exception {
		// Gets the wildcard URI
		String[] wildcardUri = requestWildcardUriArray();
		
		// Indicates if its a API.JS request, and returns the JS file
		if (wildcardUri != null && wildcardUri.length >= 1 && //
			wildcardUri[0].equalsIgnoreCase("api.js") //api.js request
		) {
			// if( JConfig().getBoolean("sys.developersMode.enabled", true) ) { //developerMode
			// 	String apiJS = buildApiScript();
			// 	getHttpServletResponse().setContentType("application/javascript");
			// 	output.println(apiJS);
			// 	return true;
			// }
			
			// Fallsback into File Servlet
			PageBuilder().outputFileServlet().processRequest( //
				getHttpServletRequest(), //
				getHttpServletResponse(), //
				requestType() == HttpRequestType.HEAD, //
				"api.js");
			return true;
		}
		
		//
		// Indicates if its a FormSet page
		//
		if (wildcardUri != null && wildcardUri.length >= 1 && //
			wildcardUri[0].equalsIgnoreCase("formset") ) {
			(new FormSet(this)).processServletPageRequest(this, Arrays.copyOfRange(wildcardUri, 1, wildcardUri.length));
			return true;
		}
		
		//
		// Indicates if its a JSML form usage
		//
		if (wildcardUri != null && wildcardUri.length >= 1 && //
			wildcardUri[0].equalsIgnoreCase("form") && //
			wildcardUri[0].equalsIgnoreCase("jsml")) {
			JSMLFormSet().processJSMLFormCollectionServlet(this, Arrays.copyOfRange(wildcardUri, 1, wildcardUri.length));
			return true;
		}
		
		//generateJS
		
		// Page builder redirect (default)
		PageBuilder().processPageBuilderServlet(this);
		return true;
	}
	
	public String buildApiScript() throws IOException {
		String apiJS = restBuilder().generateJS("api", (getContextURI() + "/api").replaceAll("//", "/"));
		FileUtils.writeStringToFile_ifDifferant(new File(getContextPath() + "/api.js"), apiJS, null /*"UTF-8"*/);
		return apiJS;
	}
	
	@Override
	public void restBuilderSetup(RESTBuilder rbObj) {
		// Setup in accordance to the defined JStruct tables
		super.restBuilderSetup(rbObj);
		
		// Base account / dev setup
		AccountLogin.setupRESTBuilder(rbObj, accountAuthTable(), "account.");
		DevToolsApiBuilder.setupRESTBuilder(rbObj, "dev.");
	}
	
	/// Does the actual final json object to json string output, with contentType "application/javascript"
	@Override
	public boolean outputJSON(Map<String, Object> outputData, Map<String, Object> templateData, PrintWriter output)
		throws Exception {
		// Gets the wildcard URI
		String[] wildcardUri = requestWildcardUriArray();
		
		// Does the API call
		if (wildcardUri.length >= 1 && (wildcardUri[0].equalsIgnoreCase("api"))) {
			if (restBuilder().servletCall(this, outputData,
				String.join(".", Arrays.copyOfRange(wildcardUri, 1, wildcardUri.length)))) {
				return super.outputJSON(outputData, templateData, output);
			} else {
				return false;
			}
		}
		return false;
	}
	
	/// Auto initialize page builder
	@Override
	public void initializeContext() throws Exception {
		super.initializeContext();
		
		boolean ignorePageBuilder = JConfig().getBoolean("developersMode.PageBuilder_ignoreInitializeContext", false)
			|| JConfig().getBoolean("developersMode.PageBuilder_ignoreInitializeContext", false);
		if (!ignorePageBuilder || this._commandLineInitialized) {
			PageBuilder().buildAllPage();
		}
		buildApiScript();
	}
	
	//---------------------------------------------------------
	//
	// Additional auto loaded modules
	//
	//---------------------------------------------------------
	
	/// Cached memoizer copy
	protected EmailBroadcaster _systemEmail = null;
	
	/// The system email broadcaster based on config : default to mailinator
	///
	/// Note if the sys.smtp.enabled is set to false, this function returns null;
	public EmailBroadcaster systemEmail() {
		// Returns cached broadcaster if possible
		if (_systemEmail != null) {
			return _systemEmail;
		}
		
		// Gets the configuration setup
		JConfig jc = JConfig();
		boolean sysSmtp = jc.getBoolean("sys.dataStack.smtp.enabled", true);
		
		// Returns null if disabled
		if (sysSmtp == false) {
			return null;
		}
		
		// Get hostname, user, pass, and from account
		String hostname = JConfigObj.getString("sys.dataStack.smtp.host", "smtp.mailinator.com:25");
		String username = JConfigObj.getString("sys.dataStack.smtp.username", "");
		String password = JConfigObj.getString("sys.dataStack.smtp.password", "");
		String emailFrom = JConfigObj.getString("sys.dataStack.smtp.emailFrom", "testingTheEmailSystem@mailinator.com");
		boolean isSSL = JConfigObj.getBoolean("sys.dataStack.smtp.ssl", false);
		
		return (_systemEmail = new EmailBroadcaster(hostname, username, password, emailFrom, isSSL));
	}
	
	/// Cached memoizer copy
	protected ServletLogging _systemLogging = null;
	
	/// the servlet logging module
	public ServletLogging systemLogging() {
		// Returns cached copy if posisble
		if (_systemLogging != null) {
			return _systemLogging;
		}
		
		return _systemLogging = new ServletLogging();
	}
	
	//---------------------------------------------------------
	//
	// Self constructing, main function.
	// Used to build the page via command line
	//
	//---------------------------------------------------------
	public boolean _commandLineInitialized = false;
	
	public static void main(String[] args) {
		
		CommonsPage mainClass = null;
		
		System.out.println("---------------------------------------------------");
		System.out.println("- Command line Page build triggered");
		
		//
		// @TODO : Consider automated stack trace if not given?,
		//         Performance is not considered an issue after all for 1 time build scripts
		//
		// http://stackoverflow.com/questions/18647613/get-caller-class-name-from-inherited-static-method
		//
		String callingClassName = args[0];
		System.out.println("- Assumed calling class name: " + callingClassName);
		
		String contextPath = args[1];
		if (!contextPath.endsWith("/")) {
			contextPath = contextPath + "/";
		}
		System.out.println("- Assumed context path: " + contextPath);
		
		String contextURI = args[2];
		if (!contextURI.endsWith("/")) {
			contextURI = contextURI + "/";
		}
		System.out.println("- Assumed context URI: " + contextURI);
		
		System.out.println("---------------------------------------------------");
		
		try {
			Class<?> c = Class.forName(callingClassName);
			Constructor<?> cons = c.getConstructor();
			
			Object built = cons.newInstance();
			if (!CommonsPage.class.isInstance(built)) {
				throw new RuntimeException("Provided class name is not extended from CommonsPage: " + callingClassName);
			}
			mainClass = (CommonsPage) built;
			
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		System.out.println("- Initialized calling class, calling initializeContext() next");
		
		mainClass._commandLineInitialized = true;
		mainClass._contextPath = contextPath;
		mainClass._contextURI = contextURI;
		try {
			mainClass.initializeContext();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		System.out.println("- initializeContext() called");
		System.out.println("---------------------------------------------------");
		
	}
}

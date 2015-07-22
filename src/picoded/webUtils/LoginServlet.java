package picoded.webUtils;

//import java.io.InputStream;
//import java.io.OutputStream;
//import java.io.OutputStreamWriter;
import java.util.Map;
//import java.util.Map.Entry;




import org.apache.commons.lang3.StringUtils;

import picoded.servlet.*;
import picoded.JStack.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.*;

public class LoginServlet extends BasePage{

	static final long serialVersionUID = 1L;
	
	@Override
	public String getContextPath(){
		return "./test-files/tmp/";
	}

	@Override
	public void doSetup() throws Exception {
		// TODO Auto-generated method stub
		super.doSetup();
	}
	
	@Override
	public boolean isJsonRequest() {
		return true;
	}
	
	@Override
	public boolean doGetJSON(Map<String,Object> outputData, Map<String,Object> templateData)
			throws Exception {
		try{
			accountAuthTable().stackSetup();
		} catch (JStackException ex){
			throw new ServletException(ex);
		}
		
		String[] names = null;
		String loginNames = null;
		
		if( currentAccount() != null ) {
			names = currentAccount().getNames();
			loginNames = StringUtils.join(names, ",");
		}
		
		outputData.put("user", loginNames);
		return true;
	}

	@Override
	public boolean doPostJSON(Map<String, Object> outputData, Map<String,Object> templateData)
			throws Exception {
		try {
			AccountObject acc;
			
			String userName = requestParameters.get("user");
			String userPW = requestParameters.get("password");
			
			if(! accountAuthTable().containsName(userName)) {
				acc = accountAuthTable().newObject(userName);
				acc.setPassword(userPW);
				acc.saveAll();

				if( !accountAuthTable().containsName(userName) ) {
					throw new ServletException("missing user setup");
				}
			}
			acc = accountAuthTable().loginAccount(httpRequest, httpResponse, userName, userPW, false);
			
			outputData.put("login-status", ( acc != null ) );
		} catch (JStackException e) {
			throw new ServletException(e);
		}
		return super.doPostRequest(templateData);
	}
	
}

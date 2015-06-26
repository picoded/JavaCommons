package picoded.codeBox;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import com.hazelcast.util.StringUtil;

import picoded.servlet.*;
import picoded.webUtils.PiHttpRequester;
import picoded.webUtils.PiHttpResponse;
import picoded.JStack.*;

public class CodeBoxLoginServlet extends BasePage{

	static final long serialVersionUID = 1L;
	
	@Override
	public String getContextPath(){
		return "./test-files/tmp/";
	}

	@Override
	public void doSetup() throws ServletException {
		// TODO Auto-generated method stub
		super.doSetup();
	}
	
	@Override
	public boolean isJsonRequest() {
		return true;
	}
	
	@Override
	public boolean doGetJSON(Map<String,Object> outputData, Map<String,Object> templateData)
			throws ServletException {
		try{
			accountAuthTable().stackSetup();
		} catch (JStackException ex){
			throw new ServletException(ex);
		}
		//-------------------------------------------------------

		//------------------------------------------
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
			throws ServletException {
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
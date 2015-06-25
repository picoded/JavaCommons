package picoded.webUtils;

//import java.io.InputStream;
//import java.io.OutputStream;
//import java.io.OutputStreamWriter;
import java.util.Map;
//import java.util.Map.Entry;




import org.apache.commons.lang3.StringUtils;

import picoded.servlet.*;
import picoded.JStack.*;

public class PiCodeBox extends BasePage{

	static final long serialVersionUID = 1L;
	
	@Override
	public String getWebInfPath() {
		// TODO Auto-generated method stub
		return super.getWebInfPath();
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
		accountAuthTable();
		
		
		String requestedUser = requestParameters.getString("user", "null");
		
		outputData.put("user", requestedUser);
		
//		String[] names = null;
//		String loginNames = null;
//		
//		if( currentAccount() != null ) {
//			names = currentAccount().getNames();
//			loginNames = StringUtils.join(names, ",");
//		}
//		
//		outputData.put("login-names", loginNames); // { "user" : "hello" }
		
		return true;
	}

	@Override
	public boolean doPostJSON(Map<String, Object> outputData, Map<String,Object> templateData)
			throws ServletException {
		// TODO Auto-generated method stub
		try {
			AccountObject acc;
			if(! accountAuthTable().containsName("sam")) {
				acc = accountAuthTable().newObject("sam");
				acc.setPassword("password");
				acc.saveAll();
			}
			acc = accountAuthTable().loginAccount(httpRequest, httpResponse, "sam", "password", false);
			
			outputData.put("login-status", ( acc != null ) );
			
		} catch (JStackException e) {
			throw new ServletException(e);
		}
		return super.doPostRequest(templateData);
	}
	
}
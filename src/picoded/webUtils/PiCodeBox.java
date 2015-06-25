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
			//JStackSetup();
		} catch (JStackException ex){
			throw new ServletException(ex);
		}
		
		//String requestedUser = requestParameters.getString("user", "null");
		
		//outputData.put("user", requestedUser);
		String[] names = null;
		String loginNames = null;
		
		if( currentAccount() != null ) {
			names = currentAccount().getNames();
			loginNames = StringUtils.join(names, ",");
		}
		
		outputData.put("user", loginNames); // { "user" : "hello" }
		return true;
	}

	@Override
	public boolean doPostJSON(Map<String, Object> outputData, Map<String,Object> templateData)
			throws ServletException {
		System.out.println("Received post request");
		
		for(String str : requestParameters.keySet()){
			System.out.println("Key: " +str+ " and value: " +requestParameters.get(str));
		}
		
		try {
			AccountObject acc;
			
			String userName = requestParameters.get("user");
			String userPW = requestParameters.get("password");
			
			if(! accountAuthTable().containsName(userName)) {
				System.out.println(userName+ " not found in account table");
				acc = accountAuthTable().newObject(userName);
				acc.setPassword(userPW);
				acc.saveAll();
			}
			acc = accountAuthTable().loginAccount(httpRequest, httpResponse, userName, userPW, false);
			
			outputData.put("login-status", ( acc != null ) );
			
		} catch (JStackException e) {
			throw new ServletException(e);
		}
		return super.doPostRequest(templateData);
	}
	
}
package picoded.webUtils;

import java.io.InputStream;
import picoded.struct.GenericConvertMap;

public interface ResponseHttp {
	
	///////////////////////////////////////////////////
	// Async Http Request waiting handling
	///////////////////////////////////////////////////
	
	public void waitForCompletedHeaders();
	public void waitForCompletedRequest();
	
	///////////////////////////////////////////////////
	// Apache HttpResponse mode
	///////////////////////////////////////////////////
	
	/// Gets the response code
	public int statusCode();
	
	/// Gets the header map. 
	public GenericConvertMap<String, String> getHeaders();
	
	/// Gets the cookies map. 
	public GenericConvertMap<String, String> getCookies();
	
	/// Gets the response content
	public InputStream inputStream();
	
	/// Gets the response content as a string
	public String toString();
	
	/// Converts the result string into a map, via JSON's
	public GenericConvertMap<String,Object> toMap();
}

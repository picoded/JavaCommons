package picoded.webUtils;

import java.io.InputStream;
import java.util.Map;

import picoded.conv.ConvertJSON;
import picoded.struct.GenericConvertMap;
import picoded.struct.ProxyGenericConvertMap;

public interface ResponseHttp {
	
	///////////////////////////////////////////////////
	// Async Http Request wait handling
	// (implment if needed)
	///////////////////////////////////////////////////
	
	public default void waitForCompletedHeaders() { };
	public default void waitForCompletedRequest() { };
	
	///////////////////////////////////////////////////
	// Response handling
	///////////////////////////////////////////////////
	
	/// Gets the response content
	public InputStream inputStream();
	
	/// Gets the response content as a string
	public String toString();
	
	/// Converts the result string into a map, via JSON's
	public default GenericConvertMap<String,Object> toMap() { 
		waitForCompletedRequest();
		
		String r = toString();
		if( r == null || r.length() <= 1 ) {
			return null;
		}
		
		Map<String,Object> rMap = ConvertJSON.toMap( r );
		if( rMap == null ) {
			return null;
		} else {
			return ProxyGenericConvertMap.ensureGenericConvertMap(rMap);
		}
	};
	
	/// Gets the response code
	public default int statusCode() { return 200; };
	
	/// Gets the header map. 
	public default Map<String, String[]> headersMap() { return null; };
	
	/// Gets the cookies map. 
	public default Map<String, String[]> cookiesMap() { return null; };
	
}

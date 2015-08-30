package picoded.webUtils._RequestHttp;

import java.io.InputStream;
import java.util.Map;

import picoded.webUtils.ResponseHttp;
import picoded.conv.ConvertJSON;
import picoded.struct.GenericConvertMap;
import picoded.struct.ProxyGenericConvertMap;

import org.apache.commons.io.IOUtils;

/// Provides the most basic implementation of ResponseHttp
/// Which is to have all the results already loaded (not async)
public class ResponseHttp_basic implements ResponseHttp {
	
	///////////////////////////////////////////////////
	// Response handling variables
	///////////////////////////////////////////////////
	
	protected InputStream _inputStream = null;
	protected String _toString = null;
	protected GenericConvertMap<String,Object> _toMap = null;
	
	///////////////////////////////////////////////////
	// Response handling functions
	///////////////////////////////////////////////////
	
	/// Gets the response content
	public InputStream inputStream() {
		waitForCompletedHeaders();
		return _inputStream;
	}
	
	/// Gets the response content as a string
	public String toString() {
		// Returns cached copy
		waitForCompletedRequest();
		if( _toString != null ) {
			return _toString;
		}
		
		/// Stores and return
		try {
			return (_toString = IOUtils.toString( inputStream() )); //, encoding 
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/// Converts the result string into a map, via JSON's
	public GenericConvertMap<String,Object> toMap() {
		// Returns cached copy
		waitForCompletedRequest();
		if( _toMap != null ) {
			return _toMap;
		}
		
		// Generates it then
		return (_toMap = ResponseHttp.super.toMap());
	}
}

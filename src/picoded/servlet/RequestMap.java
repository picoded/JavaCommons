package picoded.servlet;

import java.util.HashMap;
import java.util.Map;
import java.util.Arrays;

import org.apache.commons.collections4.map.AbstractMapDecorator;

import picoded.struct.GenericConvertMap;
import picoded.conv.ConvertJSON;

/// Class map, that handles the request map, and does the conversion between request arrays, and request values
///
/// Also implments teh generic convert class
///
/// @TODO: Optimize the class to do the conversion between String[] to String only ON DEMAND, and to cache the result
///
public class RequestMap extends AbstractMapDecorator<String,String> implements GenericConvertMap<String,String> {
	
	// Constructor
	//------------------------------------------------------------------------------
	
	/// blank constructor
	public RequestMap() {
		super(new HashMap<String,String>());
	}
	
	/// basic proxy constructor
	public RequestMap(Map<String,String> proxy) {
		super(proxy);
	}
	
	/// http map proxy constructor
	protected RequestMap(Map<String,String[]> proxy, boolean misc ) {
		super( mapConvert(proxy) );
	}
	
	// Utility functions (internal)
	//------------------------------------------------------------------------------
	
	/// Does the conversion from string array to string,
	/// Used internally for all the map conversion
	private static String stringFromArray(String[] in) {
		if( in == null || in.length == 0 ) {
			return null;
		}
		
		if( in.length == 1 ) {
			return in[0];
		}
		
		return ConvertJSON.fromList( Arrays.asList(in) );
	}
	
	/// Does the conversion of a Map<String,String[]> to a Map<String,String>
	private static Map<String,String> mapConvert( Map<String,String[]> in ) {
		HashMap<String,String> ret = new HashMap<String,String>();
		
		for ( Map.Entry<String, String[]> entry : in.entrySet() ) {
			ret.put( entry.getKey(), stringFromArray( entry.getValue() ) );
		}
		return ret;
	}
	
}
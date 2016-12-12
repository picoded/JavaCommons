package picoded.RESTBuilder;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections4.map.AbstractMapDecorator;

import picoded.enums.HttpRequestType;
import picoded.struct.GenericConvertMap;
// Exceptions used
// Objects used
// Ext lib used

///
/// RESTRequest object handler, this is usually the first argument for all
/// functions registered to the RESTBuilder framework
///
/// It represents the request object, and include utility functions 
///
public class RESTRequest extends AbstractMapDecorator<String, Object> implements
	GenericConvertMap<String, Object> {
	
	//--------------------------------------------------------------------------------
	// Protected vars
	//--------------------------------------------------------------------------------
	
	/// Build warning suppression
	static final long serialVersionUID = 1L;
	
	/// [internal] avoid use this directly
	public RESTRequest(Map<String, Object> map) {
		super(map);
	}
	
	/// [internal] avoid use this directly
	public RESTRequest() {
		super(new HashMap<String, Object>());
	}
	
	//--------------------------------------------------------------------------------
	// Protected Request vars
	//--------------------------------------------------------------------------------
	
	/// The raw request namespace
	protected String[] rawRequestNamespace = null;
	
	/// The registered api namespace
	protected String[] registeredNamespace = null;
	
	/// The wildcard specific namespace
	protected String[] wildCardNamespace = null;
	
	/// The original builder object, if its called via RESTBuilder
	protected RESTBuilder builder = null;
	
	/// The RESTRequest type called
	protected HttpRequestType requestType = null;
	
	/// The requesting CorePage, if given
	protected picoded.servlet.CorePage requestPage = null;
	
	//--------------------------------------------------------------------------------
	// Public Request vars
	//--------------------------------------------------------------------------------
	
	/// The raw request namespace
	public String[] rawRequestNamespace() {
		return rawRequestNamespace;
	}
	
	/// The registered api namespace
	public String[] registeredNamespace() {
		return registeredNamespace;
	}
	
	/// The wildcard specific namespace
	public String[] wildCardNamespace() {
		if (wildCardNamespace != null) {
			return wildCardNamespace;
		}
		
		if (rawRequestNamespace == null || registeredNamespace == null) {
			return null;
		}
		
		/// is wildcard request?
		if (registeredNamespace[registeredNamespace.length - 1].equals("*")
			&& rawRequestNamespace.length > (registeredNamespace.length - 1)) {
			
			wildCardNamespace = Arrays.copyOfRange(rawRequestNamespace,
				registeredNamespace.length - 1, rawRequestNamespace.length);
		}
		return wildCardNamespace;
	}
	
	/// The original builder object, if its called via RESTBuilder
	public RESTBuilder builder() {
		return builder;
	}
	
	/// The RESTRequest type called
	public HttpRequestType requestType() {
		return requestType;
	}
	
	/// Gets the requestPage if its set 
	public picoded.servlet.CorePage requestPage() {
		return requestPage;
	}
}

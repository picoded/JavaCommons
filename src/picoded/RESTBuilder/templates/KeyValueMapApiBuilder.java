package picoded.RESTBuilder.templates;

import java.util.*;
import java.util.Map.Entry;

import picoded.RESTBuilder.*;
import picoded.JStack.*;
import picoded.JStruct.*;
import picoded.servlet.*;
import picoded.conv.ConvertJSON;
import picoded.conv.GenericConvert;
import picoded.conv.RegexUtils;
import picoded.enums.HttpRequestType;

public class KeyValueMapApiBuilder {
	
	private Map<String, String> _keyValueMap;
	
	public KeyValueMapApiBuilder(Map<String, String> inMap) {
		_keyValueMap = inMap;
	}
	
	///
	/// # getValue (GET)
	///
	/// Gets the value for a key
	///
	/// ## HTTP Request Parameters
	///
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// | Parameter Name  | Variable Type	   | Description                                                                   |
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// | key             | String             | Key String of a key value pair in _keyValueMap                                |
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// 
	/// ## JSON Object Output Parameters
	///
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// | Parameter Name  | Variable Type	   | Description                                                                   |
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// | value           | String             | Value of the key given                                                        |
	/// | error           | String             | Errors, if any                                                                |
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	///
	public RESTFunction getValue = (req, res) -> {
		if (_keyValueMap == null) {
			res.put("error", "Key Value Map is null");
			return res;
		}
		
		//Get given key
		String key = req.getString("key", "");
		if (key.isEmpty()) {
			res.put("error", "No key was supplied");
			return res;
		}
		
		//Add found value to map
		res.put("value", _keyValueMap.get(key));
		
		return res;
	};
	
	///
	/// # getValues (GET)
	///
	/// Gets the values for a set of keys
	///
	/// ## HTTP Request Parameters
	///
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// | Parameter Name  | Variable Type	   | Description                                                                   |
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// | keys            | String[]           | Array of keys to look for                                                     |
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// 
	/// ## JSON Object Output Parameters
	///
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// | Parameter Name  | Variable Type	   | Description                                                                   |
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// | values          | Map<String, String>| Values for the given keys                                                     |
	/// | error           | String             | Errors, if any                                                                |
	/// | exceptionMsg    | String             | Exception message, if any                                                     |
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	///
	public RESTFunction getValues = (req, res) -> {
		if (_keyValueMap == null) {
			res.put("error", "Key Value Map is null");
			return res;
		}
		
		String[] keys = req.getStringArray("keys", null);
		if (keys == null) {
			res.put("error", "No key array was supplied");
			return res;
		}
		
		if (keys.length < 1) {
			res.put("error", "Key array supplied was empty");
			return res;
		}
		
		Map<String, String> valueMap = new HashMap<String, String>();
		
		try {
			for (String key : keys) {
				valueMap.put(key, _keyValueMap.get(key));
			}
		} catch (Exception ex) {
			res.put("error", "An error occured while iterating and getting/setting values");
			res.put("exceptionMsg", ex.getMessage());
			return res;
		}
		
		res.put("values", valueMap);
		
		return res;
	};
	
	///
	/// # setValue (POST)
	///
	/// Sets the value for a key
	///
	/// ## HTTP Request Parameters
	///
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// | Parameter Name  | Variable Type	   | Description                                                                   |
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// | key             | String             | Key to set value for                                                          |
	/// | value           | String             | New value for key                                                             |
	/// | allowEmptyValue | boolean {optional) | Allow null values to be set (default false)                                   |
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// 
	/// ## JSON Object Output Parameters
	///
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// | Parameter Name  | Variable Type	   | Description                                                                   |
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// | map          | Map<String, String>| Returns current mappings                                                      |
	/// | error           | String             | Errors, if any                                                                |
	/// | exceptionMsg    | String             | Exception message, if any                                                     |
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	///
	public RESTFunction setValue = (req, res) -> {
		if (_keyValueMap == null) {
			res.put("error", "Key Value Map is null");
			return res;
		}
		
		String key = req.getString("key", "");
		if (key.isEmpty()) {
			res.put("error", "No key was supplied");
			return res;
		}
		
		boolean allowEmptyValue = req.getBoolean("allowEmptyValue", false);
		String value = req.getString("value", "");
		if (value.isEmpty() && !allowEmptyValue) {
			res.put("error", "An empty value was supplied, and allowEmptyValue is false");
			return res;
		}
		
		try {
			_keyValueMap.put(key, value);
		} catch (Exception ex) {
			res.put("error", "An error occured while trying to set a value");
			res.put("exceptionMsg", ex.getMessage());
			return res;
		}
		
		res.put("map", _keyValueMap);
		
		return res;
	};
	
	///
	/// # setValues (POST)
	///
	/// Sets the values for given keys
	///
	/// ## HTTP Request Parameters
	///
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// | Parameter Name  | Variable Type	   | Description                                                                   |
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// | keyValues       | Map<String, String>| Map of new keyValuePair mappings                                              |
	/// | allowEmptyValue | boolean {optional) | Allow null values to be set (default false)                                   |
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// 
	/// ## JSON Object Output Parameters
	///
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// | Parameter Name  | Variable Type	   | Description                                                                   |
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// | map          | Map<String, String>| Returns current mappings                                                      |
	/// | error           | String             | Errors, if any                                                                |
	/// | exceptionMsg    | String             | Exception message, if any                                                     |
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	///
	public RESTFunction setValues = (req, res) -> {
		if (_keyValueMap == null) {
			res.put("error", "Key Value Map is null");
			return res;
		}
		
		Object keyValues_raw = req.get("keyValues");
		if (keyValues_raw == null) {
			res.put("error", "No keyValues map was supplied");
			return res;
		}
		
		Map<String, Object> keyValues = null;
		try {
			if (keyValues_raw instanceof String) {
				keyValues = ConvertJSON.toMap((String) keyValues_raw);
			}
		} catch (Exception ex) {
			res.put("error", "Conversion to map object failed");
			res.put("exceptionMsg", ex.getMessage());
			return res;
		}
		
		boolean allowEmptyValue = req.getBoolean("allowEmptyValue", false);
		try {
			for (String key : keyValues.keySet()) {
				String value = (String) keyValues.get(key);
				if (value.isEmpty()) {
					if (allowEmptyValue) {
						_keyValueMap.put(key, value);
					}
				} else {
					_keyValueMap.put(key, value);
				}
			}
		} catch (Exception ex) {
			res.put("error", "An error occured while trying to iterate and set values");
			res.put("exceptionMsg", ex.getMessage());
			return res;
		}
		
		res.put("map", _keyValueMap);
		
		return res;
	};
	
	///
	/// # deleteValue (POST)
	///
	/// Deletes the mapping for the given key
	///
	/// ## HTTP Request Parameters
	///
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// | Parameter Name  | Variable Type	   | Description                                                                   |
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// | key             | String             | Key to delete                                                                 |
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// 
	/// ## JSON Object Output Parameters
	///
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// | Parameter Name  | Variable Type	   | Description                                                                   |
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// | map          | Map<String, String>| Returns current mappings                                                      |
	/// | error           | String             | Errors, if any                                                                |
	/// | exceptionMsg    | String             | Exception message, if any                                                     |
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	///
	public RESTFunction deleteValue = (req, res) -> {
		if (_keyValueMap == null) {
			res.put("error", "Key Value Map is null");
			return res;
		}
		
		String key = req.getString("key", "");
		if (key.isEmpty()) {
			res.put("error", "An empty key was supplied");
			return res;
		}
		
		try {
			_keyValueMap.remove(key);
		} catch (Exception ex) {
			res.put("error", "An error occured while trying to delete a value");
			res.put("exceptionMsg", ex.getMessage());
			return res;
		}
		
		res.put("map", _keyValueMap);
		
		return res;
	};
	
	///
	/// # deleteValues (POST)
	///
	/// Deletes the mappings for the given keys
	///
	/// ## HTTP Request Parameters
	///
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// | Parameter Name  | Variable Type	   | Description                                                                   |
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// | keys            | String[]           | Array of keys to delete                                                       |
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// 
	/// ## JSON Object Output Parameters
	///
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// | Parameter Name  | Variable Type	   | Description                                                                   |
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// | map          | Map<String, String>| Returns current mappings                                                      |
	/// | error           | String             | Errors, if any                                                                |
	/// | exceptionMsg    | String             | Exception message, if any                                                     |
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	///
	public RESTFunction deleteValues = (req, res) -> {
		if (_keyValueMap == null) {
			res.put("error", "Key Value Map is null");
			return res;
		}
		
		String[] keys = req.getStringArray("keys", null);
		if (keys == null) {
			res.put("error", "No key array was supplied");
			return res;
		}
		
		if (keys.length < 1) {
			res.put("error", "Key array supplied was empty");
			return res;
		}
		
		try {
			for (String key : keys) {
				if (_keyValueMap.containsKey(key)) {
					_keyValueMap.remove(key);
				}
			}
		} catch (Exception ex) {
			res.put("error", "An error occured while iterating and deleting mappings");
			res.put("exceptionMsg", ex.getMessage());
			return res;
		}
		
		res.put("map", _keyValueMap);
		
		return res;
	};
	
	///
	/// # getMap (GET)
	///
	/// Get the current map
	///
	/// ## HTTP Request Parameters
	///
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// | Parameter Name  | Variable Type	   | Description                                                                   |
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// 
	/// ## JSON Object Output Parameters
	///
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// | Parameter Name  | Variable Type	   | Description                                                                   |
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// | map             | Map<String, String>| Returns current mappings                                                      |
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	///
	public RESTFunction getMap = (req, res) -> {
		if (_keyValueMap == null) {
			res.put("error", "Key Value Map is null");
			return res;
		}
		
		res.put("map", _keyValueMap);
		
		return res;
	};
	
	/////////////////////////////////////////////
	//
	// RestBuilder template builder
	//
	/////////////////////////////////////////////
	
	///
	/// Takes the restbuilder and implements its respective default API
	///
	public RESTBuilder setupRESTBuilder(RESTBuilder rb, String setPrefix) {
		
		//Get entire mapping
		rb.getNamespace(setPrefix + "getMap").put(HttpRequestType.GET, getMap);
		
		//Get values
		rb.getNamespace(setPrefix + "getValue").put(HttpRequestType.GET, getValue);
		rb.getNamespace(setPrefix + "getValues").put(HttpRequestType.GET, getValues);
		
		//Set values
		rb.getNamespace(setPrefix + "setValue").put(HttpRequestType.POST, setValue);
		rb.getNamespace(setPrefix + "setValues").put(HttpRequestType.POST, setValues);
		
		//Delete mappings
		rb.getNamespace(setPrefix + "deleteValue").put(HttpRequestType.POST, deleteValue);
		rb.getNamespace(setPrefix + "deleteValues").put(HttpRequestType.POST, deleteValues);
		
		return rb;
	}
}

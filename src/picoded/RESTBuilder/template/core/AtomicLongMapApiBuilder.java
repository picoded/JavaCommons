package picoded.RESTBuilder.template.core;

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

public class AtomicLongMapApiBuilder {
	
	private AtomicLongMap _atomicLongMap;
	
	public AtomicLongMapApiBuilder(AtomicLongMap inMap) {
		_atomicLongMap = inMap;
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
		if (_atomicLongMap == null) {
			res.put("error", "Atomic Long Map is null");
			return res;
		}
		
		//Get given key
		String key = req.getString("key", "");
		if (key.isEmpty()) {
			res.put("error", "No key was supplied");
			return res;
		}
		
		//Add found value to map
		res.put("value", _atomicLongMap.get(key));
		
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
		if (_atomicLongMap == null) {
			res.put("error", "Atomic Long Map is null");
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
		
		Map<String, Long> valueMap = new HashMap<String, Long>();
		
		try {
			for (String key : keys) {
				valueMap.put(key, _atomicLongMap.get(key));
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
	/// | map             | Map<String, String>| Returns current mappings                                                      |
	/// | error           | String             | Errors, if any                                                                |
	/// | exceptionMsg    | String             | Exception message, if any                                                     |
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	///
	public RESTFunction setValue = (req, res) -> {
		if (_atomicLongMap == null) {
			res.put("error", "Atomic Long Map is null");
			return res;
		}
		
		String key = req.getString("key", "");
		if (key.isEmpty()) {
			res.put("error", "No key was supplied");
			return res;
		}
		
		boolean allowEmptyValue = req.getBoolean("allowEmptyValue", false);
		Long value = req.getLong("value");
		if (value == null && !allowEmptyValue) {
			res.put("error", "An empty value was supplied, and allowEmptyValue is false");
			return res;
		}
		
		res.put("map", _atomicLongMap.put(key, value));
		
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
		if (_atomicLongMap == null) {
			res.put("error", "Atomic Long Map is null");
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
				Long value = (Long) keyValues.get(key);
				if (value == null) {
					if (allowEmptyValue) {
						_atomicLongMap.put(key, value);
					}
				} else {
					_atomicLongMap.put(key, value);
				}
			}
		} catch (Exception ex) {
			res.put("error", "An error occured while trying to iterate and set values");
			res.put("exceptionMsg", ex.getMessage());
			return res;
		}
		
		res.put("map", _atomicLongMap);
		
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
		if (_atomicLongMap == null) {
			res.put("error", "Atomic Long Map is null");
			return res;
		}
		
		String key = req.getString("key", "");
		if (key.isEmpty()) {
			res.put("error", "An empty key was supplied");
			return res;
		}
		
		try {
			_atomicLongMap.remove(key);
		} catch (Exception ex) {
			res.put("error", "An error occured while trying to delete a value");
			res.put("exceptionMsg", ex.getMessage());
			return res;
		}
		
		res.put("map", _atomicLongMap);
		
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
		if (_atomicLongMap == null) {
			res.put("error", "Atomic Long Map is null");
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
				if (_atomicLongMap.containsKey(key)) {
					_atomicLongMap.remove(key);
				}
			}
		} catch (Exception ex) {
			res.put("error", "An error occured while iterating and deleting mappings");
			res.put("exceptionMsg", ex.getMessage());
			return res;
		}
		
		res.put("map", _atomicLongMap);
		
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
		if (_atomicLongMap == null) {
			res.put("error", "Atomic Long Map is null");
			return res;
		}
		
		res.put("map", _atomicLongMap);
		
		return res;
	};
	
	public RESTFunction weakCompareAndSet = (req, res) -> {
		if (_atomicLongMap == null) {
			res.put("error", "Atomic Long Map is null");
			return res;
		}
		
		String key = req.getString("key", "");
		if (key.isEmpty()) {
			res.put("error", "No key was supplied");
			return res;
		}
		
		boolean allowEmptyValue = req.getBoolean("allowEmptyValue", false);
		Long expect = req.getLong("expect");
		if (expect == null && !allowEmptyValue) {
			res.put("error", "An empty value was supplied, and allowEmptyValue is false");
			return res;
		}
		
		Long update = req.getLong("update");
		
		res.put("map", _atomicLongMap.weakCompareAndSet(key, expect, update));
		
		return res;
	};
	
	public RESTFunction getAndAdd = (req, res) -> {
		if (_atomicLongMap == null) {
			res.put("error", "Atomic Long Map is null");
			return res;
		}
		
		//Get given key
		String key = req.getString("key", "");
		if (key.isEmpty()) {
			res.put("error", "No key was supplied");
			return res;
		}
		
		boolean allowEmptyValue = req.getBoolean("allowEmptyValue", false);
		Object delta = req.get("delta");
		if (delta == null && !allowEmptyValue) {
			res.put("error", "An empty value was supplied, and allowEmptyValue is false");
			return res;
		}
		
		res.put("value", _atomicLongMap.getAndAdd(key, delta));
		
		return res;
	};
	
	public RESTFunction increment = (req, res) -> {
		if (_atomicLongMap == null) {
			res.put("error", "Atomic Long Map is null");
			return res;
		}
		
		//Get given key
		String key = req.getString("key", "");
		if (key.isEmpty()) {
			res.put("error", "No key was supplied");
			return res;
		}
		
		// boolean allowEmptyValue = req.getBoolean("allowEmptyValue", false);
		// Long value = req.getLong("value");
		// if (value == null && !allowEmptyValue) {
		// 	res.put("error", "An empty value was supplied, and allowEmptyValue is false");
		// 	return res;
		// }
		
		//Add found value to map
		res.put("value", _atomicLongMap.incrementAndGet(key));
		
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
		rb.getNamespace(setPrefix + "map").put(HttpRequestType.GET, getMap);
		
		//Get values
		rb.getNamespace(setPrefix + "value").put(HttpRequestType.GET, getValue);
		rb.getNamespace(setPrefix + "values").put(HttpRequestType.GET, getValues);
		
		//Set values
		rb.getNamespace(setPrefix + "value").put(HttpRequestType.POST, setValue);
		rb.getNamespace(setPrefix + "values").put(HttpRequestType.POST, setValues);
		
		//Delete mappings
		rb.getNamespace(setPrefix + "deleteValue").put(HttpRequestType.POST, deleteValue);
		rb.getNamespace(setPrefix + "deleteValues").put(HttpRequestType.POST, deleteValues);
		
		rb.getNamespace(setPrefix + "weakCompareAndSet").put(HttpRequestType.POST, weakCompareAndSet);
		rb.getNamespace(setPrefix + "getAndAdd").put(HttpRequestType.POST, getAndAdd);
		rb.getNamespace(setPrefix + "increment").put(HttpRequestType.POST, increment);
		
		return rb;
	}
}

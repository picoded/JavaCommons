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
	
	public KeyValueMapApiBuilder(Map<String, String> inMap){
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
		//Get given key
		String key = req.getString("key", "");
		if(key.isEmpty()){
			res.put("error", "No key was supplied");
			return res;
		}
		
		//Add found value to map
		res.put("value",  _keyValueMap.get(key));
		
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
	public RESTFunction getValues =(req, res) -> {
		String[] keys = req.getStringArray("keys", null);
		if(keys == null){
			res.put("error", "No key array was supplied");
			return res;
		}
		
		if(keys.length < 1){
			res.put("error", "Key array supplied was empty");
			return res;
		}
		
		Map<String, String> valueMap = new HashMap<String, String>();
		
		try{
			for(String key : keys){
				valueMap.put(key, _keyValueMap.get(key));
			}
		}catch(Exception ex){
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
	/// | error           | String             | Errors, if any                                                                |
	/// | exceptionMsg    | String             | Exception message, if any                                                     |
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	///
	public RESTFunction setValue = (req, res) -> {
		String key = req.getString("key", "");
		if(key.isEmpty()){
			res.put("error", "No key was supplied");
			return res;
		}
		
		boolean allowEmptyValue = req.getBoolean("allowEmptyValue", false);
		String value = req.getString("value", "");
		if(value.isEmpty() && !allowEmptyValue){
			res.put("error", "An empty value was supplied, and allowEmptyValue is false");
			return res;
		}
		
		try{
			_keyValueMap.put(key, value);
		}catch(Exception ex){
			res.put("error", "An error occured while trying to set a value");
			res.put("exceptionMsg", ex.getMessage());
			return res;
		}
		
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
	/// | error           | String             | Errors, if any                                                                |
	/// | exceptionMsg    | String             | Exception message, if any                                                     |
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	///
	public RESTFunction setValues =(req, res) -> {
		Object keyValues_raw = req.get("meta");
		if(keyValues_raw == null){
			res.put("error", "No keyValues map was supplied");
			return res;
		}
		
		Map<String, Object> keyValues = null;
		try{
			if(keyValues_raw instanceof String){
				keyValues = ConvertJSON.toMap((String)keyValues_raw);
			}
		}catch(Exception ex){
			res.put("error", "Conversion to map object failed");
			res.put("exceptionMsg", ex.getMessage());
			return res;
		}
		
		boolean allowEmptyValue = req.getBoolean("allowEmptyValue", false);
		try{
			for(String key : keyValues.keySet()){
				String value = (String)keyValues.get(key);
				if(value.isEmpty()){
					if(allowEmptyValue){
						_keyValueMap.put(key, value);
					}
				}else{
					_keyValueMap.put(key, value);
				}
			}
		} catch (Exception ex){
			res.put("error", "An error occured while trying to iterate and set values");
			res.put("exceptionMsg", ex.getMessage());
			return res;
		}
		
		return res;
	};
}

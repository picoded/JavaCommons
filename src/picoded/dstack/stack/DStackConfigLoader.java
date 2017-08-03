package picoded.dstack.stack;

import java.security.InvalidParameterException;
import java.util.*;

import picoded.dstack.DStack;
import picoded.dstack.jsql.JSqlStack;
import picoded.dstack.jsql.connector.JSql;
import picoded.dstack.CommonStack;
import picoded.dstack.*;
import picoded.dstack.struct.simple.StructSimpleStack;

import picoded.struct.GenericConvertMap;
import picoded.conv.GenericConvert;

/**
 * Static class used to load a DStack object, from its configuration string/file
 */
public class DStackConfigLoader {
	
	/**
	 * @TODO : Port this over to something like JSqlConfigLoader, or even JSql itself
	 * @TODO : Define the various configuration option that is valid (for inConfigMap)
	 * 
	 * Generate a JSQL object using a configuration map
	 * 
	 * 
	 * @param  inConfigMap   configuration map of a single stack layer
	 * 
	 * @return  the JSql connection
	 */
	private static JSql generateJSql( Object inConfigMap ) {
		// Normalizing the config map (so its easier to use)
		GenericConvertMap<String, Object> configMap = GenericConvert.toGenericConvertStringMap(inConfigMap);

		// Get common parameters
		String engine = configMap.getString("engine", "");
		String path = configMap.getString("path", null);

		// Generate sqlite connection when applicable
		if( engine.equalsIgnoreCase("sqlite") ) {
			if ( path == null ) {
				// Get in memory
				return JSql.sqlite();
			}
			return JSql.sqlite(path);
		} 
		
		// Common settings for remote database
		String username = configMap.getString("username", "");
		String password = configMap.getString("password", "");
		String database = configMap.getString("database", "");

		// Generate the JSQL connection accordingly
		if( engine.equalsIgnoreCase("mysql") ) {
			return JSql.mysql(path, database, username, password);
		} else if( engine.equalsIgnoreCase("mssql") ) {
			throw new InvalidParameterException("MSSQL not yet implemented.");
		} else if( engine.equalsIgnoreCase("oracle")) {
			throw new InvalidParameterException("Oracle not yet implemented.");
		} else {
			throw new InvalidParameterException("Unknown engine type to load : "+engine);
		}
	}

	/**
	 * Generate a single common stack object using the configuration map
	 * 
	 * @TODO : Define the various configuration option that is valid (for inConfigMap)
	 * 
	 * @param  inConfigMap   configuration map of a single stack layer
	 * 
	 * @return  the stack implementation
	 */
	public static CommonStack generateCommonStack( Object inConfigMap ) {
		// Normalizing the config map (so its easier to use)
		GenericConvertMap<String, Object> configMap = GenericConvert.toGenericConvertStringMap(inConfigMap);
		
		// The return object
		CommonStack ret = null;

		// Get config type, and generate the return value accordingly
		String type = configMap.getString("type");
		if( type.equalsIgnoreCase("StructSimple") ) {	
			// StructSimple implementation
			ret = new StructSimpleStack();
		} else if( type.equalsIgnoreCase("JSql") ) {
			JSql conn = generateJSql(configMap);
			ret = new JSqlStack(conn);
		} else {
			throw new InvalidParameterException("Unknown configuration type to load : "+type);
		}

		// Save the config map into the stack implementaiton
		if( ret != null ) {
			ret.configMap().putAll(configMap);
		}

		// Return the loaded CommonStack
		return ret;
	}

	/**
	 * Generates the DataStack using an array of stack options
	 * 
	 * @TODO : Define the various configuration option that is valid (for stackConfig)
	 * 
	 * @param  stackConfig  The list of option objects, each represent a datastack layer
	 * 
	 * @return The generated data stack with all layers loaded
	 */
	public static DStack generateDStack( Object stackConfig ) {
		// Ensure the input is a list
		List<Object> stackOptionList = GenericConvert.toList(stackConfig);
		if( stackOptionList == null ) {
			throw new InvalidParameterException("Unexpected stackConfig, should be list<map<string,object>> compatible");
		}

		// Prepare the stack layers to use to setup the DStack
		List<CommonStack> stackLayers = new ArrayList<>();
		for ( Object stackOption : stackOptionList ) {

			// Convert the single option into a map
			Map<String,Object> optionMap = GenericConvert.toStringMap(stackOption);
			if( optionMap == null ) {
				throw new InvalidParameterException("Unexpected stackOption map found in list : "+stackOption);
			}

			// Get the CommonStack object for that option
			CommonStack stack = generateCommonStack(optionMap);
			stackLayers.add(stack);
		}

		// Use the list of CommonStack object, build the final dstack object
		DStack dstack = new DStack(stackLayers);
		return dstack;
	}

}

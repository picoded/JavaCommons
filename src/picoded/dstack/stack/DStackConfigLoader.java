package picoded.dstack.stack;

import java.security.InvalidParameterException;
import java.util.*;

import picoded.dstack.DStack;
import picoded.dstack.jsql.JSqlStack;
import picoded.dstack.jsql.connector.JSql;
import picoded.dstack.CommonStack;
import picoded.dstack.*;

import picoded.struct.GenericConvertMap;
import picoded.conv.GenericConvert;

public class DStackConfigLoader {
	
	protected static CommonStack generateCommonStack( Map<String,Object> configMap ) {
		return null;
		// GenericConvertMap<String, Object> configMap = ConvertJSON.toCustomClass(config, GenericConvertMap.class);
		// if ( configMap == null )
		// return null;
		// String type = configMap.getString("type");

		// if ( type.equalsIgnoreCase("Jsql") ) {
		// String engine = configMap.getString("engine", "");
		// String path = configMap.getString("path", "");
		// String username = configMap.getString("username", "");
		// String password = configMap.getString("password", "");
		// String database = configMap.getString("database", "");
		// if ( path.isEmpty() )
		// throw new RuntimeException("DStack path is not set.");
		// if ( engine.equalsIgnoreCase("sqlite") ) {
		// return  new JSqlStack(JSql.sqlite(path));
		// } else if ( egine.equalsIgnoreCase("mysql") ) {
		// return new JSqlStack(Jsql.mysql(path, database, username, password));
		// }
		// return null;  
		// }
	}

	/**
	 * Generates the DataStack using an array of stack options
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

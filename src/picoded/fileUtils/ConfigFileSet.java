package picoded.fileUtils;

///
import picoded.struct.GenericConvertMap;
import java.util.Map;
import java.util.Arrays;
import java.util.HashMap;
import java.io.File;
import java.io.IOException;

import org.ini4j.Ini;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.ArrayUtils;


///
/// Config file loader
///
/// Iterates a filepath and loads all ini config files
///
/// Note that folder, and filename will be used as the config path. Unless its config.ini
///
public class ConfigFileSet extends ConfigFile implements GenericConvertMap<String, String> {
	
	/// The actual inner prefix set, which is searched when a request is made
	///
	/// Note that the inner map are actually ConfigFile 
	public Map<String, Map<String,String>> prefixSetMap = new HashMap<String, Map<String,String>>();
	
	/// 
	public ConfigFileSet() {
		
	}
	
	/// Config file set iteration and load with prefix
	public ConfigFileSet addConfigSet(File inFile, String prefix) {
		if(inFile == null) {
			return this;
		}
		
		// prefix defaults to blank
		if( prefix == null ) {
			prefix = "";
		} else {
			prefix = prefix.trim();
		}
		
		// The current file / folder name
		String fileName = inFile.getName();
		String fileNameWithoutType = null;
		
		// Iterate sub file directory
		if(inFile.isDirectory()) {
			String subPrefix = prefix+"."+fileName;
			
			File[] subFiles = inFile.listFiles();
			for (int i = 0; i < subFiles.length; i++){
				addConfigSet(subFiles[i], subPrefix);
			}
			return this;
		}
		
		// Else files
		if( fileName.endsWith(".ini") ) {
			fileNameWithoutType = fileName.substring(0, fileName.length() - 4 ); // - (".ini").length() 
			
			// its a config.ini, so dun use sub namespace?
			if(fileNameWithoutType.equals("config")) {
				prefixSetMap.put(prefix, new ConfigFile(inFile));
			} else { // Use file name as sub namespace
				prefixSetMap.put(prefix+"."+fileNameWithoutType, new ConfigFile(inFile));
			}
		}
		
		return this;
	}
	
	/// Gets the config value string, from the file 
	public String get(Object key) {
		
		String keyString = key.toString();
		String[] splitKeyString = keyString.split("\\.");
		
		for(int splitPt = splitKeyString.length - 1; splitPt >= 0; --splitPt) {
			String section = StringUtils.join( ArrayUtils.subarray(splitKeyString, 0, splitPt), ".");
			String ending = StringUtils.join( ArrayUtils.subarray(splitKeyString, splitPt, splitKeyString.length), ".");
			
			Map<String,String> subMap = prefixSetMap.get(section);
			if( subMap != null ) {
				return subMap.get(ending);
			}
		}
		
		return null;
	}
	
}

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
	
	public ConfigFileSet addConfigSet(String filePath) {
		return addConfigSet(new File(filePath), null);
	}
	
	public ConfigFileSet addConfigSet(File inFile) {
		return addConfigSet(inFile, null);
	}
	
	/// Config file set iteration and load with prefix
	public ConfigFileSet addConfigSet(String filePath, String prefix) {
		return addConfigSet(new File(filePath), prefix);
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
		
		String nxtPrefix = "";
		if(prefix != null && prefix.length() > 0) {
			nxtPrefix = prefix+".";
		}
		
		// Iterate sub file directory
		if(inFile.isDirectory()) {
			File[] subFiles = inFile.listFiles();
			
			for (int i = 0; i < subFiles.length; i++){
				String subFileName = subFiles[i].getName();
				
				// @TODO : proper filename without type
				String subFileNameWithoutType = subFileName;
				if( subFileName.endsWith(".ini") ) {
					subFileNameWithoutType = subFileNameWithoutType.substring(0, subFileNameWithoutType.length() - 4 ); 
				}
				
				String subPrefix = nxtPrefix+subFileNameWithoutType;
				addConfigSet(subFiles[i], subPrefix);
			}
			return this;
		}
		
		// Else files
		String fileName = inFile.getName();
		if( fileName.endsWith(".ini") ) {
			String fileNameWithoutType = fileName.substring(0, fileName.length() - 4 ); // - (".ini").length() 
			prefixSetMap.put(prefix, new ConfigFile(inFile));
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

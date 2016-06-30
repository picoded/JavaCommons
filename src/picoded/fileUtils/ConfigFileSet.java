package picoded.fileUtils;

///
import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import picoded.struct.GenericConvertMap;

///
/// Config file loader
///
/// Iterates a filepath and loads all ini config files
///
/// Note that folder, and filename will be used as the config path. Unless its config.ini
///
public class ConfigFileSet extends ConfigFile implements GenericConvertMap<String, Object> {
	
	// / The actual inner prefix set, which is searched when a request is made
	// /
	// / Note that the inner map are actually ConfigFile
	
	// <main, <main-include.test, hello>>
	// <related <main-include.text, hi>>
	// json-><jsonFileName <jsonKey, jsonValue>>
	public Map<String, Map<String, Object>> prefixSetMap = new HashMap<String, Map<String, Object>>();
	
	public ConfigFileSet() {
		
	}
	
	// / Constructor with the default file path to scan
	public ConfigFileSet(String filePath) {
		addConfigSet(filePath);
	}
	
	public ConfigFileSet addConfigSet(File filePath) {
		return addConfigSet(filePath, "", ".");
	}
	
	public ConfigFileSet addConfigSet(String filePath) {
		return addConfigSet(filePath, "", ".");
	}
	
	public ConfigFileSet addConfigSet(String filePath, String prefix, String separator) {
		return addConfigSet_recursive(new File(filePath), prefix, separator);
	}
	
	public ConfigFileSet addConfigSet(File inFile, String prefix, String separator) {
		return addConfigSet_recursive(inFile, prefix, separator);
	}
	
	private ConfigFileSet addConfigSet_recursive(File inFile, String rootPrefix, String separator) {
		if (rootPrefix == null) {
			rootPrefix = "";
		}
		
		if (inFile.isDirectory()) {
			File[] innerFiles = inFile.listFiles();
			for (File innerFile : innerFiles) {
				if (innerFile.isDirectory()) {
					String parentFolderName = innerFile.getName();
					if (!rootPrefix.isEmpty()) {
						parentFolderName = rootPrefix + separator + parentFolderName;
					}
					addConfigSet_recursive(innerFile, parentFolderName, separator);
				} else {
					addConfigSet_recursive(innerFile, rootPrefix, separator);
				}
			}
		} else {
			String fileName = inFile.getName();
			
			// Get the filename extension
			int endingDot = fileName.lastIndexOf('.');
			String extension = "";
			if (endingDot > 0) {
				extension = fileName.substring(endingDot + 1);
			}
			
			// Only accept ini or json files
			if (extension.equalsIgnoreCase("ini") || extension.equalsIgnoreCase("json")
				|| extension.equalsIgnoreCase("js")) {
				
				ConfigFile cFile = new ConfigFile(inFile);
				
				fileName = fileName.substring(0, fileName.lastIndexOf('.'));
				String prefix = "";
				if (!rootPrefix.isEmpty()) {
					prefix += rootPrefix + separator;
				}
				
				prefixSetMap.put(prefix + fileName, cFile);
			}
		}
		
		return this;
	}
	
	public Object get(Object key) {
		String keyString = key.toString();
		String[] splitKeyString = keyString.split("\\.");
		
		// an issue could arise if there are conflicting keys
		// example
		// <a.b.c, <d, e>> //json
		// <a.b, <c.d, e>> //ini file
		// in this case, passing a key of "a.b.c.d" will always hit the json
		// file first, which might not be intended.
		// having nonconflicting keys will avoid this, but this is just a heads
		// up
		for (int splitPt = splitKeyString.length - 1; splitPt >= 0; --splitPt) {
			String fileKey = StringUtils.join(ArrayUtils.subarray(splitKeyString, 0, splitPt), ".");
			String headerKey = StringUtils.join(ArrayUtils.subarray(splitKeyString, splitPt, splitKeyString.length), ".");
			
			Object returnVal = get_safe(fileKey, headerKey);
			
			if (returnVal != null) {
				return returnVal;
			}
		}
		
		return null;
	}
	
	// use this if you know the exact keyvaluepair you want
	public Object get_safe(Object fileKey, Object headerKey) {
		String fileKeyString = fileKey.toString();
		String headerKeyString = headerKey.toString();
		Map<String, Object> subMap = prefixSetMap.get(fileKeyString);
		if (subMap != null) {
			return subMap.get(headerKeyString);
		}
		
		return null;
	}
}

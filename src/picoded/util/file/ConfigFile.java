package picoded.util.file;

import java.io.File;
import java.io.IOException;
import java.util.*;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.ini4j.Ini;

import picoded.core.conv.*;
import picoded.core.struct.*;

//import com.hazelcast.com.eclipsesource.json.JsonObject;

/**
 * Config file loader
 *
 * Takes in an INI file, gives out a map.
 *
 * @TO-DO
 * + Unit test, extended functions
 *
 * + Variable subsitution (use key values as key names)
 *   eg: sys.${sys.selectedStack; default}.database
 * + Array support
 *   eg: sys.dbStack[0].database
 * + Nested substitution
 *   eg: sys.${sys.selectedStack; sys.default}.database
 *
 * + JSON files delayed load
 * + Spliting INI, and JSON load into its own seperate class,
 *   use ConfigFileSet to switch between classes on setup
 *
 * @TO-CONSIDER
 * + Case insensitive key names?
 * + File write????
 **/
public class ConfigFile implements GenericConvertMap<String, Object> {
	
	/**
	 * The actual inner map storage
	 **/
	
	// If this file is an INI file
	Ini iniMap = null;
	
	// If its a json file
	GenericConvertMap<String, Object> jsonMap = null;
	
	// The config file name
	String fileName = "";
	
	/**
	 * Blank constructor
	 **/
	protected ConfigFile() {
		
	}
	
	/**
	 * Constructor, which takes in an file object and stores it
	 **/
	public ConfigFile(File fileObj) {
		innerConstructor(fileObj);
	}
	
	/**
	 * Constructor, which takes in an file path and stores it
	 **/
	public ConfigFile(String filePath) {
		innerConstructor(new File(filePath));
	}
	
	private void innerConstructor(File inFile) {
		try {
			fileName = inFile.getName();
			if (fileName.endsWith(".js") || fileName.endsWith(".json")) {
				String jsString = FileUtils.readFileToString(inFile);
				jsonMap = GenericConvert.toGenericConvertStringMap(jsString,
					new HashMap<String, Object>());
			} else if (fileName.endsWith(".ini")) {
				iniMap = new Ini(inFile);
			} else if (fileName.endsWith(".html")) {
				String jsString = FileUtils.readFileToString(inFile);
				jsonMap = new GenericConvertHashMap<String, Object>();
				jsonMap.put("html", jsString);
			} else {
				throw new RuntimeException("Unsupported file type : " + fileName);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public String fileName() {
		return fileName;
	}
	
	/**
	 * Gets the config value string, from the file
	 **/
	public Object get(Object key) {
		String keyString = key.toString();
		if (jsonMap != null) {
			return jsonMap.fetchObject(keyString);
		} else {
			// read from ini
			String[] splitKeyString = keyString.split("\\.");
			String section = StringUtils.join(
				ArrayUtils.subarray(splitKeyString, 0, splitKeyString.length - 1), "."); // name
			
			// [keys] in brackets are considered a section
			String sectionKey = splitKeyString[splitKeyString.length - 1];
			Ini.Section iniSection = iniMap.get(section);
			
			// Ini section handling
			return (iniSection == null) ? null : iniSection.get(sectionKey);
		}
	}
	
	/**
	 * Top layer keySet fetching
	 **/
	public Set<String> keySet() {
		if (jsonMap != null) {
			return jsonMap.keySet();
		} else {
			return iniMap.keySet();
		}
	}
}

package picoded.fileUtils;

///
import picoded.conv.ConvertJSON;
import picoded.struct.GenericConvertMap;

import java.util.HashMap;
import java.util.Map;
import java.util.Arrays;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.ini4j.Ini;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.ArrayUtils;

//import com.hazelcast.com.eclipsesource.json.JsonObject;

///
/// Config file loader
///
/// Takes in an INI file, gives out a map. 
///
/// @TO-DO
/// + Unit test, extended functions
/// + JSON files
///
/// @TO-CONSIDER
/// + Case insensitive key names?
/// + File write????
///
public class ConfigFile implements GenericConvertMap<String, Object> {
	
	/// The actual inner map storage
	Ini iniMap = null;
	
	Map<String, Object> jsonMap = null;
	
	boolean jsonMode = false;
	/// Blank constructor
	protected ConfigFile() {
		
	}
	
	/// Constructor, which takes in an INI file object and stores it 
	public ConfigFile(File fileObj) {
		innerConstructor(fileObj);
	}
	
	/// Constructor, which takes in an INI file path and stores it 
	public ConfigFile(String filePath) {
		innerConstructor(new File(filePath));
	}
	
	private void innerConstructor(File inFile){
		try {
			String fileName = inFile.getName();
			if(fileName.endsWith(".js") || fileName.endsWith(".json")){
				jsonMode = true;
				jsonMap = new HashMap<String, Object>();
				
				String jsString = FileUtils.readFileToString(inFile);
				jsonMap = ConvertJSON.toMap(jsString);
			}else{
				iniMap = new Ini(inFile);
			}
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	/// Gets the config value string, from the file 
	public Object get(Object key) {
		if(jsonMode){
			//read from json map
			return jsonMap.get(key);
		}else{
			//read from ini
			String keyString = key.toString();
			String[] splitKeyString = keyString.split("\\.");
			
			String section = StringUtils.join( ArrayUtils.subarray(splitKeyString, 0, splitKeyString.length-1), "."); //name in [] brackets is a section
			String sectionKey = splitKeyString[ splitKeyString.length - 1 ];
			
			Ini.Section iniSection = iniMap.get(section);
			
			return (iniSection == null)? null : iniSection.get(sectionKey);
		}
	}
}

package picoded.fileUtils;

///
import picoded.struct.GenericConvertMap;
import java.util.Map;
import java.io.File;
import java.io.IOException;

import org.ini4j.Ini;
import org.apache.commons.lang3.StringUtils;

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
public class ConfigFile implements GenericConvertMap<String, String> {
	
	/// The actual inner map storage
	Ini iniMap = null;
	
	
	/// Blank constructor
	protected ConfigFile() {
		
	}
	
	/// Constructor, which takes in an INI file object and stores it 
	public ConfigFile(File fileObj) {
		try {
			iniMap = new Ini(fileObj);
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	/// Constructor, which takes in an INI file path and stores it 
	public ConfigFile(String filePath) {
		try {
			iniMap = new Ini(new File(filePath));
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	/// Gets the config value string, from the file 
	public String get(Object key) {
		String keyString = key.toString();
		String[] splitKeyString = keyString.split("\\.");
		
		String section = StringUtils.join(splitKeyString, ".");
		String ending = splitKeyString[ splitKeyString.length - 1 ];
		
		Ini.Section iniSection = iniMap.get(section);
		
		return (iniSection == null)? null : iniSection.get(ending);
	}
	
}

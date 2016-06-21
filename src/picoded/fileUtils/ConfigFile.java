package picoded.fileUtils;

///
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.ini4j.Ini;

import picoded.conv.ConvertJSON;
import picoded.struct.GenericConvertMap;

//import com.hazelcast.com.eclipsesource.json.JsonObject;

///
/// Config file loader
///
/// Takes in an INI file, gives out a map. 
///
/// @TO-DO
/// + Unit test, extended functions
///
/// + Variable subsitution (use key values as key names)
///   eg: sys.${sys.selectedStack; default}.database
/// + Array support
///   eg: sys.dbStack[0].database
/// + Nested substitution
///   eg: sys.${sys.selectedStack; sys.default}.database
///
/// + JSON files delayed load
/// + Spliting INI, and JSON load into its own seperate class, 
///   use ConfigFileSet to switch between classes on setup
///
/// @TO-CONSIDER
/// + Case insensitive key names?
/// + File write????
///
public class ConfigFile implements GenericConvertMap<String, Object> {

	// / The actual inner map storage
	Ini iniMap = null;
	Map<String, Object> jsonMap = null;

	boolean jsonMode = false;

	String fileName = "";

	// / Blank constructor
	protected ConfigFile() {

	}

	// / Constructor, which takes in an INI file object and stores it
	public ConfigFile(File fileObj) {
		innerConstructor(fileObj);
	}

	// / Constructor, which takes in an INI file path and stores it
	public ConfigFile(String filePath) {
		innerConstructor(new File(filePath));
	}

	private void innerConstructor(File inFile) {
		try {
			fileName = inFile.getName();
			if (fileName.endsWith(".js") || fileName.endsWith(".json")) {
				jsonMode = true;
				jsonMap = new HashMap<String, Object>();

				String jsString = FileUtils.readFileToString(inFile);
				jsonMap = ConvertJSON.toMap(jsString);

				for (String key : jsonMap.keySet()) {
					jsonMap.put(key, jsonMap.get(key));
				}
			} else {
				iniMap = new Ini(inFile);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public String fileName() {
		return fileName;
	}

	// / Gets the config value string, from the file
	public Object get(Object key) {
		if (jsonMode) {
			return getJson(key, jsonMap);
		} else {
			// read from ini
			String keyString = key.toString();
			String[] splitKeyString = keyString.split("\\.");

			String section = StringUtils.join(ArrayUtils.subarray(
					splitKeyString, 0, splitKeyString.length - 1), "."); // name
																			// in
																			// []
																			// brackets
																			// is
																			// a
																			// section
			String sectionKey = splitKeyString[splitKeyString.length - 1];

			Ini.Section iniSection = iniMap.get(section);

			return (iniSection == null) ? null : iniSection.get(sectionKey);
		}
	}

	@SuppressWarnings("unchecked")
	private Object getJson(Object key, Object currentResult) {
		String keyString = key.toString();

		Map<String, Object> currentResultMap = null;
		if (currentResult instanceof Map) {
			currentResultMap = (Map<String, Object>) currentResult;
		}

		if (keyString.contains(".")) {
			String jsonKey = keyString.substring(0, keyString.indexOf("."));
			String jsonKeyRemainder = keyString.substring(
					keyString.indexOf(".") + 1, keyString.length());

			Object jsonObj = currentResultMap.get(jsonKey);
			return getJson(jsonKeyRemainder, jsonObj);
		} else {
			return currentResultMap.get(keyString);
		}
	}
}

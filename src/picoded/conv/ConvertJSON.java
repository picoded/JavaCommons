package picoded.conv;

// Java libs
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;

import com.fasterxml.jackson.core.JsonParser;
// Jackson library
import com.fasterxml.jackson.databind.ObjectMapper;

///
/// json simplification helpers. When you do not need custom object / array structures
///
///
/// ---------------------------------------------------------------------------------------------------
///
/// Technical notes: Jackson is used internally.
///
public final class ConvertJSON {
	
	private ConvertJSON() {
		//cachedMapperBuilder();
		
	}
	
	/// cachedMapper builder, used to setup the config
	private static ObjectMapper cachedMapperBuilder() {
		ObjectMapper cm = new ObjectMapper();
		
		// Allow comments in strings
		cm.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
		
		// Allow leading 0's in the int
		cm.configure(JsonParser.Feature.ALLOW_NUMERIC_LEADING_ZEROS, true);
		
		// Allow single quotes in JSON
		cm.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
		
		// Actual map builder
		return cm;
	}
	
	/// Internal reused object mapper, this is via jackson json conerter
	private static ObjectMapper cachedMapper = cachedMapperBuilder();
	
	/////////////////////////////////////////////////
	//
	// From java objects to JSON string conversion
	//
	/////////////////////////////////////////////////
	
	/// Converts input object into a json string
	public static String fromMap(Map<String, ?> input) {
		return fromObject(input);
	}
	
	/// Converts input object into a json string
	public static String fromList(List<?> input) {
		return fromObject(input);
	}
	
	/// Converts input object into a json string
	///
	/// Note that this is the core "to JSON string" function that all
	/// other type strict varient is built ontop of.
	public static String fromObject(Object input) {
		try {
			return cachedMapper.writeValueAsString(input);
		} catch (IOException e) { // IOException shdnt occur, as input is not a file
			throw new IllegalArgumentException(e);
		}
	}
	
	/////////////////////////////////////////////////
	//
	// From JSON string to java object
	//
	/////////////////////////////////////////////////
	
	/// Converts json string into an mapping object
	@SuppressWarnings("all")
	public static Map<String, Object> toMap(String input) {
		return (Map<String, Object>) toCustomClass(input, Map.class);
	}
	
	/// Converts json string into an list array
	@SuppressWarnings("all")
	public static List<Object> toList(String input) {
		return (List<Object>) toCustomClass(input, List.class);
	}
	
	/// Converts json string into any output object (depends on input)
	public static Object toObject(String input) {
		return toCustomClass(input, Object.class);
	}
	
	/// Converts json string into a custom output object
	///
	/// Note that this is the core "to java object" function that all
	/// other type strict varient is built ontop of.
	public static Object toCustomClass(String input, Class<?> c) {
		try {
			return cachedMapper.readValue(input, c);
		} catch (IOException e) { // IOException shdnt occur, as input is not a file
			throw new IllegalArgumentException(e);
		}
	}
	
	/////////////////////////////////////////////////
	//
	// From string to array conversion
	//
	/////////////////////////////////////////////////
	
	/// Converts a json string into a string[] array
	public static String[] toStringArray(String input) {
		List<Object> rawList = ConvertJSON.toList(input);
		if (rawList == null) {
			return new String[0];
		}
		
		String[] ret = new String[rawList.size()];
		for (int a = 0; a < rawList.size(); ++a) {
			ret[a] = (String) rawList.get(a);
		}
		return ret;
	}
	
	/// Converts a json string into a double[] array
	public static double[] toDoubleArray(String input) {
		List<Object> rawList = ConvertJSON.toList(input);
		if (rawList == null) {
			return new double[0];
		}
		
		double[] ret = new double[rawList.size()];
		for (int a = 0; a < rawList.size(); ++a) {
			ret[a] = ((Number) rawList.get(a)).doubleValue();
		}
		return ret;
	}
	
	/// Converts a json string into a int[] array
	public static int[] toIntArray(String input) {
		List<Object> rawList = ConvertJSON.toList(input);
		if (rawList == null) {
			return new int[0];
		}
		
		int[] ret = new int[rawList.size()];
		for (int a = 0; a < rawList.size(); ++a) {
			ret[a] = ((Number) rawList.get(a)).intValue();
		}
		return ret;
	}
	
	/// Converts a json string into a Object[] array
	public static Object[] toObjectArray(String input) {
		List<Object> rawList = ConvertJSON.toList(input);
		if (rawList == null) {
			return new Object[0];
		}
		
		Object[] ret = new Object[rawList.size()];
		for (int a = 0; a < rawList.size(); ++a) {
			ret[a] = rawList.get(a);
		}
		return ret;
	}
	
	/////////////////////////////////////////////////
	//
	// From array conversion to string
	//
	/////////////////////////////////////////////////
	
	/// Converts a Object[] to a json string
	public static String fromArray(Object[] input) {
		return jsonStringfromArray(input);
	}
	
	/// Converts a String[] to a json string
	public static String fromArray(String[] input) {
		return jsonStringfromArray(input);
	}
	
	/// Converts a double[] to a json string
	public static String fromArray(double[] input) {
		return jsonStringfromArray(ArrayUtils.toObject(input));
	}
	
	/// Converts a int[] to a json string
	public static String fromArray(int[] input) {
		return jsonStringfromArray(ArrayUtils.toObject(input));
	}
	
	/// Converts a object[] to a json string
	public static String jsonStringfromArray(Object[] input) {
		String jsonString = "";
		if (input == null) {
			return jsonString;
		} else if (input.length == 0) {
			jsonString += "[]";
		} else {
			jsonString += "[";
			jsonString += String.valueOf(input[0]);
			for (int i = 1; i < input.length; i++) {
				jsonString += ",";
				jsonString += String.valueOf(input[i]);
			}
			jsonString += "]";
		}
		return jsonString;
	}
}

package picoded.conv;

import java.lang.String;
import java.lang.Number;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;

/// json simplification helpers. When you do not need custom object / array structures
///
/// @TODO : Complete the tests cases
/// @TODO : fromStringArray, fromDoubleArray, fromIntArray
///
/// ---------------------------------------------------------------------------------------------------
///
/// Technical notes: Jackson is used internally.
///
public class ConvertJSON {
	
	/// Internal reused object mapper, this is via jackson json conerter
	private static ObjectMapper cachedMapper = new ObjectMapper();
	
	/////////////////////////////////////////////////
	// From java objects to JSON string conversion
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
		} catch (IOException e) { //IOException shdnt occur, as input is not a file
			throw new RuntimeException(e);
		}
	}
	
	/////////////////////////////////////////////////
	// From JSON string to java object
	/////////////////////////////////////////////////
	
	/// Converts json string into an mapping object
	@SuppressWarnings("unchecked")
	public static Map<String, Object> toMap(String input) {
		return (Map<String, Object>) toCustomClass(input, Map.class);
	}
	
	/// Converts json string into an list array
	@SuppressWarnings("unchecked")
	public static List<Object> toList(String input) {
		return (List<Object>) toCustomClass(input, List.class);
	}
	
	/// Converts json string into any output object (depends on input)
	@SuppressWarnings("unchecked")
	public static Object toObject(String input) {
		return toCustomClass(input, Object.class);
	}
	
	/// Converts json string into a custom output object
	///
	/// Note that this is the core "to java object" function that all
	/// other type strict varient is built ontop of.
	@SuppressWarnings("unchecked")
	public static Object toCustomClass(String input, Class<?> c) {
		try {
			return cachedMapper.readValue(input, c);
		} catch (IOException e) { //IOException shdnt occur, as input is not a file
			throw new RuntimeException(e);
		}
	}
	
	/////////////////////////////////////////////////
	// To refactor, Test, and creates its conversion counterpart
	/////////////////////////////////////////////////
	
	/// Converts a json string into a string[] array
	@SuppressWarnings("unchecked")
	public static String[] toStringArray(String input) {
		List<Object> rawList = ConvertJSON.toList(input);
		if (rawList == null || rawList.size() <= 0) {
			return null;
		}
		
		int len = rawList.size();
		String[] ret = new String[len];
		for (int a = 0; a < len; ++a) {
			ret[a] = (String) rawList.get(a);
		}
		return ret;
	}
	
	/// Converts a json string into a double[] array
	@SuppressWarnings("unchecked")
	public static double[] toDoubleArray(String input) {
		List<Object> rawList = ConvertJSON.toList(input);
		if (rawList == null || rawList.size() <= 0) {
			return null;
		}
		
		int len = rawList.size();
		double[] ret = new double[len];
		for (int a = 0; a < len; ++a) {
			ret[a] = ((Number) rawList.get(a)).doubleValue();
		}
		return ret;
	}
	
	/// Converts a json string into a int[] array
	@SuppressWarnings("unchecked")
	public static int[] toIntArray(String input) {
		List<Object> rawList = ConvertJSON.toList(input);
		if (rawList == null || rawList.size() <= 0) {
			return null;
		}
		
		int len = rawList.size();
		int[] ret = new int[len];
		for (int a = 0; a < len; ++a) {
			ret[a] = ((Number) rawList.get(a)).intValue();
		}
		return ret;
	}
}
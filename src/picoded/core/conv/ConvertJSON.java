package picoded.core.conv;

/**
 * Java libs
 **/
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Jackson library used
 **/
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.core.util.DefaultIndenter;

/**
 * json simplification helpers. When you do not need custom object / array structures.
 *
 * ---------------------------------------------------------------------------------------------------
 *
 * Technical notes: Jackson is used internally.
 **/

/// Which is frankly speaking should be 99.99% of the time. Seriously just use Map,
/// instead of custom classes. It will save you alot of headache in the future.

public class ConvertJSON {
	
	/**
	 * Invalid constructor (throws exception)
	 **/
	protected ConvertJSON() {
		throw new IllegalAccessError("Utility class");
	}
	
	/**
	 * Illegal JSON format type. Used to handle all format exceptions in this class.
	 *
	 * Can be treated as a RuntimeException, and IllegalArgumentException.
	 **/
	public static class InvalidFormatJSON extends IllegalArgumentException {
		/**
		 * Common message
		 **/
		public InvalidFormatJSON(Throwable cause) {
			this("Invalid Format JSON", cause);
		}
		
		/**
		 * Cloning the constructor
		 **/
		public InvalidFormatJSON(String message, Throwable cause) {
			super(message, cause);
		}
	}
	
	/**
	 * Internal reused object mapper, this is via jackson json converter.
	 *
	 * Memoizer for cachedMapper()
	 **/
	private static volatile ObjectMapper cachedMapper = null;
	
	/**
	 * cachedMapper used for JSON string parsing, this is generated once,
	 * and reused for each return call
	 *
	 * Note that the JSON formatting here ALLOW COMMENTS, and single quotes.
	 * Basically making it as "linent" as a valid JSON structure in JS.
	 *
	 * @returns The Jacksons cached map builder
	 **/
	private static ObjectMapper cachedMapper() {
		
		/**
		 * Reuse old object mapping
		 **/
		if (cachedMapper != null) {
			return cachedMapper;
		}
		
		/**
		 * New object mapping
		 **/
		ObjectMapper cm = new ObjectMapper();
		
		/**
		 * Allow comments in strings
		 **/
		cm.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
		
		/**
		 * Allow leading 0's in the int
		 **/
		cm.configure(JsonParser.Feature.ALLOW_NUMERIC_LEADING_ZEROS, true);
		
		/**
		 * Allow single quotes in JSON
		 **/
		cm.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
		
		/**
		 * Actual map builder
		 **/
		return cachedMapper = cm;
	}
	
	/////////////////////////////////////////////////
	//
	// From java objects to JSON string conversion
	//
	/////////////////////////////////////////////////
	
	/**
	 * Converts input Map into a json string
	 *
	 * @param  Input map to convert
	 *
	 * @return The json string
	 **/
	public static String fromMap(Map<String, ?> input) {
		return fromObject(input);
	}
	
	/**
	 * Converts input List into a json string
	 *
	 * @param  Input map to convert
	 *
	 * @return The json string
	 **/
	public static String fromList(List<?> input) {
		return fromObject(input);
	}
	
	/**
	 * Converts input object into a json string
	 *
	 * Note: This refers to java object types, not arrays
	 *
	 * Note: that this is the core "to JSON string" function that all
	 * other type strict varient is built on top of.
	 *
	 * @param  Input object to convert
	 *
	 * @return The json string
	 **/
	public static String fromObject(Object input) {
		try {
			return cachedMapper().writeValueAsString(input);
		} catch (IOException e) {
			/**
			 * Any exception is recasted as InvalidFormatJSON
			 **/
			throw new InvalidFormatJSON(e);
		}
	}
	
	/**
	 * Pretty print indenter
	 **/
	protected static DefaultPrettyPrinter prettyPrinter = null;
	
	/**
	 * Converts input object into a json string
	 *
	 * Note: This refers to java object types, not arrays
	 *
	 * Note: that this is the core "to JSON string" function that all
	 * other type strict varient is built on top of.
	 *
	 * @param  Input object to convert
	 * @param  Boolean true, if output as pretty print
	 *
	 * @return The json string
	 **/
	public static String fromObject(Object input, boolean prettyPrint) {
		/**
		 * No pretty print
		 **/
		if (prettyPrint != true) {
			return fromObject(input);
		}
		
		/**
		 * Ensure pretty printer indenter is configured
		 **/
		if (prettyPrinter == null) {
			/**
			 * Creates first
			 * See: http://stackoverflow.com/questions/28256852/what-is-the-simplest-way-to-configure-the-indentation-spacing-on-a-jackson-objec
			 **/
			DefaultPrettyPrinter.Indenter indenter = new DefaultIndenter("	", DefaultIndenter.SYS_LF);
			DefaultPrettyPrinter printer = new DefaultPrettyPrinter();
			printer.indentObjectsWith(indenter);
			printer.indentArraysWith(indenter);
			
			/**
			 * Then replace : prevents multithread race collisions
			 **/
			prettyPrinter = printer;
		}
		
		/**
		 * With pretty print
		 **/
		try {
			return cachedMapper().writer(prettyPrinter).writeValueAsString(input);
		} catch (IOException e) {
			// Any exception is recasted as InvalidFormatJSON
			throw new InvalidFormatJSON(e);
		}
	}
	
	/////////////////////////////////////////////////
	//
	// From array conversion to JSON string conversion
	//
	/////////////////////////////////////////////////
	
	/**
	 * Converts a Object[] to a json string
	 **/
	public static String fromArray(Object[] input) {
		return ConvertJSON.fromObject(input);
	}
	
	/**
	 * Converts a String[] to a json string
	 **/
	public static String fromArray(String[] input) {
		return ConvertJSON.fromObject(input);
	}
	
	/**
	 * Converts a double[] to a json string
	 **/
	public static String fromArray(double[] input) {
		return ConvertJSON.fromObject(input);
	}
	
	/**
	 * Converts a int[] to a json string
	 **/
	public static String fromArray(int[] input) {
		return ConvertJSON.fromObject(input);
	}
	
	/**
	 * Converts a long[] to a json string
	 **/
	public static String fromArray(long[] input) {
		return ConvertJSON.fromObject(input);
	}
	
	/**
	 * Converts a long[] to a json string
	 **/
	public static String fromArray(float[] input) {
		return ConvertJSON.fromObject(input);
	}
	
	/////////////////////////////////////////////////
	//
	// From JSON string to java object
	//
	/////////////////////////////////////////////////
	
	/**
	 * Converts json string into an mapping object
	 *
	 * @param  JSON string
	 *
	 * @return  Output Map if successful, else throws an error
	 **/
	@SuppressWarnings("unchecked")
	public static Map<String, Object> toMap(String input) {
		return (Map<String, Object>) toCustomClass(input, Map.class);
	}
	
	/**
	 * Converts json string into an list array
	 *
	 * @param  JSON string
	 *
	 * @return  Output List if successful, else throws an error
	 **/
	@SuppressWarnings("unchecked")
	public static List<Object> toList(String input) {
		return (List<Object>) toCustomClass(input, List.class);
	}
	
	/**
	 * Converts json string into any output object (depends on input)
	 *
	 * @param  JSON string
	 *
	 * @return  Output object (either map or list)
	 **/
	public static Object toObject(String input) {
		return toCustomClass(input, Object.class);
	}
	
	/**
	 * Converts json string into a custom output object
	 *
	 * Note that this is the core "to java object" function that all
	 * other type strict varient is built on top of.
	 *
	 * @param  JSON string
	 *
	 * @return  Output object (either map or list, or other class)
	 **/
	public static Object toCustomClass(String input, Class<?> c) {
		try {
			return cachedMapper().readValue(input, c);
		} catch (IOException e) {
			/**
			 * Any exception is recasted as InvalidFormatJSON
			 **/
			throw new InvalidFormatJSON(e);
		}
	}
	
	/////////////////////////////////////////////////
	//
	// From string to array conversion
	//
	/////////////////////////////////////////////////
	
	/**
	 * Converts a json string into a Object[] array
	 *
	 * @param  Input JSON string
	 *
	 * @return Converted Object[] array, or Null for input 'null'
	 **/
	public static Object[] toObjectArray(String input) {
		List<Object> rawList = ConvertJSON.toList(input);
		if (rawList == null) {
			return null;
		}
		
		/**
		 * Using stream() - optimizing a microbenchmark
		 * See: http://stackoverflow.com/questions/16635398/java-8-iterable-foreach-vs-foreach-loop
		 **/
		return rawList.stream().toArray(Object[]::new);
	}
	
	/**
	 * Converts a json string into a string[] array
	 *
	 * @param  Input JSON string
	 *
	 * @return Converted String[] array, or Null for input 'null'
	 **/
	public static String[] toStringArray(String input) {
		List<Object> rawList = ConvertJSON.toList(input);
		if (rawList == null) {
			return null;
		}
		String[] ret = new String[rawList.size()];
		for (int a = 0; a < rawList.size(); ++a) {
			if (rawList.get(a) != null) {
				ret[a] = rawList.get(a).toString();
			}
		}
		return ret;
	}
	
	/**
	 * Converts a json string into a double[] array
	 *
	 * @param  Input JSON string
	 *
	 * @return Converted double[] array, or Null for input 'null'
	 **/
	public static double[] toDoubleArray(String input) {
		List<Object> rawList = ConvertJSON.toList(input);
		if (rawList == null) {
			return null;
		}
		
		double[] ret = new double[rawList.size()];
		for (int a = 0; a < rawList.size(); ++a) {
			ret[a] = ((Number) rawList.get(a)).doubleValue();
		}
		return ret;
	}
	
	/**
	 * Converts a json string into a int[] array
	 *
	 * @param  Input JSON string
	 *
	 * @return Converted int[] array, or Null for input 'null'
	 **/
	public static int[] toIntArray(String input) {
		List<Object> rawList = ConvertJSON.toList(input);
		if (rawList == null) {
			return null;
		}
		
		int[] ret = new int[rawList.size()];
		for (int a = 0; a < rawList.size(); ++a) {
			ret[a] = ((Number) rawList.get(a)).intValue();
		}
		return ret;
	}
	
	/**
	 * Converts a json string into a float[] array
	 *
	 * @param  Input JSON string
	 *
	 * @return Converted float[] array, or Null for input 'null'
	 **/
	public static float[] toFloatArray(String input) {
		List<Object> rawList = ConvertJSON.toList(input);
		if (rawList == null) {
			return null;
		}
		
		float[] ret = new float[rawList.size()];
		for (int a = 0; a < rawList.size(); ++a) {
			ret[a] = ((Number) rawList.get(a)).floatValue();
		}
		return ret;
	}
	
	/**
	 * Converts a json string into a long[] array
	 *
	 * @param  Input JSON string
	 *
	 * @return Converted long[] array, or Null for input 'null'
	 **/
	public static long[] toLongArray(String input) {
		List<Object> rawList = ConvertJSON.toList(input);
		if (rawList == null) {
			return null;
		}
		
		long[] ret = new long[rawList.size()];
		for (int a = 0; a < rawList.size(); ++a) {
			ret[a] = ((Number) rawList.get(a)).longValue();
		}
		return ret;
	}
	
}

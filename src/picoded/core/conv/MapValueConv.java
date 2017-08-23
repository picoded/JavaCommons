package picoded.core.conv;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility conversion class, that helps convert Map values from one type to another.
 **/
public class MapValueConv {
	
	/**
	 * Invalid constructor (throws exception)
	 **/
	protected MapValueConv() {
		throw new IllegalAccessError("Utility class");
	}
	
	/**
	 * Converts a Map with List values, into array values
	 **/
	public static <A, B> Map<A, B[]> listToArray(Map<A, List<B>> source, Map<A, B[]> target,
		B[] arrayType) {
		/**
		 * Normalize array type to 0 length
		 **/
		arrayType = sanatizeArray(arrayType);
		
		for (Map.Entry<A, List<B>> entry : source.entrySet()) {
			List<B> value = entry.getValue();
			if (value == null) {
				target.put(entry.getKey(), null);
			} else {
				target.put(entry.getKey(), value.toArray(arrayType));
			}
		}
		
		return target;
	}
	
	/**
	 * Converts a Map with List values, into array values. Target map is created using HashMap
	 **/
	public static <A, B> Map<A, B[]> listToArray(Map<A, List<B>> source, B[] arrayType) {
		return listToArray(source, new HashMap<A, B[]>(), arrayType);
	}
	
	/**
	 * Converts a single value map, to an array map
	 **/
	public static <A, B> Map<A, B[]> singleToArray(Map<A, B> source, Map<A, B[]> target,
		B[] arrayType) {
		/**
		 * Normalize array type to 0 length
		 **/
		arrayType = sanatizeArray(arrayType);
		/**
		 * Convert values
		 **/
		for (Map.Entry<A, B> entry : source.entrySet()) {
			List<B> aList = new ArrayList<B>();
			aList.add(entry.getValue());
			target.put(entry.getKey(), aList.toArray(arrayType));
		}
		return target;
	}
	
	/**
	 * Converts a single value map, to an array map
	 **/
	public static <A, B> Map<A, B[]> singleToArray(Map<A, B> source, B[] arrayType) {
		return singleToArray(source, new HashMap<A, B[]>(), arrayType);
	}
	
	//--------------------------------------------------------------------------------------------------
	//
	//  Fully Qualified KEYS handling
	//
	//--------------------------------------------------------------------------------------------------
	
	public static Map<String, Object> fromFullyQualifiedKeys(Map<String, Object> source) {
		Map<String, Object> finalMap = new HashMap<String, Object>();
		for (Map.Entry<String, Object> sourceKey : source.entrySet()) {
			recreateObject(finalMap, sourceKey.getKey(), sourceKey.getValue());
		}
		
		return finalMap;
	}
	
	@SuppressWarnings("unchecked")
	public static Map<String, Object> toFullyQualifiedKeys(Object source, String rootName,
		String separator) {
		Map<String, Object> fullyQualifiedMap = new HashMap<String, Object>();
		
		if (rootName == null) {
			rootName = "";
		}
		
		if (separator.isEmpty()) {
			separator = ".";
		}
		String parentName;
		if (source instanceof List) {
			List<Object> sourceList = (List<Object>) source;
			
			int counter = 0;
			for (Object obj : sourceList) {
				parentName = getRootName(rootName, counter);
				if (obj instanceof List) {
					fullyQualifiedMap.putAll(toFullyQualifiedKeys(obj, parentName, separator));
					++counter;
					
				}
				if (obj instanceof Map) {
					Map<String, Object> objMap = (Map<String, Object>) obj;
					fullyQualifiedMap = getFullyQualifiedMap(fullyQualifiedMap, objMap, rootName,
						parentName, counter, separator);
					counter = (int) fullyQualifiedMap.get("counter");
				}
			}
		} else if (source instanceof Map) {
			Map<String, Object> sourceMap = (Map<String, Object>) source;
			for (Map.Entry<String, Object> sourceMapKey : sourceMap.entrySet()) {
				if (rootName.isEmpty()) {
					parentName = sourceMapKey.getKey();
				} else {
					parentName = rootName + separator + sourceMapKey.getKey();
				}
				
				fullyQualifiedMap.putAll(toFullyQualifiedKeys(sourceMapKey.getValue(), parentName,
					separator));
			}
		} else if (source instanceof Number) {
			fullyQualifiedMap.put(rootName, source);
		} else {
			fullyQualifiedMap.put(rootName, source.toString());
		}
		
		return fullyQualifiedMap;
	}
	
	private static String getRootName(String rootName, Integer counter) {
		if (!rootName.isEmpty()) {
			return rootName + "[" + counter + "]";
		}
		return "";
	}
	
	private static Map<String, Object> getFullyQualifiedMap(Map<String, Object> fullyQualifiedMap,
		Map<String, Object> objMap, String rootName, String parentName, int counter, String separator) {
		for (Map.Entry<String, Object> objMapKey1 : objMap.entrySet()) {
			if (rootName.isEmpty()) {
				parentName = objMapKey1.getKey();
			} else {
				parentName = rootName + "[" + counter + "]" + separator + objMapKey1.getKey();
			}
			fullyQualifiedMap.putAll(toFullyQualifiedKeys(objMap.get(objMapKey1.getKey()), parentName,
				separator));
		}
		++counter;
		fullyQualifiedMap.put("counter", counter);
		return fullyQualifiedMap;
	}
	
	@SuppressWarnings("unchecked")
	private static void recreateObject(Object source, String key, Object value) {
		if (key.contains("]") && key.contains(".")) {
			if (key.indexOf(']') < key.indexOf('.')) {
				String[] bracketSplit = key.split("\\[|\\]|\\.");
				bracketSplit = sanitiseArray(bracketSplit);
				
				if (bracketSplit.length > 1 && stringIsNumber(bracketSplit[0])) { //numbers only
					int index = Integer.parseInt(bracketSplit[0]);
					List<Object> sourceList = (List<Object>) source;
					// Get source list
					sourceList = getSourceList(sourceList, index);
					// Check String is words and recursive call of recreateObject method
					sourceList = checkStringIsWords(sourceList, index, key, value, bracketSplit);
					// Check String is number and recursive call of recreateObject method
					checkStringIsNumber(sourceList, index, key, value, bracketSplit);
				} else if (source instanceof Map) {
					Map<String, Object> sourceMap = (Map<String, Object>) source;
					List<Object> element = (List<Object>) sourceMap.get(bracketSplit[0]);
					// Get element list
					element = getElementList(sourceMap, element, bracketSplit);
					key = key.substring(bracketSplit[0].length(), key.length());
					recreateObject(element, key, value);
				}
			}
		} else {
			Map<String, Object> sourceMap = (Map<String, Object>) source;
			sourceMap.put(key, value);
		}
	}
	
	private static List<Object> getElementList(Map<String, Object> sourceMap, List<Object> element,
		String[] bracketSplit) {
		if (element == null) {
			element = new ArrayList<Object>();
			sourceMap.put(bracketSplit[0], element);
		}
		return element;
	}
	
	@SuppressWarnings("unchecked")
	private static List<Object> checkStringIsNumber(List<Object> sourceList, int index, String key,
		Object value, String[] bracketSplit) {
		if (stringIsNumber(bracketSplit[1])) { //put list [1, 0, secondLayer0]
			Object retrievedValue = sourceList.get(index);
			List<Object> newList = new ArrayList<Object>();
			
			if (retrievedValue instanceof List) {
				newList = (List<Object>) retrievedValue;
			}
			
			sourceList.remove(index);
			sourceList.add(index, newList);
			
			key = key.substring(key.indexOf(']') + 1, key.length());
			recreateObject(newList, key, value);
		}
		return sourceList;
	}
	
	@SuppressWarnings("unchecked")
	private static List<Object> checkStringIsWords(List<Object> sourceList, int index, String key,
		Object value, String[] bracketSplit) {
		if (stringIsWord(bracketSplit[1])) { //put map
			Object retrievedValue = sourceList.get(index);
			Map<String, Object> newMap = new HashMap<String, Object>();
			
			if (retrievedValue instanceof Map) {
				newMap = (Map<String, Object>) retrievedValue;
			}
			
			sourceList.remove(index);
			sourceList.add(index, newMap);
			
			key = key.substring(key.indexOf('.') + 1, key.length());
			recreateObject(newMap, key, value);
		}
		return sourceList;
	}
	
	private static List<Object> getSourceList(List<Object> sourceList, int index) {
		if (index >= sourceList.size()) {
			for (int i = sourceList.size(); i <= index; ++i) {
				sourceList.add(new Object());
			}
		}
		return sourceList;
	}
	
	protected static <B> B[] sanatizeArray(B[] in) {
		if (in != null && in.length > 0) {
			in = Arrays.copyOfRange(in, 0, 0);
		}
		return in;
	}
	
	private static String[] sanitiseArray(String[] source) {
		List<String> holder = new ArrayList<String>();
		for (int i = 0; i < source.length; ++i) {
			if (!source[i].isEmpty()) {
				holder.add(source[i]);
			}
		}
		return holder.toArray(new String[] {});
	}
	
	private static boolean stringIsNumber(String source) {
		if (source.matches("[0-9]+")) {
			return true;
		}
		return false;
	}
	
	private static boolean stringIsWord(String source) {
		if (!source.substring(0, 1).matches("[0-9]+")) {
			return true;
		}
		return false;
	}
}

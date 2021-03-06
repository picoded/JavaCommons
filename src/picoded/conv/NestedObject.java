package picoded.conv;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

///
/// Handles the manipulation of nested objects, in a Map/List.
///
/// This was originally part of the GenericConvert class, but has since been extracted and isolated out
///
public class NestedObject {
	
	/// Invalid constructor (throws exception)
	protected NestedObject() {
		throw new IllegalAccessError("Utility class");
	}
	
	//--------------------------------------------------------------------------------------------------
	//
	// NESTED object fetch (related to fully qualified keys handling)
	//
	//--------------------------------------------------------------------------------------------------
	
	///
	/// Gets an object from the map,
	/// That could very well be, a map inside a list, inside a map, inside a .....
	///
	/// Note that at each iteration step, it attempts to do a FULL key match first, 
	/// before the next iteration depth
	///
	/// @param base      Map / List to manipulate from
	/// @param key       The input key to fetch, possibly nested
	/// @param fallbck   The fallback default (if not convertable)
	///
	/// @returns         The fetched object, always possible unless fallbck null
	public static Object fetchNestedObject(Object base, String key, Object fallback) {
		
		// Invalid base -> null, or not ( map OR list ) -> fallback
		if (base == null || !((base instanceof Map) || (base instanceof List))) {
			return fallback;
		}
		
		// Reuse ret object
		//Object ret;
		
		// Full key fetching found -> if found it is returned =)
		Object ret = fetchObject(base, key, null);
		if (ret != null) {
			return ret;
		}
		
		// Fallsback if key is ALREADY EMPTY !
		// cause nothing is found, and nothing else can be done
		if (key == null || key.length() <= 0) {
			return fallback;
		}
		
		// Trim off useless spaces, and try again (if applicable)
		int keyLen = key.length();
		key = key.trim();
		if (key.length() != keyLen) {
			return fetchNestedObject(base, key, fallback);
		}
		
		// Trim off useless starting ".dots" and try again
		if (key.startsWith(".")) {
			return fetchNestedObject(base, key.substring(1), fallback);
		}
		
		// Array bracket fetching
		// This is most likely an array fetching,
		// but could also be a case of map fetching with string
		if (key.startsWith("[")) {
			return fetchNestedObject_key_startsWithLeftBracket(base, key, fallback);
		}
		
		// Fetch one nested level =(
		int dotIndex = key.indexOf('.');
		int leftBracketIndex = key.indexOf('[');
		
		if (dotIndex >= 0 && (leftBracketIndex < 0 || dotIndex <= leftBracketIndex)) {
			// Dot Index exists, and is before left bracket index OR there is no left bracket index
			//
			// This is most likely a nested object fetch -> so recursion is done
			return fetchNestedObject_key_withDotIndex(base, key, fallback, dotIndex);
		} else if (leftBracketIndex > 0) {
			// Left bracket index exists, and there is no dot before it
			//
			// This is most likely a nested object fetch -> so recursion is done
			return fetchNestedObject_key_withLeftBracketIndex(base, key, fallback, leftBracketIndex);
		}
		
		// All else failed, including full key fetch -> fallback
		return fallback;
	}
	
	///
	/// unchecked, fetch for fetchNestedObject, where the key starts wihth "["
	///
	/// @param base      Map / List to manipulate from
	/// @param key       The input key to fetch, possibly nested
	/// @param fallbck   The fallback default (if not convertable)
	///
	/// @returns         The fetched object, always possible unless fallbck null
	protected static Object fetchNestedObject_key_startsWithLeftBracket(Object base, String key,
		Object fallback) {
		int rightBracketIndex = key.indexOf(']', 1);
		if (rightBracketIndex <= 0) {
			throw new RuntimeException("Missing closing ']' right bracket for key : " + key);
		}
		
		String bracketKey = key.substring(1, rightBracketIndex).trim();
		
		// Fetch [sub object]
		Object subObject = fetchObject(base, bracketKey, null);
		
		// No sub object, cant go a step down, fallback
		//
		// Meaning this can neither be the final object (ending key)
		// nor could it be a recusive fetch.
		if (subObject == null) {
			return fallback;
		}
		
		String rightKey = key.substring(rightBracketIndex + 1).trim();
		// subObject is THE object, as its the ending key
		if (rightKey.length() <= 0) {
			return subObject;
		}
		
		// ELSE : Time to continue, recusively fetching
		return fetchNestedObject(subObject, rightKey, fallback);
	}
	
	///
	/// unchecked, fetch for fetchNestedObject, where the key next split point is a "."
	///
	/// @param base      Map / List to manipulate from
	/// @param key       The input key to fetch, possibly nested
	/// @param fallbck   The fallback default (if not convertable)
	/// @param dotIndex  dot index position in the key to process
	///
	/// @returns         The fetched object, always possible unless fallbck null
	protected static Object fetchNestedObject_key_withDotIndex(Object base, String key,
		Object fallback, int dotIndex) {
		// Gets the left.right key
		String leftKey = key.substring(0, dotIndex); //left
		String rightKey = key.substring(dotIndex + 1); //right
		
		// Fetch left
		Object left = fetchObject(base, leftKey, null);
		
		// Time to continue, recusively fetching
		return fetchNestedObject(left, rightKey, fallback);
	}
	
	///
	/// unchecked, fetch for fetchNestedObject, where the key next split point is a "["
	///
	/// @param base               Map / List to manipulate from
	/// @param key                The input key to fetch, possibly nested
	/// @param fallbck            The fallback default (if not convertable)
	/// @param leftBracketIndex   Left bracket index position in the key to process
	///
	/// @returns         The fetched object, always possible unless fallbck null
	protected static Object fetchNestedObject_key_withLeftBracketIndex(Object base, String key,
		Object fallback, int leftBracketIndex) {
		// Gets the left[right] key
		String leftKey = key.substring(0, leftBracketIndex); //left
		String rightKey = key.substring(leftBracketIndex); //[right]
		
		// Fetch left
		Object left = fetchObject(base, leftKey, null);
		
		// Time to continue, recusively fetching [right]
		return fetchNestedObject(left, rightKey, fallback);
	}
	
	///
	/// Default Null fallback, for `fetchNestedObject(base, key, fallback)`
	///
	/// @param base      Map / List to manipulate from
	/// @param key       The input key to fetch, possibly nested
	///
	/// @returns         The fetched object, always possible unless fallbck null
	///
	public static Object fetchNestedObject(Object base, String key) {
		return fetchNestedObject(base, key, null);
	}
	
	///
	/// Fetch an Object from either a Map, or a List. By attempting to use the provided key.
	///
	/// This attempts to use the key AS IT IS. Only converting it to an int for List if needed.
	/// It does not do recursive fetch, if that is needed see `fetchNestedObject`
	///
	/// @param base      Map / List to manipulate from
	/// @param key       The input key to fetch, possibly nested
	/// @param fallbck   The fallback default (if not convertable)
	///
	/// @returns         The fetched object, always possible unless fallbck null
	///
	@SuppressWarnings("unchecked")
	public static Object fetchObject(Object base, String key, Object fallback) {
		
		// Base to map / list conversion
		Map<String, Object> baseMap = null;
		List<Object> baseList = null;
		
		Object obj = GenericConvert.resolvedListOrMap(base);
		if (obj != null) {
			if (obj instanceof Map) {
				baseMap = (Map<String, Object>) obj;
			} else {
				baseList = (List<Object>) obj;
			}
		}
		
		// Fail on getting base item
		if (baseMap == null && baseList == null) {
			return fallback;
		}
		
		// Reuse vars?
		Object ret = null;
		//int idxPos = 0;
		
		// Full key fetch
		if (baseMap != null) {
			ret = baseMap.get(key);
		} else { // if( baseList != null ) {
			int idxPos = GenericConvert.toInt(key, -1);
			if (idxPos >= 0 && idxPos < baseList.size()) {
				ret = baseList.get(idxPos);
			}
		}
		
		// Full key found
		if (ret != null) {
			return ret;
		}
		
		// Fallback
		return fallback;
	}
	
	///
	/// Default Null fallback, for `fetchObject(base, key, fallback)`
	///
	/// @param base      Map / List to manipulate from
	/// @param key       The input key to fetch, possibly nested
	///
	/// @returns         The fetched object, always possible unless fallbck null
	///
	public static Object fetchObject(Object base, String key) {
		return fetchObject(base, key, null);
	}
	
	//--------------------------------------------------------------------------------------------------
	//
	// Key name manipulation and handling
	//
	//--------------------------------------------------------------------------------------------------
	
	///
	/// Split the key path into their respective component
	///
	/// @param key       The input key to fetch, possibly nested
	///
	/// @returns         The fetched object, possibly empty array if key is invalid?
	///
	public static String[] splitObjectPath(String key) {
		return GenericConvert.toStringArray(splitObjectPath(key, null));
	}
	
	///
	/// Split the key path into their respective component
	///
	/// @param key       The input key to fetch, possibly nested
	///
	/// @returns         The fetched object, possibly empty array if key is invalid?
	///
	public static List<String> splitObjectPath(String key, List<String> ret) {
		// Return array list of string
		if (ret == null) {
			ret = new ArrayList<String>();
		}
		
		//
		// No more key parts, terminates
		//
		// This is the actual termination point for the recursive function
		//
		if (key == null || key.length() <= 0) {
			//if (ret.size() < 0) {
			//ret.add("");
			//}
			return ret;
		}
		
		// Trim off useless spaces, and try again (if applicable)
		int keyLen = key.length();
		key = key.trim();
		if (key.length() != keyLen) {
			return splitObjectPath(key, ret);
		}
		
		// Trim off useless starting ".dots" and try again
		if (key.startsWith(".")) {
			return splitObjectPath(key.substring(1), ret);
		}
		
		// Fetches the next 2 index points (most probably seperator of token parts)
		int dotIndex = key.indexOf('.');
		int leftBracketIndex = key.indexOf('[');
		
		// No match found, assume last key
		if (dotIndex < 0 && leftBracketIndex < 0) {
			ret.add(key);
			return ret;
		}
		
		// Left and right string parts to recursively process
		String leftPart;
		String rightPart;
		// Begins left/right part splitting and processing
		if (leftBracketIndex == 0) {
			//
			// Array bracket fetching start
			// This is most likely an array fetching,
			// but could also be a case of map fetching with string
			//
			int rightBracketIndex = key.indexOf(']', 1);
			if (rightBracketIndex <= 0) {
				throw new RuntimeException("Missing closing ']' right bracket for key : " + key);
			}
			
			//
			// Get the left part within the bracket, and the right part after it
			// 
			// Format: [leftPart]rightPart___
			//
			leftPart = key.substring(1, rightBracketIndex).trim();
			rightPart = key.substring(rightBracketIndex + 1).trim();
			
			// Sanatize the left path from quotation marks
			if (leftPart.length() > 1
				&& ((leftPart.startsWith("\"") && leftPart.endsWith("\"")) || (leftPart
					.startsWith("\'") && leftPart.endsWith("\'")))) {
				leftPart = leftPart.substring(1, leftPart.length() - 1);
			}
			
		} else if (dotIndex >= 0 && (leftBracketIndex < 0 || dotIndex <= leftBracketIndex)) {
			//
			// Dot Index exists, and is before left bracket index OR there is no left bracket index
			//
			// This is most likely a nested object fetch -> so recursion is done
			//
			
			//
			// Get the left part before the dot, and right part after it
			//
			// Format: leftPart.rightPart___
			//
			leftPart = key.substring(0, dotIndex); //left
			rightPart = key.substring(dotIndex + 1); //right
			
		} else if (leftBracketIndex > 0) {
			//
			// Left bracket index exists, and there is no dot before it
			//
			// This is most likely a nested object fetch -> so recursion is done
			//
			
			//
			// Get the left part before the left bracket, and right part INCLUDING the left bracket it
			//
			// Format: leftPart[rightPart___
			//
			leftPart = key.substring(0, leftBracketIndex); //left
			rightPart = key.substring(leftBracketIndex); //[right]
			
		} else {
			throw new RuntimeException("Unexpected key format : " + key);
		}
		
		// Add left key to return set
		ret.add(leftPart);
		
		// There is no right key, ends and terminate at recusive termination point above
		if (rightPart == null || rightPart.length() <= 0) {
			return splitObjectPath(null, ret);
		}
		
		// ELSE : recursively process the right keys
		return splitObjectPath(rightPart, ret);
	}
	
	///
	/// Takes a possibly case insensitive key, and normalize it to the actual key path (if found) for the selected object
	///
	/// @param base      Map / List to manipulate from
	/// @param objPath   The input key to fetch, possibly nested
	///
	/// @returns         The normalized key
	///
	public static String normalizeObjectPath(Object base, String key) {
		return normalizeObjectPath(base, splitObjectPath(key, null), null).toString();
	}
	
	///
	/// Takes a possibly case insensitive key, and normalize it to the actual key path (if found) for the selected object
	///
	/// @param base      Map / List to manipulate from
	/// @param key       The input key to fetch, possibly nested
	///
	/// @returns         The normalized key
	///
	@SuppressWarnings("unchecked")
	public static StringBuilder normalizeObjectPath(Object base, List<String> splitKeyPath,
		StringBuilder res) {
		
		//
		// Result string builder setup
		//--------------------------------------------------------
		
		if (res == null) {
			res = new StringBuilder();
		}
		
		//
		// End of path recursion
		//--------------------------------------------------------
		
		/// No additional parts, return existing path
		if (splitKeyPath == null || splitKeyPath.isEmpty()) {
			return res;
		}
		
		//
		// Get base respective array or map type
		//--------------------------------------------------------
		
		// Base to map / list conversion
		Map<String, Object> baseMap = null;
		List<Object> baseList = null;
		
		Object obj = GenericConvert.resolvedListOrMap(base);
		if (obj != null) {
			if (obj instanceof Map) {
				baseMap = (Map<String, Object>) obj;
			} else {
				baseList = (List<Object>) obj;
			}
		}
		
		if (baseMap == null && baseList == null) {
			throw new RuntimeException("Unexpected key path format : "
				+ ConvertJSON.fromList(splitKeyPath));
		}
		
		//
		// Process if its map or list respectively
		//--------------------------------------------------------
		
		String currentKey = splitKeyPath.get(0);
		List<String> nextKeyPathSet = splitKeyPath.subList(1, splitKeyPath.size());
		Object subObject = fetchObject(base, currentKey);
		
		// Failed fetch with currentKey
		if (subObject == null) {
			
			// Fails if its an array : no such thing as case insensitive number
			if (baseList != null) {
				return new StringBuilder();
			}
			
			//System.out.println("normalize search - "+currentKey); 
			//System.out.println("normalize keySet - "+baseMap.keySet());
			
			// Attempt to correct the case insensitivty issue
			for (String oneKey : baseMap.keySet()) {
				if (oneKey.equalsIgnoreCase(currentKey)) {
					currentKey = oneKey;
					subObject = baseMap.get(currentKey);
					if (subObject != null) {
						break;
					}
				}
			}
			
			// Still invalid after search
			if (subObject == null) {
				return new StringBuilder();
			}
		}
		
		if (baseMap != null) {
			//
			// Base is a map
			//
			if (res.length() > 0) {
				res.append(".");
			}
			res.append(currentKey);
			
		} else { // if (baseList != null) { //baseList is never null here
			//
			// Base is a list
			//
			res.append("[");
			res.append(currentKey);
			res.append("]");
		}
		
		// Terminate when done
		if (nextKeyPathSet.isEmpty()) {
			return res;
		}
		
		// Else recursive fetch
		return normalizeObjectPath(subObject, nextKeyPathSet, res);
	}
}

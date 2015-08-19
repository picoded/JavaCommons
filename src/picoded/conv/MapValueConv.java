package picoded.conv;

import java.util.Map;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

/// 
/// Utility conversion class, that helps convert Map values from one type to another
///
public class MapValueConv {
	
	protected static <B> B[] sanatizeArray(B[] in) {
		if(in != null && in.length > 0) {
			in = Arrays.copyOfRange(in,0,0);
		}
		return in;
	}
	
	/// Converts a Map with List values, into array values
	public static <A, B> Map<A,B[]> listToArray(Map<A,List<B>> source, Map<A,B[]> target, B[] arrayType) {
		// Normalize array type to 0 length
		arrayType = sanatizeArray(arrayType);
		
		for (Map.Entry<A, List<B>> entry : source.entrySet()) {
			List<B> value = entry.getValue();
			if( value == null ) {
				target.put( entry.getKey(), null );
			} else {
				target.put( entry.getKey(), value.toArray( arrayType ) );
			}
		}
		
		return target;
	}
	
	/// Converts a Map with List values, into array values. Target map is created using HashMap
	public static <A, B> Map<A,B[]> listToArray(Map<A,List<B>> source, B[] arrayType) {
		return listToArray( source, new HashMap<A, B[]>(), arrayType );
	}
	
	/// Converts a single value map, to an array map
	public static <A, B> Map<A,B[]> singleToArray(Map<A,B> source, Map<A,B[]> target, B[] arrayType) {
		// Normalize array type to 0 length
		arrayType = sanatizeArray(arrayType);
		
		// Convert values
		for (Map.Entry<A, B> entry : source.entrySet()) {
			List<B> aList = new ArrayList<B>();
			aList.add(entry.getValue());
			target.put( entry.getKey(), aList.toArray( arrayType ) );
		}
		return target;
	}
	
	/// Converts a single value map, to an array map
	public static <A, B> Map<A,B[]> singleToArray(Map<A,B> source, B[] arrayType) {
		return singleToArray(source, new HashMap<A,B[]>(), arrayType);
	}
	
//	/// Converts 
//	@SuppressWarnings("unchecked")
//	public static Map<String, Object> toFullyQualifiedKeys (Map<String, Object> source){
//		Map<String, Object> fullyQualifiedMap = new HashMap<String, Object>();
//		
//		for(String str : source.keySet()){
//			Object rawValue = source.get(str);
//			
//			if(rawValue instanceof List){ //List<Map<String, Object>
//				List<Map<String, Object>> listOfMap = (List<Map<String, Object>>)rawValue;
//				
//				int counter = 0;
//				for(Map<String, Object> innerMap : listOfMap){
//					for(String innerMapKey : innerMap.keySet()){
//						fullyQualifiedMap.put(str+"["+counter+"]."+innerMapKey, innerMap.get(innerMapKey));
//					}
//					++counter;
//				}
//			}else{
//				fullyQualifiedMap.put(str, rawValue);
//			}
//		}
//		
//		return fullyQualifiedMap;
//	}
	
//	@SuppressWarnings("unchecked")
//	public static Map<String, Object> fromFullyQualifiedKeys (Map<String, Object> source){
//		Map<String, Object> contextQualifiedMap = new HashMap<String, Object>();
//		
//		for(String str : source.keySet()){
//			String[] strSplit = str.split("\\.");
//			if(strSplit.length > 1){
//				String[] splitByLeftBracket = strSplit[0].split("\\[");
//				if(splitByLeftBracket.length > 1){
//					String listKey = splitByLeftBracket[0];
//					String mapKey = strSplit[1];
//					String mapValue = (String)source.get(str);
//					
//					List<Map<String, Object>> innerList = (List<Map<String, Object>>)contextQualifiedMap.get(listKey);
//					if(innerList == null){
//						innerList = new ArrayList<Map<String, Object>>();
//						contextQualifiedMap.put(listKey, innerList);
//					}
//					
//					//get index
//					int index = Integer.parseInt(splitByLeftBracket[1].split("\\]")[0]);
//					if(innerList.size() == 0 || innerList.size() < index){
//						for(int i = innerList.size(); i < index + 1; ++i){
//							innerList.add(new HashMap<String, Object>());
//						}
//					}
//					
//					Map<String, Object> listMap = innerList.get(index);
//					listMap.put(mapKey, mapValue);
//				}
//			}else{
//				contextQualifiedMap.put(str, source.get(str));
//			}
//			
//		}
//		return contextQualifiedMap;
//	}
	
	@SuppressWarnings("unchecked")
	public static Map<String, Object> toFullyQualifiedKeys(Object source, String rootName, String separator){
		Map<String, Object> fullyQualifiedMap = new HashMap<String, Object>();
		
		if(rootName == null){
			rootName = "";
		}
		
		if(separator.isEmpty()){
			separator = ".";
		}
		
		if(source instanceof List){
			List<Object> sourceList = (List<Object>)source;
			
			int counter = 0;
			for(Object obj:sourceList){
				if(obj instanceof List){
					String parentName = "";
					if(!rootName.isEmpty()){
						parentName = rootName+"["+counter+"]";
					}
					fullyQualifiedMap.putAll(toFullyQualifiedKeys(obj, parentName, separator));
					++counter;
					
				}else if(obj instanceof Map){
					Map<String, Object> objMap = (Map<String, Object>)obj;;
					for(String objMapKey : objMap.keySet()){
						String parentName = "";
						if(rootName.isEmpty()){
							parentName = objMapKey;
						}else{
							parentName = rootName+"["+counter+"]"+separator+objMapKey;
						}
						
						fullyQualifiedMap.putAll(toFullyQualifiedKeys(objMap.get(objMapKey), parentName, separator));
					}
					++counter;
				}
			}
		}else if(source instanceof Map){
			Map<String, Object> sourceMap = (Map<String, Object>)source;
			for(String sourceMapKey : sourceMap.keySet()){
				String parentName = "";
				if(rootName.isEmpty()){
					parentName = sourceMapKey;
				}else{
					parentName = rootName+separator+sourceMapKey;
				}
				
				fullyQualifiedMap.putAll(toFullyQualifiedKeys(sourceMap.get(sourceMapKey), parentName, separator));
			}
		}else{
			fullyQualifiedMap.put(rootName, (String)source);
		}
		
		return fullyQualifiedMap;
	}
	
//	@SuppressWarnings("unchecked")
//	public static Map<String, Object> fromFullyQualifiedKeys (Map<String, Object> source){
//		Map<String, Object> contextQualifiedMap = new HashMap<String, Object>();
//		
//		for(String sourceKey:source.keySet()){
//			String[] separatorSplit = sourceKey.split("\\.");
//			
//			if(separatorSplit != null && separatorSplit.length > 1){
//				for(String section:separatorSplit){
//					String[] sectSplit = section.split("(\\[|\\])");
//					String baseKey = sectSplit[0];
//					Object internal = contextQualifiedMap.get(baseKey);
//					
//					for(int i = 0; i < sectSplit.length; ++i){
//						String key = sectSplit[i];
//						if(internal != null){
//							if(internal instanceof List){
//								List<Object> listInternal = (List<Object>)internal;
//								Object obj = listInternal.get(Integer.parseInt(key));
//								if(obj != null){
//									
//								}else{
//									if(i < sectSplit.length - 1){
//	
//									}else{
//										Map<String, Object> mapInternal = new HashMap<String, Object>();
//									}
//								}
//							}else{
//								
//							}
//						}else{
//							if(i < sectSplit.length - 1){
//								List<Object> listInternal = new ArrayList<Object>();
//								internal = listInternal;
//								contextQualifiedMap.put(key, listInternal);
//							}else{
////								Map<String, Object> mapInternal = new HashMap<String, Object>();
//							}
//						}
//					}
//
//				}
//			}else{
//				//just a keyvalue map
//				contextQualifiedMap.put(sourceKey, source.get(sourceKey));
//			}
//		}
//		
//		return contextQualifiedMap;
//	}
	
	public static Map<String, Object> fromFullyQualifiedKeys(Map<String, Object> source){
		Map<String, Object> finalMap = new HashMap<String, Object>();
		
		for(String sourceKey : source.keySet()){
			recreateObject(finalMap, sourceKey, source.get(sourceKey));
		}
		
		return finalMap;
	}
	
	@SuppressWarnings("unchecked")
	private static void recreateObject(Object source, String key, Object value){
		if(key.contains("]") && key.contains(".")){
			if(key.indexOf("]") < key.indexOf(".")){
				String[] bracketSplit = key.split("\\[|\\]|\\.");
				bracketSplit = sanitiseArray(bracketSplit);
				
				if(bracketSplit.length > 1){
					if(stringIsNumber(bracketSplit[0])){ //numbers only
						int index = Integer.parseInt(bracketSplit[0]);
						List<Object> sourceList = (List<Object>)source;
						
						if(index >= sourceList.size()){
							for(int i = sourceList.size(); i <= index; ++i){
								sourceList.add(new Object());
							}
						}
						
						if(stringIsWord(bracketSplit[1])){ //put map
							Object retrievedValue = sourceList.get(index);
							Map<String, Object> newMap = new HashMap<String, Object>();
							
							if(retrievedValue instanceof Map){
								newMap = (Map<String, Object>)retrievedValue;
							}
							
							sourceList.remove(index);
							sourceList.add(index, newMap);
							
							key = key.substring(key.indexOf(".") + 1, key.length());
							recreateObject(newMap, key, value);
						}else if(stringIsNumber(bracketSplit[1])){ //put list [1, 0, secondLayer0]
							Object retrievedValue = sourceList.get(index);
							List<Object> newList = new ArrayList<Object>();
							
							if(retrievedValue instanceof List){
								newList = (List<Object>)retrievedValue;
							}
							
							sourceList.remove(index);
							sourceList.add(index, newList);
							
							key = key.substring(key.indexOf("]") + 1, key.length());
							recreateObject(newList, key, value);
						}
					}else{
						Map<String, Object> sourceMap = (Map<String, Object>)source;
						List<Object> element = (List<Object>)sourceMap.get(bracketSplit[0]);
						if(element == null){
							element = new ArrayList<Object>();
							sourceMap.put(bracketSplit[0], element);
						}
						
						
						key = key.substring(bracketSplit[0].length(), key.length());
						recreateObject(element, key, value);
					}
				}
			}
		}else{
			Map<String, Object> sourceMap = (Map<String, Object>)source;
			sourceMap.put(key, value);
		}
	}
	
	private static String[] sanitiseArray(String[] source){
		List<String> holder = new ArrayList<String>();
		for(int i = 0; i < source.length; ++i){
			if(source[i] != null && !source[i].isEmpty()){
				holder.add(source[i]);
			}
		}
		return holder.toArray(new String[]{});
	}
	
	private static boolean stringIsNumber(String source){
		if(source.matches("[0-9]+")){
			return true;
		}else{
			return false;
		}
	}
	
	private static boolean stringIsWord(String source){
		
		if(!source.startsWith("[") && !source.startsWith("]")
				&& !source.startsWith(".") && !source.substring(0, 1).matches("[0-9]+")){
			return true;
		}
		else{
			return false;
		}

//		if(source.matches("[a-zA-Z]+")){
//			return true;
//		}else{
//			return false;
//		}
	}
}

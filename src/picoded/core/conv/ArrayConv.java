package picoded.core.conv;

import org.apache.commons.lang3.ArrayUtils;

/**
 *
 * Utility class to help slice out arrays out of arrays without cloning them in memory.
 *
 * This class extends `org.apache.commons.lang3.ArrayUtils`
 *
 * https://commons.apache.org/proper/commons-lang/apidocs/org/apache/commons/lang3/ArrayUtils.html
 *
 **/
public class ArrayConv extends ArrayUtils {
	
	/**
	 * Invalid constructor (throws exception)
	 **/
	protected ArrayConv() {
		throw new IllegalAccessError("Utility class");
	}
	
	/**
	 * NOTE: All these were made obselete by apache.commons.ArrayUtils.subarray()
	 **/
	
	// /// Extract out array from starting position onwards
	// public static Object[] sliceObjects(Object[] inArr, int startPos) {
	// 	return sliceObjects(inArr, startPos, inArr.length);
	// }
	//
	// /// Extract out array from starting position to ending position
	// public static Object[] sliceObjects(Object[] inArr, int startPos, int endPos) {
	// 	return Arrays.asList(inArr).subList(startPos, endPos).toArray();
	// }
	//
	// /// Extract out array from starting position onwards
	// public static String[] sliceStrings(String[] inArr, int startPos) {
	// 	return sliceStrings(inArr, startPos, inArr.length);
	// }
	//
	// /// Extract out array from starting position to ending position
	// public static String[] sliceStrings(String[] inArr, int startPos, int endPos) {
	// 	//return (String[])(Object[])(Arrays.asList(inArr).subList(startPos, endPos).toArray());
	// 	return subarray(inArr, startPos, endPos);
	// }
	
}

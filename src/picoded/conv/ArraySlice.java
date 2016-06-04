package picoded.conv;

import java.util.Arrays;

///
/// Utility class to help slice out arrays out of arrays without cloning them in memory =)
///
public class ArraySlice {
	
	/// Extract out array from starting position onwards
	public static Object[] objects(Object[] inArr, int startPos) {
		return objects(inArr, startPos, inArr.length);
	}
	
	/// Extract out array from starting position to ending position
	public static Object[] objects(Object[] inArr, int startPos, int endPos) {
		return Arrays.asList(inArr).subList(startPos, endPos).toArray();
	}
	
	/// Extract out array from starting position onwards
	public static String[] strings(String[] inArr, int startPos) {
		return strings(inArr, startPos, inArr.length);
	}
	
	/// Extract out array from starting position to ending position
	public static String[] strings(String[] inArr, int startPos, int endPos) {
		return (String[]) (Arrays.asList(inArr).subList(startPos, endPos).toArray());
	}
	
}

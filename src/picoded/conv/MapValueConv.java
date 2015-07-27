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
}

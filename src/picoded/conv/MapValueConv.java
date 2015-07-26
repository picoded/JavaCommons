package picoded.conv;

import java.util.Map;
import java.util.HashMap;
import java.util.List;

/// 
/// Utility conversion class, that helps convert Map values from one type to another
///
public class MapValueConv {
	
	/// Converts a Map with List values, into array values
	@SuppressWarnings("unchecked")
	public static <A, B> Map<A,B[]> listToArray(Map<A,List<B>> source, Map<A,B[]> target) {
		for (Map.Entry<A, List<B>> entry : source.entrySet()) {
			List<B> value = entry.getValue();
			if( value == null ) {
				target.put( entry.getKey(), null );
			} else {
				target.put( entry.getKey(), value.toArray( (B[])(new Object[value.size()]) ) );
			}
		}
		return target;
	}
	
	/// Converts a Map with List values, into array values. Target map is created using HashMap
	public static <A, B> Map<A,B[]> listToArray(Map<A,List<B>> source) {
		return listToArray( source, new HashMap<A, B[]>() );
	}
	
	/// Converts a single value map, to an array map
	@SuppressWarnings("unchecked")
	public static <A, B> Map<A,B[]> singleToArray(Map<A,B> source, Map<A,B[]> target) {
		for (Map.Entry<A, B> entry : source.entrySet()) {
			target.put( entry.getKey(), (B[])(new Object[] { entry.getValue() }) );
		}
		return target;
	}
}

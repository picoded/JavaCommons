package picoded.conv;

import java.util.Map;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/// 
/// Utility conversion class, that helps convert Map values from one type to another
///
public class ListValueConv {
	
	public static List<String> objectToString(List<Object> listObj) {
		List<String> stringList = new ArrayList<String>();
		for (Object obj : listObj) {
			stringList.add(obj != null ? GenericConvert.toString(obj,null) : null);
		}
		return stringList;
	}
	
	@SuppressWarnings("unchecked")
	public static List<String> deduplicateValuesWithoutArrayOrder(List<String> list) {
		Set<String> set = new HashSet<String>();
		set.addAll(list);
		return new ArrayList<String>(set);
	}
}

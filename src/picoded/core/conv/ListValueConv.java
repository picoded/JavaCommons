package picoded.core.conv;

import java.util.*;

/**
 * Utility conversion class, that helps convert Map values from one type to another.
 **/
public class ListValueConv {
	
	/**
	 * Invalid constructor (throws exception)
	 **/
	protected ListValueConv() {
		throw new IllegalAccessError("Utility class");
	}
	
	public static String[] objectListToStringArray(List<Object> listObj) {
		String[] stringArr = new String[listObj.size()];
		for (int a = 0; a < listObj.size(); ++a) {
			Object obj = listObj.get(a);
			stringArr[a] = obj != null ? GenericConvert.toString(obj, null) : null;
		}
		return stringArr;
	}
	
	public static List<String> objectToString(List<Object> listObj) {
		List<String> stringList = new ArrayList<String>();
		for (Object obj : listObj) {
			stringList.add(obj != null ? GenericConvert.toString(obj, null) : null);
		}
		return stringList;
	}
	
	public static List<String> deduplicateValuesWithoutArrayOrder(List<String> list) {
		Set<String> set = new HashSet<String>();
		set.addAll(list);
		return new ArrayList<String>(set);
	}
	
	public static <V> Set<String> toStringSet(List<V> list) {
		Set<String> ret = new HashSet<String>();
		for (V item : list) {
			ret.add(item.toString());
		}
		return ret;
	}
}

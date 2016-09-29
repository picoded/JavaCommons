package picoded.struct;

import java.util.Map;

public interface GenericConvertMap<K, V> extends UnsupportedDefaultMap<K, V> {

	// Static proxy build
	//--------------------------------------------------------------------------------------------------
	
	/// Ensures the returned map is a GenericConvertMap, doing the conversion if needed.
	public static <A, B> GenericConvertMap<A, B> build(Map<A, B> inMap) {
		return ProxyGenericConvertMap.ensure(inMap);
	}
}

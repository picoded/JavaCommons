package picoded.struct;

import picoded.conv.GenericConvert;

/// Java MutablePair implementation
///
/// See: http://commons.apache.org/proper/commons-lang/apidocs/org/apache/commons/lang3/tuple/Pair.html
///
/// ### Example Usage
///
/// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~{.java}
///
/// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
///
@SuppressWarnings("serial")
public class MutablePair<L, R> extends org.apache.commons.lang3.tuple.MutablePair<L, R> /*implements List<Object> */{
	
	public MutablePair() {
		super();
	}
	
	public MutablePair(L left, R right) {
		super(left, right);
	}
	
	// The following is inherited
	//-----------------------------
	// L getLeft();
	// R getRight();
	// void setLeft(L);
	// void setRight(R);
	
	/// Invalid key error message 
	static String invalidKeyMsg = "Invalid get key, use eiher 0 or 1, for left and right pair respectively - ";
	
	/// Get the left / right value using index positioning
	///
	/// @param   The index key, use either 0 (for left), or 1 (for right)
	///
	/// @return  Object value for either left/right pair
	public Object get(Object key) {
		int index = GenericConvert.toInt(key, -1);
		if( index == 0 ) {
			return getLeft();
		} else if( index == 1 ) {
			return getRight();
		}
		throw new IllegalArgumentException(invalidKeyMsg+key);
	}
	
	/// Set the left / right value using index positioning
	///
	/// @param   The index key, use either 0 (for left), or 1 (for right)
	///
	/// @return  Object value for either left/right pair
	@SuppressWarnings("unchecked")
	public void add(int index, Object value) {
		if( index == 0 ) {
			setLeft( (L)value );
		} else if( index == 1 ) {
			setRight( (R)value );
		}
		throw new IllegalArgumentException(invalidKeyMsg+index);
	}
	
	/// Remove the left / right value using index positioning
	///
	/// @param   The index key, use either 0 (for left), or 1 (for right)
	///
	/// @return  true if a non-null value was previously present
	public Object remove(int index) {
		Object val = get(index);
		if( val != null ) {
			add(index, null);
			return val;
		}
		return null;
	}
	
}

package picoded.struct;

import picoded.conv.GenericConvert;

/**
* Java MutablePair implementation
*
* See: http://commons.apache.org/proper/commons-lang/apidocs/org/apache/commons/lang3/tuple/Pair.html
*
* ### Example Usage
*
* ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~{.java}
*
* ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
*
**/
@SuppressWarnings("serial")
public class MutablePair<L, R> extends org.apache.commons.lang3.tuple.MutablePair<L, R> implements
	GenericConvertList<Object> {

	public MutablePair() {
		super();
	}

	public MutablePair(L left, R right) {
		super(left, right);
	}

	/**
	* The following is inherited
	*-----------------------------
	* L getLeft();
	* R getRight();
	* void setLeft(L);
	* void setRight(R);
	**/

	/**
	* Invalid key error message
	**/
	public static final String INVALID_KEY_MSG = "Invalid get key, use eiher 0 or 1, "
		+ "for left and right pair respectively - ";

	/**
	* Get the left / right value using index positioning
	*
	* @param   The index key, use either 0 (for left), or 1 (for right)
	*
	* @return  Object value for either left/right pair
	**/
	public Object get(Object key) {
		int index = GenericConvert.toInt(key, -1);
		if (index == 0) {
			return getLeft();
		} else if (index == 1) {
			return getRight();
		}
		throw new IllegalArgumentException(INVALID_KEY_MSG + key);
	}

	/**
	* Set the left / right value using index positioning
	*
	* @param   The index key, use either 0 (for left), or 1 (for right)
	*
	* @return  Object value for either left/right pair
	**/
	@SuppressWarnings("unchecked")
	public Object set(int index, Object value) {
		Object oldVal = get(index);
		if (index == 0) {
			setLeft((L) value);
			return oldVal;
		} else if (index == 1) {
			setRight((R) value);
			return oldVal;
		}
		throw new IllegalArgumentException(INVALID_KEY_MSG + index);
	}

	/**
	* Remove the left / right value using index positioning
	*
	* @param   The index key, use either 0 (for left), or 1 (for right)
	*
	* @return  true if a non-null value was previously present
	**/
	public Object remove(int index) {
		Object oldVal = get(index);
		if (oldVal != null) {
			add(index, null);
			return oldVal;
		}
		return null;
	}

}

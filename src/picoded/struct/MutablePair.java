package picoded.struct;

/// Java MutablePair implementation
///
/// See: http://commons.apache.org/proper/commons-lang/apidocs/org/apache/commons/lang3/tuple/Pair.html
///
/// @TODO: Implement list interface for "easy" .get() usage
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
	
}

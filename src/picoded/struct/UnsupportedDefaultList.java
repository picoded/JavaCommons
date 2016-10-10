package picoded.struct;

import java.util.List;


///
/// Simple interface pattern, that implements the default List functions, which throws an UnsupportedOperationException
///
/// ### Example Usage
///

public interface UnsupportedDefaultList<E> extends List<E> {

	// Critical functions that need to over-ride, to support Map
	// -------------------------------------------------------------------

	// / throws an UnsupportedOperationException
	default E get(Object key) {
		throw new UnsupportedOperationException("function not supported");
	}

	// / throws an UnsupportedOperationException
	@Override
	default void add(int index, E value) {
		throw new UnsupportedOperationException("function not supported");
	}

	// / throws an UnsupportedOperationException
	@Override
	default boolean remove(Object key) {
		throw new UnsupportedOperationException("function not supported");
	}

}

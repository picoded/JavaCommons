package picoded.struct;

import java.util.ArrayList;
import java.util.Collection;
import picoded.conv.*;

@SuppressWarnings("serial")
public class GenericConvertArrayList<E> extends ArrayList<E> implements GenericConvertList<E> {
	
	//------------------------------------------------------------
	//
	//    Constructors
	//
	//------------------------------------------------------------
	
	public GenericConvertArrayList() {
		super();
	}
	
	public GenericConvertArrayList(Collection<? extends E> c) {
		super(c);
	}
	
	public GenericConvertArrayList(int initialCapacity) {
		super(initialCapacity);
	}
	
	//------------------------------------------------------------
	//
	//    Implementation overwrite
	//
	//------------------------------------------------------------
	
	/// Implments a JSON to string conversion
	@Override
	public String toString() {
		return GenericConvert.toString((Object)this);
	}
}

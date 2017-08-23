package picoded.core.struct;

import java.util.ArrayList;
import java.util.List;

import picoded.core.conv.GenericConvert;

public class GenericConvertArrayList<E> extends ArrayList<E> implements GenericConvertList<E> {
	
	/**
	 * Serial version UID
	 **/
	private static final long serialVersionUID = 1L;
	
	/**
	 * Implments a JSON to string conversion
	 **/
	@Override
	public String toString() {
		return GenericConvert.toString(this);
	}
	
	// ------------------------------------------------------
	//
	// Constructors
	//
	// ------------------------------------------------------
	
	/**
	 * Consturctor
	 **/
	public GenericConvertArrayList() {
		super();
	}
	
	/**
	 * Consturctor
	 **/
	public GenericConvertArrayList(List<E> m) {
		super(m);
	}
	
}

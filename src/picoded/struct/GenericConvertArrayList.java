package picoded.struct;

import java.util.ArrayList;
import picoded.conv.*;

@SuppressWarnings("serial")
public class GenericConvertArrayList<E> extends ArrayList<E> implements GenericConvertList<E> {
	
	/// Implments a JSON to string conversion
	@Override
	public String toString() {
		return GenericConvert.toString((Object)this);
	}
}

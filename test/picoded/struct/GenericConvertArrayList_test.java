package picoded.struct;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class GenericConvertArrayList_test extends UnsupportedDefaultList_test {
	
	class GenericConvertTest<E> implements GenericConvertList<E> {
		
	}
	
	class ProxyTest<E> implements GenericConvertList<E> {
		ArrayList<E> base = new ArrayList<E>();
		
		@Override
		public int size() {
			return base.size();
		}
		
		// Implementation to actual base list
		@Override
		public void add(int index, E value) {
			base.add(index, value);
		}
		
		// Implementation to actual base list
		@Override
		public E remove(int index) {
			return base.remove(index);
		}
		
		// Implementation to actual base list
		@Override
		public E get(int key) {
			return base.get(key);
		}
	}
	
	@Override
	@Before
	public void setUp() {
		unsupported = new GenericConvertTest<>();
		list = new ProxyTest<Object>();
	}
	
	@Override
	@After
	public void tearDown() {
		unsupported = null;
		list = null;
	}
	
	@Test
	public void notNullTest() {
		assertNotNull(unsupported);
		assertNotNull(list);
	}
}

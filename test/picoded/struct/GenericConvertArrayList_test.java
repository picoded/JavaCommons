package picoded.struct;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class GenericConvertArrayList_test extends StandardArrayList_test {
	
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
	
	GenericConvertList<Object> unsupported = null;
	ProxyTest proxyList = null;
	
	@Override
	@Before
	public void setUp() {
		unsupported = new GenericConvertTest<>();
		list = new ProxyTest<Object>();
		proxyList = new ProxyTest<Object>();
	}
	
	@Override
	@After
	public void tearDown() {
		unsupported = null;
		list = null;
		proxyList = null;
	}
	
	
	@Test
	public void notNullTest() {
		assertNotNull(unsupported);
		assertNotNull(list);
		assertNotNull(proxyList);
	}
	
	//
	// Unsupported Operation Exception
	//
	
	@Test(expected = UnsupportedOperationException.class)
	public void getUnsupportedTest() {
		unsupported.get(0);
	}
	
	@Test(expected = UnsupportedOperationException.class)
	public void addUnsupportedTest() {
		unsupported.add(1, "value");
	}
	
	@Test(expected = UnsupportedOperationException.class)
	public void removeUnsupportedTest() {
		unsupported.remove("key");
	}
	
	@Test(expected = UnsupportedOperationException.class)
	public void removeWithParamIntUnsupportedTest() {
		unsupported.remove(1);
	}
	
	@Test(expected = UnsupportedOperationException.class)
	public void sizeUnsupportedTest() {
		unsupported.remove("key");
	}
	
	@Test
	public void getBooleanTest() {
		proxyList.add(0, "value");
		proxyList.getBoolean(0);
	}
}

package picoded.core.struct;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class MutablePair_test {
	
	private MutablePair<String, String> mutablePair = null;
	
	class MutablePairProxy<L, R> extends MutablePair<L, R> {
		
		private static final long serialVersionUID = 1L;
		
		public MutablePairProxy(L left, R right) {
			super(left, right);
		}
		
		@Override
		public void add(int index, Object value) {
			super.set(index, value);
		}
		
		@Override
		public Object get(Object key) {
			return super.get(key);
		}
		
		@Override
		public Object get(int key) {
			return super.get(String.valueOf(key));
		}
	}
	
	@Before
	public void setUp() {
		mutablePair = new MutablePairProxy<String, String>("left", "right");
	}
	
	@After
	public void tearDown() {
		mutablePair = null;
	}
	
	@Test
	public void notnullObjectTest() {
		assertNotNull(mutablePair);
	}
	
	@Test
	public void getTest() {
		assertEquals("left", mutablePair.get("0"));
		assertEquals("right", mutablePair.get("1"));
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void getInvalidTest() {
		assertEquals("left", mutablePair.get("4"));
	}
	
	@Test
	public void setTest() {
		assertEquals("left", mutablePair.set(0, "new left"));
		assertEquals("right", mutablePair.set(1, "new right"));
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void setInvalidTest() {
		assertEquals("left", mutablePair.set(4, "nothing"));
	}
	
	@Test
	public void removeTest() {
		assertEquals("right", mutablePair.remove(1));
		assertEquals(null, mutablePair.remove(1));
	}
}

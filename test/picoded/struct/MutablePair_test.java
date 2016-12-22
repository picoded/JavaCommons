package picoded.struct;

import java.util.ArrayList;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class MutablePair_test {
	
	private MutablePair mutablePair = null;
	
	class MutablePairProxy<L, R> extends MutablePair<L, R> {
		
		private static final long serialVersionUID = 1L;
		ArrayList<L> base = new ArrayList<L>();
		
		public MutablePairProxy() {
			super();
		}
		
		public MutablePairProxy(L left, R right) {
			super(left, right);
		}
		
		@Override
		public int size() {
			return base.size();
		}
		
		@Override
		public void add(int index, Object value) {
			super.set(index, (L) value);
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
		mutablePair = new MutablePairProxy<>("left", "right");
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
		assertEquals(false, mutablePair.remove("0"));
		assertEquals("right", mutablePair.remove(1));
		assertEquals(null, mutablePair.remove(1));
	}
}

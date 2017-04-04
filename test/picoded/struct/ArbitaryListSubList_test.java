package picoded.struct;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ArbitaryListSubList_test {
	
	private ArbitaryListSubList<String> listSubList = null;
	private List<String> list = null;
	
	@Before
	public void setUp() {
		list = new ArrayList<>();
		list.add("hello");
		list.add("world");
		listSubList = new ArbitaryListSubList<>(list, 1, 2);
	}
	
	@After
	public void tearDown() {
		list = null;
		listSubList = null;
	}
	
	@Test
	public void notNullTest() {
		assertNotNull(listSubList);
	}
	
	@Test(expected = IndexOutOfBoundsException.class)
	public void constructorException1Test() {
		listSubList = new ArbitaryListSubList<>(list, -1, 2);
	}
	
	@Test(expected = IndexOutOfBoundsException.class)
	public void constructorException2Test() {
		listSubList = new ArbitaryListSubList<>(list, 0, 4);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void constructorException3Test() {
		listSubList = new ArbitaryListSubList<>(list, 2, 0);
	}
	
	@Test
	public void sizeTest() {
		assertEquals(1, listSubList.size());
	}
	
	@Test
	public void setTest() {
		listSubList.set(0, "my");
		assertEquals(1, listSubList.size());
	}
	
	@Test
	public void addTest() {
		listSubList.add(0, "my");
		assertEquals(2, listSubList.size());
		assertEquals("my", listSubList.get(0));
	}
	
	@Test
	public void removeTest() {
		assertEquals("world", listSubList.remove(0));
	}
}

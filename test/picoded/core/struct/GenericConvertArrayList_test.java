package picoded.core.struct;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class GenericConvertArrayList_test {
	
	GenericConvertArrayList<String> genericConvertArrayList = null;
	String strValue = null;
	
	@Before
	public void setUp() {
		genericConvertArrayList = new GenericConvertArrayList<>();
		genericConvertArrayList.add("value");
	}
	
	@After
	public void tearDown() {
		
	}
	
	@Test
	public void getTest() {
		assertNotNull(genericConvertArrayList.get(0));
	}
	
	@Test
	public void putTest() {
		genericConvertArrayList.add("value");
		assertEquals(2, genericConvertArrayList.size());
	}
	
	@Test
	public void ConstructorTest() {
		List<String> list = new ArrayList<>();
		genericConvertArrayList = new GenericConvertArrayList<>(list);
		genericConvertArrayList.add("value");
		assertNotNull(genericConvertArrayList.get(0));
	}
	
	@Test
	public void toStringTest() {
		assertNotNull(genericConvertArrayList.toString());
	}
	
	@Test
	public void buildTest() {
		List<String> list = new ArrayList<>();
		assertNotNull(GenericConvertList.build(list));
	}
	
}

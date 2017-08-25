package picoded.core.struct.query;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import picoded.core.struct.MutablePair;
import picoded.core.struct.query.OrderBy.OrderType;

public class OrderBy_test {
	
	private OrderBy<Object> orderBy = new OrderBy<Object>("ASC");
	
	@Before
	public void setUp() {
		
	}
	
	@After
	public void tearDown() {
		
	}
	
	@Test
	public void orderTypeTest() {
		assertEquals("ASC", OrderBy.OrderType.ASC.toString());
	}
	
	@Test
	public void orderTypeDescTest() {
		assertEquals("DESC", OrderBy.OrderType.DESC.name());
	}
	
	@Test
	public void comparisionConfigTest() {
		assertNotNull("DESC", orderBy._comparisionConfig);
	}
	
	//@Test
	public void constructorNullParamTest() {
		assertNull(new OrderBy<String>(null));
	}
	
	@Test(expected = RuntimeException.class)
	public void constructorEmptyParamTest() {
		assertNull(new OrderBy<String>(""));
	}
	
	@Test
	public void constructorComplexParamTest() {
		assertNotNull(new OrderBy<String>("name asc, dept desc"));
	}
	
	@Test
	public void constructorInvalidParamTest() {
		assertEquals("\"name as\" ASC, \"dept\" DESC",
			new OrderBy<String>("name as, dept desc").toString());
	}
	
	@Test
	public void compareTest() {
		assertEquals(0, orderBy.compare(null, null));
		assertEquals(1, orderBy.compare("String", null));
		assertEquals(-1, orderBy.compare(null, "String"));
		assertEquals(0, orderBy.compare("String", "String"));
		assertEquals(1, orderBy.compare("String", "abc"));
		assertEquals(1, orderBy.compare("String desc", "abc asc"));
	}
	
	@Test
	public void compareMapTest() {
		List<MutablePair<String, OrderType>> _comparisionConfig = new ArrayList<MutablePair<String, OrderType>>();
		MutablePair<String, OrderType> mp = new MutablePair<>();
		mp.setLeft("left");
		mp.setRight(OrderType.ASC);
		mp.setValue(OrderType.ASC);
		_comparisionConfig.add(mp);
		orderBy._comparisionConfig = _comparisionConfig;
		Map<String, String> map = new HashMap<String, String>();
		map.put("left", "left");
		assertEquals(1, orderBy.compare(map, "right"));
	}
	
	@Test
	public void compareMapDTest() {
		List<MutablePair<String, OrderType>> _comparisionConfig = new ArrayList<MutablePair<String, OrderType>>();
		MutablePair<String, OrderType> mp = new MutablePair<>();
		mp.setLeft("left");
		mp.setRight(OrderType.DESC);
		mp.setValue(OrderType.DESC);
		_comparisionConfig.add(mp);
		orderBy._comparisionConfig = _comparisionConfig;
		Map<String, String> map = new HashMap<String, String>();
		map.put("left", "left");
		assertEquals(-1, orderBy.compare(map, "right"));
	}
	
	@Test
	public void getKeyNamesTest() {
		List<MutablePair<String, OrderType>> _comparisionConfig = new ArrayList<MutablePair<String, OrderType>>();
		MutablePair<String, OrderType> mp = new MutablePair<>();
		mp.setLeft("left");
		mp.setRight(OrderType.DESC);
		mp.setValue(OrderType.DESC);
		_comparisionConfig.add(mp);
		orderBy._comparisionConfig = _comparisionConfig;
		Map<String, String> map = new HashMap<String, String>();
		map.put("left", "left");
		assertEquals("[left]", orderBy.getKeyNames().toString());
	}
	
	@Test
	public void replaceKeyNameTest() {
		List<MutablePair<String, OrderType>> _comparisionConfig = new ArrayList<MutablePair<String, OrderType>>();
		MutablePair<String, OrderType> mp = new MutablePair<>();
		mp.setLeft("left");
		mp.setRight(OrderType.DESC);
		mp.setValue(OrderType.DESC);
		_comparisionConfig.add(mp);
		orderBy._comparisionConfig = _comparisionConfig;
		assertFalse(orderBy.replaceKeyName("left", "right"));
	}
	
	@Test
	public void replaceKeyNameFalseTest() {
		List<MutablePair<String, OrderType>> _comparisionConfig = new ArrayList<MutablePair<String, OrderType>>();
		MutablePair<String, OrderType> mp = new MutablePair<>();
		mp.setLeft("left");
		mp.setRight(OrderType.DESC);
		mp.setValue(OrderType.DESC);
		_comparisionConfig.add(mp);
		orderBy._comparisionConfig = _comparisionConfig;
		assertFalse(orderBy.replaceKeyName("left1", "right"));
	}
	
}

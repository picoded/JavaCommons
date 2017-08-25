package picoded.core.struct;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertNotNull;

import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ProxyGenericConvertMap_test {
	private ProxyGenericConvertMap<String, String> proxyGenericConvertMap = null;
	
	@Before
	public void setUp() {
		proxyGenericConvertMap = new ProxyGenericConvertMap<>();
	}
	
	@After
	public void tearDown() {
		
	}
	
	@Test
	public void getConstructorTest() {
		Map<String, String> map = new HashMap<>();
		proxyGenericConvertMap = new ProxyGenericConvertMap<>(map);
		assertNull(proxyGenericConvertMap.get("key1"));
	}
	
	@Test
	public void ensureTest() {
		GenericConvertMap<String, String> genericConvertMap = new ProxyGenericConvertMap<>();
		assertNotNull(ProxyGenericConvertMap.ensure(genericConvertMap));
		
	}
	
	@Test
	public void ensure2Test() {
		Map<String, String> map = new HashMap<>();
		assertNotNull(ProxyGenericConvertMap.ensure(map));
	}
	
	@Test
	public void toStringTest() {
		Map<String, String> map = new HashMap<>();
		proxyGenericConvertMap = new ProxyGenericConvertMap<>(map);
		assertNotNull(proxyGenericConvertMap.toString());
	}
}

package picoded.core.struct;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import picoded.core.conv.ConvertJSON;

public class ProxyGenericConvertList_test {
	
	private ProxyGenericConvertList<String> proxyGenericConvertList = null;
	
	@Before
	public void setUp() {
	}
	
	@After
	public void tearDown() {
		
	}
	
	@Test
	public void getConstructorSecondTest() {
		List<String> list = new ArrayList<String>();
		list.add("me");
		proxyGenericConvertList = new ProxyGenericConvertList<>(list);
		assertEquals("me", proxyGenericConvertList.get(0));
	}
	
	@Test
	public void toStringTest() {
		List<String> list = new ArrayList<String>();
		list.add("me");
		proxyGenericConvertList = new ProxyGenericConvertList<>(list);
		assertEquals(ConvertJSON.fromObject(list), proxyGenericConvertList.toString());
	}
	
	@Test
	public void ensureTest() {
		List<String> list = new ArrayList<String>();
		list.add("me");
		GenericConvertList<String> temp = ProxyGenericConvertList.ensure(list);
		assertEquals(list.get(0), temp.get(0));
	}
	
	@Test
	public void ensureIfConditionTest() {
		List<String> list = new ArrayList<String>();
		list.add("me");
		proxyGenericConvertList = new ProxyGenericConvertList<>(list);
		GenericConvertList<String> temp = ProxyGenericConvertList.ensure(proxyGenericConvertList);
		assertEquals(list.get(0), temp.get(0));
	}
}

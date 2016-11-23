package picoded.JStruct.internal;

import static org.junit.Assert.*;

import java.util.concurrent.ConcurrentHashMap;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

@SuppressWarnings("static-access")
public class JStruct_AtomicLongMap_test {
	
	JStruct_AtomicLongMap jStruct_AtomicLongMap = null;
	
	@Before
	public void setUp() {
		jStruct_AtomicLongMap = new JStruct_AtomicLongMap();
	}
	
	@After
	public void tearDown() {
		
	}
	
	@Test
	public void currentSystemTimeInSecondsTest() {
		assertNotNull(jStruct_AtomicLongMap.currentSystemTimeInSeconds());
	}
	
	@Test
	public void putTest() {
		ConcurrentHashMap<String, Long> valueMap = new ConcurrentHashMap<String, Long>();
		valueMap.put("key", 90l);
		jStruct_AtomicLongMap.valueMap = valueMap;
		Number number = null;
		assertNull(jStruct_AtomicLongMap.put("key", number));
		valueMap.put("key", 90l);
		jStruct_AtomicLongMap.valueMap = valueMap;
		assertNull(jStruct_AtomicLongMap.put("key", 0));
		valueMap.put("key", 90l);
		jStruct_AtomicLongMap.valueMap = valueMap;
		assertNull(jStruct_AtomicLongMap.put("key", (Long) null));
	}
	
	@Test
	public void getAndAddTest() {
		ConcurrentHashMap<String, Long> valueMap = new ConcurrentHashMap<String, Long>();
		valueMap.put("key", 90l);
		jStruct_AtomicLongMap.valueMap = valueMap;
		assertNotNull(jStruct_AtomicLongMap.getAndAdd("key", (Long) 90l));
		assertNull(jStruct_AtomicLongMap.getAndAdd("key1", (Long) 90l));
	}
	
	@Test
	public void getAndIncrementTest() {
		ConcurrentHashMap<String, Long> valueMap = new ConcurrentHashMap<String, Long>();
		valueMap.put("key", 90l);
		jStruct_AtomicLongMap.valueMap = valueMap;
		assertNull(jStruct_AtomicLongMap.getAndIncrement("key1"));
	}
	
	@Test
	public void incrementAndGetTest() {
		ConcurrentHashMap<String, Long> valueMap = new ConcurrentHashMap<String, Long>();
		valueMap.put("key", 90l);
		jStruct_AtomicLongMap.valueMap = valueMap;
		assertNull(jStruct_AtomicLongMap.incrementAndGet("key1"));
	}
	
	@Test
	public void weakCompareAndSetTest() {
		ConcurrentHashMap<String, Long> valueMap = new ConcurrentHashMap<String, Long>();
		valueMap.put("key", 90l);
		jStruct_AtomicLongMap.valueMap = valueMap;
		assertFalse(jStruct_AtomicLongMap.weakCompareAndSet("key", (Long) 80l, (Long) 80l));
	}
}

package picoded.conv;

// Target test class
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
// Classes used in test case
import java.util.HashMap;

import org.apache.commons.lang3.ArrayUtils;
import org.junit.After;
// Test Case include
import org.junit.Before;
import org.junit.Test;

import picoded.conv.ConvertJSON;

///
/// Test Case for picoded.struct.CaseInsensitiveHashMap
///
public class ConvertJSON_test {
	
	HashMap<String, String> tMap;
	HashMap<String, String> tMap2;
	
	ArrayList<String> tList;
	Object object;
	Object[] objectArray;
	
	String tStr;
	String tStr2;
	
	@Before
	public void setUp() {
		tMap = new HashMap<String, String>();
		tList = new ArrayList<String>();
		object = new Object();
		
	}
	
	@After
	public void tearDown() {
		
	}
	
	///
	/// Test the following functions
	///
	/// + ConvertJSON.fromMap
	/// + ConvertJSON.fromObject
	///
	
	//@Test
	public void testConvertJSON() {
	//	new ConvertJSON();
	}
	
	@Test
	public void basicMapConversion() {
		tMap.put("Hello", "WORLD");
		tMap.put("WORLD", "Hello");
		assertEquals("{\"Hello\":\"WORLD\",\"WORLD\":\"Hello\"}", (tStr = ConvertJSON.fromMap(tMap)));
		assertEquals(tMap, ConvertJSON.toMap(tStr));
	}
	
	@Test
	public void checkIfCommentsInJsonBreaksThings() {
		tMap.put("Hello", "WORLD");
		tMap.put("WORLD", "Hello");
		assertEquals(tMap,
			ConvertJSON
				.toMap("{ /* Hello folks. comment here is to break things */ \"Hello\":\"WORLD\",\"WORLD\":\"Hello\"}"));
		assertEquals("{\"Hello\":\"WORLD\",\"WORLD\":\"Hello\"}", (tStr = ConvertJSON.fromMap(tMap)));
		
	}
	
	@Test
	public void basicListConversion() {
		tList.add("Hello");
		tList.add("WORLD");
		assertEquals("[\"Hello\",\"WORLD\"]", (tStr = ConvertJSON.fromList(tList)));
		assertEquals(tList, ConvertJSON.toList(tStr));
	}
	
	@Test
	public void basicToObjectConversion() {
		object = ConvertJSON.toObject("{\"Hello\":\"WORLD\",\"WORLD\":\"Hello\"}");
		assertEquals("{Hello=WORLD, WORLD=Hello}", (object).toString());
		object = ConvertJSON.toObject("{\"Hello\":\"WORLD\"}");
		assertEquals("{Hello=WORLD}", (object).toString());
	}
	
	@Test
	public void basicToStringArrayConversion() {
		objectArray = ConvertJSON.toStringArray("[\"Hello\",\"WORLD\",\"WORLD\",\"Hello\"]");
		assertEquals("[Hello, WORLD, WORLD, Hello]", Arrays.deepToString(objectArray));
		objectArray = ConvertJSON.toStringArray("[\"Hello\",\"WORLD\"]");
		assertEquals("[Hello, WORLD]", Arrays.deepToString(objectArray));
		objectArray = ConvertJSON.toStringArray("[\"Hello\"]");
		assertEquals("[Hello]", Arrays.deepToString(objectArray));
	}
	
	@Test
	public void basicToDoubleArrayConversion() throws IOException {
		objectArray = ArrayUtils.toObject(ConvertJSON.toDoubleArray("[]"));
		assertEquals("[]", Arrays.deepToString(objectArray));
		objectArray = ArrayUtils.toObject(ConvertJSON.toDoubleArray("[12.1, 12.9, 23.9, 4.0]"));
		assertEquals("[12.1, 12.9, 23.9, 4.0]", Arrays.deepToString(objectArray));
		objectArray = ArrayUtils.toObject(ConvertJSON.toDoubleArray("[-7.1, 22.234, 86.7, -99.02]"));
		assertEquals("[-7.1, 22.234, 86.7, -99.02]", Arrays.deepToString(objectArray));
		objectArray = ArrayUtils.toObject(ConvertJSON.toDoubleArray("[-1.0, 2.9]"));
		assertEquals("[-1.0, 2.9]", Arrays.deepToString(objectArray));
	}
	
	@Test
	public void basicToIntArrayConversion() throws IOException {
		objectArray = ArrayUtils.toObject(ConvertJSON.toIntArray("[]"));
		assertEquals("[]", Arrays.deepToString(objectArray));
		objectArray = ArrayUtils.toObject(ConvertJSON.toIntArray("[121, 129, 239, 40]"));
		assertEquals("[121, 129, 239, 40]", Arrays.deepToString(objectArray));
		objectArray = ArrayUtils.toObject(ConvertJSON.toIntArray("[-71, 22234, 867, -9902]"));
		assertEquals("[-71, 22234, 867, -9902]", Arrays.deepToString(objectArray));
		objectArray = ArrayUtils.toObject(ConvertJSON.toIntArray("[-10, 29]"));
		assertEquals("[-10, 29]", Arrays.deepToString(objectArray));
	}
	
	@Test
	public void basicToObjectArrayConversion() {
		objectArray = ConvertJSON.toObjectArray("[\"Hello\",\"WORLD\",\"WORLD\",\"Hello\"]");
		assertEquals("[Hello, WORLD, WORLD, Hello]", Arrays.deepToString(objectArray));
		objectArray = ConvertJSON.toObjectArray("[\"Hello\",\"WORLD\"]");
		assertEquals("[Hello, WORLD]", Arrays.deepToString(objectArray));
		objectArray = ConvertJSON.toObjectArray("[\"Hello\"]");
		assertEquals("[Hello]", Arrays.deepToString(objectArray));
	}
	
	@Test
	public void testObjectArrayToString() throws IOException {
		assertEquals("", ConvertJSON.fromArray((Object[]) null));
		assertEquals("[]", ConvertJSON.fromArray(new Object[0]));
		assertEquals("[Hello]", ConvertJSON.fromArray(new Object[] { "Hello" }));
		assertEquals("", ConvertJSON.fromArray((Object[]) null).toString());
		assertEquals("[]", ConvertJSON.fromArray(new Object[0]).toString());
		assertEquals("[Hello]", ConvertJSON.fromArray(new Object[] { "Hello" }).toString());
	}
	
	@Test
	public void testStringArrayToString() throws IOException {
		assertEquals("", ConvertJSON.fromArray((String[]) null));
		assertEquals("[]", ConvertJSON.fromArray(new String[0]));
		assertEquals("[a]", ConvertJSON.fromArray(new String[] { "a" }));
		assertEquals("[a,b,c]", ConvertJSON.fromArray(new String[] { "a", "b", "c" }));
		assertEquals("", ConvertJSON.fromArray((String[]) null).toString());
		assertEquals("[]", ConvertJSON.fromArray(new String[0]).toString());
		assertEquals("[a]", ConvertJSON.fromArray(new String[] { "a" }).toString());
		assertEquals("[a,b,c]", ConvertJSON.fromArray(new String[] { "a", "b", "c" }).toString());
	}
	
	@Test
	public void testIntArrayToString() throws IOException {
		assertEquals("", ConvertJSON.fromArray((int[]) null));
		assertEquals("[]", ConvertJSON.fromArray(new int[0]));
		assertEquals("[12]", ConvertJSON.fromArray(new int[] { 12 }));
		assertEquals("[-7,22,86,-99]", ConvertJSON.fromArray(new int[] { -7, 22, 86, -99 }));
		assertEquals("", ConvertJSON.fromArray((int[]) null).toString());
		assertEquals("[]", ConvertJSON.fromArray(new int[0]).toString());
		assertEquals("[12]", ConvertJSON.fromArray(new int[] { 12 }).toString());
		assertEquals("[-7,22,86,-99]", ConvertJSON.fromArray(new int[] { -7, 22, 86, -99 }).toString());
	}
	
	@Test
	public void testDoubleArrayToString() throws IOException {
		assertEquals("", ConvertJSON.fromArray((double[]) null));
		assertEquals("[]", ConvertJSON.fromArray(new double[0]));
		assertEquals("[12.8]", ConvertJSON.fromArray(new double[] { 12.8 }));
		assertEquals("[-7.1,22.234,86.7,-99.02]", ConvertJSON.fromArray(new double[] { -7.1, 22.234, 86.7, -99.02 }));
		assertEquals("", ConvertJSON.fromArray((double[]) null).toString());
		assertEquals("[]", ConvertJSON.fromArray(new double[0]).toString());
		assertEquals("[12.8]", ConvertJSON.fromArray(new double[] { 12.8 }).toString());
		assertEquals("[-7.1,22.234,86.7,-99.02]", ConvertJSON.fromArray(new double[] { -7.1, 22.234, 86.7, -99.02 })
			.toString());
	}
}

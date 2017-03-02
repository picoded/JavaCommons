package picoded.JStruct.internal;

import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import picoded.JStruct.MetaObject;

public class JStructUtils_test extends Mockito {
	
	@Before
	public void setUp() {
	}
	
	@After
	public void tearDown() {
	}
	
	/// Invalid constructor test
	@Test(expected = IllegalAccessError.class)
	public void invalidConstructor() throws Exception {
		new JStructUtils();
		
	}
	
	@Test
	public void sortAndOffsetListToArray() {
		List<MetaObject> retList = new ArrayList<MetaObject>();
		retList.add(mock(picoded.JStruct.MetaObject.class));
		retList.add(mock(picoded.JStruct.MetaObject.class));
		retList.add(mock(picoded.JStruct.MetaObject.class));
		assertNotNull(JStructUtils.sortAndOffsetListToArray(retList, null, 2, -1));
		retList = new ArrayList<MetaObject>();
		retList.add(mock(picoded.JStruct.MetaObject.class));
		retList.add(mock(picoded.JStruct.MetaObject.class));
		retList.add(mock(picoded.JStruct.MetaObject.class));
		assertNotNull(JStructUtils.sortAndOffsetListToArray(retList, null, 2, -1));
		assertNotNull(JStructUtils.sortAndOffsetListToArray(retList, " ", 2, -1));
		assertNotNull(JStructUtils.sortAndOffsetListToArray(retList, "", 2, -1));
		assertNotNull(JStructUtils.sortAndOffsetListToArray(retList, "asc", 2, -1));
	}
}

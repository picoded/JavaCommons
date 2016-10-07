package picoded.struct;

import static org.mockito.Mockito.withSettings;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.CALLS_REAL_METHODS;

import java.util.List;
import java.util.Observer;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class UnsupportedDefaultList_test {

	@SuppressWarnings("unchecked")
	UnsupportedDefaultList<String> unsupportedDefaultList = mock(UnsupportedDefaultList.class, CALLS_REAL_METHODS);
	
	@Before
	public void setUp() {
		
	}
	
	@After
	public void tearDown() {
		
	}
	
	@Test (expected = UnsupportedOperationException.class)
	public void getTest() {
		when(unsupportedDefaultList.get("key")).thenCallRealMethod();
		unsupportedDefaultList.get("key");
	}
	
	@Test (expected = UnsupportedOperationException.class)
	public void addTest() {
		doCallRealMethod().when(unsupportedDefaultList).add(1, "value");
		unsupportedDefaultList.add(1, "value");
	}
	
	@Test (expected = UnsupportedOperationException.class)
	public void removeTest() {
		when(unsupportedDefaultList.remove("key")).thenCallRealMethod();
		unsupportedDefaultList.remove("key");
	}
}

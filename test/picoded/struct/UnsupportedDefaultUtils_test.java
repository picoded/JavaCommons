package picoded.struct;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class UnsupportedDefaultUtils_test {
	
	@Before
	public void setUp() {
		
	}
	
	@After
	public void tearDown() {
		
	}
	
	@Test(expected = IllegalAccessError.class)
	public void constructorInvalidTest() {
		UnsupportedDefaultUtils unsupportedDefaultUtils = new UnsupportedDefaultUtils();
		assertNotNull(unsupportedDefaultUtils);
	}
	
	@Test
	public void checkIndexRangeTest() {
		UnsupportedDefaultUtils.checkIndexRange(0, 1);
	}
	
	@Test(expected = IndexOutOfBoundsException.class)
	public void checkIndexRangeInvalid1Test() {
		UnsupportedDefaultUtils.checkIndexRange(-1, 1);
	}
	
	@Test(expected = IndexOutOfBoundsException.class)
	public void checkIndexRangeInvalid2Test() {
		UnsupportedDefaultUtils.checkIndexRange(3, 1);
	}
	
	@Test(expected = IndexOutOfBoundsException.class)
	public void checkIndexRangeInvalid3Test() {
		UnsupportedDefaultUtils.checkIndexRange(3, 3);
	}
	
	@Test
	public void checkInsertRangeTest() {
		UnsupportedDefaultUtils.checkInsertRange(0, 1);
	}
	
	@Test(expected = IndexOutOfBoundsException.class)
	public void checkInsertRangeInvalid1Test() {
		UnsupportedDefaultUtils.checkInsertRange(-1, 1);
	}
	
	@Test(expected = IndexOutOfBoundsException.class)
	public void checkInsertRangeInvalid2Test() {
		UnsupportedDefaultUtils.checkInsertRange(3, 1);
	}
}

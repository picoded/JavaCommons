package picodedTests.misc;

import static org.junit.Assert.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.lang.Class;

/// This test is not used for a component specically,
/// but is used on a possible "abuse" of super class
/// being able to call its own subclass type.
///
/// This test case, is to ensure this functionality,
/// which is used in servlet page, is functional.
public class SuperClassReplication_test {
	
	// Class objects used in test
	private class baseClass {
		public Class<?> selfClass() {
			return this.getClass();
		}
	}
	
	private class subClass extends baseClass {
		
	}
	
	//@Before
	//public void setUp() { }
	
	//@After
	//public void tearDown() { }
	
	@Test
	public void constructor() {
		assertNotNull(new subClass());
		assertNotNull(new baseClass());
	}
	
	@Test
	public void baseClassCheck() {
		baseClass bcObj = new baseClass();
		assertEquals((Class) baseClass.class, (Class) bcObj.selfClass());
	}
	
	@Test
	public void subClassCheck() {
		subClass scObj = new subClass();
		assertEquals((Class) subClass.class, (Class) scObj.selfClass());
		
		//does not work with junit < 11
		//assertNotEquals( (Class)baseClass.class, (Class)scObj.selfClass() );
	}
	
}

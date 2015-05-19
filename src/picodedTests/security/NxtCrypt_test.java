package picodedTests.security;

import org.junit.*;
import static org.junit.Assert.*;
import java.lang.System;
import java.util.*;

import picoded.security.NxtCrypt;

public class NxtCrypt_test {
	
	@Before
	public void setUp() {
		
	}
	
	@After
	public void tearDown() {
		
	}
	
	@Test
	public void slowEquals() {
		String aStr = "Hello World";
		String bStr = "Hello World";
		
		assertTrue("slowEquals test for string", NxtCrypt.slowEquals(aStr, bStr));
		assertTrue("slowEquals test for byteArray", NxtCrypt.slowEquals(aStr.getBytes(), bStr.getBytes()));
	}
	
	@Test
	public void slowEquals_falseTest() {
		String aStr = "Hello World??";
		String bStr = "Has it ended?";
		
		assertFalse("slowEquals false test for string", NxtCrypt.slowEquals(aStr, bStr));
		assertFalse("slowEquals false test for byteArray", NxtCrypt.slowEquals(aStr.getBytes(), bStr.getBytes()));
	}
	
	@Test
	public void randomString() {
		String aStr = NxtCrypt.randomString(10);
		
		assertNotNull("Random string created", aStr);
		assertEquals("String created as expected length", 10, aStr.length());
	}
	
	@Test
	public void randomString_multipleLengths() {
		for (int a = 1; a < 25; ++a) {
			String aStr = NxtCrypt.randomString(a);
			
			assertNotNull("Random string created", aStr);
			assertEquals("String created as expected length", a, aStr.length());
		}
	}
	
	@Test
	public void getSaltedHash() {
		String rawPass = "Swordfish";
		String saltStr = "12345678901234567890123456789012"; //32 char salt str
		int iterations = 1500;
		int keyLength = 256;
		
		String saltedHashA = NxtCrypt.getSaltedHash(rawPass, saltStr, iterations, keyLength);
		assertNotNull("GetSaltedHash rd1", saltedHashA);
		
		String saltedHashB = NxtCrypt.getSaltedHash(rawPass, saltStr, iterations, keyLength);
		assertNotNull("GetSaltedHash rd2", saltedHashB);
		
		assertEquals("GetSaltedHash equals", saltedHashA, saltedHashB);
	}
	
	@Test
	public void getSaltedHash_byteArr() {
		String rawPass = "Swordfish";
		String saltStr = "12345678901234567890123456789012"; //32 char salt str
		int iterations = 1500;
		int keyLength = 256;
		
		String saltedHashA = NxtCrypt.getSaltedHash(rawPass, saltStr.getBytes(), iterations, keyLength);
		assertNotNull("GetSaltedHash rd1", saltedHashA);
		
		String saltedHashB = NxtCrypt.getSaltedHash(rawPass, saltStr.getBytes(), iterations, keyLength);
		assertNotNull("GetSaltedHash rd2", saltedHashB);
		
		assertEquals("GetSaltedHash equals", saltedHashA, saltedHashB);
	}
	
	@Test
	public void getSaltedHash_default() {
		String rawPass = "Swordfish";
		String saltStr = "12345678901234567890123456789012"; //32 char salt str
		
		String saltedHashA = NxtCrypt.getSaltedHash(rawPass, saltStr);
		assertNotNull("GetSaltedHash rd1", saltedHashA);
		
		String saltedHashB = NxtCrypt.getSaltedHash(rawPass, saltStr);
		assertNotNull("GetSaltedHash rd2", saltedHashB);
		
		assertEquals("GetSaltedHash equals", saltedHashA, saltedHashB);
	}
	
	@Test
	public void getSaltedHash_byteArr_default() {
		String rawPass = "Swordfish";
		String saltStr = "12345678901234567890123456789012"; //32 char salt str
		
		String saltedHashA = NxtCrypt.getSaltedHash(rawPass, saltStr.getBytes());
		assertNotNull("GetSaltedHash rd1", saltedHashA);
		
		String saltedHashB = NxtCrypt.getSaltedHash(rawPass, saltStr.getBytes());
		assertNotNull("GetSaltedHash rd2", saltedHashB);
		
		assertEquals("GetSaltedHash equals", saltedHashA, saltedHashB);
	}
	
	@Test
	public void getAndValidatePassHash() {
		String rawPass = "Swordfish";
		String saltStr = null; //32 char salt str
		int iterations = 1500;
		int keyLength = 256;
		
		String passHash = NxtCrypt.getPassHash(rawPass);
		
		assertNotNull("Password hash generated", passHash);
		assertTrue("Validated password hash as equal", NxtCrypt.validatePassHash(passHash, rawPass));
	}
	
	@Test
	public void getAndValidatePassHash_fail() {
		String rawPass = "Swordfish";
		String wrongPass = "NOOOOO";
		String saltStr = null; //32 char salt str
		int iterations = 1500;
		int keyLength = 256;
		
		String passHash = NxtCrypt.getPassHash(rawPass);
		
		assertNotNull("Password hash generated", passHash);
		assertFalse("Validated password hash as equal", NxtCrypt.validatePassHash(passHash, wrongPass));
	}
	
	@Test
	public void getAndValidatePassHash_againstSaltedVarient() {
		String rawPass = "Swordfish";
		String saltStr = null; //32 char salt str
		String saltedHashA = null;
		String saltedHashB = null;
		int iterations = 1500;
		int keyLength = 256;
		
		String passHash = NxtCrypt.getPassHash(rawPass);
		
		assertNotNull("Password hash generated", passHash);
		assertTrue("Validated password hash as equal", NxtCrypt.validatePassHash(passHash, rawPass));
		
		saltStr = NxtCrypt.extractSalt(passHash);
		assertNotNull("Salt extracted", saltStr);
		
		saltedHashA = NxtCrypt.extractSaltedHash(passHash);
		assertNotNull("SaltHash extracted", saltedHashA);
		
		saltedHashB = NxtCrypt.getSaltedHash(rawPass, saltStr, iterations, keyLength);
		assertNotNull("SaltHash generated", saltedHashB);
		
		assertEquals("Salthash passHash against generated", saltedHashA, saltedHashB);
	}
	
	//@Test
	//public void validateSaltedHash() { } //salted hash > rawPassword
	
}
package picoded.security;

import org.apache.commons.codec.binary.Base64;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;

/// Password hashing and crypt function : This uses PBEKeySpec (which will be upgraded in the future).
/// This is conceptually similar to [PHP password_hash] (http://sg2.php.net/manual/en/function.password-hash.php),
/// but however is not backwards compatible with older hash methods, like DES
///
/// *******************************************************************************
///
/// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~{.java}
///
/// String rawPass = "Swordfish";
/// String passHash = NxtCrypt.getPassHash( rawPass );
///
/// assertNotNull("Password hash generated", passHash );
/// assertTrue("Validated password hash as equal", NxtCrypt.validatePassHash( passHash, rawPass) );
///
/// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
///
/// *******************************************************************************
///
/// *Terminology refrence*
/// SaltedHash -> Hash of password, with salt applied
/// PassHash -> Full string database entry of salt, encryption scheme, and passHash
///
/// *******************************************************************************
///
/// Techinical Note: This is a static functions only class
///
/// *******************************************************************************
///
/// TODO list
/// * remove apache commons, for apache commons3
/// * from/toHex usage instead of base64
/// * validateSaltedHash( saltedHash, salt, rawPassword ) to implement
/// * [optional] migrate to from/toHex instead of Base64. This removes the apache.commons.* dependancy
/// * hashInfo : User text friendly version of current password encryption scheme
/// * needsRehash : Indicate true, on legacy hashes
/// * in document example usage.
/// * configurable class default values, with class functions, in addition of static global defaults. Should fallback to global default if not set.
public class NxtCrypt {
	
	/// Reusable crypt objects
	private static SecretKeyFactory pbk = null;
	
	/// Reusable Random objects objects
	private static SecureRandom ran = null;
	
	/// Hash storage seperator, @ is intentionally used as opposed to $, as to make the stored passHash obviously not "php password_hash" format.
	private static String seperator = "@"; 
	
	/// Definable default salt length
	public static int defaultSaltLength = 32; //bytes
	/// Definable default salt iterations
	public static int defaultIterations = 1500;
	/// Definable default salt keylength
	public static int defaultKeyLength = 256;
	
	/// Setup the default setting for SecureRandom
	public static boolean isStrongSecureRandom = false;
	
	/**
	 * Compares two byte arrays in length-constant time. This comparison method
	 * is used so that password hashes cannot be extracted from an on-line
	 * system using a timing attack and then attacked off-line.
	 *
	 * @param   a       the first byte array
	 * @param   b       the second byte array
	 * @return          true if both byte arrays are the same, false if not
	 */
	public static boolean slowEquals(byte[] a, byte[] b) {
		int diff = a.length ^ b.length;
		for (int i = 0; i < a.length && i < b.length; i++) {
			diff |= a[i] ^ b[i];
		}
		return diff == 0;
	}
	
	/**
	 * String varient to the slowEquals(byte[] a, byte[] b)
	 *
	 * @param   a       the first byte array
	 * @param   b       the second byte array
	 * @return          true if both byte arrays are the same, false if not
	 */
	public static boolean slowEquals(String a, String b) {
		return NxtCrypt.slowEquals(a.getBytes(), b.getBytes());
	}
	
	/**
	 * Converts a string of hexadecimal characters into a byte array.
	 *
	 * @param   hex         the hex string
	 * @return              the hex string decoded into a byte array
	 */
	private static byte[] fromHex(String hex) {
		byte[] binary = new byte[hex.length() / 2];
		for (int i = 0; i < binary.length; i++) {
			binary[i] = (byte) Integer.parseInt(hex.substring(2 * i, 2 * i + 2), 16);
		}
		return binary;
	}
	
	/**
	 * Converts a byte array into a hexadecimal string.
	 *
	 * @param   array       the byte array to convert
	 * @return              a length*2 character string encoding the byte array
	 */
	private static String toHex(byte[] array) {
		BigInteger bi = new BigInteger(1, array);
		String hex = bi.toString(16);
		int paddingLength = (array.length * 2) - hex.length();
		if (paddingLength > 0) {
			return String.format("%0" + paddingLength + "d", 0) + hex;
		} else {
			return hex;
		}
	}
	
	/// Setup static reuse object / default hash objects
	private static void setupReuseObjects() throws NoSuchAlgorithmException {
		if (NxtCrypt.pbk == null) {
			NxtCrypt.pbk = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
		}
		if (NxtCrypt.ran == null) {
			if( NxtCrypt.isStrongSecureRandom == false ) {
				//
				// Using just plain old SecureRandom by default now.
				// Frankly speaking I personally feel this is "secure enough",
				// Cause its over 9000x easier to do social engineering attacks,
				// then a side channel timing attack. 
				//
				// Unless of course your opponent is NSA. ¯\_(ツ)_/¯
				//
				// Or more frankly, one of their admins setting their password as "I-am-@wsome-123"
				//
				// https://tersesystems.com/2015/12/17/the-right-way-to-use-securerandom/
				//
				NxtCrypt.ran = new SecureRandom();
			} else {
				//
				// Originally the secure random module uses SHA1PRNG AKA
				// `NxtCrypt.ran = SecureRandom.getInstance("SHA1PRNG");`
				//
				// Now it uses java 8 SecureRandom.getInstanceStrong();
				// Which will hopefully make the entropy starvation issue better
				// in certain environments.
				//
				NxtCrypt.ran = SecureRandom.getInstanceStrong();
			}
		}
	}
	
	/// Generic SecurityException varient for setupReuseObjects
	private static void setupReuseObjects_generic() throws SecurityException {
		try {
			NxtCrypt.setupReuseObjects();
		} catch (NoSuchAlgorithmException e) {
			throw new SecurityException(e);
		}
	}
	
	/// Gets the salted hash of the raw password only (not the entire passHash)
	public static String getSaltedHash(String rawPassword, byte[] salt, int iteration, int keyLen)
		throws IllegalArgumentException, SecurityException {
		if (rawPassword == null || rawPassword.length() == 0) {
			throw new IllegalArgumentException("Empty/NULL passwords are not supported.");
		}
		if (salt == null) {
			throw new IllegalArgumentException("Empty/NULL salts are not supported.");
		}
		
		if (iteration <= 0) {
			iteration = NxtCrypt.defaultIterations;
		}
		if (keyLen <= 0) {
			keyLen = NxtCrypt.defaultKeyLength;
		}
		
		setupReuseObjects_generic();
		
		SecretKey key;
		try {
			PBEKeySpec kSpec = new PBEKeySpec(rawPassword.toCharArray(), salt, iteration, keyLen);
			
			key = NxtCrypt.pbk.generateSecret(kSpec);
		} catch (Exception e) {
			throw new SecurityException(e);
		}
		
		return Base64.encodeBase64String(key.getEncoded());
	}
	
	/// String salt varient of getSaltedHash (instead of byte[])
	public static String getSaltedHash(String rawPassword, String salt, int iteration, int keyLen)
		throws IllegalArgumentException, SecurityException {
		if (salt == null || salt.length() == 0) {
			throw new IllegalArgumentException("Empty/NULL salts are not supported.");
		}
		
		return getSaltedHash(rawPassword, Base64.decodeBase64(salt), iteration, keyLen);
	}
	
	/// Default values varient of getSaltedHash
	public static String getSaltedHash(String rawPassword, byte[] salt) throws IllegalArgumentException,
		SecurityException {
		return getSaltedHash(rawPassword, salt, defaultIterations, defaultKeyLength);
	}
	
	/// Default values, and string salt varient of getSaltedHash
	public static String getSaltedHash(String rawPassword, String salt) throws IllegalArgumentException,
		SecurityException {
		return getSaltedHash(rawPassword, salt, defaultIterations, defaultKeyLength);
	}
	
	/// Generates a random alphanumeric string up to length (but not guranteeded in length)
	private static String someRandomeString(int len) {
		return Base64.encodeBase64String(NxtCrypt.ran.generateSeed(len)).replaceAll("[^A-Za-z0-9]", "");
	}
	
	/// Generate a random byte array of strings at indicated length
	/// Note: that the generated string array is strictly "alphanumeric" character spaces chars,
	/// This is intentional as this format can be safely stored in databases, and passed around
	public static String randomString(int len) {
		setupReuseObjects_generic();
		//return new String(NxtCrypt.ran.generateSeed(len+len), 0, len);
		String resStr = someRandomeString(len + 5);
		while (resStr.length() < len) {
			resStr = resStr + someRandomeString(len - resStr.length() + 5);
		}
		return resStr.substring(0, len);
	}
	
	/// Generate a random byte array at indicated length
	public static byte[] randomBytes(int len) {
		setupReuseObjects_generic();
		return NxtCrypt.ran.generateSeed(len);
	}
	
	/// Gets the full password hash of [salt@protocall@hash] (currently only PBKeySpec)
	///
	/// ********************************************************************************
	///
	/// Notes on protocall format
	/// * P#N-#K = PBKeySpec, with #N number of iterations & #K keylength
	private static String getPassHash(String rawPassword, int saltLen, int iteration, int keyLen)
		throws IllegalArgumentException, SecurityException {
		if (saltLen <= 0) {
			saltLen = NxtCrypt.defaultSaltLength;
		}
		if (iteration <= 0) {
			iteration = defaultIterations;
		}
		if (keyLen <= 0) {
			keyLen = defaultKeyLength;
		}
		
		setupReuseObjects_generic();
		
		byte[] salt = NxtCrypt.ran.generateSeed(saltLen);
		
		return Base64.encodeBase64String(salt) + seperator + "P" + iteration + "-" + keyLen + seperator
			+ getSaltedHash(rawPassword, salt, iteration, keyLen);
	}
	
	/// Default values varient of getPassHash
	public static String getPassHash(String rawPassword) throws IllegalArgumentException, SecurityException {
		return getPassHash(rawPassword, 0, 0, 0);
	}
	
	/// Extract out the salted hash from the full passHash. see getPassHash
	public static String extractSaltedHash(String passHash) throws SecurityException {
		String[] splitStr = passHash.split(seperator, 3);
		
		if (splitStr.length < 3) {
			throw new SecurityException("Invalid salted hash of less then 3 components");
		}
		
		return splitStr[2];
	}
	
	/// Extract out the salt from the full passHash. see getPassHash
	public static String extractSalt(String passHash) throws SecurityException {
		String[] splitStr = passHash.split(seperator, 3);
		
		if (splitStr.length < 3) {
			throw new SecurityException("Invalid salted hash of less then 3 components");
		}
		
		return splitStr[0];
	}
	
	/// Validates the password hash against the raw password given
	public static boolean validatePassHash(String passHash, String rawPassword) throws SecurityException {
		String[] splitStr = passHash.split(seperator, 3);
		
		if (splitStr.length < 3) {
			throw new SecurityException("Invalid salted hash of less then 3 components: " + Arrays.toString(splitStr));
		}
		
		String salt = splitStr[0];
		String hash = splitStr[2];
		
		//String type;
		int iteration = 0;
		int keyLen = 0;
		
		if (splitStr[1].length() >= 1) {
			if ((splitStr[1]).substring(0, 1).equals("P")) {
				String[] splitProtocol = (splitStr[1]).substring(1).split("-", 2);
				
				if (splitProtocol != null && splitProtocol.length >= 2) {
					iteration = Integer.parseInt(splitProtocol[0]);
					keyLen = Integer.parseInt(splitProtocol[1]);
				} else {
					throw new SecurityException("Unknown hash P settings : " + splitStr[1]);
				}
				//type = "P";
			} else {
				throw new SecurityException("Unknown hash type : " + splitStr[1]);
			}
		}
		
		String toCheckHash = getSaltedHash(rawPassword, salt, iteration, keyLen);
		
		if (NxtCrypt.slowEquals(hash, toCheckHash)) {
			return true;
		}
		return false;
	}
}

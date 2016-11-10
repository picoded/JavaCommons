package picoded.RESTBuilder.template.social;

import picoded.RESTBuilder.RESTBuilder;
import picoded.RESTBuilder.RESTFunction;
import picoded.enums.HttpRequestType;
import picoded.servlet.CommonsPage;
import picoded.servlet.BasePage;
import picoded.conv.ConvertJSON;
import picoded.JStruct.AccountTable;
import picoded.JStruct.AccountObject;

import java.util.Base64;
import java.util.Map;
import java.util.HashMap;
import java.util.Formatter;
import java.util.List;
import java.util.ArrayList;

import javax.crypto.spec.SecretKeySpec;
import javax.crypto.Mac;

public class DisqusIntegration {
	
	public DisqusIntegration() {
		
	}
	
	/// Generates disqus SSO information
	///
	/// ## HTTP Request Parameters
	///
	/// ## JSON Object Output Parameters
	///
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// | Parameter Name  | Variable Type	   | Description                                                                   |
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// | hmac_sha1       | String             | The generated hmac_sha1 string for SSO.                                       |
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// | error           | String (Optional)  | Errors encounted if any                                                       |
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	///
	public RESTFunction sso_login = (req, res) -> {
		res.put("error", null);
		res.put("hmac_sha1", null);
		res.put("restLog", "");
		
		List<String> restLog = new ArrayList<String>();
		
		BasePage bp = (BasePage) req.requestPage();
		AccountTable at = bp.accountAuthTable();
		AccountObject ao = at.getRequestUser(bp.getHttpServletRequest());
		
		if (ao == null) {
			res.put("restLog", "User is not logged in.");
		} else {
			String oid = ao._oid();
			if (oid == null || oid.isEmpty()) {
				res.put("error", "No oid found for user.");
				return res;
			}
			
			String username = ao.getString("username", "test_user"); //change me
			String email = ao.getString("email", "test_user@mailinator.com"); //change me
			
			String secretKey = bp.JConfig().getString("site.apiKey.disqus.secretKey", "");
			if (secretKey.isEmpty()) {
				res.put("error", "No secret key found, unable to generate hmac_sha1 for disqus login");
				return res;
			}
			
			String publicKey = bp.JConfig().getString("site.apiKey.disqus.publicKey", "");
			if (publicKey.isEmpty()) {
				res.put("error", "No public key found, unable to generate hmac_sha1 for disqus login");
				return res;
			}
			res.put("publicKey", publicKey);
			
			Map<String, String> message = new HashMap<String, String>();
			message.put("id", oid);
			message.put("username", username);
			message.put("email", email);
			
			String signature = "";
			try {
				signature = sso_hmac_sha1(message, secretKey);
			} catch (Exception e) {
				res.put("error", e.getMessage());
				return res;
			}
			res.put("hmac_sha1", signature);
		}
		
		res.put("restLog", restLog);
		return res;
	};
	
	/// Generates disqus SSO information
	///
	/// ## HTTP Request Parameters
	///
	/// ## JSON Object Output Parameters
	///
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// | Parameter Name  | Variable Type	   | Description                                                                   |
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// | hmac_sha1       | String             | The generated hmac_sha1 string for SSO.                                       |
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// | error           | String (Optional)  | Errors encounted if any                                                       |
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	///
	public RESTFunction sso_logout = (req, res) -> {
		res.put("error", null);
		res.put("hmac_sha1", null);
		res.put("restLog", "");
		
		BasePage bp = (BasePage) req.requestPage();
		
		String secretKey = bp.JConfig().getString("site.apiKey.disqus.secretKey", "");
		if (secretKey.isEmpty()) {
			res.put("error", "No secret key found, unable to generate hmac_sha1 for disqus logout");
			return res;
		}
		
		String publicKey = bp.JConfig().getString("site.apiKey.disqus.publicKey", "");
		if (publicKey.isEmpty()) {
			res.put("error", "No public key found, unable to generate hmac_sha1 for disqus logout");
			return res;
		}
		res.put("publicKey", publicKey);
		
		Map<String, String> message = new HashMap<String, String>();
		String signature = "";
		try {
			signature = sso_hmac_sha1(message, secretKey);
		} catch (Exception e) {
			res.put("error", e.getMessage());
			return res;
		}
		res.put("hmac_sha1", signature);
		
		return res;
	};
	
	private static String sso_hmac_sha1(Map<String, String> message, String secretKey)
		throws Exception {
		//convert message object to json string, then base64 encode it
		String jsonMessage = ConvertJSON.fromObject(message);
		String base64EncodedStr = new String(Base64.getEncoder().encodeToString(
			jsonMessage.getBytes()));
		
		long timestamp = System.currentTimeMillis() / 1000;
		
		// Assemble the HMAC-SHA1 signature
		String signature = calculateRFC2104HMAC(base64EncodedStr + " " + timestamp, secretKey);
		return base64EncodedStr + " " + signature + " " + timestamp;
	}
	
	private static String toHexString(byte[] bytes) {
		Formatter formatter = new Formatter();
		for (byte b : bytes) {
			formatter.format("%02x", b);
		}
		
		return formatter.toString();
	}
	
	private static String calculateRFC2104HMAC(String data, String key) throws Exception {
		String HMAC_SHA1_ALGORITHM = "HmacSHA1";
		SecretKeySpec signingKey = new SecretKeySpec(key.getBytes(), HMAC_SHA1_ALGORITHM);
		Mac mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
		mac.init(signingKey);
		return toHexString(mac.doFinal(data.getBytes()));
	}
	
	public RESTBuilder setupRestBuilder(RESTBuilder rbObj, String setPrefix) {
		rbObj.getNamespace(setPrefix + "sso_login").put(HttpRequestType.GET, sso_login);
		rbObj.getNamespace(setPrefix + "sso_login").put(HttpRequestType.POST, sso_login);
		
		rbObj.getNamespace(setPrefix + "sso_logout").put(HttpRequestType.GET, sso_logout);
		rbObj.getNamespace(setPrefix + "sso_logout").put(HttpRequestType.POST, sso_logout);
		
		return rbObj;
	}
}

package picoded.RESTBuilder.template.social;

import picoded.RESTBuilder.RESTBuilder;
import picoded.RESTBuilder.RESTFunction;
import picoded.enums.HttpRequestType;
import picoded.servlet.CommonsPage;
import picoded.conv.ConvertJSON;

import java.util.Base64;
import java.util.Map;
import java.util.HashMap;
import java.util.Formatter;

import javax.crypto.spec.SecretKeySpec;
import javax.crypto.Mac;

// import org.codehaus.jackson.map.ObjectMapper;

public class DisqusIntegration {

	public DisqusIntegration(){

	}

	///
	/// Generates disqus SSO information
	///
	/// ## HTTP Request Parameters
	///
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// | Parameter Name  | Variable Type	   | Description                                                                   |
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
	/// | _oid            | String             | object ID used to generate the string. If no oid is given, return null.       |
	/// | username        | String             | Username used to generate the string. If no username is given, return null.   |
	/// | email           | String             | Email used to generate the string. If no email is given, return null.         |
	/// +-----------------+--------------------+-------------------------------------------------------------------------------+
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
	public RESTFunction sso_hmac_sha1 = (req, res) -> {
		res.put("error", null);
		res.put("hmac_sha1", null);
		res.put("restLog", "");

		String oid = req.getString("_oid", "");
		if(oid.isEmpty()){
			res.put("error", "No _oid specified, unable to generate hmac_sha1");
			return res;
		}

		String username = req.getString("username", "");
		if(username.isEmpty()){
			res.put("error", "No username specified, unable to generate hmac_sha1");
			return res;
		}

		String email = req.getString("email", "");
		if(email.isEmpty()){
			res.put("error", "No email specified, unable to generate hmac_sha1");
			return res;
		}

		CommonsPage corePageObj = (CommonsPage)(req.requestPage());
		String secretKey = corePageObj.JConfig().getString("site.apiKey.disqus.secretKey", "");
		if(secretKey.isEmpty()){
			res.put("error", "No secret key found, unable to generate hmac_sha1");
			return res;
		}

		String publicKey = corePageObj.JConfig().getString("site.apiKey.disqus.publicKey", "");
		if(publicKey.isEmpty()){
			res.put("error", "No public key found, unable to generate hmac_sha1");
			return res;
		}
		res.put("publicKey", publicKey);

		Map<String, String> message = new HashMap<String, String>();
		message.put("id", oid);
		message.put("username", username);
		message.put("email", email);

		//convert message object to json string, then base64 encode it
		String jsonMessage = ConvertJSON.fromObject(message);
		String base64EncodedStr = new String(Base64.getEncoder().encodeToString(jsonMessage.getBytes()));

		long timestamp = System.currentTimeMillis()/1000;

		// Assemble the HMAC-SHA1 signature
		String signature = "";
		try{
			signature = calculateRFC2104HMAC(base64EncodedStr + " " + timestamp, secretKey);
		}catch(Exception e){
			res.put("error", e.getMessage());
			return res;
		}

		res.put("hmac_sha1", signature);
		return res;
	};

	private static String toHexString(byte[] bytes)
	{
		Formatter formatter = new Formatter();
		for (byte b : bytes)
		{
			formatter.format("%02x", b);
		}

		return formatter.toString();
	}

	private static String calculateRFC2104HMAC(String data, String key) throws Exception
	{
		String HMAC_SHA1_ALGORITHM = "HmacSHA1";
		SecretKeySpec signingKey = new SecretKeySpec(key.getBytes(), HMAC_SHA1_ALGORITHM);
		Mac mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
		mac.init(signingKey);
		return toHexString(mac.doFinal(data.getBytes()));
	}

	public RESTBuilder setupRestBuilder(RESTBuilder rbObj, String setPrefix) {
		rbObj.getNamespace(setPrefix + "sso_hmac_sha1").put(HttpRequestType.GET, sso_hmac_sha1);

		return rbObj;
	}
}

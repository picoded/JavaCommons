package picoded.servlet;

import java.util.*;

import picoded.core.struct.GenericConvertMap;
import picoded.servlet.api.ApiBuilder;
import picoded.servlet.api.module.struct.MapProxyApi;
import picoded.web.EmailBroadcaster;

public class CommonsPage extends BasePage {


	@Override
	public void initializeContext() throws Exception {
		super.initializeContext();
	}

	@Override
	public boolean doAuth(Map<String, Object> templateData) throws Exception {
		return super.doAuth(templateData);
	}

	////////////////////////////////////////////
	//
	// api modules setup / integration
	//
	////////////////////////////////////////////

	//------------------------------------------
	// apiSetup loaded
	//------------------------------------------

	/**
	 * !To Override
	 * to configure the ApiBuilder steps
	 *
	 * @param  The APIBuilder object used for setup
	 **/
	@Override
	public void apiSetup(ApiBuilder api) {
		super.apiSetup(api);

		// api.config setup
		apiSetup_config();
	}
		
	//------------------------------------------
	// api.config - support
	//------------------------------------------

	/** apiSetup_config return memoizer */
	protected MapProxyApi apiSetup_config = null;
	
	/**
	 * The configuration api module, if enabled.
	 * @return The used MapProxyApi, or null if disabled
	 */
	public MapProxyApi apiSetup_config() {
		if(apiSetup_config != null) {
			return apiSetup_config;
		}

		// return null if disabled
		if(DConfig().getBoolean("sys.api.config.enabled", true) == false){
			return null;
		}

		// Get the configuration map
		GenericConvertMap<String,Object> configMap = DConfig().getGenericConvertStringMap("sys.api.config","{}");

		// Prepare the map proxy api
		MapProxyApi ret = new MapProxyApi(DConfig());

		// Setup the api
		ret.apiSetup( apiBuilder(), configMap.getString("namespace","config"), configMap);

		// The return object
		apiSetup_config = ret;
		return ret;
	}

	//------------------------------------------
	// systemEmail - support
	//------------------------------------------

	/** systemEmail return memoizer */
	protected EmailBroadcaster systemEmail = null;

	/**
	 * Stadnard broadcast email module support,
	 * this is based on sys.smtp configuration
	 */
	public EmailBroadcaster systemEmail(){
		if (systemEmail != null){
			return systemEmail;
		}

		// return null if disabled
		if(DConfig().getBoolean("sys.smtp.enabled", true) == false){
			return null;
		}

		// Set up the system email settings
		// Get hostname, user, pass, and from account
		String hostname = DConfig().getString("sys.smtp.host", "smtp.mailinator.com:25");
		String username = DConfig().getString("sys.smtp.username", "");
		String password = DConfig().getString("sys.smtp.password", "");
		String emailFrom = DConfig().getString("sys.smtp.emailFrom", "testingTheEmailSystem@mailinator.com");
		boolean isSSL = DConfig().getBoolean("sys.smtp.ssl", false);
		systemEmail = new EmailBroadcaster(hostname, username, password, emailFrom, isSSL);
		systemEmail.setAdminEmail(DConfig().getString("sys.smtp.adminEmail", "eugene@uilicious.com"));
		return systemEmail;
	}
}

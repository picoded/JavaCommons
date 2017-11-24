package picoded.servlet;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

import picoded.core.struct.GenericConvertMap;
import picoded.servlet.api.ApiBuilder;
import picoded.servlet.api.module.struct.MapProxyApi;
import picoded.web.EmailBroadcaster;

public class CommonsPage extends BasePage {
	
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
		if (apiSetup_config != null) {
			return apiSetup_config;
		}
		
		// return null if disabled
		if (DConfig().getBoolean("sys.api.config.enabled", true) == false) {
			return null;
		}
		
		// Get the configuration map
		GenericConvertMap<String, Object> configMap = DConfig().getGenericConvertStringMap(
			"sys.api.config", "{}");
		
		// Prepare the map proxy api
		MapProxyApi ret = new MapProxyApi(DConfig());
		
		// Setup the api
		ret.apiSetup(apiBuilder(), configMap.getString("namespace", "config"), configMap);
		
		// The return object
		apiSetup_config = ret;
		return ret;
	}
	
	////////////////////////////////////////////
	//
	// background threading : public
	//
	////////////////////////////////////////////
	
	/**
	 * This is to be called only within "backgroundProcess"
	 * @return true, if the process is a background thread
	 */
	public boolean isBackgroundThread() {
		return backgroundThread != null;
	}
	
	/**
	 * This is to be called only within "backgroundProcess"
	 * @return true, if the process is a background thread, and not interrupted
	 */
	public boolean isBackgroundThreadAlive() {
		return (isBackgroundThread() && !Thread.interrupted());
	}
	
	/**
	 * [To be extended by sub class, if needed]
	 * The background process to execute per tick.
	 */
	public void backgroundProcess() {
		// Does nothing, for now
	}
	
	////////////////////////////////////////////
	//
	// background threading : internal
	// [ NOT OFFICIALLY SUPPORTED FOR EXTENSION ]
	//
	////////////////////////////////////////////
	
	/**
	 * The background thread handler, isolated as a runnable.
	 *
	 * This allows a clean isolation of the background thread,
	 * From the initializeContext thread. Especially for 'sleep' calls
	 */
	Runnable backgroundThreadHandler = () -> {
		// The config to use
		GenericConvertMap<String, Object> bgConfig = DConfig().getGenericConvertStringMap(
			"sys.background", "{}");
		
		// is "start" interval mode
		boolean intervalModeIsStart = bgConfig.getString("mode", "between").equalsIgnoreCase("start");
		long configInterval = bgConfig.getLong("interval", 10000);
		
		// The invcoation timestamp in previous call
		long previousStartTimestamp = 0;
		
		// Start of background thread loop
		while (isBackgroundThreadAlive()) {
			// Get the new start timestamp
			long startTimestamp = System.currentTimeMillis();
			
			// Does the background process
			try {
				backgroundProcess();
			} catch (Exception e) {
				log().warning("WARNING - Uncaught 'backgroundProcess' exception : " + e.getMessage());
				log()
					.warning(
						"          Note that the 'backgroundProcess' should be designed to never throw an exception,");
				log()
					.warning(
						"          As it will simply be ignored and diverted into the logs (with this message)");
				log().warning(picoded.core.exception.ExceptionUtils.getStackTrace(e));
			}
			
			// Does the appropriate interval delay, takes interruptException as termination
			try {
				if (!isBackgroundThreadAlive()) {
					// Background thread was interrupted, time to break the loop
					break;
				} else if (intervalModeIsStart) {
					// Does the calculations between the timestamp now, the previous start run
					long runtimeLength = System.currentTimeMillis() - startTimestamp;
					// Get the time needed to "wait"
					long sleepRequired = configInterval - runtimeLength;
					// Induce the sleep ONLY if its required
					if (sleepRequired > 0) {
						Thread.sleep(sleepRequired);
					}
				} else {
					// Default mode is "between"
					// Note if an interrupt is called here, it is 'skipped'
					Thread.sleep(configInterval);
				}
			} catch (InterruptedException e) {
				// Log the InterruptedException
				if (isBackgroundThreadAlive()) {
					log()
						.info(
							"backgroundThreadHandler - caught InterruptedException (possible termination event)");
				} else {
					log()
						.warning(
							"backgroundThreadHandler - caught Unexpected InterruptedException (outside termination event)");
				}
			}
			
			// Update the previous start timestamp
			previousStartTimestamp = startTimestamp;
		}
	};
	
	// The running background thread
	Thread backgroundThread = null;
	
	/**
	 * Loads the configuration and start the background thread
	 */
	void backgroundThreadHandler_start() {
		if (DConfig().getBoolean("sys.background.enable", true)) {
			// Start up the background thread start process, only if its enabled
			backgroundThread = new Thread(backgroundThreadHandler);
			// And start it up
			backgroundThread.run();
		}
	}
	
	/**
	 * Loads the configuration and stop the background thread
	 * Either gracefully, or forcefully.
	 */
	@SuppressWarnings("deprecation")
	void backgroundThreadHandler_stop() {
		// Checks if there is relevent background thread first
		if (backgroundThread != null) {
			// Set the interupption flag
			backgroundThread.interrupt();
			// Attempts to perform a join first
			try {
				backgroundThread.join(DConfig().getLong("sys.background.contextDestroyJoinTimeout",
					10000));
			} catch (InterruptedException e) {
				log().warning(
					"backgroundThreadHandler - Unexpected InterruptedException on Thread.join : "
						+ e.getMessage());
				log().warning(picoded.core.exception.ExceptionUtils.getStackTrace(e));
			}
			
			// Does the actual termination if needed
			if (backgroundThread.isAlive()) {
				backgroundThread.stop();
			}
		}
	}
	
	/**
	 * [To be extended by sub class, if needed]
	 * Initialize context setup process
	 **/
	@Override
	public void initializeContext() throws Exception {
		super.initializeContext();
		// backgroundThreadHandler_start();
	}
	
	/**
	 * [To be extended by sub class, if needed]
	 * Initialize context destroy process
	 **/
	@Override
	public void destroyContext() throws Exception {
		super.destroyContext();
		// backgroundThreadHandler_stop();
	}
	
	////////////////////////////////////////////
	//
	// common modules
	//
	////////////////////////////////////////////
	
	//------------------------------------------
	// systemEmail - support
	//------------------------------------------
	
	/** systemEmail return memoizer */
	protected EmailBroadcaster systemEmail = null;
	
	/**
	 * Stadnard broadcast email module support,
	 * this is based on sys.smtp configuration
	 */
	public EmailBroadcaster systemEmail() {
		if (systemEmail != null) {
			return systemEmail;
		}
		
		// return null if disabled
		if (DConfig().getBoolean("sys.smtp.enabled", true) == false) {
			return null;
		}
		
		// Set up the system email settings
		// Get hostname, user, pass, and from account
		String hostname = DConfig().getString("sys.smtp.host", "smtp.mailinator.com:25");
		String username = DConfig().getString("sys.smtp.username", "");
		String password = DConfig().getString("sys.smtp.password", "");
		String emailFrom = DConfig().getString("sys.smtp.emailFrom",
			"testingTheEmailSystem@mailinator.com");
		boolean isSSL = DConfig().getBoolean("sys.smtp.ssl", false);
		systemEmail = new EmailBroadcaster(hostname, username, password, emailFrom, isSSL);
		systemEmail.setAdminEmail(DConfig().getString("sys.smtp.adminEmail", "eugene@uilicious.com"));
		return systemEmail;
	}
}

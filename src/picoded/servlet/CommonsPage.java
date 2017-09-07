package picoded.servlet;

import java.util.*;
import picoded.web.EmailBroadcaster;

public class CommonsPage extends BasePage {

	EmailBroadcaster systemEmail = null;

	@Override
	public void initializeContext() throws Exception {
		super.initializeContext();
	}

	@Override
	public boolean doAuth(Map<String, Object> templateData) throws Exception {
		return super.doAuth(templateData);
	}

	public EmailBroadcaster systemEmail(){
		if (systemEmail != null){
			return systemEmail;
		}
		boolean setUpSMTP = DConfig().getBoolean("sys.smtp.enabled", true);
		// return null if disabled
		if(setUpSMTP== false){
			return null;
		}

		// Set up the system email settings
		// Get hostname, user, pass, and from account
		String hostname = DConfig().getString("sys.smtp.host", "smtp.mailinator.com:25");
		String username = DConfig().getString("sys.smtp.username", "");
		String password = DConfig().getString("sys.smtp.password", "");
		String emailFrom = DConfig().getString("sys.smtp.emailFrom", "testingTheEmailSystem@mailinator.com");
		boolean isSSL = DConfig().getBoolean("sys.smtp.ssl", false);

		return (systemEmail = new EmailBroadcaster(hostname, username, password, emailFrom, isSSL));
	}
}

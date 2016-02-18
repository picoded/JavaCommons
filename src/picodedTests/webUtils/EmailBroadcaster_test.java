package picodedTests.webUtils;

import java.util.HashMap;

import org.junit.*;

import static org.junit.Assert.*;

import picoded.webUtils.EmailBroadcaster;

public class EmailBroadcaster_test {
	
	// Manually set this to true, to run the email tests
	// This is currently disabled to prevent accidental "email spam"
	// By the automated build system.
	private boolean testEnabled = false;
	private final String smtpUrl = "smtp.live.com:25";
	private final String username = "abc@xyz.com";
	private final String password = "xxx";
	private final String fromAddress = "abc@xyz.com";
	
	private EmailBroadcaster emailBroadcaster;
	
	@Before
	public void setUp() {
		emailBroadcaster = new EmailBroadcaster(smtpUrl, username, password, fromAddress, true);
	}
	
	@Test
	public void sendEmailTest() throws Exception {
		if(!testEnabled) {
			return;
		}
		
		String subject = "Hi, from :" + username;
		String htmlContent = "Hi, This is " + username + " , how are you? Regards " + username;
		String toAddresses = "xyz@abc.com";
		HashMap<String, String> fileAttachments = new HashMap<String, String>();
		emailBroadcaster.sendEmail(subject, htmlContent, toAddresses, fileAttachments);
	}
}
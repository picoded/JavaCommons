package picodedTests.webUtils;

import java.util.HashMap;

import org.junit.*;

import static org.junit.Assert.*;

import picoded.webUtils.EmailBroadcaster;

public class EmailBroadcaster_test {
	
	private final String smtpUrl = "smtp.gmail.com:587";
	private final String username = "amlendu.espire@gmail.com";
	private final String password = "Bibha1427@GM";
	private final String fromAddress = "amlendu.espire@gmail.com";
	
	private EmailBroadcaster emailBroadcaster;
	
	@Before
	public void setUp() {
		emailBroadcaster = new EmailBroadcaster(smtpUrl, username, password, fromAddress);
	}
	
	@Test
	public void sendEmailTest() throws Exception {
		String subject = "Hi From :" + username;
		String htmlContent = "Hi, This is " + username + " , how are you? Reards Amlendu";
		String toAddresses = "amlendu.espire@gmail.com";
		HashMap<String, String> fileAttachments = new HashMap<String, String>();
		emailBroadcaster.sendEmail(subject, htmlContent, toAddresses, fileAttachments);
	}
}
package picoded.webUtils;

import java.util.Properties;
import java.util.HashMap;
import java.util.Map;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Message.RecipientType;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

///
/// Email broad casting module, used to send out emails =)
///
/// This is embedded inside CommonsPage
///
public class EmailBroadcaster {
	
	private Session session;
	private String fromEmail;
	
	///
	/// Email constructor, used to setup the SMTP connection
	///
	/// @params  SMTP Url address (with port)
	/// @params  Username for sender account
	/// @params  Password for sender account
	/// @Params  Sending email address
	///
	public EmailBroadcaster(final String smtpUrl, final String username, final String password, final String fromAddress) {
		
		this(smtpUrl, username, password, fromAddress, false);
	}
	
	///
	/// Email constructor, used to setup the SMTP over SSL connection
	///
	/// @params  SMTP Url address (with port)
	/// @params  Username for sender account
	/// @params  Password for sender account
	/// @Params  Sending email address
	/// @Params  Use SSL over SMTP
	///
	public EmailBroadcaster(final String smtpUrl, final String username, final String password,
		final String fromAddress, boolean isSSL) {
		
		fromEmail = fromAddress;
		Properties props = new Properties();
		
		String[] parts = smtpUrl.split(":");
		String smtpAdd = parts[0];
		String smtpPort = (parts.length > 1) ? parts[1] : "465";
		
		props.put("mail.transport.protocol", "smtp");
		props.put("mail.smtp.host", smtpAdd);
		if (smtpAdd.contains("gmail") || smtpAdd.contains("live.com")) {
			props.put("mail.smtp.starttls.enable", "true");
		}
		
		if (isSSL) {
			props.put("mail.smtp.socketFactory.port", smtpPort);
			props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
		}
		
		//props.put("mail.smtp.socketFactory.port", smtpPort );
		//props.put("mail.smtp.socketFactory.class","javax.net.ssl.SSLSocketFactory");
		props.put("mail.smtp.port", smtpPort);
		
		if (username != null && username.trim().length() > 0) {
			props.put("mail.smtp.auth", "true");
			session = Session.getInstance(props, new javax.mail.Authenticator() {
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(username, password);
				}
			});
		} else {
			props.put("mail.smtp.auth", "false");
			props.put("mail.smtp.auth.login.disable", "true");
			
			session = Session.getInstance(props, null);
		}
		
	}
	
	///
	/// Full featured sendEmail function, all other "overloaded" functions are convinence functiosn based on this
	///
	/// @params  email subject
	/// @params  email HTML content
	/// @params  sending to email addresses array
	/// @params  CC to email addresses array
	/// @params  BCC to email adresses array
	/// @params  File attachment Map<String filename, String absolute file path>
	/// @params  Overwrite default, "from address"
	///
	public boolean sendEmail(String subject, String htmlContent, String toAddresses[], String ccAddresses[],
		String bccAddresses[], Map<String, String> fileAttachments, String fromAddress) throws Exception {
		// Actual message contianer used by the function
		MimeMessage message = new MimeMessage(session);
		
		// Process "FROM" address field
		if (fromAddress != null) {
			message.setFrom(new InternetAddress(fromAddress));
		} else {
			message.setFrom(new InternetAddress(fromEmail));
		}
		
		// Process "TO" address field
		if (toAddresses == null || toAddresses.length <= 0) {
			throw new Exception("Sending to email address is not allowed to be 'empty'");
		}
		InternetAddress[] addressTo = new InternetAddress[toAddresses.length];
		for (int i = 0; i < toAddresses.length; i++) {
			addressTo[i] = new InternetAddress(toAddresses[i]);
		}
		message.setRecipients(RecipientType.TO, addressTo);
		
		// Process "CC" address field
		if (ccAddresses != null && ccAddresses.length > 0) {
			InternetAddress[] addressCC = new InternetAddress[ccAddresses.length];
			for (int i = 0; i < ccAddresses.length; i++) {
				addressCC[i] = new InternetAddress(ccAddresses[i]);
			}
			if (addressCC.length > 0) {
				message.setRecipients(RecipientType.CC, addressCC);
			}
		}
		
		// Process "BCC" address field
		if (bccAddresses != null && bccAddresses.length > 0) {
			InternetAddress[] addressBCC = new InternetAddress[bccAddresses.length];
			for (int i = 0; i < bccAddresses.length; i++) {
				addressBCC[i] = new InternetAddress(bccAddresses[i]);
			}
			if (addressBCC.length > 0) {
				message.setRecipients(RecipientType.BCC, addressBCC);
			}
		}
		
		// Set email SUBJECT
		message.setSubject(subject);
		
		// Message container (for body, and attachments)
		Multipart multipart = new MimeMultipart();
		
		// Set email HTML CONTENT
		if (htmlContent != null) {
			MimeBodyPart messageBodyPart = new MimeBodyPart();
			messageBodyPart.setContent(htmlContent, "text/html");
			multipart.addBodyPart(messageBodyPart);
		}
		
		// Loops through file attachments, and add it
		if (fileAttachments != null) {
			for (Map.Entry<String, String> entry : fileAttachments.entrySet()) {
				MimeBodyPart messageBodyPart = new MimeBodyPart();
				String key = entry.getKey();
				String value = entry.getValue();
				
				DataSource source = new FileDataSource(value);
				messageBodyPart.setDataHandler(new DataHandler(source));
				messageBodyPart.setFileName(key);
				multipart.addBodyPart(messageBodyPart);
			}
		}
		
		// Apply message content
		message.setContent(multipart);
		
		//Sends the message
		Transport.send(message);
		
		return true;
	}
	
	/// Shorten convinence function (does not need testing, test the core function directly instead)
	public boolean sendEmail(String subject, String htmlContent, String toAddresses[], String ccAddresses[],
		String bccAddresses[], HashMap<String, String> fileAttachments) throws Exception {
		return sendEmail(subject, htmlContent, toAddresses, ccAddresses, bccAddresses, fileAttachments, null);
		
	}
	
	/// Shorten convinence function (does not need testing, test the core function directly instead)
	public boolean sendEmail(String subject, String htmlContent, String toAddresses, String ccAddresses,
		String bccAddresses, HashMap<String, String> fileAttachments) throws Exception {
		return sendEmail(subject, htmlContent, ((toAddresses != null) ? (toAddresses.split(",")) : null),
			((ccAddresses != null) ? (ccAddresses.split(",")) : null), ((bccAddresses != null) ? (bccAddresses.split(","))
				: null), fileAttachments, null);
	}
	
	/// Shorten convinence function (does not need testing, test the core function directly instead)
	public boolean sendEmail(String subject, String htmlContent, String toAddresses,
		HashMap<String, String> fileAttachments) throws Exception {
		return sendEmail(subject, htmlContent, ((toAddresses != null) ? (toAddresses.split(",")) : null), null, null,
			fileAttachments, null);
	}
	
}

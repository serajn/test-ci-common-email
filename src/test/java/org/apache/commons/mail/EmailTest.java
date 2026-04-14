package org.apache.commons.mail;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Date;
import java.util.Properties;

import javax.mail.Session;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class EmailTest {
	
	private static final String[] TEST_EMAILS = {"ab@bc.com", "a.b@c.org", 
			"abcdefghijklmnopqrst@abcdefghijklmnopqrst.com.bd"};

	// Concrete Email class used to test the abstract Email class
	private EmailConcrete email;
	
	@Before
	public void setUpEmailTest() throws Exception {
		email = new EmailConcrete(); // instantiate before each test
	}
	
	@After
	public void tearDownEmailTest() throws Exception {
        // Cleanup after each test
    }
	
	
	/*******************************************************************
	 * Unit Tests for addBcc() method
	 *******************************************************************/
	
	@Test
	public void testAddBcc() throws Exception {
		email.addBcc(TEST_EMAILS);
		assertEquals(3, email.getBccAddresses().size()); // check all BCC addresses added
	}
	
	
	/*******************************************************************
	 * Unit Tests for addCc() method
	 *******************************************************************/
	
	@Test
	public void testAddCc() throws Exception {
		email.addCc(TEST_EMAILS[0]);
		assertEquals(1, email.getCcAddresses().size()); // check CC address added
	}
	
	
	/*******************************************************************
	 * Unit Tests for addHeader() method
	 *******************************************************************/
	
	@Test
	public void testAddHeader() throws Exception {
		email.addHeader("From", TEST_EMAILS[0]);
		assertEquals(1, email.headers.size()); // header added
	}
	
	@Test
	public void testAddHeaderEmptyName() throws Exception {
		try {
			email.addHeader("", TEST_EMAILS[0]);
			fail("Expected IllegalArgumentException for empty header name");
		} catch (IllegalArgumentException e) {
			assertEquals("Header name cannot be empty", e.getMessage());
		}
	}
	
	
	/*******************************************************************
	 * Unit Tests for addReplyTo() method
	 *******************************************************************/
	
	@Test
	public void testAddReplyTo() throws Exception {
		email.addReplyTo(TEST_EMAILS[0]);
		assertEquals(1, email.getReplyToAddresses().size()); // reply-to address added
	}
	
	
	/*******************************************************************
	 * Unit Tests for buildMimeMessage() method
	 *******************************************************************/
	
	@Test(expected = IllegalStateException.class)
	public void testBuildTwiceThrows() throws Exception {
		email.setHostName("smtp.example.com");
		email.addTo("test@example.com");
		email.setFrom("from@example.com");
		
		email.buildMimeMessage();
		email.buildMimeMessage(); // building twice should throw IllegalStateException
	}
	
	@Test(expected = EmailException.class)
	public void testNoFromAddressThrows() throws Exception {
		email.setHostName("smtp.example.com");
		email.addTo("test@example.com");
		email.buildMimeMessage(); // missing FROM should throw EmailException
	}
	
	@Test(expected = EmailException.class)
	public void testNoRecipientsThrow() throws Exception {
		email.setHostName("smtp.example.com");
		email.setFrom("from@example.com");
		email.buildMimeMessage(); // no recipients should throw EmailException
	}
	
	@Test
	public void testBuildMimeMessageSuccess() throws Exception {
		email.setHostName("smtp.example.com");
		email.addTo("test@example.com");
		email.setFrom("from@example.com");
		email.setSubject("Test Subject");
		
		email.buildMimeMessage();
		assertNotNull(email.getMimeMessage()); // message should be created successfully
	}
	
	@Test
	public void testSubjectSet() throws Exception {
		email.setHostName("smtp.example.com");
		email.addTo("test@example.com");
		email.setFrom("from@example.com");
		email.setSubject("Hello");
		
		email.buildMimeMessage();
		assertEquals("Hello", email.getMimeMessage().getSubject()); // subject correctly set
	}
	
	@Test
	public void testHeadersAdded() throws Exception {
		email.setHostName("smtp.example.com");
		email.addTo("test@example.com");
		email.setFrom("from@example.com");
		email.addHeader("X-Test", "value");
		
		email.buildMimeMessage();
		String[] headers = email.getMimeMessage().getHeader("X-Test");
		assertNotNull(headers);
		assertEquals("value", headers[0]); // header correctly added to message
	}
	
	@Test
	public void testEmptyContentDefaultsToEmptyString() throws Exception {
		email.setHostName("smtp.example.com");
		email.addTo("test@example.com");
		email.setFrom("from@example.com");
		email.buildMimeMessage();
		
		String content = (String) email.getMimeMessage().getContent();
		assertEquals("", content); // empty content defaults to empty string
	}
	
	@Test
	public void testPlainTextContent() throws Exception {
	    email.setHostName("smtp.example.com");
	    email.setFrom("from@example.com");
	    email.addTo("to@example.com");
	    email.setContent("Hello world", "text/plain");
	    email.buildMimeMessage();
	    String content = (String) email.getMimeMessage().getContent();
	    assertTrue(content.contains("Hello world")); // plain text content set correctly
	}
	
	@Test
	public void testHtmlContent() throws Exception {
	    email.setHostName("smtp.example.com");
	    email.setFrom("from@example.com");
	    email.addTo("to@example.com");
	    email.setContent("<h1>Hello</h1>", "text/html");
	    email.buildMimeMessage();
	    email.getMimeMessage().saveChanges();
	    assertTrue(email.getMimeMessage().getContentType().contains("text/html")); // HTML content type
	}
	
	
	/*******************************************************************
	 * Unit Tests for getHostName() method
	 *******************************************************************/
	
	@Test
	public void testGetHostName() throws Exception {
		email.setHostName("HostName");
		email.getHostName(); // retrieves host name
	}
	
	@Test
	public void testGetHostNameEmptyName() {
		email.hostName = "";
		email.getHostName(); // handles empty host name
	}
	
	
	/******************************************************************
	 * Unit Tests for getMailSession() method
	 ******************************************************************/
	
	@Test
	public void testSessionCreated() throws EmailException {
		email.setHostName("smptp.example.com");
		Session session = email.getMailSession();
		assertNotNull(session); // session is created
	}
	
	@Test
	public void testSessionIsCached() throws EmailException {
		email.setHostName("smtp.example.com");
		Session s1 = email.getMailSession();
		Session s2 = email.getMailSession();
		assertSame(s1, s2); // session is cached
	}
	
	@Test
	public void testThrowsExceptionWhenNoHost() {
		try {
			email.getMailSession();
			fail("Expected EmailException to be thrown");
		} catch (EmailException e) {
			assertEquals("Cannot find valid hostname for mail session", e.getMessage()); // correct exception
		}
	}
	
	@Test
	public void testPropertiesSetCorrectly() throws EmailException {
		email.setHostName("smtp.example.com");
		email.setSmtpPort(25);
		Session session = email.getMailSession();
		Properties props = session.getProperties();
		assertEquals("smtp.example.com", props.getProperty("mail.smtp.host"));
		assertEquals("25", props.getProperty("mail.smtp.port")); // verify session properties
	}
	
	
	/*******************************************************************
	 * Unit Tests for getSentDate() method
	 *******************************************************************/
	
	@Test
	public void testGetSentDateWhenNull() {
		Date before = new Date();
		Date result = email.getSentDate();
		Date after = new Date();
		assertTrue(result.equals(before) || result.after(before));
		assertTrue(result.equals(after) || result.before(after)); // current date returned if sentDate null
	}
	
	@Test
	public void testGetSentDateReturnsCopy() {
		Date original = new Date(1000000);
		email.setSentDate(original);
		Date result = email.getSentDate();
		result.setTime(2000000); // modifying returned date should not affect internal
		Date internal = email.getSentDate();
		assertEquals(1000000, internal.getTime());
	}
	
	
	/*******************************************************************
	 * Unit Tests for getSocketConnectionTimeout() method
	 *******************************************************************/
	
	@Test
	public void testSocketConnectionTimeoutSet() {
	    email.setSocketConnectionTimeout(5000);
	    assertEquals(5000, email.getSocketConnectionTimeout()); // verifies getter/setter
	}
	
	
	/*******************************************************************
	 * Unit Tests for setFrom() method
	 *******************************************************************/
	
	@Test
	public void testSetFromValidEmail() throws Exception {
	    email.setFrom("from@example.com");
	    assertNotNull(email.getFromAddress());
	    assertEquals("from@example.com", email.getFromAddress().getAddress()); // sets FROM address correctly
	}	
}
package com.darkenedsky.gemini.service;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.*;
import java.util.ArrayList;
import java.util.Properties;


/**
 * Use this as a base class for form emails we'll send to make
 * the process easier of handling all the string manipulation
 *
 * @author  Matt Holden
 */
public class Email implements java.io.Serializable {
    
	/**
	 * 
	 */
	private static final long serialVersionUID = 449836500103292345L;
	
	/** Subject */ 
	protected String subject;
	
	/** Recipient */ 
	protected String recipient;
	
	/** Sender */
	protected String sender;
	
	/** Body */ 
	protected String body, htmlBody; 
	        
	/** the properties for the client */
	protected Properties fMailServerConfig;
	
	/** The authenticator to use to hook up to the mail server */
	protected javax.mail.Authenticator authenticator;
	
	/** The settings from the XML config file. */
	private com.darkenedsky.gemini.Message settings;

	/** Addresses that will be CC'ed. */
	private ArrayList<String> cc = new ArrayList<String>();
	
	/** Addresses that will be BCC'ed */
	private ArrayList<String> bcc = new ArrayList<String>();
	
	/** File paths of attachments */
	private ArrayList<String> attachments = new ArrayList<String>();
	
	/** Add an address to CC 
	 *   
	 * @param address the address to cc
	 */
    public void addCC(String address)  { 
		cc.add(address);
	}
	
    /** Add an address to BCC 
	 *   
	 * @param address the address to bcc
	 */
    public void addBCC(String address) { 
		bcc.add(address);	
	}
	
	
    /** Creates a new instance of Email 
     *  @param _settings email settings to use
        @param rec recipient address
        @param sub subject     
        @param text body of the message 
        @param html html text of the message
     */   
    public Email(com.darkenedsky.gemini.Message _settings, String rec, String sub, String text, String html)  throws AddressException {
    	
    	this.settings = _settings;
        subject = sub;
        recipient = rec;
        body = text;      
        sender = settings.getString("email-sender");        

        fMailServerConfig = System.getProperties();
        fMailServerConfig.put("mail.smtp.host", settings.getString("email-server"));
        fMailServerConfig.put("mail.from", sender);    
        authenticator = new Authenticator() { 
        	public PasswordAuthentication getPasswordAuthentication() {  
        		return new PasswordAuthentication(settings.getString("email-user"), settings.getString("email-password"));        	
        	}
        };
    
    }
    
    
    /**
     * Getter for property subject.
     * @return Value of property subject.
     */
    public java.lang.String getSubject() {
        return subject;
    }
    
    /**
     * Setter for property subject.
     * @param subject New value of property subject.
     */
    public void setSubject(java.lang.String subject) {
        this.subject = subject;
    }
    
    /**
     * Getter for property recipient.
     * @return Value of property recipient.
     */
    public java.lang.String getRecipient() {
        return recipient;
    }
    
    /**
     * Setter for property recipient.
     * @param recipient New value of property recipient.
     */
    public void setRecipient(java.lang.String recipient) {
        this.recipient = recipient;
    }
    
    /**
     * Getter for property body.
     * @return Value of property body.
     */
    public java.lang.String getTextBody() {
        return body;
    }
    
    /**
     * Setter for property body.
     * @param body New value of property body.
     */
    public void setTextBody(java.lang.String body) {
        this.body = body;
    }
 
    /** Add a file attachment to the email
     * @param file The file path to add.
     */
	public void addAttachment(String file) { 
		attachments.add(file);
	}

    
	 /** Build the message 
	 * @throws MessagingException 
	 * */
    protected MimeMessage buildMessage() throws MessagingException { 
	    Session session = Session.getInstance( fMailServerConfig, authenticator);        
	    MimeMessage message = new MimeMessage( session );
	    InternetAddress[] replyTo = { new InternetAddress(settings.getString("email-replyto")) };
	    message.setReplyTo(replyTo);
	    message.setSentDate(new java.util.Date(System.currentTimeMillis()));
	    
	      message.addRecipient(
	        Message.RecipientType.TO, new InternetAddress(recipient)        
	      );
	      for (String address : cc)
	    	  message.addRecipient(Message.RecipientType.CC, new InternetAddress(address));
	      for (String address : bcc)
	    	  message.addRecipient(Message.RecipientType.BCC, new InternetAddress(address));
	      
	      message.setFrom(new InternetAddress(sender));
	      message.setSubject( subject );
	      
	      MimeMultipart mp = new MimeMultipart();
	
	      if (body != null) { 
			  BodyPart textpart = new MimeBodyPart();
			  textpart.setContent(body, "text/plain");
			  mp.addBodyPart(textpart);
	      }
	      
		  if (htmlBody != null) { 
			  BodyPart htmlpart = new MimeBodyPart();
			  htmlpart.setContent(htmlBody, "text/html");
			  mp.addBodyPart(htmlpart);
		  }
		  
		  for (String file : attachments) { 
		  	MimeBodyPart attach = new MimeBodyPart();
		    DataSource source =  new FileDataSource(file);
		    attach.setDataHandler(new DataHandler(source));
		    attach.setFileName(file);
		    mp.addBodyPart(attach);
		  }
		    
	      message.setContent(mp);
	      return message;
    }
    
  /**
  * Send a single email.
  */
  public void send() throws MessagingException { 	 
      Transport.send( buildMessage() );
  }     

  
  
} 


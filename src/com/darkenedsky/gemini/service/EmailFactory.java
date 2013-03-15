package com.darkenedsky.gemini.service;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;

import com.darkenedsky.gemini.Languages;
import com.darkenedsky.gemini.Message;
import com.darkenedsky.gemini.exception.InvalidEmailTemplateException;
import com.darkenedsky.gemini.tools.StringTools;

public class EmailFactory {

	private Message settings;
	
	private static class EmailTemplate { 
		HashMap<String, String> subject = new HashMap<String, String>();
		HashMap<String, String> plaintext = new HashMap<String, String>();
		HashMap<String, String> html = new HashMap<String, String>();		
	}
	
	protected HashMap<String, EmailTemplate> templates = new HashMap<String, EmailTemplate>();	
	
	public EmailFactory(Message settings, JDBCConnection jdbc) throws Exception { 
		this.settings = settings;
		
		PreparedStatement ps = jdbc.prepareStatement("select * from email_templates;");
		ResultSet set = null;
		try { 
			set = ps.executeQuery();
			if (set.first()) { 
				while (true) { 
					EmailTemplate t = templates.get(set.getString("templatename"));
					if (t == null) { 
						t = new EmailTemplate();
						templates.put(set.getString("templatename"), t);
					}
					String lang = set.getString("language");
					t.html.put(lang, set.getString("html"));
					t.plaintext.put(lang, set.getString("plaintext"));
					t.subject.put(lang, set.getString("subject"));
					
					if (set.isLast()) break;
					set.next();
				}
			}
			set.close();
		}
		catch (Exception x) { 
			if (set != null)
				set.close();
			throw x;
		}
		
	}
	
	public void sendEmail(String recipient, String template, String language, HashMap<String, String> fieldsToReplace) throws AddressException, MessagingException { 
		
		EmailTemplate t = templates.get(template);
		if (t == null)
			throw new InvalidEmailTemplateException(template);
		
		String html = t.html.get(language);
		String text = t.plaintext.get(language);
		String subject = t.subject.get(language);
		
		// try to default to english if the language isnt there
		if (html == null) html = t.html.get(Languages.ENGLISH);
		if (text == null) text = t.plaintext.get(Languages.ENGLISH);
		if (subject == null) subject =t.subject.get(Languages.ENGLISH);
		
		if (html == null) html = "";
		if (text == null) text = "";
		if (subject == null) subject = "";
		
		for (Map.Entry<String, String> replace : fieldsToReplace.entrySet()) { 
			html = StringTools.stringReplace(html, replace.getKey(), replace.getValue());
			text = StringTools.stringReplace(html, replace.getKey(), replace.getValue());
			subject = StringTools.stringReplace(html, replace.getKey(), replace.getValue());
		}
		
		new Email(settings, recipient, subject, text, html).send();
	}
}

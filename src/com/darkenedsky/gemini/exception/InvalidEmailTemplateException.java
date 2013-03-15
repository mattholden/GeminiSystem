package com.darkenedsky.gemini.exception;

public class InvalidEmailTemplateException extends GeminiException { 
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 7646165549646633194L;

	public InvalidEmailTemplateException(String template) { 
		super(INVALID_EMAIL_TEMPLATE, "The specified email template was not found.");
		details.put("template", template);
		
	}

}

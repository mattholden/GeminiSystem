package com.darkenedsky.gemini.exception;

public class RequiredFieldException extends GeminiException { 
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -76045281741840968L;

	public RequiredFieldException(String field) { 
		super(REQUIRED_FIELD, "A required field was omitted from your message.");
		details.put("field", field);
	}

}

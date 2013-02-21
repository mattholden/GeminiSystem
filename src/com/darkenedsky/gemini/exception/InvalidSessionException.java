package com.darkenedsky.gemini.exception;

/** Thrown when a client presents a session ID that is no longer valid. */
public class InvalidSessionException extends GeminiException { 
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -7040448708593151098L;
	
	/** Construct the exception.
	 * 
	 * @param sess The session token that was presented.
	 */
	public InvalidSessionException(String sess) { 
		super(INVALID_SESSION, "Invalid session token.");
		details.put("token", sess);
	}
	
}

package com.darkenedsky.gemini.exception;

/** Thrown when an invalid action number, or a message with invalid parameters to support the requested action,
 *  is received.
 *  
 * @author Matt Holden
 *
 */
public class InvalidActionException extends GeminiException { 
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -7147330164592362076L;
	
	/** Construct the exception
	 * 
	 * @param ac the action number 
	 */
	public InvalidActionException(Integer ac) { 
		super(INVALID_ACTION, "Invalid action!");
		details.put("action", ac);
	}
	
}

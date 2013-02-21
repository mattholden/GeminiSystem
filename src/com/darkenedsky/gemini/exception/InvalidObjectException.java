package com.darkenedsky.gemini.exception;

/** Thrown when an object ID is not found. */
public class InvalidObjectException extends GeminiException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6431558346285006152L;

	/** Construct the exception
	 *  @param id the object ID that was not found
	 */
	public InvalidObjectException(long id) {
		super(ExceptionCodes.INVALID_OBJECT, "Object not found!");
		details.put("id", id);
	} 
	
	

}

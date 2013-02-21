package com.darkenedsky.gemini.exception;

/** Thrown when a library secion is not found in the Library. */
public class InvalidLibrarySectionException extends GeminiException {


	/**
	 * 
	 */
	private static final long serialVersionUID = 6190631115815173691L;

	/** Construct the exception
	 *  @param id the section that was not found
	 */
	public InvalidLibrarySectionException(String id) {
		super(ExceptionCodes.INVALID_LIBRARY_SECTION, "Library section not found!");
		details.put("section", id);
	} 
	
	

}

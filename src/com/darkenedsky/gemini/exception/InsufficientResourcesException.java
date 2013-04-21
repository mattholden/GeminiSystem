package com.darkenedsky.gemini.exception;


public class InsufficientResourcesException extends RuleException {


	/**
	 * 
	 */
	private static final long serialVersionUID = 685613611383931428L;

	public InsufficientResourcesException() {
		super(INSUFFICIENT_RESOURCES, "You lack the necessary resources to perform this action.");		
	}

}

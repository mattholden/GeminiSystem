package com.darkenedsky.gemini.exception;

public class ChargeProcessingException extends GeminiException { 
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -8014081596331102421L;

	public ChargeProcessingException(String result) { 
		super(CHARGE_PROCESSING, result);
	}

}

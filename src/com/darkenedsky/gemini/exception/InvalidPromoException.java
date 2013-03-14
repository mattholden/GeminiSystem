package com.darkenedsky.gemini.exception;

public class InvalidPromoException extends GeminiException { 
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1825830760881374344L;

	public InvalidPromoException(String promo) { 
		super(INVALID_PROMO, "The promotional code you entered is not valid.");
		details.put("promocode", promo);
	}

}

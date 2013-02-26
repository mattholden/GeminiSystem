package com.darkenedsky.gemini.exception;

public class CCGInvalidDeckException extends GeminiException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6170488607878234796L;

	public CCGInvalidDeckException(long did) { 
		super(CCG_INVALID_DECK, "Invalid deck ID!");
		this.details.put("deckid", did);
	}
	
}

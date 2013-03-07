package com.darkenedsky.gemini.exception;

public class InvalidCardTypeException extends RuleException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3557648453053627016L;

	public InvalidCardTypeException(long cardid) {
		super(INVALID_CARD_TYPE, "Invalid card type!");
		details.put("cardid", cardid);
	}

}

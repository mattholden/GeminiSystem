package com.darkenedsky.gemini.exception;

public class CCGInvalidCardTypeException extends RuleException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3557648453053627016L;

	public CCGInvalidCardTypeException(long cardid) {
		super(CCG_INVALID_CARD_TYPE, "Invalid card type!");
		details.put("cardid", cardid);
	}

}

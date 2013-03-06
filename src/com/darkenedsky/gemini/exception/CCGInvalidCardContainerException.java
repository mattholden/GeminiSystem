package com.darkenedsky.gemini.exception;

public class CCGInvalidCardContainerException extends RuleException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3892295361068911523L;

	public CCGInvalidCardContainerException(long cardid) {
		super(CCG_INVALID_CARD_CONTAINER, "Invalid card location!");
		details.put("cardid", cardid);
	}

}

package com.darkenedsky.gemini.exception;

public class InvalidCardContainerException extends RuleException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3892295361068911523L;

	public InvalidCardContainerException(long cardid) {
		super(INVALID_CARD_CONTAINER, "Invalid card location!");
		details.put("cardid", cardid);
	}

}

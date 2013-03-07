package com.darkenedsky.gemini.exception;

public class InvalidCardControllerException extends RuleException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7979064730350577727L;

	public InvalidCardControllerException(long cardid) {
		super(INVALID_CARD_CONTROLLER, "Invalid card controller!");
		details.put("cardid", cardid);
	}

}

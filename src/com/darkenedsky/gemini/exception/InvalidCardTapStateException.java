package com.darkenedsky.gemini.exception;

public class InvalidCardTapStateException extends RuleException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8307225781703521521L;

	public InvalidCardTapStateException(long cardid) {
		super(INVALID_CARD_TAPSTATE, "Invalid card tapped state!");
		details.put("cardid", cardid);
	}

}

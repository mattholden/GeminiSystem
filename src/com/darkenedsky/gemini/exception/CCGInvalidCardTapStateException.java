package com.darkenedsky.gemini.exception;

public class CCGInvalidCardTapStateException extends RuleException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8307225781703521521L;

	public CCGInvalidCardTapStateException(long cardid) {
		super(CCG_INVALID_CARD_TAPSTATE, "Invalid card tapped state!");
		details.put("cardid", cardid);
	}

}

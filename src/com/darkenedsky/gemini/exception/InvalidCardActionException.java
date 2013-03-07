package com.darkenedsky.gemini.exception;

public class InvalidCardActionException extends RuleException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2030074838707856855L;

	public InvalidCardActionException(long cardid) {
		super(INVALID_CARD_ACTION, "Invalid action for the specified card.");
		details.put("cardid", cardid);
	}

}

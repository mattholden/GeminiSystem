package com.darkenedsky.gemini.exception;


public class InvalidCardTargetException extends RuleException  {


	/**
	 * 
	 */
	private static final long serialVersionUID = 6569110358578003793L;

	public InvalidCardTargetException(long cardid) {
		super(INVALID_CARD_ACTION_TARGET, "Invalid target for this action.");
		details.put("cardid", cardid);
	}

}

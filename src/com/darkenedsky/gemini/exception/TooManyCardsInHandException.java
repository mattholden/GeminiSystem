package com.darkenedsky.gemini.exception;

public class TooManyCardsInHandException extends RuleException { 
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -7157906577694993219L;

	public TooManyCardsInHandException(int howManyOver) { 
		super(TOO_MANY_CARDS_IN_HAND, "You have too many cards in your hand! Discard some.");
		details.put("overby", howManyOver);
	}

}

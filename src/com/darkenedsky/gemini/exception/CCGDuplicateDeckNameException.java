package com.darkenedsky.gemini.exception;

public class CCGDuplicateDeckNameException extends RuleException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4885952764410059778L;

	public CCGDuplicateDeckNameException() {
		super(CCG_DUPLICATE_DECKNAME, "You already have a deck with this name.");
	}

}

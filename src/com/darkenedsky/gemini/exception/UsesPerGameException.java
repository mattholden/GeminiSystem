package com.darkenedsky.gemini.exception;

public class UsesPerGameException extends RuleException  { 
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -8466977821721150798L;

	public UsesPerGameException() { 
		super(USES_PER_GAME, "You may not use this ability any more times this game.");
	}

}

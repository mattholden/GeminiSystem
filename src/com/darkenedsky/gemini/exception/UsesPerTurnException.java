package com.darkenedsky.gemini.exception;

public class UsesPerTurnException extends RuleException  { 
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 2603284387196647815L;

	public UsesPerTurnException() { 
		super(USES_PER_TURN, "You may not use this ability any more times this turn.");
	}

}

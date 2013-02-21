package com.darkenedsky.gemini.exception;

/** Thrown when an action is passed in that cannot be executed during this game phase. */
public class NotOnThisPhaseException extends RuleException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6989870879931894221L;

	/** Construct the exception */
	public NotOnThisPhaseException() {
		super(NOT_ON_THIS_PHASE, "You may not execute this action during this game phase.");
	}

}

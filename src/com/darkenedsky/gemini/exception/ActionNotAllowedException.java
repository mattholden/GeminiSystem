package com.darkenedsky.gemini.exception;

/** Thrown when an action is valid, but is not permitted given the current service state. */
public class ActionNotAllowedException extends GeminiException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4230911801963278003L;

	public ActionNotAllowedException(boolean requiredLoggedIn) {
		super(ACTION_NOT_ALLOWED, "Action is not allowed when you are " + ((requiredLoggedIn) ? "not " : "") + "logged in.");
		// TODO Auto-generated constructor stub
	}

	
}

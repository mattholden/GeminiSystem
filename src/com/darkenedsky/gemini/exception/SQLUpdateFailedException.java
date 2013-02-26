package com.darkenedsky.gemini.exception;

public class SQLUpdateFailedException extends GeminiException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7576713964337173155L;

	public SQLUpdateFailedException() {
		super(SQL_UPDATE_FAILED, "Database insert/update was not completed successfully.");
	}

}

package com.darkenedsky.gemini.exception;

/** Thrown when a user tries to login with an invalid username or password. */
public class InvalidLoginException extends GeminiException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -666774937397555645L;

	/** Construct the exception */
	public InvalidLoginException() { 
		super(INVALID_LOGIN, "Invalid username or password."	);
	}
}

package com.darkenedsky.gemini.exception;

/** Thrown when you try to join a password-protected game and you didn't enter the correct password. */
public class InvalidGamePasswordException extends GeminiException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5517919856856854997L;

	/** Construct the exception. */
	public InvalidGamePasswordException() { 
		super(INVALID_GAME_PASSWORD, "Invalid password to join game!");	
	}


}

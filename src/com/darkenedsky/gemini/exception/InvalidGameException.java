package com.darkenedsky.gemini.exception;

/** Thrown when a message is received with a game ID that does not match any known game. */
public class InvalidGameException extends GeminiException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4017269953436804249L;
	 
	/** Construct the exception
	 * 
	 * @param g The game ID we couldn't find
	 */
	public InvalidGameException(Long g) { 
		super(INVALID_GAME, "Invalid game ID!");
		details.put("gameid", g);
	}
	
	
	

}

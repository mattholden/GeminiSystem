package com.darkenedsky.gemini.exception;

/** Thrown when an action is valid, but is not permitted if you are/are not a player in the game.. */
public class GamePlayerException extends GeminiException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4230911801963278003L;

	public GamePlayerException(boolean requiredLoggedIn) {
		super(GAME_PLAYER, "Action is not allowed when you are " + ((requiredLoggedIn) ? "not " : "") + "a player in the game.");
	}

	
}

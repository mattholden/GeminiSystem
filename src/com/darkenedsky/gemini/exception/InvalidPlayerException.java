package com.darkenedsky.gemini.exception;

/** Thown when we try to look up a player by ID that is not logged in or a member of the game */
public class InvalidPlayerException extends GeminiException {

	/** Construct the exception.
	 * 
	 * @param player the player ID we were looking for and did not find
	 */
	public InvalidPlayerException(long player) {
		super(INVALID_PLAYER, "Player not found!");
		details.put("playerid", player);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -3421589570476662062L;


	
}

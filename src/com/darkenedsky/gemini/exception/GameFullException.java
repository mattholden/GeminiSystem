package com.darkenedsky.gemini.exception;

/** Exception thrown when someone tries to join a game and it's already at its max players. */
public class GameFullException extends RuleException {

	/** Throw an exception.
	 *  
	 * @param game the game ID you tried to join.
	 */
	public GameFullException(long game) {
		super(GAME_FULL, "The game has its maximum number of players.");
		details.put("gameid", game);
	}

	private static final long serialVersionUID = -5068237830513023729L;

}

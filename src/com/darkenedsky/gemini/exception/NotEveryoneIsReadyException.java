package com.darkenedsky.gemini.exception;

/** An exception to indicate when you tried to start the game before everyone was ready. */
public class NotEveryoneIsReadyException extends RuleException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -310379573279042929L;

	/** Construct the exception
	 * 
	 * @param gameid the ID of the afflicted game.
	 */
	public NotEveryoneIsReadyException(long gameid) {
		super(NOT_EVERYONE_IS_READY, "The game cannot be started until all players are ready.");
		details.put("gameid", gameid);
		
	}

}

package com.darkenedsky.gemini.exception;

public class PlayerEliminatedException extends RuleException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4762942853256469467L;

	public PlayerEliminatedException() {
		super(PLAYER_ELIMINATED, "You have been eliminated from this game.");		
	} 
	
	

}

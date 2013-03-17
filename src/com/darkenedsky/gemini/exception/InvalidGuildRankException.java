package com.darkenedsky.gemini.exception;

public class InvalidGuildRankException extends RuleException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -164801654100491007L;

	public InvalidGuildRankException() {
		super(INVALID_GUILD_RANK, "You entered an invalid guild rank. Valid values are 0 - 9.");		
	}

}

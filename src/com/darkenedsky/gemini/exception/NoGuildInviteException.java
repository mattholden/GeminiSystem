package com.darkenedsky.gemini.exception;

public class NoGuildInviteException extends GeminiException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8243929472631503123L;

	public NoGuildInviteException() {
		super(NO_GUILD_INVITE, "The guild you tried to join is invite-only.");
		
	}

}

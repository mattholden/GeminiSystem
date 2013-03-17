package com.darkenedsky.gemini.exception;

public class AlreadyGuildMemberException extends GeminiException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6852683298730999986L;

	public AlreadyGuildMemberException() {
		super(ALREADY_GUILD_MEMBER, "You are already a member of a guild. To join a new one, leave your old one first.");	
	} 
	

}

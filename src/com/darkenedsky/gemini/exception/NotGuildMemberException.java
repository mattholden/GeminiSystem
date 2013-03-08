package com.darkenedsky.gemini.exception;

public class NotGuildMemberException extends RuleException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5144164895374408697L;

	public NotGuildMemberException(long guildid) {
		super(NOT_GUILD_MEMBER, "You are not a member of the specified guild.");
		details.put("guildid", guildid);
		
	} 

	public NotGuildMemberException() {
		super(NOT_GUILD_MEMBER, "You are not a member of a guild.");
	} 
}

package com.darkenedsky.gemini.exception;

public class NotGuildMemberException extends RuleException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5144164895374408697L;

	public NotGuildMemberException(long guildid, long guild2) {
		super(NOT_GUILD_MEMBER, "You are not a member of the same guild as the specified player.");
		details.put("guildid", guildid);
		details.put("playerguildid", guild2);
		
	} 

	public NotGuildMemberException(long guildid) {
		super(NOT_GUILD_MEMBER, "You are not a member of the specified guild.");
		details.put("guildid", guildid);
		
	} 

	public NotGuildMemberException() {
		super(NOT_GUILD_MEMBER, "You are not a member of a guild.");
	} 
}

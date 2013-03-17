package com.darkenedsky.gemini.exception;

public class GuildPermissionException extends RuleException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1359394430711516110L;

	public GuildPermissionException() {
		super(GUILD_PERMISSION, "This action cannot be performed by guild members of your rank.");
	}

}

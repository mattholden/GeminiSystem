package com.darkenedsky.gemini.exception;

public class GuildIsOpenEnrollmentException extends RuleException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5730658181591182284L;

	public GuildIsOpenEnrollmentException() {
		super(GUILD_IS_OPEN_ENROLLMENT, "This guild does not require invitations to join.");		
	} 
	

}

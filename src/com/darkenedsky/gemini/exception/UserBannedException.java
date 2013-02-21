package com.darkenedsky.gemini.exception;

import java.sql.Timestamp;

/** Thrown when a user tries to log in, and is rejected because he is banned. */
public class UserBannedException extends GeminiException { 
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -3111778441381601391L;
	
	/** Construct the exception.
	 * 
	 * @param pid The player ID who is banned
	 * @param when The time when the ban will be lifted, in epoch time
	 * @param why The reason for the ban.
	 */
	public UserBannedException(long pid, Timestamp when, String why) { 
		super(1007, "User is banned!");
		details.put("playerid", pid);
		details.put("until", when.getTime());
		details.put("reason", why);
	}
	
}

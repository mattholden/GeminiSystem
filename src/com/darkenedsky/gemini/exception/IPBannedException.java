package com.darkenedsky.gemini.exception;

import java.sql.Timestamp;

/** Thrown when a user tries to log in, and is rejected because he is banned. */
public class IPBannedException extends GeminiException { 
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -5077396400614735672L;

	/** Construct the exception.
	 * 
	 * @param ip The IP that is banned
	 * @param when The time when the ban will be lifted, in epoch time
	 * @param why The reason for the ban.
	 */
	public IPBannedException(String ip, Timestamp when, String why) { 
		super(ExceptionCodes.IP_BANNED, "IP is banned!");
		details.put("ip", ip);
		details.put("until", when.getTime());
		details.put("reason", why);
	}
	
}

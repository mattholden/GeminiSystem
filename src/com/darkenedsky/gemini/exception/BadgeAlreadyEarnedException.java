package com.darkenedsky.gemini.exception;

public class BadgeAlreadyEarnedException extends RuleException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1425659774252828657L;

	public BadgeAlreadyEarnedException(int badge) {
		super(BADGE_ALREADY_EARNED, "You have already earned this badge!");
		details.put("badgeid", badge);
	}

}

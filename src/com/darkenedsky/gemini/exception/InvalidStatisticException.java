package com.darkenedsky.gemini.exception;

public class InvalidStatisticException extends GeminiException implements ExceptionCodes {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5802846610508175374L;

	public InvalidStatisticException(String stat, Long obj, Long playerID) {
		super(INVALID_STATISTIC, "No statistic found named " + stat);
		details.put("stat", stat);
		if (obj != null)
			details.put("objectid", obj);
		if (playerID != null)
			details.put("playerid", playerID);
	}

}

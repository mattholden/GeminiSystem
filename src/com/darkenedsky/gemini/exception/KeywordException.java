package com.darkenedsky.gemini.exception;

public class KeywordException extends RuleException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1203509534446292038L;

	public KeywordException(long cardid, String key, boolean required) {
		super(KEYWORD, "This card must " + (required ? "" : "not") + " have the keyword or tag " + key + ".");
		this.details.put("cardid", cardid);
		this.details.put("required", required);
		this.details.put("keyword", key);
	} 
	

}

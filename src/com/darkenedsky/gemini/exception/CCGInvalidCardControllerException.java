package com.darkenedsky.gemini.exception;

public class CCGInvalidCardControllerException extends RuleException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7979064730350577727L;

	public CCGInvalidCardControllerException(long cardid) {
		super(CCG_INVALID_CARD_CONTROLLER, "Invalid card controller!");
		details.put("cardid", cardid);
	}

}

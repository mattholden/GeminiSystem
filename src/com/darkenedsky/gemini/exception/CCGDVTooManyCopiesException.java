package com.darkenedsky.gemini.exception;


public class CCGDVTooManyCopiesException extends CCGDeckValidationException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 823731062173986646L;

	public CCGDVTooManyCopiesException(int card) {
		super(CCG_DV_TOO_MANY_COPIES, true, "You added more copies of this card to the deck than the maximum allowed.");
		details.put("definitionid", card);
	}

}

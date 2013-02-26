package com.darkenedsky.gemini.exception;


public class CCGDVUnpurchasedCardException extends CCGDeckValidationException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 823731062173986646L;

	public CCGDVUnpurchasedCardException(int card) {
		super(CCG_DV_UNPURCHASED_CARD, true, "You attempted to use a card in a deck which you do not own.");
		details.put("definitionid", card);
	}

}

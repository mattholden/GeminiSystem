package com.darkenedsky.gemini.exception;

public class CCGDVTotalDeckSizeException extends CCGDeckValidationException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4356170412350362169L;

	public CCGDVTotalDeckSizeException(int min, int max, int amt) {
		super(CCG_DV_TOTAL_DECK_SIZE, true, "Decks must be between " + min + " and " + max + " cards. Yours contains " + amt + ".");
		details.put("min", min);
		details.put("max", max);
		details.put("decksize", amt);
	}

}

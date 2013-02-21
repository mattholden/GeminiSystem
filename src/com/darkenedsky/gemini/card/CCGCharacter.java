package com.darkenedsky.gemini.card;

import com.darkenedsky.gemini.Player;

public class CCGCharacter<TCard extends CCGCard> extends CardCharacter<TCard> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3319409072145168480L;

	private CardDeck<TCard> deck = null;
	private long deckID = -1;
	
	public CCGCharacter(Player p) {
		super(p);	
	}

	void setDeck(CardDeck<TCard> dek, long dekid) { 
		deckID = dekid;
		deck = dek;
	}

	public CardDeck<TCard> getDeck() {
		return deck;
	}

	public long getDeckID() {
		return deckID;
	}
	
}

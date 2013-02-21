package com.darkenedsky.gemini.card;

import com.darkenedsky.gemini.GameCharacter;
import com.darkenedsky.gemini.Player;

public class CardCharacter<TCard extends Card> extends GameCharacter {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -9023523757348937805L;

	/** Cards in your hand. */
	private CardDeck<TCard> hand = new CardDeck<TCard>();
	
	public CardCharacter(Player p) {
		super(p);	
	}

	public CardDeck<TCard> getHand() {
		return hand;
	}
	
}

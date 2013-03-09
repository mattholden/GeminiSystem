package com.darkenedsky.gemini.card;
import com.darkenedsky.gemini.GameCharacter;
import com.darkenedsky.gemini.GameObject;
import com.darkenedsky.gemini.Player;

public class CardCharacter<TCard extends Card> extends GameCharacter {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -9023523757348937805L;
	
	/** Cards in your hand. */
	private CardDeck<TCard> hand;
	
	public CardCharacter(Player p) {
		super(p);	
	}

	@Override
	public void onGameStart() throws Exception { 
		super.onGameStart();
		hand = new CardDeck<TCard>(getGame().getNextObjectID(), getPlayer().getPlayerID(), CardDeck.HAND);
	}
	
	public CardDeck<TCard> getHand() {
		return hand;
	}
	
}

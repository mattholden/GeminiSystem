package com.darkenedsky.gemini.card;
import com.darkenedsky.gemini.GameCharacter;
import com.darkenedsky.gemini.Player;
import com.darkenedsky.gemini.exception.TooManyCardsInHandException;
import com.darkenedsky.gemini.stats.Statistic;

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

	public static final String MAX_CARDS_IN_HAND = "max_cards_in_hand";
	
	@Override
	public void onGameStart() throws Exception { 
		super.onGameStart();
		hand = new CardDeck<TCard>(getGame().getNextObjectID(), getPlayer().getPlayerID(), CardDeck.HAND);
		statistics.put(MAX_CARDS_IN_HAND, new Statistic("Max Cards In Hand", 0, Statistic.ALWAYS_HIDDEN));
		
	}
	
	public CardDeck<TCard> getHand() {
		return hand;
	}
	

	@Override
	public void validateYourTurnEnd() throws Exception { 
		super.validateYourTurnEnd();
		if (hand.size() > getStatValue(MAX_CARDS_IN_HAND))
			throw new TooManyCardsInHandException(hand.size() - getStatValue(MAX_CARDS_IN_HAND));
	}
	
}

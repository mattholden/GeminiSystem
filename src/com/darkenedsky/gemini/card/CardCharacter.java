package com.darkenedsky.gemini.card;

import java.util.Vector;

import com.darkenedsky.gemini.GameCharacter;
import com.darkenedsky.gemini.Message;
import com.darkenedsky.gemini.Player;
import com.darkenedsky.gemini.exception.InsufficientResourcesException;
import com.darkenedsky.gemini.exception.TooManyCardsInHandException;
import com.darkenedsky.gemini.stats.Statistic;

public class CardCharacter<TCard extends Card> extends GameCharacter {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3319409072145168480L;

	/** Cards in your hand. */
	private CardDeck<TCard> hand;
	
	private CardDeck<TCard> deck = null;
	private long deckID = -1;
	private Resources resourcePool;
	
	public static final String MAX_CARDS_IN_HAND = "max_cards_in_hand";
	
	public Resources getResourcePool() { return resourcePool; }
	
	public void validateResources(Resources res) { 
		if (!resourcePool.isAtLeast(res))
			throw new InsufficientResourcesException();
	}
	
	public void gainResources(Resources res) throws Exception { 
		resourcePool.add(res);
		((CardGame<?,?>)getGame()).observeGainResources(res, getPlayer());
	}
	
	public void spendResources(Resources res) throws Exception { 
		resourcePool.add(res);
		((CardGame<?,?>)getGame()).observeSpendResources(res, getPlayer());
	}
	
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

	public CardCharacter(Player p) {
		super(p);	
	}

	public void setDeck(CardDeck<TCard> dek, long dekid) throws Exception { 
		deckID = dekid;
		deck = dek;
	}

	public CardDeck<TCard> getDeck() {
		return deck;
	}

	public long getDeckID() {
		return deckID;
	}
	
	public void shuffleDeck() throws Exception { 
		getDeck().shuffle();
	}
	
	@Override
	public Message serialize(Player p) { 
		Message m = super.serialize(p);
		if (p.getPlayerID() == getPlayer().getPlayerID())
			m.put("deckid", deckID);
		return m;
	}
	
		/** Draw the top 'num' cards, removing it from the deck.
	 *  NOTE: Will NOT throw an exception if you don't have enough cards;
	 *  you'll need to check and make sure that the returned list matches the expected size
	 *  if this matters (for things like losing the game if you run out of cards).
	 *  
	 *  @param num Number of cards to draw from the top of the deck. 
	 *  @return the top 'num' cards.
	 */
	@SuppressWarnings("unchecked")
	public Vector<TCard> draw(int num) throws Exception { 
		
		int todraw = num;
		if (deck.getCards().size() < num) { 
			todraw = deck.getCards().size();
		}
		
		Vector<TCard> drawn = new Vector<TCard>();
		
		// Avoid ConcurrentModification - add the cards, then remove them
		for (int i = 0; i < todraw; i++) { 
			drawn.add(deck.getCards().get(i));
		}
		
		// Now remove them
		for (TCard draw : drawn) { 
			deck.getCards().remove(draw);
		}
		
		// Listener. Do this after removal because the listener might need to check the size of the hand, etc.
		for (TCard draw : drawn) { 
			draw.onDrawn();
			((CardGame<TCard,?>)getGame()).observeDraw(draw, getPlayer());
		}
		return drawn;
	}

}

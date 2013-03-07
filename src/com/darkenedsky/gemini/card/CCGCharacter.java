package com.darkenedsky.gemini.card;

import java.util.Vector;

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
			((CCGGame<TCard,? extends CCGCharacter<TCard>>)this.getGame()).observeDraw(draw, getPlayer());
		}
		return drawn;
	}

}

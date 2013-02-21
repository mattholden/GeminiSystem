package com.darkenedsky.gemini.card;

import java.util.Collections;
import java.util.Vector;

import com.darkenedsky.gemini.Dice;
import com.darkenedsky.gemini.exception.InvalidObjectException;

public class CardDeck<TCard extends Card> extends Vector<TCard> {
		
	/**
	 * 
	 */
	private static final long serialVersionUID = 1012037472580622295L;

	/** Shuffle the deck. */
	public void shuffle() { 
		Collections.shuffle(this, Dice.rand);
	}
	
	/** Get a card by its ID
	 *  @param id the card id
	 *  @return the card
	 *  @throws InvalidObjectException if we couldn't find it
	 */
	public TCard getCard(long id) { 
		
		for (TCard card : this) { 
			if (card.getObjectID() == id) { 
				return card;
			}
		}
		throw new InvalidObjectException(id);
	}
	
	/** Draw the top 'num' cards, removing it from the deck.
	 *  NOTE: Will NOT throw an exception if you don't have enough cards;
	 *  you'll need to check and make sure that the returned list matches the expected size
	 *  if this matters (for things like losing the game if you run out of cards).
	 *  
	 *  @param num Number of cards to draw from the top of the deck. 
	 *  @return the top 'num' cards.
	 */
	public CardDeck<TCard> draw(int num) throws Exception { 
		
		int todraw = num;
		if (size() < num) { 
			todraw = size();
		}
		
		CardDeck<TCard> drawn = new CardDeck<TCard>();
		
		// Avoid ConcurrentModification - add the cards, then remove them
		for (int i = 0; i < todraw; i++) { 
			drawn.add(get(i));
		}
		
		// Now remove them
		for (TCard draw : drawn) { 
			this.remove(draw);
		}
		
		// Listener. Do this after removal because the listener might need to check the size of the hand, etc.
		for (TCard draw : drawn) { 
			draw.onDrawn();
		}
		return drawn;
	}
	
	

}

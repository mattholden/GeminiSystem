package com.darkenedsky.gemini.card;

import java.util.Collections;
import java.util.Vector;

import com.darkenedsky.gemini.Dice;
import com.darkenedsky.gemini.exception.InvalidObjectException;

public class CardDeck<TCard extends Card> implements CardContainer<TCard> {
		
	/**
	 * 
	 */
	private static final long serialVersionUID = 1012037472580622295L;

	/** store the cards */
	private Vector<TCard> cards = new Vector<TCard>();
	
	private int deckType;
	
	private Long playerID;
	
	@Override
	public int getContainerType() {
		return deckType;
	}

	public Long getPlayerID() {
		return playerID;
	}

	public static final int
		DECK = 0, HAND = 1, DISCARD = 2;
	
	/** Shuffle the deck. */
	public void shuffle() { 
		Collections.shuffle(cards, Dice.rand);
	}
	
	@Override
	public boolean isContainerInPlay() { 
		return false;
	}
	
	private Long objectID;
	
	public CardDeck(Long objID, Long player, int type) { 
		objectID = objID;
		playerID = player;
		deckType = type;
	}
	
	/** Get a card by its ID
	 *  @param id the card id
	 *  @return the card
	 *  @throws InvalidObjectException if we couldn't find it
	 */
	public TCard getCard(long id) { 
		
		for (TCard card : cards) { 
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
	public Vector<TCard> draw(int num) throws Exception { 
		
		int todraw = num;
		if (cards.size() < num) { 
			todraw = cards.size();
		}
		
		Vector<TCard> drawn = new Vector<TCard>();
		
		// Avoid ConcurrentModification - add the cards, then remove them
		for (int i = 0; i < todraw; i++) { 
			drawn.add(cards.get(i));
		}
		
		// Now remove them
		for (TCard draw : drawn) { 
			cards.remove(draw);
		}
		
		// Listener. Do this after removal because the listener might need to check the size of the hand, etc.
		for (TCard draw : drawn) { 
			draw.onDrawn();
		}
		return drawn;
	}


	@Override
	public Vector<TCard> getCards() {
		return cards;
	}

	@Override
	public Long getObjectID() {
		return objectID;
	}

	@Override
	public void onCardAdded(TCard card) throws Exception {
	}

	@Override
	public void onCardRemoved(TCard card) throws Exception {
	}

	@Override
	public void validateAddCard(TCard card) throws Exception {
	}

	@Override
	public void validateRemoveCard(TCard card) throws Exception {
	}

	@Override
	public void remove(TCard card) throws Exception {
		validateRemoveCard(card);
		cards.remove(card);
		onCardRemoved(card);
	}

	@Override
	public void remove(long cardid) throws Exception {
		remove(getCard(cardid));		
	}

	@Override
	public void add(TCard card) throws Exception{
		validateAddCard(card);
		cards.add(card);
		card.setController(this.playerID);
		onCardAdded(card);		
	}
	
	

}

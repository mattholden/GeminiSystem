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
	
	public int size() { return cards.size(); }
	
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

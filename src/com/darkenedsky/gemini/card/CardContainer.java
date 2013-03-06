package com.darkenedsky.gemini.card;

import java.util.Vector;

public interface CardContainer<TCard extends Card> {

	public void add(TCard card) throws Exception;
	public void remove(TCard card) throws Exception;
	public void remove(long cardid) throws Exception;
	public TCard getCard(long cardid);
	public Vector<TCard> getCards();
	
	public Long getObjectID();

	public void onCardAdded(TCard card) throws Exception;
	public void onCardRemoved(TCard card) throws Exception;
	public void validateAddCard(TCard card) throws Exception;
	public void validateRemoveCard(TCard card) throws Exception;

	public int getContainerType() throws Exception;
	public boolean isContainerInPlay() throws Exception;
	
}

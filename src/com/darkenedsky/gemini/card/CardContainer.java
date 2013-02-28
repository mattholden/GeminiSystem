package com.darkenedsky.gemini.card;

import java.util.Vector;

public interface CardContainer<TCard extends Card> {

	public void addCard(TCard card);
	public void removeCard(TCard card);
	public void removeCard(long cardid);
	public TCard getCard(long cardid);
	public Vector<TCard> getCards();
	public int getDefinitionID();
	public Long getObjectID();

	public void onCardAdded(TCard card) throws Exception;
	public void onCardRemoved(TCard card) throws Exception;
	public void validateAddCard(TCard card) throws Exception;
	public void validateRemoveCard(TCard card) throws Exception;

}

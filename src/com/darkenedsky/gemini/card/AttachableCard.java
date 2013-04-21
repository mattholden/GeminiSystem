package com.darkenedsky.gemini.card;

/** Used for cards that can be attached to other cards. */
public interface AttachableCard {
	

	public void onAttach(Card attachedTo) throws Exception;
	public void onDetach(Card attachedTo) throws Exception;
	
	public void validateAttach(Card attachedTo) throws Exception;
	public void validateDetach(Card attachedTo) throws Exception;
	
}

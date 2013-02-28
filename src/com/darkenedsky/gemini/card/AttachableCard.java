package com.darkenedsky.gemini.card;

/** Used for cards that can be attached to other cards. */
public interface AttachableCard<TCard extends Card> {
	

	public void onAttach(TCard attachedTo) throws Exception;
	public void onDetach(TCard attachedTo) throws Exception;
	
	public void validateAttach(TCard attachedTo) throws Exception;
	public void validateDetach(TCard attachedTo) throws Exception;
	
}

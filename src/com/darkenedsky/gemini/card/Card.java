package com.darkenedsky.gemini.card;
import com.darkenedsky.gemini.AdvancedGameObject;


public class Card extends AdvancedGameObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2552819634147335453L;

	public Card(int defID, Long objID, String englishName) {
		super(defID, objID, englishName);		
	}

	public void onDrawn() throws Exception { /* Deliberately blank */}
	
	
}

package com.darkenedsky.gemini.card;
import com.darkenedsky.gemini.GameObjectWithStats;


public class Card extends GameObjectWithStats {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2552819634147335453L;

	public Card(int defID, Long objID, String englishName) {
		super(defID, objID, englishName);		
	}

	public void onDrawn() throws Exception { /* Deliberately blank */}
	public void onDiscarded() throws Exception { /* Deliberately blank */ }
	
	
}

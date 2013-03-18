package com.darkenedsky.gemini.card;
import java.util.Vector;
import com.darkenedsky.gemini.GameObject;
import com.darkenedsky.gemini.Library;

public class CardSet<TCard extends Card> extends GameObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3686690028782002815L;
	
	private Vector<TCard> cards = new Vector<TCard>();
	
	public CardSet(int defID, Long objID, String englishName) {
		super(defID, objID, englishName);		
	}

	protected void add(TCard card) { 
		cards.add(card);
	}
	
	public Vector<TCard> getCards() { return cards; }
	
	public void addToLibrary(Library lib) { 
		lib.getSection("sets").add(this);
		
		for (TCard c : cards) { 
			lib.getSection("cards").add(c);
		}
	}
	

}

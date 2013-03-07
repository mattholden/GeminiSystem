package com.darkenedsky.gemini.card;
import java.util.concurrent.ConcurrentHashMap;

import com.darkenedsky.gemini.GameObjectWithStats;
import com.darkenedsky.gemini.Languages;
import com.darkenedsky.gemini.Message;
import com.darkenedsky.gemini.Player;


public class Card extends GameObjectWithStats {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2552819634147335453L;

	/** The ID of the card type */
	private int cardType;
	private boolean tapped = false;
	protected Long owner;
	protected Long controller;
	
	protected String artist = null;
	protected ConcurrentHashMap<String, String> rulesText = new ConcurrentHashMap<String, String>();
	protected ConcurrentHashMap<String, String> flavorText = new ConcurrentHashMap<String, String>();

	public Card(int defID, int type, Long objID, Long ownerID, String englishName) {
		super(defID, objID, englishName);		
		owner = ownerID;
		controller = ownerID;
		cardType = type;
	}
	
	public Card(int defID, int type, Long objID, String englishName) {
		super(defID, objID, englishName);		
		cardType = type;
	}

	public void onDrawn() throws Exception { /* Deliberately blank */}
	public void onDiscarded() throws Exception { /* Deliberately blank */ }
	public void onTurnStart() throws Exception { /* blank */ } 
	public void onTurnEnd() throws Exception { /* blank */ } 
	public void onControllerTurnStart() throws Exception { /* blank */ } 
	public void onControllerTurnEnd() throws Exception { /* blank */ } 
	public void validateTap(Card tap) throws Exception { /* blank */ }
	public void observeTap(Card tap) throws Exception { /* blank */ }
	public void validateUntap(Card tap) throws Exception { /* blank */ }
	public void observeUntap(Card tap) throws Exception { /* blank */ }
	
	public int getType() { 
		return cardType;
	}
	
	public boolean isTapped() { 
		return tapped;
	}

	public void onTapped() throws Exception { } 
	public void onUntapped() throws Exception { } 
	
	@SuppressWarnings("unchecked")
	public void setTapped(boolean tap) throws Exception { 
		
		if (tap == this.tapped) return;		
		
		if (tap) { 
				((CardGame<Card,?>)game).validateTap(this);		
				tapped = tap;		
				onTapped();
				((CardGame<Card,?>)game).observeTap(this);
		}
		else { 
				((CardGame<Card,?>)game).validateUntap(this);		
				tapped = tap;		
				onUntapped();
				((CardGame<Card,?>)game).observeUntap(this);
		}
	}
	
	public Long getController() {
		return controller;
	}


	public void setController(Long controller) {
		this.controller = controller;
	}


	public Long getOwner() {
		return owner;
	}
	
	protected CardGame<? extends Card,?> game;
	
	public CardGame<? extends Card,?> getGame() { return game; } 
	public void setGame(CardGame<? extends Card,?> g) { game = g; } 
	
	
	@Override
	public Message serialize(Player p) { 
		Message m = super.serialize(p);
		String lang = (p == null) ? Languages.ENGLISH : p.getLanguage();
		String rules = this.rulesText.get(lang);
		String flavor = this.flavorText.get(lang);
		m.put("card_type", cardType);
		m.put("rules", rules);
		m.put("flavor", flavor);
		m.put("controller", controller);
		m.put("owner", owner);
		m.put("tapped", tapped);

		if (p == null && artist != null) { 
			m.put("artist", artist);
		}
		return m;
	}

}

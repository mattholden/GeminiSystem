package com.darkenedsky.gemini.card;

import java.util.Map;

import com.darkenedsky.gemini.Message;
import com.darkenedsky.gemini.Player;
import com.darkenedsky.gemini.stats.Statistic;
import com.darkenedsky.gemini.stats.Tag;

public abstract class CCGCard extends Card {

	private Long owner;
	private Long controller;
	private int maxInDeck = 4;
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 5790615972027062731L;

	public CCGCard(String englishName) { 
		this(null, null, englishName);
	}
	
	public CCGCard(Long objID, Long owner, String englishName) {
		super(objID, englishName);		
		this.owner = owner;
		controller = owner;
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
	
	@Override
	/** Serialize the object. 
	 *  
	 *  @param player the player being serialized for
	 *  @return the Message object.
	 */
	public Message serialize(Player p) { 
		Message m = super.serialize(p);
		
		if (controller != null)
			m.put("controller", controller);
		if (owner != null)
			m.put("owner", owner);
		if (owner == null && controller == null) { 
			m.put("maxindeck", maxInDeck);
		}
		
		// The base AdvancedGameObject creates the list and stores all non-secret stats/tags; 
		// just append the secret ones if you're the controller.
		for (Map.Entry<String, Statistic> stat : statistics.entrySet()) {
	
			if (!stat.getValue().isSecret() || (p != null && p.getPlayerID() != this.controller)) continue;
			Message s = stat.getValue().serialize(p);
			s.put("stat", stat.getKey());
			m.addToList("stats", s, p);
		}
		
		for (Tag t : tags.values()) { 
			if (!t.isSecret() || (p != null && p.getPlayerID() != this.controller)) continue;
			m.addToList("tags", t, p);
		}
		return m;
	}

	public void setMaxInDeck(int maxInDeck) {
		this.maxInDeck = maxInDeck;
	}

	public int getMaxInDeck() {
		return maxInDeck;
	}
	
}

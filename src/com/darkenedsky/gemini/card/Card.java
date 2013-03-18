package com.darkenedsky.gemini.card;
import java.util.HashMap;
import com.darkenedsky.gemini.GameObjectWithStats;
import com.darkenedsky.gemini.Message;
import com.darkenedsky.gemini.Player;
import com.darkenedsky.gemini.stats.Bonus;
import com.darkenedsky.gemini.stats.Statistic;


public abstract class Card extends GameObjectWithStats {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2552819634147335453L;

	/** The ID of the card type */
	private int cardType;
	private boolean tapped = false;
	protected Long owner;
	protected Long controller;
	protected boolean psuedoCard = false;
	
	protected String artist = null;
	protected HashMap<String, String> rulesText = new HashMap<String, String>();
	protected HashMap<String, String> flavorText = new HashMap<String, String>();

	public static final String UNTAPS_AT_TURN_START = "untaps_at_turn_start", COUNTERS = "counters";
	
	public Card(int defID, int type, Long objID, Long ownerID, String englishName) {
		super(defID, objID, englishName);		
		owner = ownerID;
		controller = ownerID;
		cardType = type;
		statistics.put(UNTAPS_AT_TURN_START, new Statistic("untaps at turn start", 1, Statistic.ALWAYS_HIDDEN));
		statistics.put(COUNTERS, new Statistic("Counters", 0, Statistic.HIDDEN_IF_ZERO));
	}
	
	public Card(int defID, int type, Long objID, String englishName) {
		super(defID, objID, englishName);		
		cardType = type;
	}

	public boolean isFriendlyTo(long playerid) { 
		return (playerid == controller);
	}
	
	public void onDrawn() throws Exception { /* Deliberately blank */}
	public void onDiscarded() throws Exception { /* Deliberately blank */ }
	
	public void onTurnStart() throws Exception { expireBonuses(Bonus.START_OF_NEXT_TURN); } 
	public void onTurnEnd() throws Exception { expireBonuses(Bonus.END_OF_THIS_TURN);  } 
	public void onControllerTurnStart() throws Exception { 
		if (getStatValue(UNTAPS_AT_TURN_START) > 0)
			setTapped(false);
		expireBonuses(Bonus.START_OF_YOUR_NEXT_TURN); 
	} 
	public void onControllerTurnEnd() throws Exception { expireBonuses(Bonus.END_OF_YOUR_NEXT_TURN); } 
		
	public void validateTap(Card tap) throws Exception { /* blank */ }
	public void observeTap(Card tap) throws Exception { /* blank */ }
	public void validateUntap(Card tap) throws Exception { /* blank */ }
	public void observeUntap(Card tap) throws Exception { /* blank */ }
	
	public boolean isPsuedoCard() {
		return psuedoCard;
	}

	public void setPsuedoCard(boolean psuedoCard) {
		this.psuedoCard = psuedoCard;
	}

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
		m.put("card_type", cardType);
		m.put("rules", localize(rulesText, p));
		m.put("flavor", localize(flavorText, p));
		m.put("controller", controller);
		m.put("owner", owner);
		m.put("tapped", tapped);

		if (p == null && artist != null) { 
			m.put("artist", artist);
		}
		return m;
	}


}

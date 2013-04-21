package com.darkenedsky.gemini.card;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import com.darkenedsky.gemini.ActionList;
import com.darkenedsky.gemini.GameObjectWithStats;
import com.darkenedsky.gemini.Message;
import com.darkenedsky.gemini.Player;
import com.darkenedsky.gemini.exception.InvalidActionException;
import com.darkenedsky.gemini.stats.Bonus;
import com.darkenedsky.gemini.stats.Statistic;

public abstract class Card extends GameObjectWithStats implements CardObserver {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2552819634147335453L;

	public static final String UNTAPS_AT_TURN_START = "untaps_at_turn_start", COUNTERS = "counters";
	protected String artist = null;
	/** The ID of the card type */
	private int cardType;

	protected Long controller;
	protected HashMap<String, String> flavorText = new HashMap<String, String>();
	private CardGame<?, ?> game;
	private int maxInDeck = 4;
	protected Long owner;

	protected boolean psuedoCard = false;
	protected HashMap<String, String> rulesText = new HashMap<String, String>();
	protected Vector<Special> specials = new Vector<Special>();

	protected boolean tapped = false;

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

	protected void addSpecial(Special spec) {
		specials.add(spec);
	}

	public Long getController() {
		return controller;
	}

	public CardGame<?, ?> getGame() {
		return game;
	}

	public int getMaxInDeck() {
		return maxInDeck;
	}

	public Long getOwner() {
		return owner;
	}

	public Special getSpecial(int idx) {
		if (idx < 0 || idx >= specials.size())
			throw new InvalidActionException(ActionList.CCG_SPECIAL0 + idx);
		return specials.get(idx);
	}

	public int getSpecialCount() {
		return specials.size();
	}

	public Special getSpecialForAction(int action) {
		return getSpecial(action - ActionList.CCG_SPECIAL0);
	}

	public int getType() {
		return cardType;
	}

	public boolean isFriendlyTo(long playerid) {
		return (playerid == controller);
	}

	public boolean isPsuedoCard() {
		return psuedoCard;
	}

	public boolean isTapped() {
		return tapped;
	}

	@Override
	public void observeDraw(Card drawn, Player p) throws Exception {
	}

	@Override
	public void observeGainResources(Resources amt, Player p) throws Exception {
	}

	@Override
	public void observeSpecial(Card card, Message m, Player p) throws Exception {
	}

	@Override
	public void observeSpendResources(Resources amt, Player p) throws Exception {
	}

	@Override
	public void observeTap(Card tap) throws Exception {
	}

	@Override
	public void observeUntap(Card tap) throws Exception {
	}

	public void onControllerTurnEnd() throws Exception {
		expireBonuses(Bonus.END_OF_YOUR_NEXT_TURN);
	}

	public void onControllerTurnEnd(Player p) throws Exception {
	}

	public void onControllerTurnStart() throws Exception {
	}

	public void onControllerTurnStart(Player p) throws Exception {
	}

	public void onDiscard() {
	}

	public void onDiscarded() throws Exception { /* Deliberately blank */
	}

	public void onDraw() {
	}

	public void onDrawn() throws Exception { /* Deliberately blank */
	}

	public void onTap() {
	}

	public void onTapped() throws Exception {
	}

	public void onTurnEnd() throws Exception {
		expireBonuses(Bonus.END_OF_THIS_TURN);
	}

	public void onTurnStart() throws Exception {
		expireBonuses(Bonus.START_OF_NEXT_TURN);
		for (Special s : specials)
			s.clearUsesThisTurn();
	}

	public void onUntap() {
	}

	public void onUntapped() throws Exception {
	}

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

		if (owner == null && controller == null) {
			m.put("maxindeck", maxInDeck);
		}

		// The base AdvancedGameObject creates the list and stores all
		// non-secret stats/tags;
		// just append the secret ones if you're the controller.
		for (Map.Entry<String, Statistic> stat : statistics.entrySet()) {

			if (!stat.getValue().isSecret() || (p != null && p.getPlayerID() != this.controller))
				continue;
			Message s = stat.getValue().serialize(p);
			s.put("stat", stat.getKey());
			m.addToList("stats", s, p);
		}

		return m;
	}

	public void setController(Long controller) {
		this.controller = controller;
	}

	public void setGame(CardGame<?, ?> game) {
		this.game = game;
	}

	public void setMaxInDeck(int maxInDeck) {
		this.maxInDeck = maxInDeck;
	}

	public void setPsuedoCard(boolean psuedoCard) {
		this.psuedoCard = psuedoCard;
	}

	public void setTapped(boolean tap) throws Exception {

		// nothing to see
		if (tap == this.tapped) {
			return;
		}

		tapped = tap;
		if (tap) {
			game.validateTap(this);
			game.observeTap(this);
			onTap();
		} else {
			game.validateUntap(this);
			game.observeUntap(this);
			onUntap();
		}
	}

	@Override
	public void validateSpecial(Card special, Message m, Player p) throws Exception {
	}

	@Override
	public void validateTap(Card tap) throws Exception {
	}

	@Override
	public void validateUntap(Card tap) throws Exception {
	}

}

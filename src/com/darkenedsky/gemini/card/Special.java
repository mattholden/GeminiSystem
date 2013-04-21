package com.darkenedsky.gemini.card;

import com.darkenedsky.gemini.Message;
import com.darkenedsky.gemini.MessageSerializable;
import com.darkenedsky.gemini.Player;
import com.darkenedsky.gemini.handler.EliminatedValidator;
import com.darkenedsky.gemini.handler.GameStateValidator;
import com.darkenedsky.gemini.handler.Handler;
import com.darkenedsky.gemini.handler.HandlerValidator;
import com.darkenedsky.gemini.handler.PlayerInGameValidator;
import com.darkenedsky.gemini.handler.SessionValidator;
import com.darkenedsky.gemini.handler.TurnStateValidator;

public abstract class Special extends Handler implements MessageSerializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8866467647354959893L;

	private Resources cost;
	private Card parentCard;
	private boolean taps;
	private int usesThisTurn = 0, usesThisGame = 0;

	public Special(Card card, Resources theCost, boolean taps, HandlerValidator... vals) {
		this(card, theCost, taps, -1, -1, vals);
	}

	public Special(Card card, Resources theCost, boolean taps, int game, int turn, HandlerValidator... vals) {
		this(card, theCost, taps, game, turn, ANY, MAIN_PHASE, vals);
	}

	public Special(Card card, Resources theCost, boolean taps, int game, int turn, int turnState, Integer gameState, HandlerValidator... vals) {
		parentCard = card;
		cost = theCost;
		this.taps = taps;

		addValidator(new SessionValidator());
		addValidator(new PlayerInGameValidator());
		addValidator(new EliminatedValidator());
		addValidator(new TurnStateValidator(turnState));
		addValidator(new GameStateValidator(gameState));

		setUseLimits(game, turn);

		for (HandlerValidator v : vals)
			addValidator(v);

		addValidator(new ResourceValidator(theCost));
	}

	public void clearUsesThisTurn() {
		usesThisTurn = 0;

	}

	public Card getCard() {
		return parentCard;
	}

	public Resources getCost() {
		return cost;
	}

	public int getUsesThisGame() {
		return usesThisGame;
	}

	public int getUsesThisTurn() {
		return usesThisTurn;
	}

	public abstract void onSpecial(Message m, Player p) throws Exception;

	@Override
	public void processMessage(Message m, Player p) throws Exception {
		usesThisTurn++;
		usesThisGame++;

		onSpecial(m, p);
		getCard().getGame().observeSpecial(getCard(), m, p);
	}

	@Override
	public Message serialize(Player p) {
		Message m = new Message();
		m.put("cost", cost, p);
		m.put("taps", taps);
		return m;
	}

	public void setCost(Resources cost) {
		this.cost = cost;
	}

	public void setUseLimits(int turn, int game) {
		if (turn != -1 || game != -1)
			addValidator(new UsedSpecialCountValidator(this, game, turn));
	}

	public boolean tapsCard() {
		return taps;
	}

}

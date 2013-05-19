package com.darkenedsky.gemini.card;

import com.darkenedsky.gemini.Message;
import com.darkenedsky.gemini.Player;
import com.darkenedsky.gemini.exception.UsesPerGameException;
import com.darkenedsky.gemini.exception.UsesPerTurnException;
import com.darkenedsky.gemini.handler.AbstractGameHandlerValidator;

public class UsedSpecialCountValidator extends AbstractGameHandlerValidator {

	private int maxGame = -1, maxTurn = -1;
	private Special special;

	public UsedSpecialCountValidator(Special spec, int maxPerGame, int maxPerTurn) {
		maxGame = maxPerGame;
		maxTurn = maxPerTurn;
		this.special = spec;
	}

	@Override
	public void validate(Message m, Player p) throws Exception {

		if (maxGame != -1 && special.getUsesThisGame() >= maxGame)
			throw new UsesPerGameException();
		if (maxTurn != -1 && special.getUsesThisTurn() >= maxTurn)
			throw new UsesPerTurnException();
	}
}

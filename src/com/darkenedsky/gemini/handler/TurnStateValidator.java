package com.darkenedsky.gemini.handler;

import com.darkenedsky.gemini.Message;
import com.darkenedsky.gemini.Player;
import com.darkenedsky.gemini.exception.NotOnThisTurnException;

public class TurnStateValidator extends AbstractGameHandlerValidator {

	public int state = ANY;

	public TurnStateValidator() {
		this(YOU);
	}

	public TurnStateValidator(int theState) {
		state = theState;
	}

	@Override
	public void validate(Message m, Player p) throws Exception {
		// check if you're the correct player
		boolean isCurrent = (getGame().getCurrentPlayer() == p.getPlayerID());

		if (isCurrent && state == NOT_YOU) {
			throw new NotOnThisTurnException(true);
		} else if (!isCurrent && state == YOU) {
			throw new NotOnThisTurnException(false);
		}
	}
}

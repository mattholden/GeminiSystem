package com.darkenedsky.gemini.handler;

import com.darkenedsky.gemini.Game;
import com.darkenedsky.gemini.Message;
import com.darkenedsky.gemini.Player;
import com.darkenedsky.gemini.exception.NotOnThisTurnException;

public class TurnStateValidator extends AbstractGameHandlerValidator<Game<?>> {

	public int state = ANY;
	
	public TurnStateValidator(int theState) { 
		state = theState;
	}
	
	public TurnStateValidator() {
		this(YOU);
	}

	@Override
	public void validate(Message m, Player p) throws Exception {
		// check if you're the correct player
		boolean isCurrent = (getGame().getCurrentPlayer() == p.getPlayerID());	
		
		if (isCurrent && state == NOT_YOU) { 
			throw new NotOnThisTurnException(true);
		}
		else if (!isCurrent && state == YOU) { 
			throw new NotOnThisTurnException(false);
		}	
	}
}

package com.darkenedsky.gemini.handler;

import com.darkenedsky.gemini.Game;
import com.darkenedsky.gemini.Message;
import com.darkenedsky.gemini.Player;
import com.darkenedsky.gemini.exception.PlayerEliminatedException;

public class EliminatedValidator extends AbstractGameHandlerValidator<Game<?>> {

	private Boolean isEliminated;
	
	public EliminatedValidator(Boolean elim) { 
		isEliminated = elim;
	}
	
	public EliminatedValidator() { this(REQUIRES_NO); }
	
	@Override
	public void validate(Message m, Player p) throws Exception {
		// not eliminated
		if (isEliminated != null && isEliminated != getGame().getCharacter(p.getPlayerID()).isEliminated()) { 
			throw new PlayerEliminatedException();
		}
					
	}
	

}

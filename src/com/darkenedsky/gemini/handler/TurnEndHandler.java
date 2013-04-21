package com.darkenedsky.gemini.handler;

import com.darkenedsky.gemini.ActionList;
import com.darkenedsky.gemini.Game;
import com.darkenedsky.gemini.Message;
import com.darkenedsky.gemini.Player;

public class TurnEndHandler extends GameHandler<Game<?>> {

	public TurnEndHandler(Game<?> g) {
		super(g);	
		addValidator(new PlayerInGameValidator());
		addValidator(new SessionValidator());
		addValidator(new NotGameStateValidator(ActionList.CREATE_GAME));
		addValidator(new TurnStateValidator());
		addValidator(new NotBlockedValidator());
	}

	@Override
	public void processMessage(Message e, Player p) throws Exception {				
		getGame().startNewTurn();
	}

}

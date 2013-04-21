package com.darkenedsky.gemini.handler;

import com.darkenedsky.gemini.ActionList;
import com.darkenedsky.gemini.Game;
import com.darkenedsky.gemini.Message;
import com.darkenedsky.gemini.Player;

public class DropPlayerHandler extends GameHandler<Game<?>> {

	public DropPlayerHandler(Game<?> g) {
		super(g);	
		addValidator(new PlayerInGameValidator());
		addValidator(new SessionValidator());
		addValidator(new GameStateValidator(ActionList.CREATE_GAME));
	}

	@Override
	public void processMessage(Message e, Player p) throws Exception {
		getGame().dropPlayer(p);
	}

}

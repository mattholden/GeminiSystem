package com.darkenedsky.gemini.handler;

import com.darkenedsky.gemini.ActionList;
import com.darkenedsky.gemini.Message;
import com.darkenedsky.gemini.Player;
import com.darkenedsky.gemini.exception.NotEveryoneIsReadyException;

public class StartGameHandler extends GameHandler {

	public StartGameHandler() {

		addValidator(new PlayerInGameValidator());
		addValidator(new SessionValidator());
		addValidator(new GameStateValidator(ActionList.CREATE_GAME));
	}

	@Override
	public void processMessage(Message e, Player p) throws Exception {
		getGame().startGame();
		getGame().startNewTurn();
	}

	@Override
	public void validate(Message e, Player p) throws Exception {
		super.validate(e, p);

		if (!getGame().isEveryoneReady())
			throw new NotEveryoneIsReadyException(getGame().getGameID());
	}

}

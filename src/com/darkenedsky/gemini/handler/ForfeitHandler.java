package com.darkenedsky.gemini.handler;

import com.darkenedsky.gemini.ActionList;
import com.darkenedsky.gemini.Game;
import com.darkenedsky.gemini.Message;
import com.darkenedsky.gemini.Player;

public class ForfeitHandler extends GameHandler<Game<?>> {

	public ForfeitHandler(Game<?> g) {
		super(g);	
		addValidator(new PlayerInGameValidator());
		addValidator(new SessionValidator());
		addValidator(new NotGameStateValidator(ActionList.CREATE_GAME));
		addValidator(new TurnStateValidator());
		addValidator(new NotBlockedValidator());
	}

	@Override
	public void processMessage(Message e, Player p) throws Exception {
		getGame().getCharacter(p.getPlayerID()).setEliminated(true);
		Message reply = new Message(FORFEIT, getGame().getGameID(), p.getPlayerID());
		for (Player play : getGame().getPlayers()) { play.pushOutgoingMessage(reply); }				
		getGame().startNewTurn();
	}

}

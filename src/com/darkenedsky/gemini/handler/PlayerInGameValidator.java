package com.darkenedsky.gemini.handler;

import com.darkenedsky.gemini.Message;
import com.darkenedsky.gemini.Player;
import com.darkenedsky.gemini.exception.GamePlayerException;

public class PlayerInGameValidator extends AbstractGameHandlerValidator {

	private Boolean requiresInGame;

	public PlayerInGameValidator() {
		this(REQUIRES_YES);
	}

	public PlayerInGameValidator(Boolean in) {
		requiresInGame = in;
	}

	@Override
	public void validate(Message m, Player p) throws Exception {

		if (requiresInGame == null)
			return;

		boolean foundPlayer = false;
		for (Player ply : getGame().getPlayers()) {
			if (ply.getPlayerID() == p.getPlayerID()) {
				if (requiresInGame == REQUIRES_NO) {
					throw new GamePlayerException(requiresInGame);
				}
				foundPlayer = true;
			}
		}
		if (!foundPlayer && requiresInGame == REQUIRES_YES) {
			throw new GamePlayerException(requiresInGame);
		}
	}

}

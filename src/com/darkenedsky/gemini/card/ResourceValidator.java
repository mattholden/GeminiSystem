package com.darkenedsky.gemini.card;

import com.darkenedsky.gemini.Message;
import com.darkenedsky.gemini.Player;
import com.darkenedsky.gemini.handler.AbstractGameHandlerValidator;

public class ResourceValidator extends AbstractGameHandlerValidator {

	private Resources cost;
	private CardGame<?, ?> game;

	public ResourceValidator(Resources theCost) {
		cost = theCost;
	}

	@Override
	public void validate(Message m, Player p) throws Exception {
		CardCharacter<?> chr = game.getCharacter(p.getPlayerID());
		chr.validateResources(cost);
	}

}

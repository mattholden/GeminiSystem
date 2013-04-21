package com.darkenedsky.gemini.handler;

import com.darkenedsky.gemini.Game;
import com.darkenedsky.gemini.Message;
import com.darkenedsky.gemini.Player;
import com.darkenedsky.gemini.exception.BlockedException;

public class NotBlockedValidator extends AbstractGameHandlerValidator<Game<?>> {

	@Override
	public void validate(Message m, Player p) throws Exception {
		if (this.getGame().getBlockingHandler(p.getPlayerID()) != null)
			throw new BlockedException();
		
	}

}

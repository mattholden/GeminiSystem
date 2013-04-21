package com.darkenedsky.gemini.handler;

import com.darkenedsky.gemini.Game;
import com.darkenedsky.gemini.Message;
import com.darkenedsky.gemini.Player;

public class ExecuteBlockingHandler<TGame extends Game<?>> extends GameHandler<TGame> {

	public ExecuteBlockingHandler(TGame g) {
		super(g);
	}

	@Override
	public void validate(Message m, Player p) throws Exception { 
		Handler b = getGame().getBlockingHandler(p.getPlayerID());
		if (b == null) return;
		b.validate(m, p);
	}
	
	@Override
	public void processMessage(Message m, Player p) throws Exception { 
		Handler b = getGame().getBlockingHandler(p.getPlayerID());
		if (b == null) return;
		b.processMessage(m, p);
		
		getGame().setBlockingHandler(p,  null);
	}
}

package com.darkenedsky.gemini.handler;

import com.darkenedsky.gemini.Game;

public interface GameHandlerValidator<TGame extends Game<?>> extends HandlerValidator {

	public TGame getGame();
	
	public void setGame(TGame g);
	
}

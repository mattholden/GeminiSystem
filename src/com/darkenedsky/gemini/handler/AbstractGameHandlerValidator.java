package com.darkenedsky.gemini.handler;

import com.darkenedsky.gemini.Game;

public abstract class AbstractGameHandlerValidator<TGame extends Game<?>> implements GameHandlerValidator<TGame> {

	private TGame game;
	
	@Override
	public TGame getGame() {
		return game;
	}

	@Override
	public void setGame(TGame g) {
		game = g;
	} 
	

}

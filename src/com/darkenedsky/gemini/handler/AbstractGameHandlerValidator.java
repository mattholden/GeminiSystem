package com.darkenedsky.gemini.handler;

import com.darkenedsky.gemini.Game;

public abstract class AbstractGameHandlerValidator extends AbstractHandlerValidator {

	public Game<?> getGame() {
		return (Game<?>) getHandler().getService();
	}

}

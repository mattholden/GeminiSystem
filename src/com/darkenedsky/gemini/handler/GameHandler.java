package com.darkenedsky.gemini.handler;

import com.darkenedsky.gemini.Game;

public abstract class GameHandler extends Handler {

	public void addStandardValidators() {
		addStandardValidators(YOU, MAIN_PHASE);
	}

	public void addStandardValidators(int turn, Integer... phases) {
		addValidator(new SessionValidator());
		addValidator(new PlayerInGameValidator());
		addValidator(new EliminatedValidator());
		addValidator(new TurnStateValidator(turn));
		addValidator(new GameStateValidator(phases));
	}

	public Game<?> getGame() {
		return (Game<?>) getService();
	}

}

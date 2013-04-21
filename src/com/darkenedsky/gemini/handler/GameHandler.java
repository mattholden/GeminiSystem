package com.darkenedsky.gemini.handler;

import java.util.Vector;

import com.darkenedsky.gemini.Game;

public abstract class GameHandler<TGame extends Game<?>> extends Handler { 
	
	protected TGame game;
	
	public TGame getGame() { return game; }
	
	public void setGame(TGame g) {
		game = g;
	}

	public GameHandler(TGame g) { 
		setGame(g);
	}
	
	public GameHandler(TGame g, Vector<GameHandlerValidator<TGame>> vals) { 
		this(g);
		
		for (GameHandlerValidator<TGame> v : vals)
			addValidator(v);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void addValidator(HandlerValidator val) {		
		super.addValidator(val);
		if (val instanceof GameHandlerValidator<?>) {		
			((GameHandlerValidator<TGame>)val).setGame(game);
		}
	}
	
	public void addStandardValidators() { addStandardValidators(YOU, MAIN_PHASE); }
	
	public void addStandardValidators(int turn, Integer... phases) { 
		addValidator(new SessionValidator());
		addValidator(new PlayerInGameValidator());
		addValidator(new EliminatedValidator());
		addValidator(new TurnStateValidator(turn));
		addValidator(new GameStateValidator(phases));
	}
	
}

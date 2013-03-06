package com.darkenedsky.gemini;
import java.util.Vector;

public abstract class Handler implements MessageProcessor, HandlerValidator { 
		
	protected Vector<HandlerValidator> validators = new Vector<HandlerValidator>();
	protected GeneralHandlerValidator generalHandlerValidator = new GeneralHandlerValidator();
	
	protected Game<? extends GameCharacter> game;
	
	public Handler() { /* Deliberately empty */ }
	
	public Handler(Game<? extends GameCharacter> gm, HandlerValidator... vals) { 
		game = gm;
		
		generalHandlerValidator.setGame(game);
		validators.add(generalHandlerValidator);
		
		for (HandlerValidator hv : vals) { 
			validators.add(hv);
		}
	}
	
	public void addValidator(HandlerValidator val) { 
		validators.add(val);
	}
	
	public GeneralHandlerValidator getGeneralValidator() { 
		return generalHandlerValidator;
	}
	
	public Game<? extends GameCharacter> getGame() { return game; }
	
	@Override
	public void setGame(Game<?> g) {
		game = g;
	}

	public void validate(Message msg, Player p) throws Exception { 
		for (HandlerValidator val : validators) { 
			val.validate(msg, p);
		}
	}

	
}

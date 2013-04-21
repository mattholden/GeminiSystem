package com.darkenedsky.gemini.card;
import com.darkenedsky.gemini.Message;
import com.darkenedsky.gemini.Player;
import com.darkenedsky.gemini.handler.GameHandlerValidator;

public class ResourceValidator implements GameHandlerValidator<CardGame<?,?>> {

	private CardGame<?,?> game;
	private Resources cost;
	
	public ResourceValidator(Resources theCost) {
		cost = theCost;
	}

	@Override
	public void validate(Message m, Player p) throws Exception {
		CardCharacter<?> chr = game.getCharacter(p.getPlayerID());
		chr.validateResources(cost);
	}

	@Override
	public CardGame<?,?> getGame() {
		return game;
	}

	@Override
	public void setGame(CardGame<?,?> game) {
		this.game = game;
	} 
	

}

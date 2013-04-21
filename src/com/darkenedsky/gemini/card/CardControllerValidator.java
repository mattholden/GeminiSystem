package com.darkenedsky.gemini.card;
import com.darkenedsky.gemini.Message;
import com.darkenedsky.gemini.Player;
import com.darkenedsky.gemini.exception.InvalidCardControllerException;

public class CardControllerValidator implements CardValidator {

	public static final Integer CONTROLLER_YOU = 0, CONTROLLER_HOSTILE = 1, CONTROLLER_FRIENDLY = 2, CONTROLLER_ANY = null;

	private Integer controllerType;
	
	public CardControllerValidator() { this(CONTROLLER_YOU); }
	
	public CardControllerValidator(int cont) { 
		controllerType = cont;
	}

	@Override
	public void validate(Card card, CardContainer<?> container, CardGame<?,?> game, Message m, Player p) throws Exception {
		
		if (controllerType != null && card.getController() != null && p != null) { 
			if (controllerType == CONTROLLER_YOU && p.getPlayerID() != card.getController()) { 
				throw new InvalidCardControllerException(card.getObjectID());				
			}
		}
	}
	
	
}

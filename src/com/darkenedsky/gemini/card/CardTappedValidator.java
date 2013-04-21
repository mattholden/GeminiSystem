package com.darkenedsky.gemini.card;
import com.darkenedsky.gemini.Message;
import com.darkenedsky.gemini.Player;
import com.darkenedsky.gemini.exception.InvalidCardTapStateException;

public class CardTappedValidator implements CardValidator {

	private Boolean tapped;
	
	public CardTappedValidator() { this(false); }
	
	public CardTappedValidator(Boolean t) { 
		tapped = t;
	}

	@Override
	public void validate(Card card, CardContainer<?> container, CardGame<?,?> game, Message m, Player p) throws Exception {

		if (tapped == null) return;
		if (card.isTapped() != tapped)
			throw new InvalidCardTapStateException(card.getObjectID());
		
	}
	
	
}

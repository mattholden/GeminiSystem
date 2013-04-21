package com.darkenedsky.gemini.card;

import java.util.Vector;
import com.darkenedsky.gemini.Message;
import com.darkenedsky.gemini.Player;
import com.darkenedsky.gemini.exception.InvalidCardTypeException;

public class CardTypeValidator implements CardValidator {

	private Vector<Integer> cardType = new Vector<Integer>(); 
	
	public CardTypeValidator(Integer... types) { 
		for (Integer i : types) cardType.add(i);
	}

	@Override
	public void validate(Card card, CardContainer<?> container, CardGame<?,?> game, Message m, Player p) throws Exception {
	
		if (!cardType.isEmpty()) { 
			boolean found = false;
			for (Integer i : cardType) { 
				if (card.getType() == i) { 
					found = true;
					break;
				}
			}
			if (!found) 
				throw new InvalidCardTypeException(card.getObjectID());
		}
	}
	
	
}

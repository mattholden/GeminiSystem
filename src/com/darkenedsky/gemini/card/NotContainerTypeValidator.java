package com.darkenedsky.gemini.card;
import java.util.Vector;
import com.darkenedsky.gemini.Message;
import com.darkenedsky.gemini.Player;
import com.darkenedsky.gemini.exception.InvalidCardContainerException;


public class NotContainerTypeValidator implements CardValidator {

	private Vector<Integer> containerType = new Vector<Integer>(); 
	
	public NotContainerTypeValidator(Integer... types) { 
		for (Integer i : types) containerType.add(i);
	}

	@Override
	public void validate(Card card, CardContainer<?> container, CardGame<?,?> game, Message m, Player p) throws Exception {
	
		if (!containerType.isEmpty()) { 
			boolean found = false;
			for (Integer i : containerType) { 
				if (container.getContainerType() == i) { 
					found = true;
					break;
				}
			}
			if (found) 
				throw new InvalidCardContainerException(card.getObjectID());
		}
	}
	
	
}

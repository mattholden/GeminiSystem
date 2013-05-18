package com.darkenedsky.gemini.card;

import com.darkenedsky.gemini.Message;
import com.darkenedsky.gemini.Player;
import com.darkenedsky.gemini.exception.InvalidCardTargetException;

public class MaxStatValueValidator implements CardValidator {

	private int max;
	private String stat;

	public MaxStatValueValidator(String theStat, int maxVal) {
		stat = theStat;
		max = maxVal;
	}

	@Override
	public void validate(Card card, CardContainer<?> container, CardGame<?, ?> game, Message m, Player p) throws Exception {
		if (card.getStat(stat).getValueWithBonuses() > max)
			throw new InvalidCardTargetException(card.getObjectID());
	}

}

package com.darkenedsky.gemini.card;

import com.darkenedsky.gemini.Message;
import com.darkenedsky.gemini.Player;
import com.darkenedsky.gemini.card.Card;
import com.darkenedsky.gemini.card.CardContainer;
import com.darkenedsky.gemini.card.CardGame;
import com.darkenedsky.gemini.card.CardValidator;
import com.darkenedsky.gemini.exception.InvalidCardTargetException;

public class MinStatValueValidator implements CardValidator {

	private int min;
	private String stat;

	public MinStatValueValidator(String theStat, int minVal) {
		stat = theStat;
		min = minVal;
	}

	@Override
	public void validate(Card card, CardContainer<?> container, CardGame<?, ?> game, Message m, Player p) throws Exception {
		if (card.getStat(stat).getValueWithBonuses() < min)
			throw new InvalidCardTargetException(card.getObjectID());
	}

}

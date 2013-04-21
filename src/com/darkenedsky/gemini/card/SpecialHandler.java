package com.darkenedsky.gemini.card;

import com.darkenedsky.gemini.Message;
import com.darkenedsky.gemini.Player;
import com.darkenedsky.gemini.handler.GameHandler;

public class SpecialHandler extends GameHandler<CardGame<?, ?>> {

	public SpecialHandler(CardGame<?, ?> game) {
		super(game);
	}

	@Override
	public void processMessage(Message m, Player p) throws Exception {

		Card card = getGame().getCard(m.getLong(CardValidator.CARDID));
		card.getSpecialForAction(m.getRequiredInt("action")).processMessage(m, p);

	}

	@Override
	public void validate(Message m, Player p) throws Exception {

		// call all our validators for the basics
		super.validate(m, p);

		// There's very little specific validation we can do that's unique to
		// all card specials...
		Card card = getGame().getCard(m.getLong(CardValidator.CARDID));
		Special special = card.getSpecialForAction(m.getRequiredInt("action"));
		special.validate(m, p);
	}

}

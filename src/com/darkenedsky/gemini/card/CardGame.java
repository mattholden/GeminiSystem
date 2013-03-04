package com.darkenedsky.gemini.card;

import com.darkenedsky.gemini.Game;
import com.darkenedsky.gemini.Message;
import com.darkenedsky.gemini.Player;

public abstract class CardGame<TCard extends Card, TChar extends CardCharacter<TCard>> extends Game<TChar> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7449571310224635559L;

	public CardGame(long gid, Message e, Player p, Class<TChar> tcharClazz)
			throws Exception {
		super(gid, e, p, tcharClazz);
	}
	

}

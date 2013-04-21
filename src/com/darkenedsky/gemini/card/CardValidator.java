package com.darkenedsky.gemini.card;
import com.darkenedsky.gemini.Message;
import com.darkenedsky.gemini.Player;

public interface CardValidator {

	public static final int HAND = CardDeck.HAND, DISCARD = CardDeck.DISCARD, DECK = CardDeck.DECK;

	public static final String CARDID = "cardid", TARGET_CARDID = "targetcardid";
	
	public void validate(Card card, CardContainer<?> container, CardGame<?,?> game, Message m, Player p) throws Exception;
	
}

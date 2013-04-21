package com.darkenedsky.gemini.card;
import com.darkenedsky.gemini.Message;
import com.darkenedsky.gemini.Player;
import com.darkenedsky.gemini.exception.KeywordException;

public class CardKeywordValidator implements CardValidator {

	private String keyword;
	private boolean has;
	
	public CardKeywordValidator(String key, boolean has) {
		keyword = key;
		this.has = has;
	}
	
	public CardKeywordValidator(String key) { 
		this(key, true);
	}

	@Override
	public void validate(Card card, CardContainer<?> container, CardGame<?,?> game, Message m, Player p) throws Exception {

		if (card.hasKeywordOrTag(keyword) != has)
			throw new KeywordException(card.getObjectID(), keyword, has);
		
	}
	
	
}

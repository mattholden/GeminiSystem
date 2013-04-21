package com.darkenedsky.gemini.card;
import java.util.Vector;
import com.darkenedsky.gemini.Message;
import com.darkenedsky.gemini.Player;
import com.darkenedsky.gemini.handler.AbstractGameHandlerValidator;

public class CardValidationSet extends AbstractGameHandlerValidator<CardGame<?,?>> {


	public static final String CARDID = "cardid", TARGET_CARDID = "targetcardid";
	
	protected String cardIdField = CARDID;
	private Vector<CardValidator> cardValidators = new Vector<CardValidator>();
	
	public void addCardValidator(CardValidator val) { 
		cardValidators.add(val);
	}
	
	public CardValidationSet() { }
	
	public CardValidationSet(String field) { 
		cardIdField = field;
	}

	public void setCardIdField(String cardIdField) {
		this.cardIdField = cardIdField;
	}

	public void validate(Message m, Player p) throws Exception { 
		
		long cardid = m.getLong(cardIdField);
		Card card = ((CardGame<?,?>)getGame()).getCard(cardid);
		CardContainer<?> container =  ((CardGame<?,?>)getGame()).findCard(cardid);
		
		for (CardValidator v : this.cardValidators) { 
			v.validate(card, container, getGame(), m, p);
		}
		
	}

}

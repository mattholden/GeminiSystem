package com.darkenedsky.gemini.card;

import java.util.Vector;

import com.darkenedsky.gemini.Game;
import com.darkenedsky.gemini.HandlerValidator;
import com.darkenedsky.gemini.Message;
import com.darkenedsky.gemini.Player;
import com.darkenedsky.gemini.exception.InvalidCardContainerException;
import com.darkenedsky.gemini.exception.InvalidCardControllerException;
import com.darkenedsky.gemini.exception.InvalidCardTapStateException;
import com.darkenedsky.gemini.exception.InvalidCardTypeException;

public class CardHandlerValidator<TCard extends Card> implements HandlerValidator {

	protected String cardIdField = CARDID;
	private Vector<Integer> cardType = new Vector<Integer>(); 
	private Boolean cardIsTapped = REQUIRES_NO;
	private Integer controllerType = CONTROLLER_YOU;
	private Integer[] containerTypes; 
	private CardGame<TCard,?> game;
	
	public static final int IN_PLAY = 0, HAND = CardDeck.HAND, DISCARD = CardDeck.DISCARD, DECK = CardDeck.DECK;
	
	public static final int CONTROLLER_YOU = 0, CONTROLLER_HOSTILE = 1, CONTROLLER_FRIENDLY = 2;
	
	public static final String CARDID = "cardid", TARGET_CARDID = "targetcardid";
	
	public CardHandlerValidator(Integer type, Integer controller, Boolean tapped, String field, Integer... containers) { 
		
		if (type != null)
			cardType.add(type);
		
		controllerType = controller;
		cardIsTapped = tapped;
		cardIdField = field;
		containerTypes = containers;
	}
	
	public void addCardType(int type) { 
		cardType.add(type);
	}
	
	public CardHandlerValidator(Integer type, Integer controller) { 
		this(type, controller, null, "cardid");
	}
	

	public void setCardIdField(String cardIdField) {
		this.cardIdField = cardIdField;
	}

	public void setCardIsTapped(Boolean cardIsTapped) {
		this.cardIsTapped = cardIsTapped;
	}

	public void setControllerType(Integer controllerType) {
		this.controllerType = controllerType;
	}

	public void setContainerTypes(Integer[] containerTypes) {
		this.containerTypes = containerTypes;
	}

	public void setGame(CardGame<TCard, ?> game) {
		this.game = game;
	}

	public void validate(Message m, Player p) throws Exception { 
		
		long cardid = m.getLong(cardIdField);
		Card card = game.getCard(cardid);
		CardContainer<TCard> container = game.findCard(cardid);
		
		if (!cardType.isEmpty()) { 
			boolean found = false;
			for (Integer i : cardType) { 
				if (card.getType() == i) { 
					found = true;
					break;
				}
			}
			if (!found) 
				throw new InvalidCardTypeException(cardid);
		}
			
		
		if (cardIsTapped != null && cardIsTapped != card.isTapped()) 
			throw new InvalidCardTapStateException(cardid);
		
		if (controllerType != null && card.getController() != null && p != null) { 
			if (controllerType == CONTROLLER_YOU && p.getPlayerID() != card.getController()) { 
				throw new InvalidCardControllerException(cardid);				
			}
		}

		if (containerTypes != null) { 
			boolean contained = false;
			for (Integer i : containerTypes) { 
				if (container.getContainerType() == i) { 
					contained = true; 
					break;
				}
			}
			if (!contained) { 
				throw new InvalidCardContainerException(cardid);
			}
		}
	}

	@SuppressWarnings("unchecked")
	public void setGame(Game<?> game) {
		this.game = (CardGame<TCard,?>)game;
	}

	public Game<?> getGame() {
		return game;
	}
	
}

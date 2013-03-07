package com.darkenedsky.gemini.card;
import java.util.Vector;
import com.darkenedsky.gemini.Game;
import com.darkenedsky.gemini.Message;
import com.darkenedsky.gemini.Player;
import com.darkenedsky.gemini.exception.InvalidCardTapStateException;

public abstract class CardGame<TCard extends Card, TChar extends CardCharacter<TCard>> extends Game<TChar> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7449571310224635559L;

	public CardGame(long gid, Message e, Player p, Class<TChar> tcharClazz)
			throws Exception {
		super(gid, e, p, tcharClazz);
	}
	

	public abstract CardContainer<TCard> findCard(long cardID) throws Exception;
	public abstract TCard getCard(long cardID) throws Exception;
	protected abstract Vector<TCard> getAllCardsInPlay(); 
		
	@Override
	public void onTurnStart() throws Exception {
		super.onTurnStart();
		
		for (TCard card : getAllCardsInPlay()) { 
			card.onTurnStart();
			if (card.getController() == this.getCurrentPlayer()) 
				card.onControllerTurnStart();
		}
	}
	@Override
	public void onTurnEnd() throws Exception {
		super.onTurnEnd();
		
		for (TCard card : getAllCardsInPlay()) { 
			card.onTurnEnd();
			if (card.getController() == this.getCurrentPlayer()) 
				card.onControllerTurnEnd();
		}
	}
	
	public void observeTap(TCard tapping) throws Exception {	
		for (TCard card : getAllCardsInPlay()) { 
			card.observeTap(tapping);
		}
	}

	public void validateTap(TCard tapping) throws Exception { 
		if (tapping.isTapped()) 
			throw new InvalidCardTapStateException(tapping.getObjectID());
		
		for (TCard card : getAllCardsInPlay()) { 
			card.validateTap(tapping);
		}
	}

	public void observeUntap(TCard tapping) throws Exception { 
		for (TCard card : getAllCardsInPlay()) { 
			card.observeUntap(tapping);
		}
	}

	public void validateUntap(TCard tapping) throws Exception { 

		for (TCard card : getAllCardsInPlay()) { 
			card.validateUntap(tapping);
		}
	}

}

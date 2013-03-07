package com.darkenedsky.gemini.card;
import java.util.concurrent.ConcurrentHashMap;
import com.darkenedsky.gemini.Message;
import com.darkenedsky.gemini.Player;
import com.darkenedsky.gemini.exception.CCGDeckValidationException;

public abstract class CCGGame<TCard extends CCGCard, TChar extends CCGCharacter<TCard>>
		extends CardGame<TCard, TChar>  {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4148601915525160132L;

	/** Deck service for validation */
	private transient CCGDeckService<TCard> deckService;
	
	/** Store the deck IDs selected. Used only during the lobby phase. */
	private transient ConcurrentHashMap<Long, Long> deckIDs = new ConcurrentHashMap<Long, Long>();

	public CCGGame(long gid, Message e, Player p, Class<TChar> tcharClazz)
			throws Exception {
		super(gid, e, p, tcharClazz);
	}

	@Override
	protected void setReady(Message m, Player p) throws Exception { 
	
		// can't use if any errors exist
		for (CCGDeckValidationException x : deckService.validateDeck(m.getLong("deckid"), p)) { 
			if (x.isError()) { 
				throw x;
			}
		}
		
		deckIDs.put(p.getPlayerID(), m.getLong("deckid"));		
		super.setReady(m, p);
	}
	
	
	@Override
	protected void onGameStart() throws Exception {
		super.onGameStart();
		
			for (TChar chr : characters) {
			long did = deckIDs.get(chr.getPlayer().getPlayerID());
			chr.setDeck(deckService.spawnDeck(this, chr.getPlayer(), did), did);
		}

	}

	@SuppressWarnings("unchecked")
	public void setDeckService(CCGDeckService<?> deckService2) { 
		deckService = (CCGDeckService<TCard>)deckService2;
	}

	public void observeDraw(TCard drawn, Player p) throws Exception { 
		for (TCard card : getAllCardsInPlay()) { 
			card.observeDraw(drawn, p);
		}
	}

}

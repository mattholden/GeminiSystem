package com.darkenedsky.gemini.card;

import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import com.darkenedsky.gemini.ActionList;
import com.darkenedsky.gemini.Game;
import com.darkenedsky.gemini.Message;
import com.darkenedsky.gemini.Player;
import com.darkenedsky.gemini.exception.CCGDeckValidationException;

public abstract class CardGame<TCard extends Card, TChar extends CardCharacter<TCard>> extends Game<TChar> implements CardObserver {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7452188806918237609L;

	/** Store the deck IDs selected. Used only during the lobby phase. */
	private transient ConcurrentHashMap<Long, Long> deckIDs = new ConcurrentHashMap<Long, Long>();

	public CardGame(long gid, Message e, Player p, Class<TChar> tcharClazz) throws Exception {
		super(gid, e, p, tcharClazz);

		SpecialHandler special = new SpecialHandler(this);
		for (int i = ActionList.CCG_SPECIAL0; i <= ActionList.CCG_SPECIAL9; i++)
			this.handlers.put(i, special);

	}

	public abstract CardContainer<TCard> findCard(long id);

	public abstract Vector<TCard> getAllCardsInPlay();

	public abstract TCard getCard(long id);

	@Override
	public void observeDraw(Card drawn, Player p) throws Exception {
		for (TCard card : getAllCardsInPlay()) {
			card.observeDraw(drawn, p);
		}
	}

	@Override
	public void observeGainResources(Resources amt, Player p) throws Exception {
		for (TCard card : getAllCardsInPlay()) {
			card.observeGainResources(amt, p);
		}
	}

	@Override
	public void observeSpecial(Card card, Message m, Player p) throws Exception {
		for (Card tcard : getAllCardsInPlay()) {
			tcard.observeSpecial(card, m, p);
		}

	}

	@Override
	public void observeSpendResources(Resources amt, Player p) throws Exception {
		for (TCard card : getAllCardsInPlay()) {
			card.observeSpendResources(amt, p);
		}
	}

	@Override
	protected void onGameStart() throws Exception {
		super.onGameStart();

		@SuppressWarnings("unchecked")
		CCGDeckService<TCard> deckService = (CCGDeckService<TCard>) this.getServer().getService(CCGDeckService.class);

		for (TChar chr : characters) {
			long did = deckIDs.get(chr.getPlayer().getPlayerID());
			chr.setDeck(deckService.spawnDeck(this, chr.getPlayer(), did), did);
			for (TCard card : chr.getDeck().getCards())
				card.setGame(this);
		}

	}

	@Override
	public void setReady(Message m, Player p) throws Exception {

		@SuppressWarnings("unchecked")
		CCGDeckService<TCard> deckService = (CCGDeckService<TCard>) this.getServer().getService(CCGDeckService.class);

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
	public void validateSpecial(Card special, Message m, Player p) throws Exception {
		for (TCard card : getAllCardsInPlay()) {
			card.validateSpecial(card, m, p);
		}
	}

}

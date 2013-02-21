package com.darkenedsky.gemini.card;

import java.lang.reflect.Constructor;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.concurrent.ConcurrentHashMap;
import com.darkenedsky.gemini.LibrarySection;
import com.darkenedsky.gemini.Message;
import com.darkenedsky.gemini.Player;

public abstract class CCGGame<TCard extends CCGCard, TChar extends CCGCharacter<TCard>, TPlay extends Player>
		extends CardGame<TCard, TChar, TPlay>  {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4148601915525160132L;

	/** Store the deck IDs selected. Used only during the lobby phase. */
	private ConcurrentHashMap<Long, Long> deckIDs = new ConcurrentHashMap<Long, Long>();

	public CCGGame(long gid, Message e, Player p, Class<TChar> tcharClazz)
			throws Exception {
		super(gid, e, p, tcharClazz);
		
	}

	@Override
	protected void setReady(Message m, Player p) throws Exception { 
		deckIDs.put(p.getPlayerID(), m.getLong("deckid"));
		super.setReady(m, p);
	}
	
	
	@Override
	protected void onGameStart() throws Exception {
		super.onGameStart();
	
		for (TChar chr : characters) {
			long did = deckIDs.get(chr.getPlayer().getPlayerID());
			chr.setDeck(spawnDeck(chr, did), did);
		}

	}

	private CardDeck<TCard> spawnDeck(TChar chr, long deckid) throws Exception {

		PreparedStatement ps1 = this.getService().getJDBC()
				.prepareStatement("select * from ccg_deckcards where deckid = ?;");
		ps1.setLong(1, deckid);
		ResultSet rs1 = null;
		try {
			rs1 = ps1.executeQuery();
			LibrarySection lib = getService().getLibrary().getSection("cards");
			CardDeck<TCard> deck = new CardDeck<TCard>();

			if (rs1.first()) {
				while (true) {
					int defid = rs1.getInt("definitionid");
					int qty = rs1.getInt("qty");

					@SuppressWarnings("unchecked")
					Class<TCard> defClass = (Class<TCard>) lib.get(defid).getClass();
					Constructor<TCard> defCon = defClass.getConstructor(Long.class, Long.class);
					for (int i = 0; i < qty; i++) {
						deck.add(defCon.newInstance(this.getNextObjectID(), chr.getPlayer().getPlayerID()));
					}

					if (rs1.isLast())
						break;
					rs1.next();
				}
			}
			rs1.close();
			deck.shuffle();
			return deck;
		} catch (Exception x) {
			if (rs1 != null)
				rs1.close();
			throw x;
		}
	}
}

package com.darkenedsky.gemini.card;

import java.lang.reflect.Constructor;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import com.darkenedsky.gemini.Library;
import com.darkenedsky.gemini.LibrarySection;
import com.darkenedsky.gemini.Message;
import com.darkenedsky.gemini.Player;
import com.darkenedsky.gemini.exception.CCGDVTooManyCopiesException;
import com.darkenedsky.gemini.exception.CCGDVTotalDeckSizeException;
import com.darkenedsky.gemini.exception.CCGDVUnpurchasedCardException;
import com.darkenedsky.gemini.exception.CCGDeckValidationException;
import com.darkenedsky.gemini.exception.CCGInvalidDeckException;
import com.darkenedsky.gemini.exception.GeminiException;
import com.darkenedsky.gemini.exception.SQLUpdateFailedException;
import com.darkenedsky.gemini.handler.Handler;
import com.darkenedsky.gemini.service.JDBCConnection;
import com.darkenedsky.gemini.service.Service;

public class CCGDeckService<TCard extends Card> extends Service {

	private int minDeckSize = 40, maxDeckSize = 100;

	/** The Service ID */
	private int serviceID;

	public CCGDeckService(int svcid, int minSize, int maxSize) throws Exception {
		serviceID = svcid;
		minDeckSize = minSize;
		maxDeckSize = maxSize;

		addHandler(CCG_GET_VALID_CARDS, new Handler() {
			@Override
			public void processMessage(Message m, Player p) throws Exception {
				getValidCards(m, p);
			}
		});

		addHandler(CCG_GET_DECKS, new Handler() {
			@Override
			public void processMessage(Message m, Player p) throws Exception {
				getDecks(p, p.getPlayerID());
			}
		});

		addHandler(CCG_GET_STARTER_DECKS, new Handler() {
			@Override
			public void processMessage(Message m, Player p) throws Exception {
				getDecks(p, null);
			}
		});

		addHandler(CCG_CREATE_DECK, new Handler() {
			@Override
			public void processMessage(Message m, Player p) throws Exception {
				createDeck(m, p);
			}
		});

		addHandler(CCG_DELETE_DECK, new Handler() {
			@Override
			public void processMessage(Message m, Player p) throws Exception {
				deleteDeck(m, p);
			}
		});

		addHandler(CCG_CLONE_DECK, new Handler() {
			@Override
			public void processMessage(Message m, Player p) throws Exception {
				cloneDeck(m, p);
			}
		});

		addHandler(CCG_EDIT_DECK, new Handler() {
			@Override
			public void processMessage(Message m, Player p) throws Exception {
				editDeck(m, p);
			}
		});

	}

	protected void cloneDeck(Message m, Player p) throws Exception {
		Message reply = new Message(CCG_CLONE_DECK);
		JDBCConnection jdbc = getServer().getJDBC();

		ResultSet set = null;
		try {
			PreparedStatement ps1 = jdbc.prepareStatement("select * from ccg_clone_deck(?,?);");
			ps1.setLong(1, p.getPlayerID());
			ps1.setLong(2, m.getLong("deckid"));
			set = ps1.executeQuery();
			if (set.first()) {
				long deck = set.getLong("ccg_clone_deck");
				reply.put("deckid", deck);
				p.pushOutgoingMessage(reply);
				set.close();
			} else {
				set.close();
				throw new SQLUpdateFailedException();
			}
		} catch (SQLException x) {
			if (set != null)
				set.close();
			throw GeminiException.translateSQLException(x);
		}
	}

	protected void createDeck(Message m, Player p) throws Exception {
		JDBCConnection jdbc = getServer().getJDBC();

		Message reply = new Message(CCG_CREATE_DECK);
		ResultSet set = null;
		try {
			PreparedStatement ps1 = jdbc.prepareStatement("select * from ccg_create_deck(?,?,?);");
			ps1.setLong(1, p.getPlayerID());
			ps1.setInt(2, serviceID);
			ps1.setString(3, m.getString("deckname"));
			set = ps1.executeQuery();
			if (set.first()) {
				long deck = set.getLong("ccg_create_deck");
				reply.put("deckid", deck);
				p.pushOutgoingMessage(reply);
				set.close();
			} else {
				set.close();
				throw new SQLUpdateFailedException();
			}
		} catch (SQLException x) {
			if (set != null)
				set.close();
			throw GeminiException.translateSQLException(x);
		}
	}

	protected void deleteDeck(Message m, Player p) throws Exception {
		JDBCConnection jdbc = getServer().getJDBC();

		try {
			PreparedStatement ps1 = jdbc.prepareStatement("select * from ccg_delete_deck(?,?,?);");

			ps1.setLong(1, p.getPlayerID());
			ps1.setInt(2, serviceID);
			ps1.setLong(3, m.getLong("deckid"));
			int rows = ps1.executeUpdate();
			if (rows == 0) {
				throw new CCGInvalidDeckException(m.getLong("deckid"));
			}
			Message reply = new Message(CCG_DELETE_DECK);
			reply.put("deckid", m.getLong("deckid"));
			p.pushOutgoingMessage(reply);
		} catch (SQLException x) {
			throw GeminiException.translateSQLException(x);
		}

	}

	protected void editDeck(Message m, Player p) throws Exception {
		JDBCConnection jdbc = getServer().getJDBC();
		try {
			long deckid = m.getLong("deckid");
			String name = m.getString("deckname");

			jdbc.setAutoCommit(false);

			List<HashMap<String, Object>> cards = m.getJSONList("cards");
			Message reply = new Message(CCG_EDIT_DECK);
			reply.put("deckid", deckid);
			if (name != null) {
				reply.put("deckname", name);
			}

			if (cards != null && !cards.isEmpty()) {
				reply.addList("cards");
				reply.addList("problems");

				boolean hasErrors = false;
				Vector<CCGDeckValidationException> issues = validate(m, p);
				for (CCGDeckValidationException issue : issues) {
					if (issue.isError()) {
						hasErrors = true;
					}
					reply.addToList("problems", issue, p);
				}
				if (hasErrors) {
					p.pushOutgoingMessage(reply);
					return;
				}

				PreparedStatement ps2 = jdbc.prepareStatement("delete from ccg_deckcards where deckid = ? and deckid in (select deckid from ccg_decks where playerid = ? and serviceid = ?);");
				ps2.setLong(1, deckid);
				ps2.setLong(2, p.getPlayerID());
				ps2.setInt(3, serviceID);
				ps2.executeUpdate();

				for (HashMap<String, Object> card : cards) {
					PreparedStatement psc = jdbc.prepareStatement("insert into ccg_deckcards (deckid, definitionid, qty) values (?,?,?);");
					psc.setLong(1, deckid);
					psc.setLong(2, (Long) card.get("definitionid"));
					psc.setInt(3, (Integer) card.get("qty"));
					psc.executeUpdate();
				}
			}

			if (name != null) {
				PreparedStatement ps1 = jdbc.prepareStatement("update ccg_decks set deckname = ? where deckid = ? and playerid = ? and serviceid = ?;");
				ps1.setString(1, name);
				ps1.setLong(2, deckid);
				ps1.setLong(3, p.getPlayerID());
				ps1.setLong(4, serviceID);
				int rows = ps1.executeUpdate();
				if (rows == 0) {
					throw new SQLUpdateFailedException();
				}
			}

			jdbc.commit();
			jdbc.setAutoCommit(true);
			p.pushOutgoingMessage(reply);
		} catch (SQLException x) {
			jdbc.rollback();
			jdbc.setAutoCommit(true);
			throw GeminiException.translateSQLException(x);
		}
	}

	public final Map<Integer, Integer> getDeckCards(long deckid) throws Exception {
		JDBCConnection jdbc = getServer().getJDBC();

		PreparedStatement ps1 = jdbc.prepareStatement("select * from ccg_deckcards where deckid = ?;");

		ps1.setLong(1, deckid);
		ResultSet rs1 = null;
		try {

			// get a list of all the cards in the deck
			rs1 = ps1.executeQuery();
			HashMap<Integer, Integer> cardCount = new HashMap<Integer, Integer>(20);
			if (rs1.first()) {
				while (true) {
					int defid = rs1.getInt("definitionid");
					int qty = rs1.getInt("qty");
					cardCount.put(defid, qty);

					if (rs1.isLast())
						break;
					rs1.next();
				}
			}
			rs1.close();
			return cardCount;

		} catch (Exception x) {
			if (rs1 != null)
				rs1.close();
			throw x;
		}
	}

	private void getDecks(Player p, Long playerID) throws Exception {
		JDBCConnection jdbc = getServer().getJDBC();
		Library library = getServer().getLibrary();

		PreparedStatement ps1 = jdbc.prepareStatement("select * from ccg_deckcards join ccg_decks on (ccg_decks.deckid = ccg_deckcards.deckid) where ccg_decks.playerid = ? and ccg_decks.serviceid = ? order by ccg_deckcards.deckid;");
		if (playerID == null)
			ps1.setNull(1, Types.BIGINT);
		else
			ps1.setLong(1, playerID);
		ps1.setInt(2, serviceID);

		ResultSet rs1 = null;
		try {
			rs1 = ps1.executeQuery();
			LibrarySection lib = library.getSection("cards");
			Message msg = new Message(CCG_GET_DECKS);
			msg.addList("decks");

			long deckid = -1;
			Message msg2 = null;
			if (rs1.first()) {

				while (true) {

					if (rs1.getLong("deckid") != deckid) {
						if (msg2 != null) {
							msg.addToList("decks", msg2, p);
						}
						deckid = rs1.getLong("deckid");
						msg2 = new Message();
						msg2.put("name", rs1.getString("deckname"));
						msg2.put("deckid", rs1.getLong("deckid"));
						msg2.addList("cards");
					}

					int defid = rs1.getInt("definitionid");
					int qty = rs1.getInt("qty");

					@SuppressWarnings("unchecked")
					Class<TCard> defClass = (Class<TCard>) lib.get(defid).getClass();
					Constructor<TCard> defCon = defClass.getConstructor(Long.class, Long.class);
					TCard card = defCon.newInstance(null, null);
					for (int i = 0; i < qty; i++) {
						msg2.addToList("cards", card.serialize(null));
					}
					if (rs1.isLast()) {
						if (msg2 != null)
							msg.addToList("decks", msg2, p);
						break;
					}
					rs1.next();
				}
			}
			rs1.close();
			p.pushOutgoingMessage(msg);

		} catch (Exception x) {
			if (rs1 != null)
				rs1.close();
			throw x;
		}
	}

	public void getValidCards(Message m, Player p) throws Exception {
		Message msg = new Message(CCG_GET_VALID_CARDS);
		msg.addList("cards");

		for (Map.Entry<Integer, TCard> card : getValidCardsMap(p).entrySet()) {
			msg.addToList("cards", card.getValue().serialize(null));
		}
		p.pushOutgoingMessage(msg);
	}

	protected HashMap<Integer, TCard> getValidCardsMap(Player p) throws Exception {

		JDBCConnection jdbc = getServer().getJDBC();
		Library library = getServer().getLibrary();

		// get a list of all the cards you can access
		HashMap<Integer, TCard> legalCards = new HashMap<Integer, TCard>(200);
		PreparedStatement ps = jdbc.prepareStatement("select * from ccg_get_usable_sets(?,?);");
		ps.setLong(1, p.getPlayerID());
		ps.setInt(2, serviceID);
		ResultSet set = ps.executeQuery();
		if (set.first()) {
			while (true) {
				@SuppressWarnings("unchecked")
				CardSet<TCard> cardSet = (CardSet<TCard>) library.getSection("sets").get(set.getInt("setid"));
				for (TCard card : cardSet.getCards())
					legalCards.put(card.getDefinitionID(), card);
				if (set.isLast())
					break;
				set.next();
			}
		}
		set.close();
		return legalCards;
	}

	@Override
	public void init() {
		Library library = getServer().getLibrary();
		library.addSection("sets");
		library.addSection("cards");

	}

	public CardDeck<TCard> spawnDeck(CardGame<?, ?> g, Player p, long deckid) throws Exception {
		Library library = getServer().getLibrary();

		LibrarySection lib = library.getSection("cards");

		Map<Integer, Integer> cardCount = getDeckCards(deckid);

		// Can't spawn if any errors exist.
		// We might not need this because we validate when you select the deck
		// for the game,
		// but since we don't have any way to prevent the deck from being edited
		// between the time it's
		// selected for the game and the time the game starts, be slow and safe
		// here for now.
		for (CCGDeckValidationException x : validateDeck(cardCount, p)) {
			if (x.isError()) {
				throw x;
			}
		}

		// spawn the cards
		CardDeck<TCard> deck = new CardDeck<TCard>(g.getNextObjectID(), p.getPlayerID(), CardDeck.DECK);
		for (Map.Entry<Integer, Integer> card : cardCount.entrySet()) {
			@SuppressWarnings("unchecked")
			Class<TCard> defClass = (Class<TCard>) lib.get(card.getKey()).getClass();
			Constructor<TCard> defCon = defClass.getConstructor(Long.class, Long.class);
			for (int i = 0; i < card.getValue(); i++) {
				TCard tcard = (defCon.newInstance(g.getNextObjectID(), p.getPlayerID()));
				tcard.setGame(g);
				deck.add(tcard);
			}
		}

		deck.shuffle();
		return deck;

	}

	private final Vector<CCGDeckValidationException> validate(Message m, Player p) throws Exception {

		Map<Integer, Integer> cardCount = new HashMap<Integer, Integer>(20);
		List<HashMap<String, Object>> cards = m.getJSONList("cards");
		for (HashMap<String, Object> card : cards) {
			Integer cardid = (Integer) (card.get("definitionid"));

			Integer count = cardCount.get(cardid);
			if (count == null)
				count = 0;
			Integer q = (Integer) card.get("qty");
			cardCount.put(cardid, count + q);
		}
		return validateDeck(cardCount, p);
	}

	public final Vector<CCGDeckValidationException> validateDeck(long deckid, Player p) throws Exception {
		return validateDeck(getDeckCards(deckid), p);
	}

	public Vector<CCGDeckValidationException> validateDeck(Map<Integer, Integer> cardCount, Player p) throws Exception {

		Vector<CCGDeckValidationException> issues = new Vector<CCGDeckValidationException>();
		int total = 0;

		HashMap<Integer, TCard> legalCards = getValidCardsMap(p);

		// Complain if they have more copies of a particular card in the deck
		// than they should
		for (Map.Entry<Integer, Integer> count : cardCount.entrySet()) {
			TCard card = legalCards.get(count.getKey());
			if (card == null) {
				issues.add(new CCGDVUnpurchasedCardException(count.getKey()));
			}

			if (card.getMaxInDeck() < count.getValue()) {
				issues.add(new CCGDVTooManyCopiesException(count.getKey()));
			}
			total += count.getValue();
		}

		// check total deck size
		if (minDeckSize > total || maxDeckSize < total) {
			issues.add(new CCGDVTotalDeckSizeException(minDeckSize, maxDeckSize, total));
		}

		return issues;
	}

}

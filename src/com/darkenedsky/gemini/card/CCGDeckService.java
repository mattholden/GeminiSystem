package com.darkenedsky.gemini.card;
import java.lang.reflect.Constructor;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;
import java.util.Vector;
import org.json.simple.JSONObject;
import com.darkenedsky.gemini.Handler;
import com.darkenedsky.gemini.Library;
import com.darkenedsky.gemini.LibrarySection;
import com.darkenedsky.gemini.Message;
import com.darkenedsky.gemini.exception.CCGDVUnpurchasedCardException;
import com.darkenedsky.gemini.exception.CCGDeckValidationException;
import com.darkenedsky.gemini.exception.CCGInvalidDeckException;
import com.darkenedsky.gemini.exception.GeminiException;
import com.darkenedsky.gemini.exception.SQLUpdateFailedException;
import com.darkenedsky.gemini.service.JDBCConnection;
import com.darkenedsky.gemini.service.Service;
import com.darkenedsky.gemini.Player;

public class CCGDeckService<TCard extends Card> extends Service {

	/** The Service ID */
	private int serviceID;
	
	/** The game's Library */
	private Library library;
	
	/** JDBC Connection */
	private JDBCConnection jdbc;
	
	public CCGDeckService(int svcid, JDBCConnection jDB, Library lib) throws Exception { 
		serviceID = svcid;
		jdbc = jDB;
		library = lib;
		
		
		
		handlers.put(CCG_GET_DECKS, new Handler(null) { 
			@Override
			public void processMessage(Message m, Player p) throws Exception {
				getDecks(p, p.getPlayerID());
			}
		});
		
		handlers.put(CCG_GET_STARTER_DECKS, new Handler(null) { 
			@Override
			public void processMessage(Message m, Player p) throws Exception {
				getDecks(p, null);
			}
		});
		
		handlers.put(CCG_CREATE_DECK, new Handler(null) { 
			@Override
			public void processMessage(Message m, Player p) throws Exception {
				createDeck(m, p);
			}
		});
		
		handlers.put(CCG_DELETE_DECK, new Handler(null) { 
			@Override
			public void processMessage(Message m, Player p) throws Exception {
				deleteDeck(m, p);
			}
		});
		
		handlers.put(CCG_CLONE_DECK, new Handler(null) { 
			@Override
			public void processMessage(Message m, Player p) throws Exception {
				cloneDeck(m, p);
			}
		});
		
		handlers.put(CCG_EDIT_DECK, new Handler(null) { 
			@Override
			public void processMessage(Message m, Player p) throws Exception {
				editDeck(m, p);
			}
		});
		
		// Load all the sets into the library
		loadSets();
		
	}

	@SuppressWarnings("unchecked")
	private void loadSets() throws Exception { 

		ResultSet set = null;
		try { 
			PreparedStatement ps = jdbc.prepareStatement("select * from card_sets where serviceid = ?;");
			ps.setInt(1, serviceID);
			set = ps.executeQuery();
			if (set.first()) { 
				while (true) {  
					String clazz = set.getString("javaclass");
					Class<CardSet<? extends Card>> setClass = (Class<CardSet<? extends Card>>) Class.forName(clazz);
					Constructor<CardSet<? extends Card>> setCon = setClass.getConstructor();
					CardSet<? extends Card> theSet = (CardSet<? extends Card>) setCon.newInstance();
					theSet.addToLibrary(library);
					
					if (set.isLast()) break;
					set.next();
				}
			}
			set.close();
		}
		catch (Exception x) { 
			if (set != null) { 
				set.close();
			}
			throw x;
		}
	}
	
	public Vector<CCGDeckValidationException> validateDeck(Message m, Player p) throws Exception { 
		Vector<CCGDeckValidationException> issues = new Vector<CCGDeckValidationException>();
		
		// get a list of all the cards you can access
		Vector<Integer> legalCards = new Vector<Integer>(200);
		PreparedStatement ps = jdbc.prepareStatement("select * from ccg_get_usable_cards(?,?);");
		ps.setLong(1, p.getPlayerID());
		ps.setInt(2, serviceID);
		ResultSet set = ps.executeQuery();
		if (set.first()) { 
			while (true) { 
				legalCards.add(set.getInt("cardid"));
				if (set.isLast()) break;
				set.next();
			}
		}
		set.close();
		
		// Now we know what cards the player can use...
		// See if he's trying to use any he can't.
		List<JSONObject> cards = m.getJSONList("cards");
		for (JSONObject card : cards) { 
			Integer cardid = (Integer)(card.get("definitionid"));
			if (!legalCards.contains(cardid)) { 
				issues.add(new CCGDVUnpurchasedCardException(cardid));
			}
		}
				
		return issues;
	} 
	
	protected void editDeck(Message m, Player p) throws Exception {
		try { 
						
			long deckid = m.getLong("deckid");
			String name = m.getString("deckname");		
			
			jdbc.setAutoCommit(false);
			
			List<JSONObject> cards = m.getJSONList("cards");
			Message reply = new Message(CCG_EDIT_DECK);
			reply.put("deckid", deckid);
			if (name != null) { 
				reply.put("deckname", name);
			}
			
			if (cards != null && !cards.isEmpty()) {
				reply.addList("cards");
				reply.addList("problems");
				
				boolean hasErrors = false;
				Vector<CCGDeckValidationException> issues = validateDeck(m, p);
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
				
				for (JSONObject card : cards) { 
					PreparedStatement psc = jdbc.prepareStatement("insert into ccg_deckcards (deckid, definitionid, qty) values (?,?,?);");
					psc.setLong(1, deckid);
					psc.setLong(2, (Long)card.get("definitionid"));
					psc.setInt(3, (Integer)card.get("qty"));
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
		}
		catch (SQLException x) { 
			jdbc.rollback();
			jdbc.setAutoCommit(true);
			throw GeminiException.translateSQLException(x);
		}
	}
	
	protected void cloneDeck(Message m, Player p) throws Exception {
		Message reply = new Message(CCG_CLONE_DECK);
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
			}
			else { 
				set.close();
				throw new SQLUpdateFailedException();
			}
		}
		catch (SQLException x) {
			if (set != null) set.close();
			throw GeminiException.translateSQLException(x);
		}	
	}

	protected void deleteDeck(Message m, Player p) throws Exception {
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
		}
		catch (SQLException x) { 
			throw GeminiException.translateSQLException(x);
		}
		
	}

	protected void createDeck(Message m, Player p) throws Exception {
		
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
			}
			else { 
				set.close();
				throw new SQLUpdateFailedException();
			}
		}
		catch (SQLException x) {
			if (set != null) set.close();
			throw GeminiException.translateSQLException(x);
		}		
	}
	
	private void getDecks(Player p, Long playerID) throws Exception {

	
		PreparedStatement ps1 = 
				jdbc.prepareStatement("select * from ccg_deckcards join ccg_decks on (ccg_decks.deckid = ccg_deckcards.deckid) where ccg_decks.playerid = ? and ccg_decks.serviceid = ?;");
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
			
			if (rs1.first()) {
				Message msg2 = new Message();
				msg2.put("name", rs1.getString("deckname"));
				msg2.put("deckid", rs1.getLong("deckid"));
				msg2.addList("cards");
				
				while (true) {
					int defid = rs1.getInt("definitionid");
					int qty = rs1.getInt("qty");

					@SuppressWarnings("unchecked")
					Class<TCard> defClass = (Class<TCard>) lib.get(defid).getClass();
					Constructor<TCard> defCon = defClass.getConstructor(Long.class, Long.class);
					TCard card = defCon.newInstance(null, null);
					for (int i = 0; i < qty; i++) {
						msg2.addToList("cards", card.serialize(null));
					}
					msg.addToList("decks", msg2);
					if (rs1.isLast())
						break;
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
	
	
}

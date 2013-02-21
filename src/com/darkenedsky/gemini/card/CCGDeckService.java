package com.darkenedsky.gemini.card;
import java.lang.reflect.Constructor;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.darkenedsky.gemini.Handler;
import com.darkenedsky.gemini.Library;
import com.darkenedsky.gemini.LibrarySection;
import com.darkenedsky.gemini.Message;
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
	
	public CCGDeckService(int svcid, JDBCConnection jDB, Library lib) { 
		serviceID = svcid;
		jdbc = jDB;
		library = lib;
		
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
		
		handlers.put(CCG_RENAME_DECK, new Handler(null) { 
			@Override
			public void processMessage(Message m, Player p) throws Exception {
				renameDeck(m, p);
			}
		});
		
		handlers.put(CCG_ADD_TO_DECK, new Handler(null) { 
			@Override
			public void processMessage(Message m, Player p) throws Exception {
				addToDeck(m, p);
			}
		});
		handlers.put(CCG_REMOVE_FROM_DECK, new Handler(null) { 
			@Override
			public void processMessage(Message m, Player p) throws Exception {
				removeFromDeck(m, p);
			}
		});
		
		
	}

	protected void removeFromDeck(Message m, Player p) {
		// TODO Auto-generated method stub
		
	}

	protected void addToDeck(Message m, Player p) {
		// TODO Auto-generated method stub
		
	}

	protected void renameDeck(Message m, Player p) {
		// TODO Auto-generated method stub
		
	}

	protected void cloneDeck(Message m, Player p) {
		// TODO Auto-generated method stub
		
	}

	protected void deleteDeck(Message m, Player p) {
		// TODO Auto-generated method stub
		
	}

	protected void createDeck(Message m, Player p) {
		
		PreparedStatement ps1 = jdbc.prepareStatement("insert into ccg_decks (playerid, serviceid, deckname) values (?,?,'Deck #' || deckid);");
		
	}
	
	private void getDeck(Message m, Player p) throws Exception {

		PreparedStatement ps1 = 
				jdbc.prepareStatement("select * from ccg_deckcards join ccg_decks on (ccg_decks.deckid = ccg_deckcards.deckid) where ccg_decks.deckid = ? and ccg_decks.playerid = ? and ccg_decks.serviceid = ?;");
		ps1.setLong(1, m.getLong("deckid"));
		ps1.setLong(2, p.getPlayerID());
		ps1.setInt(3, serviceID);
		
		ResultSet rs1 = null;
		try {
			rs1 = ps1.executeQuery();
			LibrarySection lib = library.getSection("cards");
			Message msg = new Message(CCG_GET_DECK);
			msg.addList("cards");
			
			if (rs1.first()) {
				msg.put("name", rs1.getString("deckname"));				
				
				while (true) {
					int defid = rs1.getInt("definitionid");
					int qty = rs1.getInt("qty");

					@SuppressWarnings("unchecked")
					Class<TCard> defClass = (Class<TCard>) lib.get(defid).getClass();
					Constructor<TCard> defCon = defClass.getConstructor(Long.class, Long.class);
					TCard card = defCon.newInstance(null, null);
					for (int i = 0; i < qty; i++) {
						msg.addToList("cards", card.serialize(null));
					}

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

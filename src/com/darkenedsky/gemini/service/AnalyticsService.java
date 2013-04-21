package com.darkenedsky.gemini.service;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import com.darkenedsky.gemini.Message;
import com.darkenedsky.gemini.Player;
import com.darkenedsky.gemini.exception.InvalidActionException;
import com.darkenedsky.gemini.exception.InvalidPlayerException;
import com.darkenedsky.gemini.handler.Handler;

public class AnalyticsService extends Service {

	private JDBCConnection jdbc;
	
	public AnalyticsService(final JDBCConnection jDBC) { 
		
		jdbc = jDBC;
		
		handlers.put(GET_ANALYTICS, new Handler() { 
			@Override
			public void processMessage(Message m, Player p) throws Exception { 
				
				doAnalytics(m,p);
			}
		});
	}
	
	private void doAnalytics(Message m, Player p) throws Exception { 
		
		// if you're not an admin, don't even admit the action is there 
		if (!isAdmin(p)) 
			throw new InvalidActionException(m.getInt("action"));
		
		Message reply = new Message(GET_ANALYTICS);
		
		// TODO: actually get some analytics? ;)
		
		p.pushOutgoingMessage(reply);
	}
	
	protected boolean isAdmin(Player p) throws Exception { 
		ResultSet set = null;
		boolean admin = false;
		PreparedStatement ps1 = jdbc.prepareStatement("select admin from playeraccounts where playerid = ?;");
		ps1.setLong(1, p.getPlayerID());
		try { 
			set = ps1.executeQuery();
			if (!set.first()) 
				throw new InvalidPlayerException(p.getPlayerID());
			
			admin = set.getBoolean("admin");
			set.close();
			return admin;
		}
		catch (Exception x) { 
			if (set != null)
				set.close();
			throw x;
		}
	}
	
}

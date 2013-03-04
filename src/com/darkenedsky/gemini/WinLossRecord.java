package com.darkenedsky.gemini;

import java.sql.ResultSet;
import java.sql.SQLException;


public class WinLossRecord implements MessageSerializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7671313591520156295L;
	private int wins,losses,draws,rating;
	private long playerID;
	private int serviceID;
	
	@Override
	public Message serialize(Player p) {
		
		Message m = new Message();
		m.put("wins", wins);
		m.put("losses", losses);
		m.put("draws", draws);
		m.put("rating", rating);
		return m;		
	}

	
	public WinLossRecord(ResultSet set) throws SQLException { 
		wins = set.getInt("wins");
		losses = set.getInt("losses");
		draws = set.getInt("draws");
		rating = set.getInt("rating");
		serviceID = set.getInt("serviceid");
		playerID = set.getLong("playerid");
		
	}


	public int getWins() {
		return wins;
	}


	public int getLosses() {
		return losses;
	}


	public int getDraws() {
		return draws;
	}


	public int getRating() {
		return rating;
	}


	public long getPlayerID() {
		return playerID;
	}


	public int getServiceID() {
		return serviceID;
	}
	
	
}

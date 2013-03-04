package com.darkenedsky.gemini.service;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.darkenedsky.gemini.Message;
import com.darkenedsky.gemini.WinLossRecord;
import com.darkenedsky.gemini.exception.InvalidPlayerException;

/** Class to handle updating the database to store win-loss-draw records for players. */
public class WinLossRecordManager {

	/** JDBC connection to store the data to. */
	private JDBCConnection jdbc;
	
	/** The service ID - the "project" so that multiple "projects" can live in the same DB */
	private int serviceID;
	
	/** Construct the manager 
	 
	 * @param sets The system settings object - used to get the service ID
	 * @param jDBC the JDBC connection
	 */
	public WinLossRecordManager(Message sets, JDBCConnection jDBC) { 
		jdbc = jDBC;
		serviceID = sets.getInt("serviceid");
	}
	
	/** Record that a player won a game. 
	 *   
	 * @param playerid The player ID
	 * @return the updated WinLossRecord
	 * @throws Exception
	 */
	public WinLossRecord win(long playerid) throws Exception { 
		return update(playerid, 1,0,0);
	}

	/** Record that a player lost a game. 
	 *   
	 * @param playerid The player ID
	 * @return the updated WinLossRecord
	 * @throws Exception
	 */
	public WinLossRecord lose(long playerid) throws Exception { 
		return update(playerid, 0,1,0);
	}
	
	/** Record that a player drew a game. 
	 *   
	 * @param playerid The player ID
	 * @return the updated WinLossRecord
	 * @throws Exception
	 */
	public WinLossRecord draw(long playerid) throws Exception { 
		return update(playerid, 0,0,1);
	}
	
	/** Get the win-loss record for a player.  
	 *  @param pid player ID
	 *  @return their win-loss record
	 */
	public WinLossRecord get(long playerid) throws Exception { 
		PreparedStatement ps = jdbc.prepareStatement("select * from winlossrecords where serviceid = ? and playerid = ?;");
		ps.setInt(1, serviceID);
		ps.setLong(2, playerid);
		ResultSet set = null;
		WinLossRecord record = null;
		try { 
			set = ps.executeQuery();
			if (set.first()) { 
				record = new WinLossRecord(set);
			}
			set.close();
			return record;
		}
		catch (Exception x) { 
			if (set != null) 
				set.close();
			throw x;
		}
	}
	
	/** Update the database to reflect newer scores
	 *  
	 * @param playerid the player ID to update
	 * @param w the number of wins to add
	 * @param l the number of losses to add
	 * @param d the number of draws to add
	 * @return the updated win-loss-draw record count
	 * @throws Exception
	 */
	private WinLossRecord update(long playerid, int w, int l, int d) throws Exception { 
		ResultSet set = null;
		try { 
			PreparedStatement ps = jdbc.prepareStatement("select * from set_winlossrecord(?,?,?,?,?);");
			ps.setInt(1, serviceID);
			ps.setLong(2, playerid);
			ps.setInt(3, w);
			ps.setInt(4, l);
			ps.setInt(5, d);
			set = ps.executeQuery();
			WinLossRecord rec = null;
			if (set.first()) {
				rec = new WinLossRecord(set);
			}
			else {
				throw new InvalidPlayerException(playerid);
			}			
			set.close();
			set = null;
			return rec;
		}
		catch (SQLException x) { 
			if (set != null)
				set.close();
			throw x;
		}
		
	}
	
	
}

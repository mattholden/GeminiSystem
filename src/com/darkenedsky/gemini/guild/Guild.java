package com.darkenedsky.gemini.guild;
import java.sql.ResultSet;
import com.darkenedsky.gemini.Message;
import com.darkenedsky.gemini.MessageSerializable;
import com.darkenedsky.gemini.Player;
import com.darkenedsky.gemini.exception.InvalidGuildRankException;

public class Guild implements MessageSerializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5643299279486069347L;
	
	private String name, charter, website;
	private long founder, guildID, dateFounded;
	private boolean openEnrollment;
	private String[] rankTitle = new String[10];
	private int minCanEdit = 0, minCanPromote = 0, minCanKick = 0, minCanInvite = 0, minCanChat = 9, minCanEditPermissions = 0;
	
	public Guild(ResultSet set) throws Exception { 
		name = set.getString("name");
		charter = set.getString("charter");
		website = set.getString("website");
		founder = set.getLong("founder");
		guildID = set.getLong("guildid");
		dateFounded = set.getTimestamp("datefounded").getTime();
		openEnrollment = set.getBoolean("openenrollment");
		minCanEdit = set.getInt("minrank_edit");
		minCanInvite = set.getInt("minrank_invite");
		minCanPromote = set.getInt("minrank_promote");
		minCanKick = set.getInt("minrank_kick");
		minCanChat = set.getInt("minrank_chat");
		minCanEditPermissions = set.getInt("minrank_editpermissions");
		
		for (int i = 0; i < 10; i++) { 
			String field = "rank" + Integer.toString(i).trim() + "title";
			rankTitle[i] = set.getString(field);
		}
		
	}
	
	
	private int validateRank(int i) throws InvalidGuildRankException { 
		if (i < 0 || i > 9) throw new InvalidGuildRankException();
		return i;
	}
	
	public void editPermissions(Message m) throws InvalidGuildRankException { 
		if (m.getInt("minrank_edit") != null)
			minCanEdit = validateRank(m.getInt("minrank_edit"));
		if (m.getInt("minrank_kick") != null)
			minCanKick = validateRank(m.getInt("minrank_kick"));
		if (m.getInt("minrank_promote") != null)
			minCanPromote = validateRank(m.getInt("minrank_promote"));
		if (m.getInt("minrank_invite") != null)
			minCanInvite = validateRank(m.getInt("minrank_invite"));
		if (m.getInt("minrank_chat") != null)
			minCanChat = validateRank(m.getInt("minrank_chat"));
		if (m.getInt("minrank_editpermissions") != null)
			minCanEditPermissions = validateRank(m.getInt("minrank_editpermissions"));	
	}
	
	public void edit(Message m) { 
		
		if (m.getString("charter") != null)
			charter = m.getString("charter");
		if (m.getString("website") != null)
			website = m.getString("website");
		if (m.getString("name") != null)
			name = m.getString("name");
		if (m.getBoolean("openenrollment") != null)
			openEnrollment = m.getBoolean("openenrollment");
		
		for (int i = 0; i < 10; i++) { 
			String field = "rank" + Integer.toString(i).trim() + "title";
			if (m.getString(field) != null)
				rankTitle[i] = m.getString(field);
		}
		
		
	}
	
	public String getCharter() {
		return charter;
	}
	
	public String getWebsite() {
		return website;
	}
	public long getFounder() {
		return founder;
	}

	public long getDateFounded() {
		return dateFounded;
	}

	public String getRankTitle(int rank) {
		return rankTitle[rank];
	}
	public int getMinCanEdit() {
		return minCanEdit;
	}

	public int getMinCanPromote() {
		return minCanPromote;
	}

	public int getMinCanKick() {
		return minCanKick;
	}

	public int getMinCanInvite() {
		return minCanInvite;
	}
	public int getMinCanEditPermissions() { 
		return minCanEditPermissions;
	}
	
	public long getGuildID() { return guildID; }
	public String getName() { return name; }
	public boolean isOpenEnrollment() { return openEnrollment; }
	
	@Override
	public Message serialize(Player player) {
		Message m = new Message();
		m.put("name", name);
		m.put("guildid", guildID);
		m.put("website", website);
		m.put("founder", founder);
		m.put("datefounded", dateFounded);
		m.put("charter", charter);
		m.put("openenrollment", openEnrollment);
		m.put("minrank_promote", minCanPromote);
		m.put("minrank_edit", minCanEdit);
		m.put("minrank_kick", minCanKick);
		m.put("minrank_invite", minCanInvite);
		m.put("minrank_chat", minCanChat);
		m.put("minrank_editpermissions", minCanEditPermissions);
			for (int i = 0; i < 10; i++) { 
			String field = "rank" + Integer.toString(i).trim() + "title";
			m.put(field, rankTitle[i]);
		}
		return m;
	}

	public int getMinCanChat() {
		return minCanChat;
	} 
	

}

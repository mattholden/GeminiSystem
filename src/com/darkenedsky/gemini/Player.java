package com.darkenedsky.gemini;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Vector;
import com.darkenedsky.gemini.badge.Badge;
import com.darkenedsky.gemini.guild.Guild;
import com.darkenedsky.gemini.stats.Gender;

public class Player implements MessageSerializable, Gender {

	/**
	 * 
	 */
	private static final long serialVersionUID = 543569984430309674L;
	
	private long playerID;
	private String username;
	private String language = "en";
	private int gender = Gender.GENDER_UNDISCLOSED;
	private Long guildID;
	private Integer guildRank = null;
	private Vector<Badge> badges = new Vector<Badge>();
	private Vector<Long> currentGameIDs = new Vector<Long>();
	private Vector<Message> outgoingMessages = new Vector<Message>();
	private WinLossRecord record = null;
	private Guild guild;
	
	public Player(ResultSet set) throws SQLException { 
		username = set.getString("username");
		language = set.getString("language");
		if (language == null || "".equals(language)) { 
			language = Languages.ENGLISH;
		}
		playerID = set.getLong("playerid");
		gender = set.getInt("gender");
		guildID = set.getLong("guildid");
		if (set.wasNull())
			guildID = null;
		guildRank = set.getInt("guildrank");
		if (set.wasNull())
			guildRank = null;
		
	}
	

	public Vector<Message> popOutgoingMessages() { 
		Vector<Message> v = new Vector<Message>();
		v.addAll(outgoingMessages);
		outgoingMessages.clear();
		return v;
	} 
	
	public Vector<Long> getCurrentGames() { 
		return currentGameIDs;
	}

	public Guild getGuild() { return guild; }
	public void setGuild(Guild g) { guild = g; }
	
	public Vector<Badge> getBadges() { 
		return badges;
	}
	public void setBadges(Vector<Badge> bad) { 
		badges = bad;
	}
	
	public void addCurrentGame(long id) { 
		currentGameIDs.add(id);
	}
	
	public void removeCurrentGame(long id) { 
		currentGameIDs.remove(id);
	}
	
	public void pushOutgoingMessage(Message e) { outgoingMessages.add(e); }
	
	
	public boolean equals(Object other) { 
		if (other instanceof Player) {
			return ((Player)other).playerID == playerID;
		}
		return false;
	}
	
	public WinLossRecord getRecord() { 
		return record;
	}
	public void setRecord(WinLossRecord rec) { 
		record = rec;
	}
	
	public long getPlayerID() {
		return playerID;
	}
	public void setPlayerID(long playerID) {
		this.playerID = playerID;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getLanguage() {
		return language;
	}
	public void setLanguage(String language) {
		this.language = language;
	}
	
	@Override
	public int getGender() { return gender; } 
	
	@Override
	public void setGender(int gend) { gender = gend; }
	
	public Long getGuildID() {
		return guildID;
	}

	public void setGuildID(Long guildID) {
		this.guildID = guildID;
	}

	public Integer getGuildRank() {
		return guildRank;
	}

	public void setGuildRank(Integer grank) {
		this.guildRank = grank;
	}

	@Override
	public Message serialize(Player p) {
		Message m = new Message();
		m.put("username", username);
		m.put("playerid", playerID);
		m.put("language", language);
		m.put("gender", gender);
		
		// don't put the whole guild, thats overkill for this
		if (guild != null) {
			Message mguild = new Message();
			mguild.put("guildid", guildID);
			mguild.put("guildrank", guildRank);
			mguild.put("name", guild.getName());
			m.put("guild", mguild, p);
		}
		
		if (p.getPlayerID() == playerID) { 
			m.addList("games");
			for (Long game : this.currentGameIDs) { 
				Message g = new Message();
				g.put("gameid", game);
				m.addToList("games", g);
			}
		}
		m.addList("badges");
		for (Badge bad: this.badges) { 			
			m.addToList("badges", bad, p);
		}
		
		if (record != null)
			m.put("record", record, p);
		
		return m;
	}
	
}

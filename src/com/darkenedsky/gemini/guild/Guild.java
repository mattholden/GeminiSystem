package com.darkenedsky.gemini.guild;
import java.sql.ResultSet;
import com.darkenedsky.gemini.Message;
import com.darkenedsky.gemini.MessageSerializable;
import com.darkenedsky.gemini.Player;

public class Guild implements MessageSerializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5643299279486069347L;
	
	private String name, charter, website;
	private long founder, guildID, dateFounded;
	
	public Guild(ResultSet set) throws Exception { 
		name = set.getString("name");
		charter = set.getString("charter");
		website = set.getString("website");
		founder = set.getLong("founder");
		guildID = set.getLong("guildid");
		dateFounded = set.getTimestamp("datefounded").getTime();
	}
	
	public long getGuildID() { return guildID; }
	public String getName() { return name; }
	
	@Override
	public Message serialize(Player player) {
		Message m = new Message();
		m.put("name", name);
		m.put("guildid", guildID);
		m.put("website", website);
		m.put("founder", founder);
		m.put("datefounded", dateFounded);
		m.put("charter", charter);
		return m;
	} 
	

}

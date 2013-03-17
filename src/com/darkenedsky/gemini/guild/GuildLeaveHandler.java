package com.darkenedsky.gemini.guild;

import java.sql.PreparedStatement;

import com.darkenedsky.gemini.ActionList;
import com.darkenedsky.gemini.Handler;
import com.darkenedsky.gemini.Message;
import com.darkenedsky.gemini.Player;
import com.darkenedsky.gemini.exception.NotGuildMemberException;
import com.darkenedsky.gemini.exception.SQLUpdateFailedException;

public class GuildLeaveHandler extends Handler {

	private GuildService service;
	
	public GuildLeaveHandler(GuildService gs) { service = gs; }

	
	@Override
	public void processMessage(Message e, Player p) throws Exception {
				
		if (p.getGuildID() == null)
			throw new NotGuildMemberException();
	
		long guildid = p.getGuildID();
		p.setGuildID(null);
		p.setGuildRank(null);
		p.setGuild(null);
		
		PreparedStatement ps = service.getJDBC().prepareStatement("update players set guildid = null, guildrankid = null where playerid = ?;");
		ps.setLong(1, p.getPlayerID());
		if (ps.executeUpdate() != 0)
			throw new SQLUpdateFailedException();
		
		Message m = new Message(ActionList.GUILD_LEAVE);		
		m.put("guildid", guildid);
		m.put("playerid", p.getPlayerID());
		m.put("username", p.getUsername());
		p.pushOutgoingMessage(m);
		for (Player guildie : service.getSessionManager().getPlayersInGuild(guildid)) { 
			guildie.pushOutgoingMessage(m);
		}
		
	}

}
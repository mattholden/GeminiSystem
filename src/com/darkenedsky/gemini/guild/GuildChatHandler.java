package com.darkenedsky.gemini.guild;

import com.darkenedsky.gemini.ActionList;
import com.darkenedsky.gemini.Handler;
import com.darkenedsky.gemini.Message;
import com.darkenedsky.gemini.Player;
import com.darkenedsky.gemini.exception.GuildPermissionException;
import com.darkenedsky.gemini.exception.NotGuildMemberException;
import com.darkenedsky.gemini.service.SessionManager;

public class GuildChatHandler extends Handler {

	private SessionManager<?> sessions;
	
	public GuildChatHandler(SessionManager<?> sess) { 
		sessions = sess;
	}
	
	@Override
	public void processMessage(Message e, Player p) throws Exception {
		
		String message = e.getRequiredString("message");
		
		if (p.getGuild() == null || p.getGuildID() == null)
			throw new NotGuildMemberException();
	
		// insufficient rank
		// this one's not as big a deal if a few slip through the cracks so used the cached
		// guild and save the SQL hit for every chat message, which could get very expensive
		if (p.getGuild().getMinCanChat() > p.getGuildRank()) 
			throw new GuildPermissionException();
	
		for (Player guildmate : sessions.getPlayersInGuild(p.getGuildID())) { 
			Message m = new Message(ActionList.GUILD_CHAT);
			m.put("guildid", p.getGuildID());
			m.put("playerid", p.getPlayerID());
			m.put("username", p.getUsername());
			m.put("message", message);
			guildmate.pushOutgoingMessage(m);
		}
		
	}

}

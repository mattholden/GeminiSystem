package com.darkenedsky.gemini.guild;

import com.darkenedsky.gemini.ActionList;
import com.darkenedsky.gemini.Handler;
import com.darkenedsky.gemini.Message;
import com.darkenedsky.gemini.Player;
import com.darkenedsky.gemini.exception.NotGuildMemberException;
import com.darkenedsky.gemini.service.SessionManager;

public class GuildChatHandler extends Handler {

	private SessionManager<?> sessions;
	
	public GuildChatHandler(SessionManager<?> sess) { 
		sessions = sess;
	}
	
	@Override
	public void processMessage(Message e, Player p) throws Exception {
		
		if (p.getGuildID() == null)
			throw new NotGuildMemberException();
				
		for (Player guildmate : sessions.getPlayersInGuild(p.getGuildID())) { 
			Message m = new Message(ActionList.GUILD_CHAT);
			m.put("guildid", p.getGuildID());
			m.put("message", e.getString("message"));
			guildmate.pushOutgoingMessage(m);
		}
		
	}

}

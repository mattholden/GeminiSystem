package com.darkenedsky.gemini.guild;

import com.darkenedsky.gemini.ActionList;
import com.darkenedsky.gemini.Handler;
import com.darkenedsky.gemini.Message;
import com.darkenedsky.gemini.Player;

public class GetGuildHandler extends Handler {

	private GuildService service;
	
	public GetGuildHandler(GuildService gs) { service = gs; }

	
	@Override
	public void processMessage(Message e, Player p) throws Exception {
		Guild g = service.getGuild(e.getLong("guildid"));
		Message m = new Message(ActionList.GUILD_GET);
		m.put("guild", g, p);
		p.pushOutgoingMessage(m);
	}

}

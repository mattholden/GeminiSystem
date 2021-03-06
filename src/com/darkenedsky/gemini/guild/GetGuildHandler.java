package com.darkenedsky.gemini.guild;

import com.darkenedsky.gemini.ActionList;
import com.darkenedsky.gemini.Message;
import com.darkenedsky.gemini.Player;
import com.darkenedsky.gemini.handler.Handler;
import com.darkenedsky.gemini.handler.SessionValidator;

public class GetGuildHandler extends Handler {

	public GetGuildHandler() {

		addValidator(new SessionValidator());
	}

	@Override
	public void processMessage(Message e, Player p) throws Exception {
		Long guildid = e.getRequiredLong("guildid");
		Guild g = ((GuildService) getService()).getGuild(guildid);
		Message m = new Message(ActionList.GUILD_GET);
		m.put("guildid", g.getGuildID());
		m.put("guild", g, p);
		p.pushOutgoingMessage(m);
	}

}

package com.darkenedsky.gemini.guild;

import com.darkenedsky.gemini.ActionList;
import com.darkenedsky.gemini.Message;
import com.darkenedsky.gemini.Player;
import com.darkenedsky.gemini.exception.NotGuildMemberException;
import com.darkenedsky.gemini.handler.Handler;
import com.darkenedsky.gemini.handler.SessionValidator;
import com.darkenedsky.gemini.service.SessionManagerService;

public class GetGuildMembersOnlineHandler extends Handler {

	public GetGuildMembersOnlineHandler() {
		addValidator(new SessionValidator());

	}

	@Override
	public void processMessage(Message e, Player p) throws Exception {

		SessionManagerService<?> sessions = (SessionManagerService<?>) getService().getServer().getService(SessionManagerService.class);

		if (p.getGuildID() == null)
			throw new NotGuildMemberException();

		Message m = new Message(ActionList.GUILD_GET_MEMBERS_ONLINE);
		m.addList("players");
		m.put("guildid", p.getGuildID());
		for (Player guildmate : sessions.getPlayersInGuild(p.getGuildID())) {
			m.addToList("players", guildmate, p);
		}
		p.pushOutgoingMessage(m);

	}

}

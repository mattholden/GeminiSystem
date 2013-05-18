package com.darkenedsky.gemini.guild;

import java.sql.PreparedStatement;

import com.darkenedsky.gemini.ActionList;
import com.darkenedsky.gemini.Message;
import com.darkenedsky.gemini.Player;
import com.darkenedsky.gemini.exception.NotGuildMemberException;
import com.darkenedsky.gemini.exception.SQLUpdateFailedException;
import com.darkenedsky.gemini.handler.Handler;
import com.darkenedsky.gemini.handler.SessionValidator;
import com.darkenedsky.gemini.service.SessionManagerService;

public class GuildLeaveHandler extends Handler {

	private GuildService service;

	public GuildLeaveHandler(GuildService gs) {
		service = gs;
		addValidator(new SessionValidator());

	}

	@Override
	public void processMessage(Message e, Player p) throws Exception {

		if (p.getGuildID() == null)
			throw new NotGuildMemberException();

		long guildid = p.getGuildID();
		p.setGuildID(null);
		p.setGuildRank(null);
		p.setGuild(null);
		SessionManagerService<?> sessionManager = (SessionManagerService<?>) service.getServer().getService(SessionManagerService.class);

		PreparedStatement ps = service.getServer().getJDBC().prepareStatement("update players set guildid = null, guildrankid = null where playerid = ?;");
		ps.setLong(1, p.getPlayerID());
		if (ps.executeUpdate() != 0)
			throw new SQLUpdateFailedException();

		Message m = new Message(ActionList.GUILD_LEAVE);
		m.put("guildid", guildid);
		m.put("playerid", p.getPlayerID());
		m.put("username", p.getUsername());
		p.pushOutgoingMessage(m);
		for (Player guildie : sessionManager.getPlayersInGuild(guildid)) {
			guildie.pushOutgoingMessage(m);
		}

	}

}
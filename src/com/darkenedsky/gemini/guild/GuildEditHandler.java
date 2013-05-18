package com.darkenedsky.gemini.guild;

import com.darkenedsky.gemini.Message;
import com.darkenedsky.gemini.Player;
import com.darkenedsky.gemini.exception.GuildPermissionException;
import com.darkenedsky.gemini.exception.InvalidObjectException;
import com.darkenedsky.gemini.exception.NotGuildMemberException;
import com.darkenedsky.gemini.handler.Handler;
import com.darkenedsky.gemini.handler.SessionValidator;
import com.darkenedsky.gemini.service.SessionManagerService;

public class GuildEditHandler extends Handler {

	private GuildService service;

	public GuildEditHandler(GuildService gs) {
		service = gs;
		addValidator(new SessionValidator());

	}

	@Override
	public void processMessage(Message e, Player p) throws Exception {

		SessionManagerService<?> sessionManager = (SessionManagerService<?>) service.getServer().getService(SessionManagerService.class);
		if (p.getGuildID() == null)
			throw new NotGuildMemberException();

		Long guildid = p.getGuildID();
		Guild guild = service.getGuild(guildid);
		if (guild == null)
			throw new InvalidObjectException(guildid);

		// insufficient rank
		int rank = p.getGuildRank();
		if (guild.getMinCanEdit() > rank)
			throw new GuildPermissionException();

		guild.edit(e);

		// if you don't have permission to edit permissions, just ignore it if
		// you tried to do so.
		if (guild.getMinCanEditPermissions() <= rank)
			guild.editPermissions(e);

		service.updateGuild(guild);

		for (Player guildie : sessionManager.getPlayersInGuild(guildid)) {
			Message msg = new Message(GUILD_EDIT);
			msg.put("guildid", guildid);
			msg.put("guild", guild, guildie);
			guildie.pushOutgoingMessage(msg);
			guildie.setGuild(guild);
		}

	}

}

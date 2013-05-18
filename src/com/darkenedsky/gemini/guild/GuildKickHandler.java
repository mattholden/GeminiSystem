package com.darkenedsky.gemini.guild;

import java.sql.PreparedStatement;

import com.darkenedsky.gemini.Message;
import com.darkenedsky.gemini.Player;
import com.darkenedsky.gemini.exception.GuildPermissionException;
import com.darkenedsky.gemini.exception.InvalidObjectException;
import com.darkenedsky.gemini.exception.InvalidPlayerException;
import com.darkenedsky.gemini.exception.NotGuildMemberException;
import com.darkenedsky.gemini.exception.SQLUpdateFailedException;
import com.darkenedsky.gemini.handler.Handler;
import com.darkenedsky.gemini.handler.SessionValidator;
import com.darkenedsky.gemini.service.SessionManagerService;

public class GuildKickHandler extends Handler {

	private GuildService service;

	public GuildKickHandler(GuildService gs) {
		service = gs;
		addValidator(new SessionValidator());

	}

	@Override
	public void processMessage(Message e, Player p) throws Exception {

		if (p.getGuildID() == null)
			throw new NotGuildMemberException();

		Long guildid = p.getGuildID();
		Long playerid = e.getRequiredLong("playerid");

		Guild guild = service.getGuild(guildid);
		if (guild == null)
			throw new InvalidObjectException(guildid);

		// insufficient rank
		if (guild.getMinCanKick() > p.getGuildRank())
			throw new GuildPermissionException();

		SessionManagerService<?> sessionManager = (SessionManagerService<?>) service.getServer().getService(SessionManagerService.class);

		Integer opposingRank = -1;
		Long opposingGuild = -1L;
		Player kicked = null;
		boolean isOnline = false;

		// get the player's rank - first let's try to get lucky and see if
		// they're online
		try {
			kicked = sessionManager.getSession(playerid);
			opposingRank = kicked.getGuildRank();
			opposingGuild = kicked.getGuildID();
			isOnline = true;
		}
		// they weren't online; get it from the DB
		catch (InvalidPlayerException x) {
			kicked = sessionManager.loadPlayerFromDatabase(playerid);
		}

		// make sure you're in the same guild and he doesn't outrank you
		if (opposingGuild != guildid)
			throw new NotGuildMemberException(guildid, opposingGuild);
		if (opposingRank >= p.getGuildRank())
			throw new GuildPermissionException();

		// ok, toss him out
		PreparedStatement ps = service.getServer().getJDBC().prepareStatement("update players set guildid = null, guildrank = null where playerid = ?;");
		ps.setLong(1, p.getPlayerID());
		if (ps.executeUpdate() == 0)
			throw new SQLUpdateFailedException();

		Message msg = new Message(GUILD_KICK);
		msg.put("guildid", guildid);
		msg.put("guildname", guild.getName());
		msg.put("kickedplayerid", playerid);
		msg.put("kickedusername", kicked.getUsername());
		msg.put("kickedbyplayerid", p.getPlayerID());
		msg.put("kickedbyusername", p.getUsername());
		if (e.getString("reason") != null)
			msg.put("reason", e.getString("reason"));

		if (isOnline) {
			kicked.setGuild(null);
			kicked.setGuildID(null);
			kicked.setGuildRank(null);
			kicked.pushOutgoingMessage(msg);
		}
		for (Player guildie : sessionManager.getPlayersInGuild(guildid)) {
			guildie.pushOutgoingMessage(msg);
		}

	}

}

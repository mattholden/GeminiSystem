package com.darkenedsky.gemini.guild;

import java.sql.PreparedStatement;

import com.darkenedsky.gemini.Message;
import com.darkenedsky.gemini.Player;
import com.darkenedsky.gemini.exception.GuildPermissionException;
import com.darkenedsky.gemini.exception.InvalidGuildRankException;
import com.darkenedsky.gemini.exception.InvalidObjectException;
import com.darkenedsky.gemini.exception.InvalidPlayerException;
import com.darkenedsky.gemini.exception.NotGuildMemberException;
import com.darkenedsky.gemini.exception.SQLUpdateFailedException;
import com.darkenedsky.gemini.handler.Handler;
import com.darkenedsky.gemini.handler.SessionValidator;
import com.darkenedsky.gemini.service.SessionManagerService;

public class GuildSetRankHandler extends Handler {

	public GuildSetRankHandler() {

		addValidator(new SessionValidator());

	}

	@Override
	public void processMessage(Message e, Player p) throws Exception {

		if (p.getGuildID() == null)
			throw new NotGuildMemberException();

		SessionManagerService<?> sessionManager = (SessionManagerService<?>) getService().getServer().getService(SessionManagerService.class);

		Long guildid = p.getGuildID();
		Long playerid = e.getRequiredLong("playerid");
		int newRank = e.getRequiredInt("guildrank");

		if (newRank < 0 || newRank > 9)
			throw new InvalidGuildRankException();

		Guild guild = ((GuildService) getService()).getGuild(guildid);
		if (guild == null)
			throw new InvalidObjectException(guildid);

		// insufficient rank
		if (guild.getMinCanPromote() > p.getGuildRank())
			throw new GuildPermissionException();

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
		if (opposingRank >= p.getGuildRank() || newRank > p.getGuildRank())
			throw new GuildPermissionException();

		// ok, change his rank
		PreparedStatement ps = getService().getServer().getJDBC().prepareStatement("update players set guildrank = ? where playerid = ?;");
		ps.setInt(1, newRank);
		ps.setLong(2, p.getPlayerID());
		if (ps.executeUpdate() == 0)
			throw new SQLUpdateFailedException();

		Message msg = new Message(GUILD_SETRANK);
		msg.put("guildid", guildid);
		msg.put("guildname", guild.getName());
		msg.put("oldrank", opposingRank);
		msg.put("oldranktitle", guild.getRankTitle(opposingRank));
		msg.put("newrank", newRank);
		msg.put("newranktitle", guild.getRankTitle(newRank));
		msg.put("changedplayerid", playerid);
		msg.put("changedusername", kicked.getUsername());
		msg.put("changedbyplayerid", p.getPlayerID());
		msg.put("changedbyusername", p.getUsername());
		if (e.getString("reason") != null)
			msg.put("reason", e.getString("reason"));

		if (isOnline)
			kicked.pushOutgoingMessage(msg);
		for (Player guildie : sessionManager.getPlayersInGuild(guildid)) {
			guildie.pushOutgoingMessage(msg);
		}

	}

}

package com.darkenedsky.gemini.guild;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.darkenedsky.gemini.ActionList;
import com.darkenedsky.gemini.Message;
import com.darkenedsky.gemini.Player;
import com.darkenedsky.gemini.exception.AlreadyGuildMemberException;
import com.darkenedsky.gemini.exception.InvalidObjectException;
import com.darkenedsky.gemini.exception.NoGuildInviteException;
import com.darkenedsky.gemini.exception.SQLUpdateFailedException;
import com.darkenedsky.gemini.handler.Handler;
import com.darkenedsky.gemini.handler.SessionValidator;
import com.darkenedsky.gemini.service.SessionManagerService;

public class GuildJoinHandler extends Handler {

	public GuildJoinHandler() {

		addValidator(new SessionValidator());
	}

	@Override
	public void processMessage(Message e, Player p) throws Exception {

		SessionManagerService<?> sessionManager = (SessionManagerService<?>) getService().getServer().getService(SessionManagerService.class);

		if (p.getGuildID() != null)
			throw new AlreadyGuildMemberException();

		long guildid = e.getRequiredLong("guildid");

		Guild guild = ((GuildService) getService()).getGuild(guildid);
		if (guild == null)
			throw new InvalidObjectException(guildid);

		// make sure you're invited
		if (!guild.isOpenEnrollment()) {
			ResultSet inv = null;
			PreparedStatement ps1 = getService().getServer().getJDBC().prepareStatement("select * from guildinvites where guildid = ? and playerinvited = ?;");
			ps1.setLong(1, guildid);
			ps1.setLong(2, p.getPlayerID());
			try {
				inv = ps1.executeQuery();
				if (!inv.first()) {
					inv.close();
					throw new NoGuildInviteException();
				}
				inv.close();
			} catch (Exception x) {
				if (inv != null)
					inv.close();
				throw x;
			}
		}

		p.setGuildID(guildid);
		p.setGuild(guild);
		p.setGuildRank(9);

		PreparedStatement ps = getService().getServer().getJDBC().prepareStatement("update players set guildid = ?, guildrank = 9 where playerid = ?;");
		ps.setLong(2, guildid);
		ps.setLong(2, p.getPlayerID());
		if (ps.executeUpdate() != 0)
			throw new SQLUpdateFailedException();

		// Don't need these anymore if you're joining...
		if (!guild.isOpenEnrollment()) {
			PreparedStatement ps1 = getService().getServer().getJDBC().prepareStatement("delete from guildinvites where guildid = ? and playerinvited = ?;");
			ps1.setLong(1, guildid);
			ps1.setLong(2, p.getPlayerID());
			if (ps1.executeUpdate() == 0)
				throw new SQLUpdateFailedException();
		}

		Message m = new Message(ActionList.GUILD_JOIN);
		m.put("playerid", p.getPlayerID());
		m.put("username", p.getUsername());
		m.put("guildid", guildid);
		m.put("guildrank", 9);

		for (Player guildie : sessionManager.getPlayersInGuild(guildid)) {
			guildie.pushOutgoingMessage(m);
		}

	}

}
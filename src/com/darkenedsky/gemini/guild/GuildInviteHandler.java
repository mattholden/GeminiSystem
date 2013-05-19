package com.darkenedsky.gemini.guild;

import java.sql.PreparedStatement;

import com.darkenedsky.gemini.Message;
import com.darkenedsky.gemini.Player;
import com.darkenedsky.gemini.exception.AlreadyGuildMemberException;
import com.darkenedsky.gemini.exception.GuildIsOpenEnrollmentException;
import com.darkenedsky.gemini.exception.GuildPermissionException;
import com.darkenedsky.gemini.exception.InvalidObjectException;
import com.darkenedsky.gemini.exception.InvalidPlayerException;
import com.darkenedsky.gemini.exception.SQLUpdateFailedException;
import com.darkenedsky.gemini.handler.Handler;
import com.darkenedsky.gemini.handler.SessionValidator;
import com.darkenedsky.gemini.service.SessionManagerService;

public class GuildInviteHandler extends Handler {

	public GuildInviteHandler() {
		addValidator(new SessionValidator());

	}

	@Override
	public void processMessage(Message e, Player p) throws Exception {
		SessionManagerService<?> sessionManager = (SessionManagerService<?>) getService().getServer().getService(SessionManagerService.class);

		if (p.getGuildID() != null)
			throw new AlreadyGuildMemberException();

		Long guildid = p.getGuildID();
		Long playerid = e.getRequiredLong("playerid");

		Guild guild = ((GuildService) getService()).getGuild(guildid);
		if (guild == null)
			throw new InvalidObjectException(guildid);

		// don't do unnecessary invites
		if (guild.isOpenEnrollment()) {
			throw new GuildIsOpenEnrollmentException();
		}
		// insufficient rank
		if (guild.getMinCanInvite() > p.getGuildRank())
			throw new GuildPermissionException();

		Player invited = null;
		boolean isOnline = false;
		try {
			invited = sessionManager.getSession(playerid);
			isOnline = true;
		} catch (InvalidPlayerException x) {
			invited = sessionManager.loadPlayerFromDatabase(playerid);
		}

		PreparedStatement ps = ((GuildService) getService()).getServer().getJDBC().prepareStatement("insert into guildinvites (guildid, playerinvited, playerinvitedby) values(?,?,?);");
		ps.setLong(1, guildid);
		ps.setLong(2, playerid);
		ps.setLong(3, p.getPlayerID());
		if (ps.executeUpdate() == 0)
			throw new SQLUpdateFailedException();

		Message msg = new Message(GUILD_INVITE);
		msg.put("guildid", guildid);
		msg.put("guildname", guild.getName());
		msg.put("invitedplayerid", playerid);
		msg.put("invitedusername", invited.getUsername());
		msg.put("invitedbyplayerid", p.getPlayerID());
		msg.put("invitedbyusername", p.getUsername());
		msg.put("invitedbyrank", guild.getRankTitle(p.getGuildRank()));

		for (Player guildie : sessionManager.getPlayersInGuild(guildid)) {
			guildie.pushOutgoingMessage(msg);
		}
		if (isOnline)
			invited.pushOutgoingMessage(msg);
	}

}

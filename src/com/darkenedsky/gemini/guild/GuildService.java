package com.darkenedsky.gemini.guild;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.darkenedsky.gemini.exception.InvalidObjectException;
import com.darkenedsky.gemini.exception.SQLUpdateFailedException;
import com.darkenedsky.gemini.service.JDBCConnection;
import com.darkenedsky.gemini.service.Service;

public class GuildService extends Service {

	public GuildService() {

		handlers.put(GUILD_CHAT, new GuildChatHandler(this));
		handlers.put(GUILD_GET, new GetGuildHandler(this));
		handlers.put(GUILD_GET_MEMBERS_ONLINE, new GetGuildMembersOnlineHandler(this));
		handlers.put(GUILD_JOIN, new GuildJoinHandler(this));
		handlers.put(GUILD_LEAVE, new GuildLeaveHandler(this));
		handlers.put(GUILD_INVITE, new GuildInviteHandler(this));
		handlers.put(GUILD_KICK, new GuildKickHandler(this));
		handlers.put(GUILD_SETRANK, new GuildSetRankHandler(this));
		handlers.put(GUILD_EDIT, new GuildEditHandler(this));
		handlers.put(GUILD_DECLINE, new GuildDeclineHandler(this));
	}

	public Guild getGuild(long guildid) throws Exception {

		JDBCConnection jdbc = getServer().getJDBC();
		PreparedStatement ps = jdbc.prepareStatement("select * from guilds where guildid = ?;");
		ps.setLong(1, guildid);
		ResultSet set = null;
		Guild guild = null;

		try {
			set = ps.executeQuery();
			if (set.first()) {
				guild = new Guild(set);
				set.close();
				return guild;
			} else {
				set.close();
				throw new InvalidObjectException(guildid);
			}
		} catch (Exception x) {
			if (set != null)
				set.close();
			throw x;
		}
	}

	public void updateGuild(Guild g) throws Exception {

		JDBCConnection jdbc = getServer().getJDBC();
		PreparedStatement ps = jdbc.prepareStatement("update guilds set name = ?, charter = ?, website = ?, minrank_edit = ?, minrank_promote = ?, " + "minrank_kick = ?, minrank_invite = ?, minrank_chat = ?, minrank_editpermissions = ?, rank0title = ?, rank1title = ?, rank2title = ?, rank3title = ?, rank4title = ?, " + "rank5title = ?, rank6title = ?, rank7title = ?, rank8title = ?, rank9title = ? where guildid = ?;");
		ps.setString(1, g.getName());
		ps.setString(2, g.getCharter());
		ps.setString(3, g.getWebsite());
		ps.setInt(4, g.getMinCanEdit());
		ps.setInt(5, g.getMinCanPromote());
		ps.setInt(6, g.getMinCanKick());
		ps.setInt(7, g.getMinCanInvite());
		ps.setInt(8, g.getMinCanChat());
		ps.setInt(9, g.getMinCanEditPermissions());
		for (int i = 0; i < 10; i++) {
			ps.setString(10 + i, g.getRankTitle(i));
		}
		ps.setLong(20, g.getGuildID());
		if (ps.executeUpdate() == 0)
			throw new SQLUpdateFailedException();
	}
}

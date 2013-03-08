package com.darkenedsky.gemini.guild;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.darkenedsky.gemini.exception.InvalidObjectException;
import com.darkenedsky.gemini.service.JDBCConnection;
import com.darkenedsky.gemini.service.Service;
import com.darkenedsky.gemini.service.SessionManager;

public class GuildService extends Service {

	private JDBCConnection jdbc;
	
	public GuildService(JDBCConnection db, SessionManager<?> sessions) { 
		jdbc = db;		
	
		handlers.put(GUILD_CHAT, new GuildChatHandler(sessions));
		handlers.put(GUILD_GET, new GetGuildHandler(this));
	}
	
	public Guild getGuild(long guildid) throws Exception { 
		
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
			}
			else { 
				set.close();
				throw new InvalidObjectException(guildid);
			}
		}
		catch (Exception x) { 
			if (set != null)
				set.close();
			throw x;
		}
	}
}

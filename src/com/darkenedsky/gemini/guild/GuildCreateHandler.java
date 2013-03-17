package com.darkenedsky.gemini.guild;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import com.darkenedsky.gemini.ActionList;
import com.darkenedsky.gemini.Handler;
import com.darkenedsky.gemini.Message;
import com.darkenedsky.gemini.Player;
import com.darkenedsky.gemini.exception.AlreadyGuildMemberException;
import com.darkenedsky.gemini.exception.SQLUpdateFailedException;

/** Handler for creating a new guild. Will also add the founder to the guild (with the rank of Founder,
 * obviously) and assign all default permissions and rank titles. 
 * 
 * @author Matt Holden
 *
 */
public class GuildCreateHandler extends Handler { 
	
	/** The parent GuildService */
	private GuildService service;
	
	/** Construct the handler	
	 * @param gs the parent GuildService
	 */
	public GuildCreateHandler(GuildService gs) { 
		super(null);
		service = gs;
	}
	
	@Override
	public void processMessage(Message m, Player p) throws Exception { 
		
		if (p.getGuildID() != null)
			throw new AlreadyGuildMemberException();
	
		String name = m.getRequiredString("name");
		
		PreparedStatement ps = service.getJDBC().prepareStatement("select * from create_guild(?,?);");
		ps.setLong(1, p.getPlayerID());
		ps.setString(2, name);
		ResultSet set = null;
		try { 
			set = ps.executeQuery();
			
			if (set.first()) { 
				Guild g = new Guild(set);
				p.setGuild(g);
				p.setGuildID(g.getGuildID());
				p.setGuildRank(0);				
				
				Message reply = new Message(ActionList.GUILD_CREATE);
				reply.put("guildid", g.getGuildID());
				reply.put("guildrank", 0);
				reply.put("guild", g, p);
				p.pushOutgoingMessage(reply);
				set.close();
			}
			else  
				throw new SQLUpdateFailedException();
		}
		catch (Exception x) { 
			if (set != null)
				set.close();
			throw x;
		}		
	}

}

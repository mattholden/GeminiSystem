package com.darkenedsky.gemini.guild;

import java.sql.PreparedStatement;
import com.darkenedsky.gemini.Message;
import com.darkenedsky.gemini.Player;
import com.darkenedsky.gemini.handler.Handler;
import com.darkenedsky.gemini.handler.SessionValidator;

public class GuildDeclineHandler extends Handler {

	private GuildService service;
	
	public GuildDeclineHandler(GuildService gs) { 
		service = gs;
		addValidator(new SessionValidator());
		
	}
	
	@Override
	public void processMessage(Message e, Player p) throws Exception {
		
		Long guildid = e.getRequiredLong("guildid");
		
		// faster to just delete them and then ask how many we deleted, rather than throwing an exception if there weren't any
		PreparedStatement ps = service.getJDBC().prepareStatement("delete from guildinvites where guildid = ? and playerinvited = ?;");
		ps.setLong(1, guildid);
		ps.setLong(2, p.getPlayerID());
		int invites = ps.executeUpdate(); 
			
		Message msg = new Message(GUILD_DECLINE);
		msg.put("guildid", guildid);
		msg.put("invitedplayerid", p.getPlayerID());
		msg.put("invitedusername", p.getUsername());
		
		// your message is successful in that you don't have any outstanding invites from that guild
		p.pushOutgoingMessage(msg);
		
		// only tell the guildmates if there actually were invitations
		if (invites > 0) { 
			for (Player guildie : service.getSessionManager().getPlayersInGuild(guildid)) { 
				guildie.pushOutgoingMessage(msg);
			}
		}
		
	}

	
}

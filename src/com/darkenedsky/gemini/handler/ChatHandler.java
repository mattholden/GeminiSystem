package com.darkenedsky.gemini.handler;

import com.darkenedsky.gemini.Message;
import com.darkenedsky.gemini.Player;

public class ChatHandler extends GameHandler {

	public ChatHandler() {

		addValidator(new SessionValidator());
		addValidator(new PlayerInGameValidator());
	}

	@Override
	public void processMessage(Message m, Player p) throws Exception {
		Message sending = new Message(CHAT, getGame().getGameID(), p.getPlayerID());
		sending.put("message", m.getString("message"));
		sending.put("playerid", p.getPlayerID());
		sending.put("username", p.getUsername());

		for (Player play : getGame().getPlayers()) {
			play.pushOutgoingMessage(sending);
		}
	}

}

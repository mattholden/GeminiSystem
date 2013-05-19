package com.darkenedsky.gemini.handler;

import com.darkenedsky.gemini.Message;
import com.darkenedsky.gemini.Player;

public class WhisperHandler extends GameHandler {

	public WhisperHandler() {

		addValidator(new SessionValidator());
		addValidator(new PlayerInGameValidator());
	}

	@Override
	public void processMessage(Message m, Player p) throws Exception {
		long pid = m.getLong("targetplayerid");
		Message sending = new Message(WHISPER, getGame().getGameID(), p.getPlayerID());
		sending.put("message", m.getString("message"));
		sending.put("playerid", p.getPlayerID());
		sending.put("username", p.getUsername());
		getGame().getPlayer(pid).pushOutgoingMessage(sending);
	}

	@Override
	public void validate(Message msg, Player p) throws Exception {
		super.validate(msg, p);

		// will throw PlayerNotFoundException if it should
		getGame().getPlayer(msg.getLong("targetplayerid"));
	}

}

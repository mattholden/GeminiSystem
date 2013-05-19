package com.darkenedsky.gemini.handler;

import com.darkenedsky.gemini.Message;
import com.darkenedsky.gemini.Player;

public interface HandlerValidator extends HandlerValidationConstants {

	public Handler getHandler();

	public void setHandler(Handler h);

	public void validate(Message m, Player p) throws Exception;

}

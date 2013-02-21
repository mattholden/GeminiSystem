
package com.darkenedsky.gemini;

public interface MessageProcessor extends ActionList {

	public void processMessage(Message e, Player p) throws Exception;
	
}

package com.darkenedsky.gemini.service;
import java.util.concurrent.ConcurrentHashMap;

import com.darkenedsky.gemini.Handler;
import com.darkenedsky.gemini.Message;
import com.darkenedsky.gemini.MessageProcessor;
import com.darkenedsky.gemini.Player;
import com.darkenedsky.gemini.exception.InvalidActionException;

/** Base class for any "service" (a MessageProcessor) that can process multiple actions, to identify which
 *  actions it can process. 
 *  
 * @author Matt Holden
 *
 */
public class Service implements MessageProcessor {

	/** List of the action Handlers registered on this game. Just like old times' sake! */
	protected transient ConcurrentHashMap<Integer, Handler> handlers = new ConcurrentHashMap<Integer, Handler>();
	

	/** Check to see if this plugin has the capacity to handle a particular action
	 * 
	 * @param action action number to check
	 * @return true if the plugin can handle this action
	 */
	public boolean canProcessAction(int action) { 
		return handlers.get(action) != null;
	}
	
	/** Process the message, routing it to the appropriate Handler for the action.
	 * @param e the message to process
	 * @param p the player session sending the message
	 */
	@Override
	public void processMessage(Message e, Player p) throws Exception {
		
		int action = e.getInt("action");
		Handler h = handlers.get(action);
		if (h == null) { 
			throw new InvalidActionException(action);			
		}
		// make sure we are on the right turn, phase, etc.
		h.validate(e, p);
		
		// if we didn't throw an exception above, execute
		h.processMessage(e,p);
	}
	
	/** Override to perform any initialization on this service. */
	public void init() {}
	
	/** Override to perform any shutdown code on this service */
	public void shutdown() {}
	
}

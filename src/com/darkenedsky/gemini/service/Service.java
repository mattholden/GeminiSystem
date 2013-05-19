package com.darkenedsky.gemini.service;

import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import com.darkenedsky.gemini.Message;
import com.darkenedsky.gemini.MessageProcessor;
import com.darkenedsky.gemini.Player;
import com.darkenedsky.gemini.exception.InvalidActionException;
import com.darkenedsky.gemini.handler.Handler;

/**
 * Base class for any "service" (a MessageProcessor) that can process multiple
 * actions, to identify which actions it can process.
 * 
 * @author Matt Holden
 * 
 */
public class Service implements MessageProcessor {

	/** Logger instance */
	private static final Logger LOG = Logger.getLogger(Service.class);

	/**
	 * List of the action Handlers registered on this game. Just like old times'
	 * sake!
	 */
	private transient ConcurrentHashMap<Integer, Handler> handlers = new ConcurrentHashMap<Integer, Handler>();

	protected GeminiServer server;

	public void addHandler(int verb, Handler h) {
		handlers.put(verb, h);
		h.setService(this);
	}

	/**
	 * Check to see if this plugin has the capacity to handle a particular
	 * action
	 * 
	 * @param msg
	 *            message to check
	 * @return true if the plugin can handle this action
	 */
	public boolean canProcessAction(Message msg) {
		return handlers.get(msg.getInt(Message.ACTION)) != null;
	}

	public GeminiServer getServer() {
		return server;
	}

	/** Override to perform any initialization on this service. */
	public void init() {
	}

	/**
	 * Process the message, routing it to the appropriate Handler for the
	 * action.
	 * 
	 * @param e
	 *            the message to process
	 * @param p
	 *            the player session sending the message
	 */
	@Override
	public void processMessage(Message e, Player p) throws Exception {

		int action = e.getInt("action");
		Handler h = handlers.get(action);
		if (h == null) {
			throw new InvalidActionException(action);
		}

		LOG.debug("Service: Firing action " + action);

		// make sure we are on the right turn, phase, etc.
		h.validate(e, p);

		// if we didn't throw an exception above, execute
		h.processMessage(e, p);
	}

	void setServer(GeminiServer gs) {
		server = gs;
	}

	/** Override to perform any shutdown code on this service */
	public void shutdown() {
	}

}

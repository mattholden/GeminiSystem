package com.darkenedsky.gemini.service;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.jdom2.Element;

import com.darkenedsky.gemini.Library;
import com.darkenedsky.gemini.Message;
import com.darkenedsky.gemini.Player;
import com.darkenedsky.gemini.exception.GeminiException;
import com.darkenedsky.gemini.exception.InvalidActionException;
import com.darkenedsky.gemini.exception.JavaException;
import com.darkenedsky.gemini.tools.ReflectionTools;
import com.darkenedsky.gemini.tools.XMLTools;

/**
 * The service that processes incoming messages, spawns Games, handles logins
 * and other Sessions, etc. Extend this class for each game "service" (FKA
 * projects) that you wish to run.
 */
public class GeminiServer {

	/** Logger instance */
	private static Logger LOG = Logger.getLogger(GeminiServer.class);

	/** Send localized emails */
	private EmailFactory emailFactory;

	/** The JDBC connection to the database. */
	private JDBCConnection jdbc;

	/** Library of game objects */
	private Library library;

	/**
	 * List of plugins that have been installed in this service to handle things
	 * besides games, sessions
	 */
	private Vector<Service> services = new Vector<Service>();

	/** Manager to store and control session logins/logouts. */
	protected SessionManagerService<? extends Player> sessions;

	/**
	 * Settings stored from the XML config file, for server configuration and
	 * credentials.
	 */
	private Message settings;

	/**
	 * Construct the Gemini service.
	 * 
	 * @param thePlayerClass
	 *            The class object for the specific Player subclass. Should
	 *            match TPlay.
	 * @param lib
	 *            the library of static game object definitions. Should be
	 *            defined in the Servlet.
	 * @param settingsFile
	 *            The Settings XML to read.
	 * @throws SQLException
	 * @throws ClassNotFoundException
	 * @throws IOException
	 * @throws Exception
	 */
	public GeminiServer(SessionManagerService<? extends Player> sessionManager, Library lib, Message settings) throws SQLException, ClassNotFoundException, IOException, Exception {

		sessions = sessionManager;
		addService(sessions);

		jdbc = new JDBCConnection(settings.getString("database_user"), settings.getString("database_password"), settings.getString("database_path"), settings.getString("database_driver"));
		library = lib;

		emailFactory = new EmailFactory(settings, jdbc);

		LOG.debug("Gemini Service for game " + settings.getString("servicename") + " initialized OK.");
	}

	/**
	 * Add a new plugin module to the service.
	 * 
	 * @param plug
	 *            the new plugin to add.
	 */
	public void addService(Service plug) {
		plug.setServer(this);
		services.add(plug);
		plug.init();

	}

	public ServletResponse doRequest(String xml, String json, String ip, String method, boolean isSecure) throws Exception {

		try {
			LOG.debug("Request received");

			// don't try to do anything before we're ready
			if (getSettings() != null) {
				if (getSettings().getBoolean("require-https") && !isSecure)
					throw new Exception("Insecure requests are not accepted by this server.");
				if (getSettings().getBoolean("allow-get-requests") && !method.equalsIgnoreCase("POST"))
					throw new Exception("HTTP GET requests are not accepted by this server.");
			}

			String m = xml;
			String j = json;
			if (j != null) {
				LOG.debug("===================================================");
				LOG.debug("RECEIVED JSON:");
				LOG.debug(j);
				Message msg = new Message(j);
				msg.put(Message.SESSION_IPADDRESS, ip);
				Vector<Message> replies = (processMessage(msg));
				StringBuffer sb = new StringBuffer("[\n");
				for (int i = 0; i < replies.size(); i++) {
					sb.append(replies.get(i).toJSONString());
					if (i != replies.size() - 1) {
						sb.append(",\n");
					}
				}
				sb.append("\n]");

				String retstr = sb.toString();
				LOG.debug("-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-\nRESPONDED:");
				LOG.debug(retstr);
				return new ServletResponse(retstr, "text/json");
			}

			else if (m != null) {

				LOG.debug("===================================================");
				LOG.debug("RECEIVED XML:");
				LOG.debug(m);
				Element e = XMLTools.stringToXML(m);
				e.addContent(XMLTools.xml(Message.SESSION_IPADDRESS, ip));
				Message msg = Message.getMessage(e);
				Vector<Message> replies = (processMessage(msg));
				Element reply = new Element("messages");
				for (Message mx : replies) {
					reply.addContent(mx.toXML("message"));
				}
				String retstr = XMLTools.xmlToString(reply);
				LOG.debug("-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-\nRESPONDED:");
				LOG.debug(retstr);
				return new ServletResponse(retstr, "text/xml");
			} else
				return null;

		} catch (IOException x) {
			throw x;
		}
	}

	public EmailFactory getEmailFactory() {
		return emailFactory;
	}

	/**
	 * Accessor for the JDBC connection.
	 * 
	 * @return the JDBC connection object
	 */
	public JDBCConnection getJDBC() {
		return jdbc;
	}

	/** Accessor for the Library */
	public Library getLibrary() {
		return library;
	}

	/**
	 * Access a particular Service.
	 * 
	 * @param clazz
	 *            A class for the service
	 * @return the service if we have one or null if we don't.
	 */
	public Service getService(Class<? extends Service> clazz) {

		for (Service s : services) {
			if (ReflectionTools.isSubclass(clazz, s.getClass())) {
				return s;
			}
		}
		return null;
	}

	/** Accessor for the settings created from the XML settings file. */
	public Message getSettings() {
		return settings;
	}

	/**
	 * Process an incoming message from the server. Note that this is different
	 * from the interface's processMessage method, in that this one returns a
	 * list of messages that need to get sent back to the client.
	 * 
	 * @param e
	 *            The incoming message from the client
	 * @return a list of responses for the client.
	 * @throws Exception
	 */
	public Vector<Message> processMessage(Message e) throws Exception {
		Player player = null;
		Vector<Message> replies = new Vector<Message>();

		try {
			Integer action = e.getInt(Message.ACTION);
			if (action == null)
				throw new InvalidActionException(action);

			// Validate the session token
			// Actions below 100 are reserved for things that do NOT require a
			// session, such as logging in.
			String token = e.getString(Message.SESSION_TOKEN);
			if (action >= 100) {
				player = sessions.getSession(token);

				// add some stuff to the message that things processing the
				// message might find useful
				e.put(Message.SESSION_PLAYERID, player.getPlayerID());
			}

			boolean plugged = false;
			// try the session manager first
			if (sessions.canProcessAction(e)) {
				plugged = true;
				replies.add(sessions.processSessionlessMessage(e, player));
			} else {
				for (Service plug : services) {

					if (plug.canProcessAction(e)) {
						plugged = true;
						plug.processMessage(e, player);
					}
				}
			}
			// plugins were stumped
			if (!plugged) {
				throw new InvalidActionException(action);
			}

		} catch (GeminiException re) {
			re.printStackTrace();
			LOG.debug(re);
			replies.add(re.serialize(player));
		} catch (Throwable x) {
			LOG.error(x);
			x.printStackTrace();
			replies.add(new JavaException(x).serialize(player));
		}

		if (player != null) {
			replies.addAll(player.popOutgoingMessages());
		}
		return replies;
	}

	/** Shutdown the service. */
	public void shutdown() throws Exception {

		for (Service s : services) {
			s.shutdown();
		}
		jdbc.close();
	}

}

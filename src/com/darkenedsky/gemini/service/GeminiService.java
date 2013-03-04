package com.darkenedsky.gemini.service;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.darkenedsky.gemini.ActionList;
import com.darkenedsky.gemini.Game;
import com.darkenedsky.gemini.GameCharacter;
import com.darkenedsky.gemini.Library;
import com.darkenedsky.gemini.Message;
import com.darkenedsky.gemini.Player;
import com.darkenedsky.gemini.card.CCGDeckService;
import com.darkenedsky.gemini.exception.GeminiException;
import com.darkenedsky.gemini.exception.InvalidActionException;
import com.darkenedsky.gemini.exception.JavaException;

/** The service that processes incoming messages, spawns Games, handles logins and other Sessions, etc.
 *  Extend this class for each game "service" (FKA projects) that you wish to run. */
public class GeminiService<TChar extends GameCharacter, TPlay extends Player, TGame extends Game<TChar>> implements ActionList {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1133934743984044518L;
	
	/** Logger instance */
	private static Logger LOG = Logger.getLogger(GeminiService.class);
	
	/** The JDBC connection to the database. */
	private JDBCConnection jdbc;
	
	/** Manager to store and control session logins/logouts. */
	protected SessionManager<TPlay> sessions;
	
	/** Settings stored from the XML config file, for server configuration and credentials. */
	private Message settings;
	
	/** List of plugins that have been installed in this service to handle things besides games, sessions */
	private Vector<Service> services = new Vector<Service>();
	
	/** The class object for the Players we will spawn to create sessions. */
	private Class<TPlay> playerClass;
	
	/** Hard pointer to the GameCacheService, which will also be in the services list */
	private GameCacheService<TGame> gameCacheService;
	
	/** Construct the Gemini service. 
	 * 
	 * @param theGameClass The class object for the specific Game subclass. Should match TGame.
	 * @param thePlayerClass  The class object for the specific Player subclass. Should match TPlay.
	 * @param lib the library of static game object definitions. Should be defined in the Servlet.
	 * @param settingsFile The filename of the Settings XML file to read.
	 * @throws SQLException
	 * @throws ClassNotFoundException
	 * @throws IOException
	 * @throws Exception
	 */
	public GeminiService(Class<TGame> theGameClass, Class<TPlay> thePlayerClass, Library lib, String settingsFile) throws SQLException, ClassNotFoundException, IOException, Exception { 
				
		playerClass = thePlayerClass;	
		settings = Message.parseXMLFile(settingsFile);	
		LOG.debug("XML : " + settings.getString("loaded_from_file"));
		LOG.debug("Starting up servlet for " + settings.getString("servicename"));
		System.out.println(settings.getString("database_password"));
		jdbc = new JDBCConnection(settings.getString("database_user"), settings.getString("database_password"), settings.getString("database_path"), settings.getString("database_driver"));		
		gameCacheService = new GameCacheService<TGame>(theGameClass, settings, jdbc, lib);
		addService(gameCacheService);
		addService(new AnalyticsService(jdbc));
		
		// deliberately don't add the Session Manager as a service using addService(); its actions behave 
		// differently because the user might not be logged in when interacting with them.
		sessions = new SessionManager<TPlay>(playerClass, jdbc, settings, gameCacheService.getWinLossRecordManager());		
		
		LOG.debug("Gemini Service for game " + settings.getString("servicename") + " initialized OK.");		
	}

		
	/** Accessor for the settings created from the XML settings file. */
	public Message getSettings() { 
		return settings;
	}
	
	/** Accessor for the JDBC connection.
	 *  
	 * @return the JDBC connection object
	 */
	public JDBCConnection getJDBC() { return jdbc; }
	
	/** Shutdown the service. */
	public void shutdown() throws Exception { 
		
		for (Service s : services)  { 
			s.shutdown();
		}
		jdbc.close();
	}
	
	/** Add a new plugin module to the service. 
	 *  
	 * @param plug the new plugin to add.
	 */
	public void addService(Service plug) { 
		plug.init();
		services.add(plug);
		
		// this is really cheating but easiest way to hook this in without a lot of pain
		if (plug instanceof CCGDeckService<?>) { 
			this.gameCacheService.setDeckService((CCGDeckService<?>)plug);
		}
	}

	/** Process an incoming message from the server. Note that this is different from the interface's
	 *  processMessage method, in that this one returns a list of messages that need to get sent back 
	 *  to the client.
	 *  
	 * @param e The incoming message from the client
	 * @return a list of responses for the client.
	 * @throws Exception
	 */
	public Vector<Message> processMessage(Message e) throws Exception { 
		TPlay player = null;
		Vector<Message> replies = new Vector<Message>();
		
		try {
			Integer action = e.getInt(Message.ACTION);
			if (action == null)
				throw new InvalidActionException(action);
			
			// Validate the session token
			// Actions below 100 are reserved for things that do NOT require a session, such as logging in.
			String token = e.getString(Message.SESSION_TOKEN);
			if (action >= 100) { 
				player = sessions.getSession(token);				
				
				// add some stuff to the message that things processing the message might find useful
				e.put(Message.SESSION_PLAYERID, player.getPlayerID());
			}
			
						
			// Pass actions along that need to be passed along
			Long gid = e.getLong("gameid");
			if (gid != null) { 
				TGame game = gameCacheService.getGame(gid);
				if (game != null) { 
					game.processMessage(e, player);
				}
				return player.popOutgoingMessages();															
			}
			
			// if we get here, it's something the server itself is going to need to process.			
			else { 
				switch (action) { 
				case HELLO_WORLD:
					LOG.debug("GeminiService: Fired Action HELLO_WORLD");
					Message m = new Message(HELLO_WORLD);
					m.put("hello", "Hello, world!");
					replies.add(m);
					return replies;
				case LOGIN:
					LOG.debug("GeminiService: Fired Action LOGIN");
					replies.add(sessions.login(e));
					return replies;
				case LOGOUT:
					LOG.debug("GeminiService: Fired Action LOGOUT");
					sessions.logout(token);
					replies.add(new Message(LOGOUT));
					break;
				case VERIFY_EMAIL_REDEEM:
					LOG.debug("GeminiService: Fired Action VERIFY_EMAIL_REDEEM");
					replies.add(sessions.verifyEmailRedeem(e));
					break;
				case VERIFY_EMAIL_REQUEST:
					LOG.debug("GeminiService: Fired Action VERIFY_EMAIL_REQUEST");
					sessions.verifyEmailRequest(e);
					replies.add(new Message(VERIFY_EMAIL_REQUEST));
					break;
				case FORGOTPASS_REDEEM:
					LOG.debug("GeminiService: Fired Action FORGOTPASS_REDEEM");
					replies.add(sessions.forgotPassRedeem(e));
					break;
				case FORGOTPASS_REQUEST:
					LOG.debug("GeminiService: Fired Action FORGOTPASS_REQUEST");
					sessions.forgotPassRequest(e);
					replies.add(new Message(FORGOTPASS_REQUEST));
					break;
				case CREATE_ACCOUNT:
					LOG.debug("GeminiService: Fired Action CREATE_ACCOUNT");
					replies.add(sessions.createAccount(e));
					break;
				
				default:
					// before we crap out, see if the plugins can act
					boolean plugged = false;
					for (Service plug : services) {
						
						if (plug.canProcessAction(action)) { 
							plug.processMessage(e, player);
							plugged = true;
						}						
					}
					// plugins were stumped too
					if (!plugged) { 
						throw new InvalidActionException(action);
					}
				}
			}
		}
		catch (GeminiException re) {
			re.printStackTrace();
			LOG.debug(re);
			replies.add(re.serialize(player));
		}
		catch (Throwable x) { 
			LOG.error(x);
			x.printStackTrace();
			replies.add(new JavaException(x).serialize(player));
		}
		
		if (player != null) { 
			replies.addAll(player.popOutgoingMessages());
		}
		return replies;
	}	
}

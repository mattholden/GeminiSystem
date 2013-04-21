package com.darkenedsky.gemini.service;

import java.lang.reflect.Constructor;
import java.util.concurrent.ConcurrentHashMap;
import com.darkenedsky.gemini.Library;
import com.darkenedsky.gemini.Game;
import com.darkenedsky.gemini.GameCharacter;
import com.darkenedsky.gemini.Message;
import com.darkenedsky.gemini.Player;
import com.darkenedsky.gemini.card.CCGDeckService;
import com.darkenedsky.gemini.card.CardGame;
import com.darkenedsky.gemini.exception.InvalidGameException;
import com.darkenedsky.gemini.handler.Handler;

/** Service to handle the creation, caching, and polling of games. Stores all active games and replies to 
 * messages looking for games to join.
 * 
 * @author Matt Holden
 *
 * @param <TGame>
 */
public class GameCacheService<TGame extends Game<? extends GameCharacter>> extends Service implements GameCacheInterface { 
	
	/** Cache of all the active games by ID. */
	private ConcurrentHashMap<Long, TGame> games = new ConcurrentHashMap<Long, TGame>();

	/** The class object for the games we'll create */
	private Class<TGame> gameClass;
		
	/** The JDBC connection */
	private JDBCConnection jdbc;
	
	/** The Win-Loss Records manager */
	private WinLossRecordManager winLossRecords;
	
	/** Library of all the game object definitions */
	private Library library;
	
	private CCGDeckService<?> deckService;
	
	/** These numbers have to be unique to a server, but only so long as it's running, because without
	 * game-level storage, they'll die if the server does anyway. So just increment a counter. It's ghetto but it
	 * totally works short-term and probably long-term too. 
	 * 
	 * Do not change the start number. It's 112647 for a reason. 
	 **/
	private long nextGameId = 112647;
	
	/** Create the game service.
	 * 
 	 * @param theGameClass The class of TGame, used to create the games.
	 * @param settings Settings from the server XML file.
	 * @param jdbc the JDBC connection from the core GeminiService.
	 * @param lib the library of static game definitions
	 */
	public GameCacheService(Class<TGame> theGameClass, Message settings, JDBCConnection jdbc, Library lib) { 
		gameClass = theGameClass;
		winLossRecords = new WinLossRecordManager(settings, jdbc);
		library = lib;
		
		handlers.put(CREATE_GAME, new Handler() { 
			@Override
			public void processMessage(Message e, Player p) throws Exception { 
				createGame(e, p);				
			}
		});
		
		handlers.put(GET_OPEN_GAMES, new Handler() { 
			@Override
			public void processMessage(Message e, Player p) throws Exception { 
				Message opens = new Message(GET_OPEN_GAMES);
				opens.addList("games");
				for (TGame game : games.values()) { 
					if (game.getState() == CREATE_GAME && !game.isFull()) { 
						opens.addToList("games", game, p);
					}
				}
				p.pushOutgoingMessage(opens);	
			}
		});
		
		handlers.put(POLL, new Handler() { 
			@Override
			public void processMessage(Message e, Player p) throws Exception { 
				Message reply = new Message(POLL);
				reply.addList("games");
				for (Long gameid : p.getCurrentGames()) { 
					reply.addToList("games", games.get(gameid).serialize(p));
				}
				p.pushOutgoingMessage(reply);
			}
		});
				
	}
	
	/** Create a new Game from a CREATE_GAME message.
	 * 
	 * @param e The message commanding us to create the game
	 * @param player The player session sending the message
	 * @return The newly-created game
	 * @throws Exception
	 */
	protected TGame createGame(Message e, Player player) throws Exception {
		Constructor<TGame> con = gameClass.getConstructor(long.class, Message.class, Player.class);
		TGame game = (TGame)con.newInstance(getNextGameID(), e, jdbc, player);
		game.setService(this);
		
		// if a deck service has been set up, and you're a CCG game, add the deck service to the game.
		// This is a little bit sloppy but saves us having to extend both GeminiService and GameCacheService
		// for every game project.
		if (deckService != null && game instanceof CardGame<?,?>) { 
			((CardGame<?,?>)game).setDeckService(deckService);
		}
		
		game.init();
		games.put(game.getGameID(), game);
		return game;
	}

	/** Accessor for a specific game.
	 * 
	 * @param id Game ID to retrieve
	 * @return the game if it exists
	 * @throws InvalidGameException if the game is not found
	 */
	public TGame getGame(long id) { 
		TGame game = games.get(id);
		if (game == null) { 
			throw new InvalidGameException(id);
		}
		return game;
	}
	
	/** Remove a game from the cache.
	 * @param ID the game ID to remove.
	 * 
	 */
	@Override
	public void uncacheGame(long id) { 
		games.remove(id);		
	}
	
	/** Get the next game ID, and increment the counter.
	 * @return the new game's ID
	 */
	private long getNextGameID() { 
		long next = nextGameId;
		nextGameId++;
		return next;
	}

	/** Accessor for the JDBCConnection
	 * @return the JDBC connection object
	 */
	@Override
	public JDBCConnection getJDBC() { 
		return jdbc;
	}
	
	/** Accessor for the Win/Loss Record Manasger. 
	 *  
	 * @return the WinLossRecordManager. 
	 */
	@Override
	public WinLossRecordManager getWinLossRecordManager() {
		return winLossRecords;
	}
	
	/** Accessor for the game library
	 *  @return the game library 
	 */
	@Override
	public Library getLibrary() { 
		return library;
	}

	public void setDeckService(CCGDeckService<?> svc) { 
		this.deckService = svc;
	}
		
}

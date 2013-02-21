package com.darkenedsky.gemini;
import java.lang.reflect.Constructor;
import java.util.Collections;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import com.darkenedsky.gemini.exception.GameFullException;
import com.darkenedsky.gemini.exception.InvalidGamePasswordException;
import com.darkenedsky.gemini.exception.InvalidPlayerException;
import com.darkenedsky.gemini.exception.NotEveryoneIsReadyException;
import com.darkenedsky.gemini.service.GameCacheInterface;
import com.darkenedsky.gemini.service.Service;
import com.darkenedsky.gemini.service.WinLossRecordManager;

/** The core class from which all Games are derived. */
public abstract class Game<TChar extends GameCharacter, TPlayer extends Player> extends Service implements MessageProcessor, MessageSerializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 5189403138537743010L;
	
	/** Name of the game. */
	protected String name;
	
	/** The game's ID. */
	protected long gameID;
	
	/** Give the game control of Object IDs created within it. In this way, we know that no matter what,
	 * objects in the game will have unique IDs within that game. Since we're not going to write in-game data
	 * to the database, this should be perfectly safe.
	 * 
	 * Much kudos given to whomever identifies the meaning of the starting object ID.
	 */
	private transient long nextObjectID = 30822;
		
	/** The current gamestate */
	protected int state = ActionList.CREATE_GAME;
		
	/** List of the player sessions who are playing this game. */
	protected Vector<TPlayer> players = new Vector<TPlayer>();
	
	/** List of the game characters created. */
	protected Vector<TChar> characters = new Vector<TChar>();
	
	/** Class object matching TChar. */
	protected transient Class<TChar> tcharClass;
	
	/** Flags for which players have indicated they're ready to start. Used only during lobby phase. */
	protected ConcurrentHashMap<Long, Boolean> playersReady = new ConcurrentHashMap<Long, Boolean>();
	
	/** The turn number we're on. */
	protected int turnCount = 0;	
	
	/** Maximum players to allow into the game. Used during lobby phase.*/
	private int maxplayers;	
	
	/** Password required to join the game. Used during lobby phase.*/
	private transient String password;
	
	/** The service that cached this game. Done via interface to avoid generics confusion. */
	private transient GameCacheInterface gameCacheService;
	
	/** Current player's index. The index can also be used for things like "pass your hand to the left" - 
	left will mean a lower indexed player, right a higher. */
	private int currentPlayerIndex = -1;
	
	public Game(long gid, Message e, Player p, Class<TChar> tcharClazz) throws Exception { 
		gameID = gid;
		tcharClass = tcharClazz;
		
		Integer maxPlay = e.getInt("maxplayers");
		if (maxPlay == null)
			maxPlay = 2;
		maxplayers = maxPlay;
		name = e.getString("gamename");
		if (name == null)
			name = "Game #" + Long.toString(gameID);
		password = e.getString("password");
	
		// the creator of the game probably wants to play in it
		addPlayer(e, p);
	
		handlers.put(CHAT, new Handler(this) { 
			@Override
			public void processMessage(Message m, Player p) throws Exception  { 
				Message sending = new Message(CHAT, game.getGameID());
				sending.put("from_playerid", p.getPlayerID());
				sending.put("message", m.getString("message"));
				for (TPlayer play : players) { 
					play.pushOutgoingMessage(sending);
				}
			}
		});
		
		handlers.put(WHISPER, new Handler(this) { 
			@Override
			public void processMessage(Message m, Player p)  throws Exception { 
				long pid = m.getLong(Message.TARGET_PLAYER);
				Message sending = new Message(WHISPER, game.getGameID());
				sending.put("from_playerid", p.getPlayerID());
				sending.put("message", m.getString("message"));				
				game.getPlayer(pid).pushOutgoingMessage(sending);			
			}
		});
		
		// Add a player to the game in lobby state
		Handler addPlay = new Handler(this, Handler.ON_ANY_TURN, CREATE_GAME) { 
			@Override
			public void processMessage(Message m, Player p) throws Exception { 
				addPlayer(m,p);
			}
		};
		// add player is the only action you can do when you aren't already a player
		addPlay.setRequiresInGame(Handler.REQUIRES_NO);
		handlers.put(ADD_PLAYER, addPlay);
		
		// Drop a player from the game in lobby state
		handlers.put(DROP_PLAYER, new Handler(this, Handler.ON_ANY_TURN, CREATE_GAME) { 
			@Override
			public void processMessage(Message m, Player p)  throws Exception { 

				clearReadyStatuses();
				
				players.remove(p);
				playersReady.remove(p.getPlayerID());
				p.removeCurrentGame(getGameID());
				sendToAllPlayers(DROP_PLAYER);					
}
		});
		
		// Designate a player as ready to start in lobby state
		handlers.put(SET_READY, new Handler(this, Handler.ON_ANY_TURN, CREATE_GAME) { 
			@Override
			public void processMessage(Message m, Player p)  throws Exception { 
				setReady(m, p);
			}
		});
		
		// start the game.
		handlers.put(START_GAME, new Handler(this, Handler.ON_ANY_TURN, CREATE_GAME) { 
			@Override
			public void processMessage(Message m, Player p) throws Exception {
				
				if (!isEveryoneReady()) { 
					throw new NotEveryoneIsReadyException(game.getGameID());
				}
				startGame();
			}
		});
		
	}

	/** Checks if a game is full. Used so we don't show full but not-yet-started games in GET_OPEN_GAMES. 
	 * @return true if the game is at its max_players.
	 */
	public boolean isFull() { 
		return players.size() == this.maxplayers;
	}
	
	protected void setReady(Message m, Player p) throws Exception { 
		playersReady.put(p.getPlayerID(), m.getBoolean("ready"));
		sendToAllPlayers(SET_READY);				
	}
	
	protected void onGameStart() throws Exception { } 
	protected void onTurnStart() throws Exception { } 
	protected void onTurnEnd() throws Exception { } 
	
	private void startGame() throws Exception { 

		Collections.shuffle(players);
		
		for (TPlayer play : players) { 
			Constructor<TChar> tcon = tcharClass.getConstructor(Player.class);
			characters.add(tcon.newInstance(play));
		}
	
		onGameStart();
		startNewTurn();
	}
	
	
	/** Get the next game object ID, and increment the counter. 
	 * @return the next game object ID
	 */
	public long getNextObjectID() { 
		long id = this.nextObjectID;
		nextObjectID++;
		return id;
	}
	
	public Class<TChar> getTcharClass() {
		return tcharClass;
	}

	public void setTcharClass(Class<TChar> tcharClass) {
		this.tcharClass = tcharClass;
	}

	public GameCacheInterface getService() { 
		return this.gameCacheService;
	}
	
	public void setService(GameCacheInterface callback) { 
		this.gameCacheService = callback;
	}
	
	public long getGameID() { return gameID; }
	public String getName() { return name; }
	public String toString() { return gameID + " : " + name; }
	public int getState() { return state; }
	public Vector<TPlayer> getPlayers() { return players; }
	
	@SuppressWarnings("unchecked")
	private void addPlayer(Message m, Player p) throws Exception { 
		if (playersReady.size() >= maxplayers)
			throw new GameFullException(gameID);
		
		if (password != null) { 
			String pass = m.getString("password");
			if (pass == null) throw new InvalidGamePasswordException();
			else if (!pass.equals(password)) throw new InvalidGamePasswordException();
		}			
		p.addCurrentGame(getGameID());
		players.add((TPlayer)p);
		playersReady.put(p.getPlayerID(), false);
		clearReadyStatuses();
		sendToAllPlayers(ADD_PLAYER);
	}

	private void clearReadyStatuses() { 
		for (Long l : playersReady.keySet()) { 
			playersReady.put(l, false);
		}
	}
	
	private boolean isEveryoneReady() { 
		for (Boolean b : playersReady.values()) { 
			if (!b) { 
				return false;
			}
		}
		return true;
	}
	
	
	public TPlayer getPlayer(long target) { 
		for (TPlayer c : players) { 
			if (c.getPlayerID() == target)
				return c;			
		}
		throw new InvalidPlayerException(target);
	}

		
	@Override
	public Message serialize(Player player) {
		
		Message m = new Message();
		m.put("gameid", gameID);
		m.put("name", name);
		m.put("state", state);
		
		// Only serialize the game creation stuff if we're in game creation mode.
		if (state == CREATE_GAME) { 
			m.put("maxplayers", maxplayers);
			m.put("passwordprotected", (password!=null));
			m.addList("ready");
			m.addList("unready");
		
			for (Long l : playersReady.keySet()) { 
				if (playersReady.get(l))
					m.addToList("ready", getPlayer(l).serialize(player));
				else
					m.addToList("unready", getPlayer(l).serialize(player));
			}
		}
		// If we're playing, serialize play data.
		else { 
			m.addList("characters");
			for (TChar c : characters) { 
				m.addToList("characters", c.serialize(player));
			}
			m.put("currentplayer", getCurrentPlayer());
		}
		return m;
	}
		
	
	public void sendToAllPlayers(int action) { sendToAllPlayers(action, this, "game"); }
	
	public void sendToAllPlayers(int action, MessageSerializable object, String objectTag) { 
		for (TPlayer play : players) { 
			Message m = new Message(action, getGameID());
			m.put(objectTag, object, play);
			play.pushOutgoingMessage(m);
		}
	}
	
	public Vector<TChar> getCharacters() { return characters; }
	
	public TChar getCharacter(long target) { 
		for (TChar c : characters) { 
			if (c.getPlayer().getPlayerID() == target)
				return c;			
		}
		throw new InvalidPlayerException(target);
	}
	
	public long getCurrentPlayer() { 
		return characters.get(currentPlayerIndex).getPlayer().getPlayerID();
	}
	
	public boolean isCurrentPlayer(long pid) { 
		return (pid == getCurrentPlayer());
	}

	/** Initialize a new turn. */
	protected void startNewTurn() throws Exception { 
		  		
		onTurnEnd();
		for (TChar ch : characters) { 
			ch.onTurnEnd();
		}
		
		// somebody won, let's end this.
		if (checkForWin() == null) { 
			return;
		}
		
		turnCount++;
		currentPlayerIndex++;
		if (currentPlayerIndex == characters.size())
			currentPlayerIndex = 0;
		state = TURN_START;
	
		onTurnStart();		
		for (TChar ch : characters) { 
			ch.onTurnStart();
		}
		
		sendToAllPlayers(TURN_START);
		
	}


	/** 
	 * Check to see if anyone has won the game. Do this at the start of each phase; instants can happen anytime.
	 * @return a GameResult of who won/lost if someone did, or null if the game is not yet over. 
	 */
	protected abstract GameResult<TChar> getGameResult();
	
	/** Checks to see if we have a winner or a draw. If we do, ends the game. 
	 *   
	 * @return The game result (who won/lost/drew), or null if the game isn't over yet.
	 * @throws Exception
	 */
	public GameResult<TChar> checkForWin() throws Exception  { 
		
		GameResult<TChar> result = getGameResult();
		if (result == null) return null;
		
		// if we reach this point the game is over
		state = ActionList.GAME_END;		
		
		WinLossRecordManager winLossRecords = gameCacheService.getWinLossRecordManager();
		for (TChar c : result.getWinners())
			winLossRecords.win(c.getPlayer().getPlayerID());
		for (TChar c : result.getLosers())
			winLossRecords.lose(c.getPlayer().getPlayerID());
		for (TChar c : result.getDrawers())
			winLossRecords.draw(c.getPlayer().getPlayerID());
		
		for (TPlayer p : players) { 
			p.removeCurrentGame(getGameID());
		}
		
		sendToAllPlayers(ActionList.GAME_END, result, "result");			
		gameCacheService.uncacheGame(gameID);
		return result;
	}

	
}

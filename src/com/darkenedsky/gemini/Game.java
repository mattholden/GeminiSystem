package com.darkenedsky.gemini;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import com.darkenedsky.gemini.exception.GameFullException;
import com.darkenedsky.gemini.exception.InvalidGamePasswordException;
import com.darkenedsky.gemini.exception.InvalidPlayerException;
import com.darkenedsky.gemini.handler.AddPlayerHandler;
import com.darkenedsky.gemini.handler.ChatHandler;
import com.darkenedsky.gemini.handler.DropPlayerHandler;
import com.darkenedsky.gemini.handler.ExecuteBlockingHandler;
import com.darkenedsky.gemini.handler.ForfeitHandler;
import com.darkenedsky.gemini.handler.GameHandler;
import com.darkenedsky.gemini.handler.Handler;
import com.darkenedsky.gemini.handler.SessionValidator;
import com.darkenedsky.gemini.handler.SetReadyHandler;
import com.darkenedsky.gemini.handler.StartGameHandler;
import com.darkenedsky.gemini.handler.TurnEndHandler;
import com.darkenedsky.gemini.handler.WhisperHandler;
import com.darkenedsky.gemini.service.GameCacheInterface;
import com.darkenedsky.gemini.service.Service;
import com.darkenedsky.gemini.service.WinLossRecordManager;

/** The core class from which all Games are derived. */
public abstract class Game<TChar extends GameCharacter> extends Service implements MessageProcessor, MessageSerializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5189403138537743010L;

	/** Handlers "blocking" a player - used for client callbacks */
	private HashMap<Long, Handler> blockingHandlers = new HashMap<Long, Handler>();

	/** List of the game characters created. */
	protected Vector<TChar> characters = new Vector<TChar>();

	/**
	 * Current player's index. The index can also be used for things like
	 * "pass your hand to the left" - left will mean a lower indexed player,
	 * right a higher.
	 */
	private int currentPlayerIndex = -1;

	/**
	 * The service that cached this game. Done via interface to avoid generics
	 * confusion.
	 */
	private transient GameCacheInterface gameCacheService;

	/** The game's ID. */
	protected long gameID;

	/** Maximum players to allow into the game. Used during lobby phase. */
	private int maxplayers;

	/** Name of the game. */
	protected String name;

	/**
	 * Give the game control of Object IDs created within it. In this way, we
	 * know that no matter what, objects in the game will have unique IDs within
	 * that game. Since we're not going to write in-game data to the database,
	 * this should be perfectly safe.
	 * 
	 * Much kudos given to whomever identifies the meaning of the starting
	 * object ID.
	 */
	private transient long nextObjectID = 30822;

	/** Password required to join the game. Used during lobby phase. */
	private transient String password;

	/** List of the player sessions who are playing this game. */
	protected Vector<Player> players = new Vector<Player>();

	/**
	 * Flags for which players have indicated they're ready to start. Used only
	 * during lobby phase.
	 */
	protected ConcurrentHashMap<Long, Boolean> playersReady = new ConcurrentHashMap<Long, Boolean>();

	/** The current gamestate */
	protected int state = ActionList.CREATE_GAME;

	/** Class object matching TChar. */
	protected transient Class<TChar> tcharClass;

	/** The turn number we're on. */
	protected int turnCount = 0;

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

		addHandler(CHAT, new ChatHandler());
		addHandler(WHISPER, new WhisperHandler());
		addHandler(ADD_PLAYER, new AddPlayerHandler());
		addHandler(DROP_PLAYER, new DropPlayerHandler());
		addHandler(SET_READY, new SetReadyHandler());
		addHandler(START_GAME, new StartGameHandler());
		addHandler(FORFEIT, new ForfeitHandler());
		addHandler(TURN_END, new TurnEndHandler());

		Handler h = new ExecuteBlockingHandler();
		addHandler(CHOOSER, h);
		addHandler(CONFIRM_YES_NO, h);
		addHandler(CONFIRM_OK_CANCEL, h);
		addHandler(CALLBACK, h);

		// do nothing handler will "get the game state" -
		// the service will then send the default game state message after this.
		GameHandler gameState = new GameHandler() {
			@Override
			public void processMessage(Message m, Player p) throws Exception {
			}
		};
		gameState.addValidator(new SessionValidator());
		addHandler(GAME_STATE, gameState);

	}

	public void addPlayer(Message m, Player p) throws Exception {
		if (playersReady.size() >= maxplayers)
			throw new GameFullException(gameID);

		if (password != null) {
			String pass = m.getString("password");
			if (pass == null)
				throw new InvalidGamePasswordException();
			else if (!pass.equals(password))
				throw new InvalidGamePasswordException();
		}
		p.addCurrentGame(getGameID());
		players.add(p);
		playersReady.put(p.getPlayerID(), false);
		clearReadyStatuses();
		Message reply = new Message(TURN_START, getGameID(), p.getPlayerID());
		for (Player play : getPlayers()) {
			play.pushOutgoingMessage(reply);
		}

	}

	/**
	 * Checks to see if we have a winner or a draw. If we do, ends the game.
	 * 
	 * @return The game result (who won/lost/drew), or null if the game isn't
	 *         over yet.
	 * @throws Exception
	 */
	public GameResult<TChar> checkForWin() throws Exception {

		GameResult<TChar> result = getGameResult();
		if (result == null)
			return null;

		// if we reach this point the game is over
		state = ActionList.GAME_END;

		WinLossRecordManager winLossRecords = gameCacheService.getWinLossRecordManager();
		for (TChar c : result.getWinners())
			c.getPlayer().setRecord(winLossRecords.win(c.getPlayer().getPlayerID()));
		for (TChar c : result.getLosers())
			c.getPlayer().setRecord(winLossRecords.lose(c.getPlayer().getPlayerID()));
		for (TChar c : result.getDrawers())
			c.getPlayer().setRecord(winLossRecords.draw(c.getPlayer().getPlayerID()));

		for (Player p : players) {
			p.removeCurrentGame(getGameID());
		}

		Message m = new Message(GAME_END, getGameID(), null);
		m.put("result", result, null);
		for (Player p : getPlayers()) {
			p.pushOutgoingMessage(m);
		}

		gameCacheService.uncacheGame(gameID);
		this.shutdown();
		return result;
	}

	private void clearReadyStatuses() {
		for (Long l : playersReady.keySet()) {
			playersReady.put(l, false);
		}
	}

	public void dropPlayer(Player p) {
		clearReadyStatuses();
		players.remove(p);
		playersReady.remove(p.getPlayerID());
		p.removeCurrentGame(getGameID());
		Message reply = new Message(DROP_PLAYER, getGameID(), p.getPlayerID());
		for (Player play : getPlayers()) {
			play.pushOutgoingMessage(reply);
		}

	}

	/**
	 * Get the blocking handler for a player
	 * 
	 * @param pid
	 *            player id
	 */
	public Handler getBlockingHandler(long pid) {
		return blockingHandlers.get(pid);
	}

	public TChar getCharacter(long target) {
		for (TChar c : characters) {
			if (c.getPlayer().getPlayerID() == target)
				return c;
		}
		throw new InvalidPlayerException(target);
	}

	public Vector<TChar> getCharacters() {
		return characters;
	}

	public long getCurrentPlayer() {
		return characters.get(currentPlayerIndex).getPlayer().getPlayerID();
	}

	public long getGameID() {
		return gameID;
	}

	/**
	 * Check to see if anyone has won the game. Do this at the start of each
	 * phase; instants can happen anytime.
	 * 
	 * @return a GameResult of who won/lost if someone did, or null if the game
	 *         is not yet over.
	 */
	protected GameResult<TChar> getGameResult() {

		ArrayList<TChar> alive = new ArrayList<TChar>();
		ArrayList<TChar> dead = new ArrayList<TChar>();
		for (TChar chr : this.getCharacters()) {

			if (chr.isEliminated()) {
				dead.add(chr);
			} else
				alive.add(chr);
		}
		if (alive.size() > 1)
			return null;

		boolean draw = alive.isEmpty();
		GameResult<TChar> gr = new GameResult<TChar>(this.getGameID());

		if (draw) {
			for (TChar c : getCharacters()) {
				gr.addDrawer(c);
			}
		} else {
			for (TChar c : alive) {
				gr.addWinner(c);
			}
			for (TChar c : dead) {
				gr.addLoser(c);
			}
		}
		return gr;

	}

	public String getName() {
		return name;
	}

	/**
	 * Get the next game object ID, and increment the counter.
	 * 
	 * @return the next game object ID
	 */
	public long getNextObjectID() {
		long id = this.nextObjectID;
		nextObjectID++;
		return id;
	}

	public Player getPlayer(long target) {
		for (Player c : players) {
			if (c.getPlayerID() == target)
				return c;
		}
		throw new InvalidPlayerException(target);
	}

	public Vector<Player> getPlayers() {
		return players;
	}

	public GameCacheInterface getService() {
		return this.gameCacheService;
	}

	public int getState() {
		return state;
	}

	public Class<TChar> getTcharClass() {
		return tcharClass;
	}

	public int getTurnCount() {
		return turnCount;
	}

	public boolean isCurrentPlayer(long pid) {
		return (pid == getCurrentPlayer());
	}

	public boolean isEveryoneReady() {
		for (Boolean b : playersReady.values()) {
			if (!b) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Checks if a game is full. Used so we don't show full but not-yet-started
	 * games in GET_OPEN_GAMES.
	 * 
	 * @return true if the game is at its max_players.
	 */
	public boolean isFull() {
		return players.size() == this.maxplayers;
	}

	public void observeCharacterUpdated(TChar chr) throws Exception {
		for (Player p : getPlayers()) {
			Message m = new Message(ActionList.CHARACTER_UPDATED, getGameID(), getCurrentPlayer());
			Message mObj = chr.serialize(p);
			if (mObj == null)
				continue;
			m.put("updated", mObj, p);
			p.pushOutgoingMessage(m);
		}
	}

	public void observeObjectUpdated(GameObject obj) throws Exception {
		for (Player p : getPlayers()) {
			Message m = new Message(ActionList.OBJECT_UPDATED, getGameID(), getCurrentPlayer());
			Message mObj = obj.serialize(p);
			if (mObj == null)
				continue;
			m.put("updated", mObj, p);
			p.pushOutgoingMessage(m);
		}
	}

	protected void onGameStart() throws Exception {
	}

	protected void onTurnEnd() throws Exception {
	}

	protected void onTurnStart() throws Exception {
	}

	/**
	 * Send the game state after any other messages, to make sure you didn't
	 * miss anything
	 */
	@Override
	public void processMessage(Message m, Player p) throws Exception {
		super.processMessage(m, p);

		// these can't change the gamestate
		int action = m.getInt("action");
		if (action == CHAT || action == WHISPER)
			return;

		pushGameState(p);
	}

	/*
	 * public void sendToSomePlayers(int action, MessageSerializable object,
	 * String objectTag, Long sender, long... toSendTo) { for (Long to :
	 * toSendTo) { Player play = getPlayer(to); Message m = new Message(action,
	 * getGameID(), sender); m.put("game", this, play); if (object != null)
	 * m.put(objectTag, object, play); play.pushOutgoingMessage(m); } } public
	 * void sendToAllPlayers(int action, Long sender) { sendToAllPlayers(action,
	 * null, "game", sender); } public void sendToSomePlayers(int action, Long
	 * sender, long... to) { sendToSomePlayers(action, null, "game", sender,
	 * to); }
	 * 
	 * public void sendToAllPlayers(int action, MessageSerializable object,
	 * String objectTag, Long sender) { long[] playz = new
	 * long[getPlayers().size()]; for (int i = 0; i < getPlayers().size(); i++)
	 * { playz[i] = getPlayers().get(i).getPlayerID(); }
	 * sendToSomePlayers(action, object, objectTag, sender, playz); }
	 */

	private void pushGameState(Player p) {
		// send the game state...
		Message gstate = new Message(GAME_STATE, getGameID(), p.getPlayerID());
		gstate.put("game", this, p);
		p.pushOutgoingMessage(gstate);
	}

	@Override
	public Message serialize(Player player) {

		Message m = new Message();
		m.put("gameid", gameID);
		m.put("name", name);
		m.put("state", state);

		// Only serialize the game creation stuff if we're in game creation
		// mode.
		if (state == CREATE_GAME) {
			m.put("maxplayers", maxplayers);
			m.put("passwordprotected", (password != null));
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

	/**
	 * Add a blocking handler
	 * 
	 * @param player
	 *            Player to block
	 * @param hand
	 *            Handler to block on
	 */
	public void setBlockingHandler(Player player, Handler hand) {
		blockingHandlers.put(player.getPlayerID(), hand);
	}

	public void setReady(Message m, Player p) throws Exception {
		playersReady.put(p.getPlayerID(), m.getBoolean("ready"));
		Message reply = new Message(SET_READY, getGameID(), p.getPlayerID());
		for (Player play : getPlayers()) {
			play.pushOutgoingMessage(reply);
		}
	}

	public void setService(GameCacheInterface callback) {
		this.gameCacheService = callback;
		this.server = callback.getServer();
	}

	public void setTcharClass(Class<TChar> tcharClass) {
		this.tcharClass = tcharClass;
	}

	public void startGame() throws Exception {

		Collections.shuffle(players);

		for (Player play : players) {
			Constructor<TChar> tcon = tcharClass.getConstructor(Player.class);
			TChar chr = tcon.newInstance(play);
			characters.add(chr);
			chr.setGame(this);
			chr.onGameStart();
		}
	}

	/** Initialize a new turn. */
	public void startNewTurn() throws Exception {

		if (turnCount > 0) {
			characters.get(currentPlayerIndex).validateYourTurnEnd();

			onTurnEnd();
			for (TChar ch : characters) {
				ch.onTurnEnd();
			}
			characters.get(currentPlayerIndex).onYourTurnEnd();

			// somebody won, let's end this.
			if (checkForWin() == null) {
				return;
			}
		}

		turnCount++;
		currentPlayerIndex++;
		if (currentPlayerIndex == characters.size())
			currentPlayerIndex = 0;
		state = TURN_START;

		// skip the turn of someone who is eliminated
		if (getCharacter(getCurrentPlayer()).isEliminated()) {
			startNewTurn();
		} else {
			onTurnStart();
			for (TChar ch : characters) {
				ch.onTurnStart();
			}
			characters.get(currentPlayerIndex).onYourTurnStart();

			Message m = new Message(TURN_START, getGameID(), getCurrentPlayer());
			for (Player p : getPlayers()) {
				p.pushOutgoingMessage(m);
			}

		}
	}

	@Override
	public String toString() {
		return gameID + " : " + name;
	}

}

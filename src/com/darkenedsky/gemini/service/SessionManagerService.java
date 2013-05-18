package com.darkenedsky.gemini.service;

import java.io.File;
import java.lang.reflect.Constructor;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import com.darkenedsky.gemini.ActionList;
import com.darkenedsky.gemini.Message;
import com.darkenedsky.gemini.Player;
import com.darkenedsky.gemini.badge.BadgeService;
import com.darkenedsky.gemini.exception.GeminiException;
import com.darkenedsky.gemini.exception.InvalidEmailTemplateException;
import com.darkenedsky.gemini.exception.InvalidLoginException;
import com.darkenedsky.gemini.exception.InvalidPlayerException;
import com.darkenedsky.gemini.exception.InvalidSessionException;
import com.darkenedsky.gemini.guild.GuildService;
import com.darkenedsky.gemini.tools.FileTools;
import com.darkenedsky.gemini.tools.StringTools;

/**
 * Class to manage session data. The one and only instance will be owned by the
 * GeminiService. This is primarily done to keep all "login module"-related code
 * isolated from the game lobby and main server.
 * 
 * @author Matt Holden
 * 
 * @param <TPlay>
 *            Player type
 */
public class SessionManagerService<TPlay extends Player> extends Service implements ActionList {

	private static final Logger LOG = Logger.getLogger(SessionManagerService.class);

	/** The class object for the Player sessions to generate. */
	private Class<TPlay> playerClass = null;

	/** Cache of all the currently logged-in sessions. */
	private ConcurrentHashMap<String, TPlay> sessions = new ConcurrentHashMap<String, TPlay>();

	/**
	 * Construct the Session Manager.
	 * 
	 * @param pClass
	 *            The class object for the Player subclass to generate.
	 */
	public SessionManagerService(Class<TPlay> pClass) {
		playerClass = pClass;
	}

	/**
	 * Load the player for a session token and cache it.
	 * 
	 * @param token
	 *            The token to match the player to.
	 * @return The newly-created session object.
	 * @throws Exception
	 */
	private TPlay addSession(String token) throws Exception {
		JDBCConnection jdbc = getServer().getJDBC();
		PreparedStatement ps2 = jdbc.prepareStatement("select * from playeraccounts join players on (players.playerid = playeraccounts.playerid) where sessiontoken = ?;");
		ps2.setString(1, token);
		TPlay player = null;
		ResultSet set2 = null;
		try {
			set2 = ps2.executeQuery();

			if (set2.first()) {
				Constructor<TPlay> con = playerClass.getConstructor(ResultSet.class);
				player = con.newInstance(set2);

				// TODO: Replace existing login if the user is already logged in
				sessions.put(token, player);
				set2.close();

				Service svc = getServer().getService(GuildService.class);
				GuildService guildService = (svc == null) ? null : (GuildService) svc;
				svc = getServer().getService(BadgeService.class);
				BadgeService badgeService = (svc == null) ? null : (BadgeService) svc;
				svc = getServer().getService(GameCacheService.class);
				GameCacheService<?> gameService = (svc == null) ? null : (GameCacheService<?>) svc;

				if (gameService != null && gameService.getWinLossRecordManager() != null)
					player.setRecord(gameService.getWinLossRecordManager().get(player.getPlayerID()));

				if (guildService != null && player.getGuildID() != null)
					player.setGuild(guildService.getGuild(player.getGuildID()));

				if (badgeService != null)
					player.setBadges(badgeService.loadBadges(player.getPlayerID()));

				return player;
			} else {
				throw new InvalidLoginException();
			}
		} catch (Exception x) {
			if (set2 != null)
				set2.close();
			throw x;
		}
	}

	@Override
	public boolean canProcessAction(Message msg) {
		int action = msg.getRequiredInt(Message.ACTION);

		int[] actions = { HELLO_WORLD, LOGIN, LOGOUT, VERIFY_EMAIL_REDEEM, VERIFY_EMAIL_REQUEST, FORGOTPASS_REDEEM, FORGOTPASS_REQUEST, CREATE_ACCOUNT };
		for (int a : actions)
			if (a == action)
				return true;
		return false;
	}

	/**
	 * Create an account.
	 * 
	 * @param e
	 *            the message for creation
	 * @return e the logged-in message
	 */
	public Message createAccount(Message e) throws Exception {
		JDBCConnection jdbc = getServer().getJDBC();

		PreparedStatement ps = jdbc.prepareStatement("select * from create_account(?,?,?,?,?,?,?,?,?);");
		ps.setString(1, e.getString("username"));
		ps.setString(2, e.getString("password1"));
		ps.setString(3, e.getString("password2"));
		ps.setString(4, e.getString("email"));
		ps.setBoolean(5, e.getBoolean("coppa"));
		ps.setString(6, e.getString(Message.SESSION_IPADDRESS));

		String client = e.getString("client");
		if (client == null)
			ps.setNull(7, Types.VARCHAR);
		else
			ps.setString(7, e.getString("client"));

		Integer gender = e.getInt("gender");
		if (gender == null)
			gender = 0;
		ps.setInt(8, gender);
		String lang = e.getString("language");
		if (lang == null)
			lang = "en";
		ps.setString(9, lang);

		ResultSet set = ps.executeQuery();
		try {
			if (!set.first())
				throw new InvalidLoginException();
			String token = set.getString("create_account");
			set.close();
			set = null;
			if (token == null)
				throw new InvalidLoginException();

			// verify email if that setting is turned on. The token will be
			// generated regardless.
			verifyEmailRequest(e);

			addSession(token);

			// send the welcome email
			// setting this up is optional so just log that there was no
			// template if there wasn't
			try {
				HashMap<String, String> fields = new HashMap<String, String>();
				getServer().getEmailFactory().sendEmail(e.getString("email"), "createaccount", lang, fields);
			} catch (InvalidEmailTemplateException x) {
				LOG.warn("Email template \"createaccount\" missing. No welcome email sent to new user.");
			}

			return loginMessage(token, CREATE_ACCOUNT);

		} catch (Exception x) {
			if (set != null)
				set.close();
			throw (x);
		}
	}

	/**
	 * Redeem a "forgot password" token. Will also log you in.
	 * 
	 * @param e
	 *            the message.
	 * @return the result of the login.
	 */
	public Message forgotPassRedeem(Message e) throws Exception {
		JDBCConnection jdbc = getServer().getJDBC();

		PreparedStatement ps = jdbc.prepareStatement("select * from forgotpass_redeem(?,?,?,?,?,?);");
		ps.setString(1, e.getString("email"));
		ps.setString(2, e.getString("password"));
		ps.setString(3, e.getString("password2"));
		ps.setString(4, e.getString("token"));
		ps.setString(5, e.getString(Message.SESSION_IPADDRESS));
		String client = e.getString("client");
		if (client == null)
			ps.setNull(6, Types.VARCHAR);
		else
			ps.setString(6, e.getString("client"));

		ResultSet set = ps.executeQuery();
		try {
			if (!set.first())
				throw new InvalidLoginException();
			String token = set.getString("forgotpass_redeem");
			set.close();
			set = null;
			if (token == null)
				throw new InvalidLoginException();
			addSession(token);
			return loginMessage(token, FORGOTPASS_REDEEM);
		} catch (Exception x) {
			if (set != null)
				set.close();
			throw (x);
		}
	}

	/**
	 * Request a "forgot password" email.
	 * 
	 * @param e
	 *            the message
	 */
	public void forgotPassRequest(Message e) throws Exception {
		JDBCConnection jdbc = getServer().getJDBC();

		PreparedStatement ps = jdbc.prepareStatement("select * from forgotpass_maketoken(?,?);");
		ps.setString(1, e.getString("email"));
		ps.setString(2, e.getString(Message.SESSION_IPADDRESS));

		ResultSet set = ps.executeQuery();
		try {
			if (!set.first())
				throw new InvalidLoginException();
			String token = set.getString("forgotpass_maketoken");
			set.close();
			set = null;
			if (token == null)
				throw new InvalidLoginException();

			// send the email containing the token
			HashMap<String, String> fields = new HashMap<String, String>();
			fields.put("%FORGOTPASSTOKEN%", token);

			getServer().getEmailFactory().sendEmail(e.getString("email"), "forgotpassrequest", e.getString("language"), fields);

		} catch (Exception x) {
			if (set != null)
				set.close();
			throw (x);
		}

	}

	public Vector<Player> getPlayersInGuild(long guildid) {
		Vector<Player> v = new Vector<Player>();
		for (Player p : this.sessions.values()) {
			if (p.getGuildID() == guildid)
				v.add(p);
		}
		return v;
	}

	public TPlay getSession(long playerid) throws InvalidSessionException {
		for (TPlay play : sessions.values()) {
			if (play.getPlayerID() == playerid)
				return play;
		}
		throw new InvalidPlayerException(playerid);
	}

	/**
	 * Get the session for a token presented from an incoming client message.
	 * 
	 * @param token
	 *            The token presented.
	 * @return The logged-in player session.
	 * @throws InvalidSessionException
	 */
	public TPlay getSession(String token) throws InvalidSessionException {
		if (token == null)
			throw new InvalidSessionException(null);

		TPlay player = sessions.get(token);
		if (player == null)
			throw new InvalidSessionException(token);
		return player;
	}

	/**
	 * Load the player from the database
	 * 
	 * @param playerid
	 *            ID to load
	 */
	public Player loadPlayerFromDatabase(long playerid) throws Exception {
		Player player;
		JDBCConnection jdbc = getServer().getJDBC();

		PreparedStatement kick = jdbc.prepareStatement("select * from players where playerid = ?;");
		kick.setLong(1, playerid);
		ResultSet kickset = null;
		try {
			kickset = kick.executeQuery();
			if (kickset.first()) {
				player = new Player(kickset);
				kickset.close();
				return player;
			} else {
				throw new InvalidPlayerException(playerid);
			}
		} catch (Exception y) {
			if (kickset != null)
				kickset.close();
			throw y;
		}
	}

	/**
	 * Log in, generating a session.
	 * 
	 * @param e
	 *            the login message
	 * @return the reply message.
	 * @throws Exception
	 */
	public Message login(Message e) throws Exception {
		JDBCConnection jdbc = getServer().getJDBC();

		PreparedStatement ps = jdbc.prepareStatement("select * from log_in(?,?,?,?);");
		ps.setString(1, e.getString("username"));
		ps.setString(2, e.getString("password"));
		ps.setString(3, e.getString(Message.SESSION_IPADDRESS));
		String client = e.getString("client");
		if (client == null)
			ps.setNull(4, Types.VARCHAR);
		else
			ps.setString(4, e.getString("client"));

		ResultSet set = ps.executeQuery();
		try {
			if (!set.first())
				throw new InvalidLoginException();
			String token = set.getString("log_in");
			set.close();
			set = null;
			if (token == null)
				throw new InvalidLoginException();

			addSession(token);
			return loginMessage(token, LOGIN);
		} catch (SQLException x) {
			if (set != null)
				set.close();
			throw (GeminiException.translateSQLException(x));
		}

	}

	/**
	 * Builder for the login reply message.
	 * 
	 * @param token
	 *            The token presented
	 * @param action
	 *            The action to present in the reply message
	 * @return The generated message.
	 * @throws Exception
	 */
	private Message loginMessage(String token, int action) throws Exception {
		Message retval = new Message(action);
		retval.put("session", token);
		TPlay player = getSession(token);
		retval.put("playerid", player.getPlayerID());
		return retval;
	}

	/**
	 * Log off the server
	 * 
	 * @param token
	 *            the session token to kill.
	 */
	public void logout(String token) {

		TPlay player = sessions.get(token);
		if (player != null) {
			sessions.remove(token);
		}
	}

	public Message processSessionlessMessage(Message m, Player p) throws Exception {
		int action = m.getRequiredInt(Message.ACTION);
		switch (action) {
		case HELLO_WORLD:
			LOG.debug("SessionManagerService: Fired Action HELLO_WORLD");
			Message msg = new Message(HELLO_WORLD);
			msg.put("hello", "Hello, world!");
			return msg;

		case LOGIN:
			LOG.debug("SessionManagerService: Fired Action LOGIN");
			return (login(m));

		case LOGOUT:
			LOG.debug("SessionManagerService: Fired Action LOGOUT");
			logout(m.getString(Message.SESSION_TOKEN));
			return (new Message(LOGOUT));

		case VERIFY_EMAIL_REDEEM:
			LOG.debug("SessionManagerService: Fired Action VERIFY_EMAIL_REDEEM");
			return (verifyEmailRedeem(m));

		case VERIFY_EMAIL_REQUEST:
			LOG.debug("SessionManagerService: Fired Action VERIFY_EMAIL_REQUEST");
			verifyEmailRequest(m);
			return (new Message(VERIFY_EMAIL_REQUEST));

		case FORGOTPASS_REDEEM:
			LOG.debug("SessionManagerService: Fired Action FORGOTPASS_REDEEM");
			return (forgotPassRedeem(m));

		case FORGOTPASS_REQUEST:
			LOG.debug("SessionManagerService: Fired Action FORGOTPASS_REQUEST");
			forgotPassRequest(m);
			return (new Message(FORGOTPASS_REQUEST));

		case CREATE_ACCOUNT:
			LOG.debug("SessionManagerService: Fired Action CREATE_ACCOUNT");
			return (createAccount(m));
		}

		return null;
	}

	/**
	 * Redeem a "forgot password" token. Will also log you in.
	 * 
	 * @param e
	 *            the message.
	 * @return the result of the verification.
	 */
	public Message verifyEmailRedeem(Message e) throws Exception {
		JDBCConnection jdbc = getServer().getJDBC();

		PreparedStatement ps = jdbc.prepareStatement("select * from verifyemail_redeem(?,?,?);");
		ps.setString(1, e.getString("email"));
		ps.setString(2, e.getString("token"));
		ps.setString(3, e.getString(Message.SESSION_IPADDRESS));
		ps.execute();
		return loginMessage(e.getString("session"), VERIFY_EMAIL_REDEEM);
	}

	/**
	 * Send a request to verify email addresses.
	 * 
	 * @param e
	 *            the email for the request
	 */
	public void verifyEmailRequest(Message e) throws Exception {

		Message settings = getServer().getSettings();
		JDBCConnection jdbc = getServer().getJDBC();

		// if we don't validate emails, disregard
		if (!settings.getBoolean("verify-user-emails"))
			return;

		PreparedStatement ps = jdbc.prepareStatement("select * from verifyemail_maketoken(?,?);");
		ps.setString(1, e.getString("email"));
		ps.setString(2, e.getString(Message.SESSION_IPADDRESS));

		ResultSet set = ps.executeQuery();
		try {
			if (!set.first())
				throw new InvalidLoginException();
			String token = set.getString("verifyemail_maketoken");
			set.close();
			set = null;
			if (token == null)
				throw new InvalidLoginException();

			// send the email containing the token
			String htmlEmail = new String(FileTools.getFileBytes(new File(settings.getString("validate-email-html-template"))));
			htmlEmail = StringTools.stringReplace(htmlEmail, "%VALIDATETOKEN%", token);
			String textEmail = new String(FileTools.getFileBytes(new File(settings.getString("validate-email-text-template"))));
			textEmail = StringTools.stringReplace(htmlEmail, "%VALIDATETOKEN%", token);
			new Email(settings, e.getString("email"), settings.getString("validate-email-subject"), textEmail, htmlEmail).send();

		} catch (Exception x) {
			if (set != null)
				set.close();
			throw (x);
		}

	}
}

package com.darkenedsky.gemini.service;

import java.io.File;
import java.lang.reflect.Constructor;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.concurrent.ConcurrentHashMap;

import com.darkenedsky.gemini.ActionList;
import com.darkenedsky.gemini.Message;
import com.darkenedsky.gemini.Player;
import com.darkenedsky.gemini.exception.InvalidLoginException;
import com.darkenedsky.gemini.exception.InvalidSessionException;
import com.darkenedsky.gemini.tools.FileTools;
import com.darkenedsky.gemini.tools.StringTools;
import com.darkenedsky.gemini.tools.XMLTools;

/** Class to manage session data. The one and only instance will be owned by the GeminiService. This is primarily done to keep all 
 * "login module"-related code isolated from the game lobby and main server.
 * 
 * @author Matt Holden
 *
 * @param <TPlay> Player type
 */
class SessionManager<TPlay extends Player> implements ActionList {

	/** Cache of all the currently logged-in sessions. */
	private ConcurrentHashMap<String, TPlay> sessions = new ConcurrentHashMap<String, TPlay>();
	
	/** The class object for the Player sessions to generate. */
	private Class<TPlay> playerClass = null;
	
	/** The JDBC connection to use to hit the database. */
	private JDBCConnection jdbc;
	
	/** Cache of the settings from the server config XML file. */
	private Message settings;
	
	/** Construct the Session Manager.
	 * 
	 * @param pClass The class object for the Player subclass to generate.
	 * @param jDBC  the JDBC connection 
	 * @param sets the settings from the XML file.
	 */
	public SessionManager(Class<TPlay> pClass, JDBCConnection jDBC, Message sets) { 
		playerClass = pClass;
		jdbc = jDBC;
		settings = sets;
	}
	
	/** Get the session for a token presented from an incoming client message.
	 * 
	 * @param token The token presented.
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
		
	/** Log off the server	
	 * @param token the session token to kill.
	 */
	public void logout(String token) {
		
		TPlay player = sessions.get(token);
		if (player != null) {
			player.pushOutgoingMessage(XMLTools.message(LOGOUT));
			sessions.remove(token);
		}
	}
	
	/** Create an account.
	 * @param e the message for creation
	 * @return e the logged-in message
	 */
	public Message createAccount(Message e) throws Exception { 
		PreparedStatement ps = jdbc.prepareStatement("select * from create_account(?,?,?,?,?,?,?,?,?);");
		ps.setString(1,e.getString("username"));
		ps.setString(2,e.getString("password1"));
		ps.setString(3,e.getString("password2"));
		ps.setString(4,e.getString("email"));
		ps.setBoolean(5,e.getBoolean("coppa"));
		ps.setString(6,e.getString( Message.SESSION_IPADDRESS));
		
		String client = e.getString( "client");
		if (client == null)
			ps.setNull(7, Types.VARCHAR);
		else
			ps.setString(7, e.getString( "client"));
	
		Integer gender = e.getInt("gender");
		if (gender == null)
			gender = 0;
		ps.setInt(8, gender);
		String lang = e.getString( "language");
		if (lang == null)
			lang = "en";
		ps.setString(9, lang);
		
		ResultSet set = ps.executeQuery();
		try { 
			if (!set.first()) throw new InvalidLoginException();
			String token = set.getString("create_account");
			set.close();
			set = null;
			if (token == null) throw new InvalidLoginException();
	
			// verify email if that setting is turned on. The token will be generated regardless.
			verifyEmailRequest(e);
			
			addSession(token);
			return loginMessage(token, CREATE_ACCOUNT);
			
		}
		catch (Exception x) { 
			if (set != null)
				set.close();
			throw(x); 
		}
	}
	
	/** Send a request to verify email addresses. 
	 *  @param e the email for the request
	 */
	public void verifyEmailRequest(Message e) throws Exception { 
		
		// if we don't validate emails, disregard
		if (!settings.getBoolean("verify-user-emails")) return;
		
		PreparedStatement ps = jdbc.prepareStatement("select * from verifyemail_maketoken(?,?);");
		ps.setString(1, e.getString( "email"));
		ps.setString(2, e.getString( Message.SESSION_IPADDRESS));
		
		ResultSet set = ps.executeQuery();
		try { 
			if (!set.first()) throw new InvalidLoginException();
			String token = set.getString("verifyemail_maketoken");
			set.close();
			set = null;
			if (token == null) throw new InvalidLoginException();
			
			// send the email containing the token
			String htmlEmail = new String(FileTools.getFileBytes(new File(settings.getString("validate-email-html-template"))));
			htmlEmail = StringTools.stringReplace(htmlEmail, "%VALIDATETOKEN%", token);
			String textEmail = new String(FileTools.getFileBytes(new File(settings.getString("validate-email-text-template"))));
			textEmail = StringTools.stringReplace(htmlEmail, "%VALIDATETOKEN%", token);	
			new Email(settings, e.getString( "email"), settings.getString("validate-email-subject"), textEmail, htmlEmail).send();			
			
		}
		catch (Exception x) { 
			if (set != null)
				set.close();
			throw(x); 
		}
		
	}
	

	/** 
	 * Redeem a "forgot password" token. Will also log you in.
	 * @param e the message.
	 * @return the result of the verification.	 
	 */
	public Message verifyEmailRedeem(Message e) throws Exception { 
		PreparedStatement ps = jdbc.prepareStatement("select * from verifyemail_redeem(?,?,?);");
		ps.setString(1, e.getString( "email"));
		ps.setString(2, e.getString( "token"));
		ps.setString(3, e.getString( Message.SESSION_IPADDRESS));
		ps.execute();
		return loginMessage(e.getString("session"), VERIFY_EMAIL_REDEEM);
	}
	
	/** Request a "forgot password" email.
	 * @param e the message
	 */
	public void forgotPassRequest(Message e) throws Exception { 
		PreparedStatement ps = jdbc.prepareStatement("select * from forgotpass_maketoken(?,?);");
		ps.setString(1, e.getString( "email"));
		ps.setString(2, e.getString( Message.SESSION_IPADDRESS));
		
		ResultSet set = ps.executeQuery();
		try { 
			if (!set.first()) throw new InvalidLoginException();
			String token = set.getString("forgotpass_maketoken");
			set.close();
			set = null;
			if (token == null) throw new InvalidLoginException();
			
			// send the email containing the token
			String htmlEmail = new String(FileTools.getFileBytes(new File(settings.getString("forgot-password-html-template"))));
			htmlEmail = StringTools.stringReplace(htmlEmail, "%FORGOTPASSTOKEN%", token);
			String textEmail = new String(FileTools.getFileBytes(new File(settings.getString("forgot-password-text-template"))));
			textEmail = StringTools.stringReplace(htmlEmail, "%FORGOTPASSTOKEN%", token);	
			new Email(settings, e.getString("email"), "Darkened Sky Password Request", textEmail, htmlEmail).send();
			
		}
		catch (Exception x) { 
			if (set != null)
				set.close();
			throw(x); 
		}
		
	}
	
	/** 
	 * Redeem a "forgot password" token. Will also log you in.
	 * @param e the message.
	 * @return the result of the login.	 
	 */
	public Message forgotPassRedeem(Message e) throws Exception { 
		PreparedStatement ps = jdbc.prepareStatement("select * from forgotpass_redeem(?,?,?,?,?,?);");
		ps.setString(1, e.getString( "email"));
		ps.setString(2, e.getString( "password"));
		ps.setString(3, e.getString( "password2"));
		ps.setString(4, e.getString( "token"));
		ps.setString(5, e.getString( Message.SESSION_IPADDRESS));
		String client = e.getString( "client");
		if (client == null)
			ps.setNull(6, Types.VARCHAR);
		else
			ps.setString(6, e.getString( "client"));
		
		ResultSet set = ps.executeQuery();
		try { 
			if (!set.first()) throw new InvalidLoginException();
			String token = set.getString("forgotpass_redeem");
			set.close();
			set = null;
			if (token == null) throw new InvalidLoginException();
			addSession(token);
			return loginMessage(token,FORGOTPASS_REDEEM);
		}
		catch (Exception x) { 
			if (set != null)
				set.close();
			throw(x); 
		}
	}
	
	/** 
	 * Log in, generating a session.
	 * 
	 * @param e the login message
	 * @return the reply message.
	 * @throws Exception
	 */
	public Message login(Message e) throws Exception { 
		PreparedStatement ps = jdbc.prepareStatement("select * from log_in(?,?,?,?);");
		ps.setString(1, e.getString( "username"));
		ps.setString(2, e.getString( "password"));
		ps.setString(3, e.getString( Message.SESSION_IPADDRESS));
		String client = e.getString( "client");
		if (client == null)
			ps.setNull(4, Types.VARCHAR);
		else
			ps.setString(4, e.getString( "client"));
		
		ResultSet set = ps.executeQuery();
		try { 
			if (!set.first()) throw new InvalidLoginException();
			String token = set.getString("log_in");
			set.close();
			set = null;
			if (token == null) throw new InvalidLoginException();

			addSession(token);			
			return loginMessage(token,LOGIN);
		}
		catch (Exception x) { 
			if (set != null)
				set.close();
			throw(x); 
		}
		
	}

	/** Builder for the login reply message.
	 * 
	 * @param token The token presented
	 * @param action The action to present in the reply message
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
	
	/** Load the player for a session token and cache it.
	 * 
	 * @param token The token to match the player to.
	 * @return The newly-created session object.
	 * @throws Exception
	 */
	private TPlay addSession(String token) throws Exception { 
		PreparedStatement ps2 = jdbc.prepareStatement("select * from playeraccounts join players on (players.playerid = playeraccounts.playerid) where sessiontoken = ?;");
		ps2.setString(1, token);
		TPlay player = null;
		ResultSet set2 = null;
		try { 
			set2 = ps2.executeQuery();
		
			if (set2.first()) { 
				Constructor<TPlay> con = playerClass.getConstructor(ResultSet.class);			
				player = (TPlay)con.newInstance(set2);
				sessions.put(token, player);
				set2.close();
				return player;
			}
			else { 
				throw new InvalidLoginException();
			}
		}
		catch (Exception x) {
			if (set2 != null)
				set2.close();
			throw x;
		}
	}
		
}

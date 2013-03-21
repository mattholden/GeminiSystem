package com.darkenedsky.gemini.test;
import java.util.Vector;
import org.jdom2.Element;

import com.darkenedsky.gemini.ActionList;
import com.darkenedsky.gemini.Message;
import com.darkenedsky.gemini.tools.HTTPPost;
import com.darkenedsky.gemini.tools.XMLTools;

public class TestSession {

	private long playerID;
	private String sessionToken, username, url;
	
	public TestSession(String username, String password, String postURL) throws Exception {
		super();
		this.username = username;
		url = postURL;
		
		Message login = new Message(ActionList.LOGIN);
		login.put("username", username);
		login.put("password", password);
		Vector<Message> replies = post(login);
		if (replies.isEmpty())
			return;
		Message reply = replies.get(0);
		this.playerID = reply.getLong(Message.SESSION_PLAYERID);
		this.sessionToken = reply.getString(Message.SESSION_TOKEN);
		
	}

	public Vector<Message> poll() throws Exception { 
		Message m = new Message(ActionList.POLL);
		return post(m);
	}
	
	public Vector<Message> post(Message m) throws Exception { 
		Vector<Message> msg = new Vector<Message>();
		if (m == null) return msg;
		
		if (sessionToken != null)
			m.put(Message.SESSION_TOKEN, sessionToken);
		if (playerID != 0)
			m.put(Message.SESSION_PLAYERID, playerID);
		
		HTTPPost poster = new HTTPPost(url);
		poster.addValue("xml", XMLTools.xmlToCompactString(m.toXML("message")));
		byte[] retval = poster.post();
		String retString = new String(retval);
		Element replyRoot = XMLTools.stringToXML(retString);
		for (Element e : replyRoot.getChildren("message"))
			msg.add(Message.getMessage(e));
		
		return msg;
	}
	
	public long getPlayerID() {
		return playerID;
	}

	public String getSessionToken() {
		return sessionToken;
	}

	public String getUsername() {
		return username;
	}
	
	

}

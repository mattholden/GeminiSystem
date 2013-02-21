package com.darkenedsky.gemini;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.json.simple.JSONAware;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.darkenedsky.gemini.tools.XMLTools;

public class Message implements JSONAware, MessageSerializable {
	
	public static final String 
	SESSION_PLAYERID = "session_playerid",
	SESSION_IPADDRESS = "session_ipaddress",
	SESSION_TOKEN = "session",
	ACTION = "action",
	TARGET_PLAYER = "targetplayerid";

	/** Where we're actually storing the content */
	private JSONObject object = new JSONObject();
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1035947470057581125L;
	
	public Message (Element e) { 
		
		for (Element x : e.getChildren()) { 
			
			// recognize our list pattern - multiple children, all named object
			List<Element> children = x.getChildren();	
			List<Element> objects = x.getChildren("object");
			
			// all the children were Objects, this must be a list
			if (children.size() == objects.size() && !objects.isEmpty()) { 
				addList(x.getName());
				for (Element y : objects) { 
					addToList(x.getName(), new Message(y), null);
				}
			}
			// if there were children and it's not a list, it must be an object
			else if (!children.isEmpty()) { 
				put(x.getName(), new Message(x), null);
			}
			// no children = primitive
			else { 
				put(x.getName(), x.getText());
			}
			
		}
	}
	
	public Message (JSONObject ob) { 
		object = ob;
	}
	
	public Message(String json) throws ParseException { 
		  JSONParser parser = new JSONParser();
	        Object o = parser.parse(json);
	        object = (JSONObject) o;

	}
	
	public Message(int actionid) { 
		put("action", actionid);
	}
	
	public Message (int actionid, long gameid) { 
		this(actionid);
		put("gameid", gameid);
	}
	
	public Message() {
		// deliberately blank
	}

	@SuppressWarnings("unchecked")
	public void put(String key, MessageSerializable obj, Player p) {
		if (obj == null) return;
		object.put(key, obj.serialize(p));
	}
	
	@SuppressWarnings("unchecked")
	public void addList(String key) { 
		Object o = object.get(key);
		
		// don't override an existing list
		if (o != null && o instanceof ArrayList<?>) return;
		
		object.put(key, new ArrayList<Message>());
	}
	
	@SuppressWarnings("unchecked")
	public void addToList(String key, MessageSerializable o, Player player) { 
		if (o == null) return;
		((ArrayList<Message>)object.get(key)).add(o.serialize(player));
	}
	@SuppressWarnings("unchecked")
	public void addToList(String key, Message m) { 
		if (m == null) return;
		((ArrayList<Message>)object.get(key)).add(m);
	}
	
	@SuppressWarnings("unchecked")
	public void put(String key, String value) { 
		object.put(key, value);
	}
	
	public void put(String key, Long value) { 
		put(key, Long.toString(value));
	}
	
	public void put(String key, Integer value) { 
		put(key, Integer.toString(value));
	}
	
	public void put(String key, Boolean value) { 
		put(key, Boolean.toString(value));
	}
	
	public Boolean getBoolean(String key) { 
		String s = getString(key);
		if (s == null) return null;
		return Boolean.parseBoolean(s);
	}
	
	public Integer getInt(String key) { 
		String s = getString(key);
		if (s == null) return null;
		return Integer.parseInt(s);
	}
	public Long getLong(String key) { 
		String s = getString(key);
		if (s == null) return null;
		return Long.parseLong(s);
	}
	public String getString(String key) { 
		Object s = object.get(key);
		if (s == null) return null;
		return s.toString();
	}
	
	@SuppressWarnings("unchecked")
	public ArrayList<MessageSerializable> getList(String key) { 
		return (ArrayList<MessageSerializable>)object.get(key);
	}

	@Override
	public String toJSONString() {
		return object.toJSONString();
	}

	public Element toXML(String root) {
		Element e = new Element(root);
		
		for (Object k : object.keySet()) {
			String s = k.toString(); // we've made sure all keys are strings
			Object o = object.get(s); 
			
			// because we're controlling the setters, there's only a few things it could be
			if (o instanceof String) { 
				e.addContent(XMLTools.xml(s, o.toString()));
			}
			else if (o instanceof Message) { 
				Message m = (Message)o;
				e.addContent(m.toXML(s));
			}
			else if (o instanceof ArrayList<?>) { 
				@SuppressWarnings("unchecked")
				ArrayList<Message> list = (ArrayList<Message>)o;
				Element l = new Element(s);
				for (Message m : list) { 
					l.addContent(m.toXML("object"));
				}
				e.addContent(l);
			}
			
		}
		
		return e;
	}

	public JSONObject toJSON() {
		return object;
	}

	@Override
	public Message serialize(Player p) {
		return this;
	}
	

	public static Message parseXMLFile(String filename) throws JDOMException, IOException {  
		Element e = XMLTools.loadXMLFile(filename);
		return new Message(e);
	}
	
	
		
}


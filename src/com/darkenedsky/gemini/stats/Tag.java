package com.darkenedsky.gemini.stats;

import com.darkenedsky.gemini.GameObject;
import com.darkenedsky.gemini.Message;
import com.darkenedsky.gemini.MessageSerializable;
import com.darkenedsky.gemini.Player;

public class Tag implements MessageSerializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 2048071936243398847L;
	private String tag;
	private GameObject source;
	
	public Tag(String t, GameObject src) { 
		tag = t.toLowerCase();
		source = src;		
	}

	private boolean secret = false;
	
	public boolean isSecret() { 
		return secret;
	}


	public String toString(){ return tag; }
	public boolean equals(Object other) { 
		if (other instanceof Tag || other instanceof String)
			return tag.equalsIgnoreCase(other.toString());
		else return false;
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public GameObject getSource() {
		return source;
	}

	public void setSource(GameObject source) {
		this.source = source;
	}
	

	@Override
	public Message serialize(Player p) { 
		Message m = new Message();
		m.put("tag", tag);
		m.put("source", source, p);
		return m;
	}
	
}

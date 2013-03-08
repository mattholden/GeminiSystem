package com.darkenedsky.gemini.badge;
import java.util.HashMap;
import com.darkenedsky.gemini.GameObject;
import com.darkenedsky.gemini.Message;
import com.darkenedsky.gemini.Player;

public class Badge extends GameObject {

	private HashMap<String, String> description = new HashMap<String, String>();
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 3834736953093025450L;

	public Badge(int defID, String englishName, String englishDesc) {
		super(defID, null, englishName);	
		description.put(ENGLISH, englishDesc);
	}

	@Override
	public Message serialize(Player p) { 
		Message m = super.serialize(p);
		m.put("description", localize(description, p));
		return m;
	}
}

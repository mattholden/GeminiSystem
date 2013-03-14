package com.darkenedsky.gemini.store;

import java.sql.ResultSet;

import com.darkenedsky.gemini.Message;
import com.darkenedsky.gemini.Player;

public class StoreCatalog extends StoreItem {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7382002549163274437L;
	private String title, description;
	
	public StoreCatalog(ResultSet set) throws Exception {
		super(set);
		title = set.getString("title");
		description = set.getString("description");
	}
	
	@Override
	public Message serialize(Player p) { 
		Message m = super.serialize(p);
		m.put("title", title);
		m.put("description", description);	
		return m;
	}

}

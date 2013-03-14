package com.darkenedsky.gemini.store;

import java.sql.ResultSet;

import com.darkenedsky.gemini.Message;
import com.darkenedsky.gemini.MessageSerializable;
import com.darkenedsky.gemini.Player;

public class StoreItem implements MessageSerializable { 
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 5504833413885294697L;
	private int storeItemID;
	private String detailsURL;
	private int priceCents;
	private Integer subscriptionID;
	private Integer setID;
	
	public StoreItem(ResultSet set) throws Exception { 
		storeItemID = set.getInt("storeitemid");
		detailsURL = set.getString("detailsurl");
		priceCents = set.getInt("pricecents");
		subscriptionID = set.getInt("subscriptionid");
		if (set.wasNull())
			subscriptionID = null;
		setID = set.getInt("setid");
		if (set.wasNull())
			setID = null;
		
	}

	public Integer getSubscriptionID() {
		return subscriptionID;
	}

	public Integer getSetID() {
		return setID;
	}

	public int getStoreItemID() {
		return storeItemID;
	}

	public String getDetailsURL() {
		return detailsURL;
	}

	public int getPriceCents() {
		return priceCents;
	}

	public double getPrice() { return priceCents * 0.01; }
	
	@Override
	public Message serialize(Player p) { 
		Message m  = new Message();
		m.put("storeitemid", storeItemID);
		m.put("detailsurl", detailsURL);
		m.put("pricecents", priceCents);
		m.put("subscriptionid", subscriptionID);
		m.put("setid", setID);
		return m;
	}
}

package com.darkenedsky.gemini.store;

import java.sql.ResultSet;


public class Subscription  {

	private int subscriptionID;
	private String planKey;
	
	public Subscription(ResultSet set) throws Exception { 
		subscriptionID = set.getInt("subscriptionid");
		planKey = set.getString("plankey");
	}

	public int getSubscriptionID() {
		return subscriptionID;
	}

	public String getPlanKey() {
		return planKey;
	}

}

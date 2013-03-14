package com.darkenedsky.gemini.store;

import java.sql.ResultSet;

public class PromoCode {

	private String promoCode;
	private int promoID;
	private double discountPercent;
	private int storeItemID;
	
	public PromoCode(ResultSet set) throws Exception { 
		promoCode = set.getString("promocode");
		promoID = set.getInt("promoid");
		discountPercent = set.getDouble("discountpercent");
		storeItemID = set.getInt("storeitemid");
	}

	public int getStoreItemID() { return storeItemID; } 
	
	public String getPromoCode() {
		return promoCode;
	}

	public int getPromoID() {
		return promoID;
	}

	public double getDiscountPercent() {
		return discountPercent;
	}
	
}

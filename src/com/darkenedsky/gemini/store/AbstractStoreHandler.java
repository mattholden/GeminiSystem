package com.darkenedsky.gemini.store;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

import com.darkenedsky.gemini.Handler;
import com.darkenedsky.gemini.Message;
import com.darkenedsky.gemini.Player;
import com.darkenedsky.gemini.exception.InvalidObjectException;
import com.darkenedsky.gemini.exception.InvalidPlayerException;
import com.darkenedsky.gemini.exception.InvalidPromoException;
import com.darkenedsky.gemini.exception.SQLUpdateFailedException;
import com.darkenedsky.gemini.service.JDBCConnection;
import com.stripe.model.Customer;

public abstract class AbstractStoreHandler extends Handler {

	protected JDBCConnection jdbc;
	
	public AbstractStoreHandler(JDBCConnection j) { 
		super(null);
		jdbc = j;
	
	}

	protected Subscription lookupSubscription(StoreItem item) throws Exception {
		
		if (item.getSubscriptionID() == null) return null;
		
		ResultSet set = null;
		PreparedStatement ps1 = jdbc.prepareStatement("select * from subscriptions where subscriptionid = ?;");
		ps1.setInt(1, item.getSubscriptionID());
		try { 
			set = ps1.executeQuery();
			if (!set.first()) 
				throw new InvalidObjectException(item.getSubscriptionID());
			Subscription sub = new Subscription(set);
			set.close();
			return sub;
		}
		catch (Exception x) { 
			if (set != null)
				set.close();
			throw x;
		}
	}
	
	protected StoreItem lookupItem(int itemid) throws Exception { 
		ResultSet set = null;
		PreparedStatement ps1 = jdbc.prepareStatement("select * from storeitems where storeitemid = ? and availablestart < now() and availableend > now() and stocklimit > (select count(storepurchases.storeitemid) from storepurchases where storepurchases.storeitemid = storeitems.storeitemid);");
		ps1.setInt(1, itemid);
		try { 
			set = ps1.executeQuery();
			if (!set.first()) 
				throw new InvalidObjectException(itemid);
			StoreItem item = new StoreItem(set);
			set.close();
			return item;
		}
		catch (Exception x) { 
			if (set != null)
				set.close();
			throw x;
		}
	}
	
	protected PromoCode lookupPromo(String promoCode) throws Exception { 
		ResultSet set = null;
		PreparedStatement ps1 = jdbc.prepareStatement("select * from promocodes where promocode ilike ? and expirationdate < now() and usesallowed < (select count(promoid) from storepurchases where promoid = promocode.promoid);");
		ps1.setString(1, promoCode);
		try { 
			set = ps1.executeQuery();
			if (!set.first()) 
				throw new InvalidPromoException(promoCode);
			PromoCode promo = new PromoCode(set);
			set.close();
			return promo;
		}
		catch (Exception x) { 
			if (set != null)
				set.close();
			throw x;
		}
	}
	
	protected Customer createCustomer(Message e, Player p, HashMap<String, String> account) throws Exception { 
		
		Map<String, Object> params = new HashMap<String, Object>();			
		params.put("description", p.getUsername());
		params.put("email", account.get("email"));
		params.put("card",e.getString("cardtoken"));
		Customer c = Customer.create(params);			
		account.put("stripecustomerid", c.getId());
		
		PreparedStatement update = jdbc.prepareStatement("update playeraccounts set stripecustomerid = ? where playerid = ?;");
		update.setString(1, c.getId());
		update.setLong(2, p.getPlayerID());
		int rows = update.executeUpdate();
		if (rows == 0)
			throw new SQLUpdateFailedException();
					
		return c;			
	}

	protected Customer retrieveCustomer(HashMap<String, String> account) throws Exception { 
		return Customer.retrieve(account.get("stripecustomerid"));
	}

	protected HashMap<String, String> lookupAccount(Player p) throws Exception { 
		ResultSet set = null;
		PreparedStatement ps1 = jdbc.prepareStatement("select * from playeraccounts where playerid = ?;");
		ps1.setLong(1, p.getPlayerID());
		try { 
			set = ps1.executeQuery();
			if (!set.first()) 
				throw new InvalidPlayerException(p.getPlayerID());
			
			HashMap<String, String> stuff = new HashMap<String, String>();
			stuff.put("stripecustomerid", set.getString("stripecustomerid"));
			stuff.put("email", set.getString("email"));
			set.close();
			return stuff;
		}
		catch (Exception x) { 
			if (set != null)
				set.close();
			throw x;
		}
	}
	
}

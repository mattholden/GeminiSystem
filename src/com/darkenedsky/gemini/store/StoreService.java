package com.darkenedsky.gemini.store;

import com.darkenedsky.gemini.Message;
import com.darkenedsky.gemini.service.JDBCConnection;
import com.darkenedsky.gemini.service.Service;
import com.stripe.Stripe;

public class StoreService extends Service {
	
	private JDBCConnection jdbc;
	
	public JDBCConnection getJDBC() { return jdbc; }
	
	public StoreService(Message settings, JDBCConnection j) { 
		jdbc = j;
		if (settings.getBoolean("stripe_testmode"))
			Stripe.apiKey = settings.getString("stripe_testkey");
		else
			Stripe.apiKey = settings.getString("stripe_prodkey");
		
		handlers.put(STORE_PURCHASE, new PurchaseHandler(jdbc));
		handlers.put(STORE_CANCELSUB, new CancelSubHandler(jdbc, settings));
		handlers.put(STORE_EDITCARD, new EditCardHandler(jdbc));
		handlers.put(STORE_GETCATALOG, new GetCatalogHandler(jdbc));
	}
	
}

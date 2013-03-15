package com.darkenedsky.gemini.store;

import com.darkenedsky.gemini.Message;
import com.darkenedsky.gemini.service.EmailFactory;
import com.darkenedsky.gemini.service.JDBCConnection;
import com.darkenedsky.gemini.service.Service;
import com.stripe.Stripe;

public class StoreService extends Service {
	
	private JDBCConnection jdbc;
	
	public JDBCConnection getJDBC() { return jdbc; }
	
	public StoreService(Message settings, JDBCConnection j, EmailFactory email) { 
		jdbc = j;
		if (settings.getBoolean("stripe_testmode"))
			Stripe.apiKey = settings.getString("stripe_testkey");
		else
			Stripe.apiKey = settings.getString("stripe_prodkey");
		
		handlers.put(STORE_PURCHASE, new PurchaseHandler(jdbc, email));
		handlers.put(STORE_CANCELSUB, new CancelSubHandler(jdbc, settings, email));
		handlers.put(STORE_EDITCARD, new EditCardHandler(jdbc, email));
		handlers.put(STORE_GETCATALOG, new GetCatalogHandler(jdbc));
	}
	
}

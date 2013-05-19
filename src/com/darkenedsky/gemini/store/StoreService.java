package com.darkenedsky.gemini.store;

import com.darkenedsky.gemini.Message;
import com.darkenedsky.gemini.service.Service;
import com.stripe.Stripe;

public class StoreService extends Service {

	@Override
	public void init() {
		Message settings = getServer().getSettings();

		if (settings.getBoolean("stripe_testmode"))
			Stripe.apiKey = settings.getString("stripe_testkey");
		else
			Stripe.apiKey = settings.getString("stripe_prodkey");

		addHandler(STORE_PURCHASE, new PurchaseHandler());
		addHandler(STORE_CANCELSUB, new CancelSubHandler());
		addHandler(STORE_EDITCARD, new EditCardHandler());
		addHandler(STORE_GETCATALOG, new GetCatalogHandler());
	}
}

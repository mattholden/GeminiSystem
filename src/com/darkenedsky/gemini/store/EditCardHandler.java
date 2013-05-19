package com.darkenedsky.gemini.store;

import java.util.HashMap;

import org.apache.log4j.Logger;

import com.darkenedsky.gemini.Message;
import com.darkenedsky.gemini.Player;
import com.darkenedsky.gemini.exception.InvalidEmailTemplateException;
import com.darkenedsky.gemini.exception.RequiredFieldException;
import com.darkenedsky.gemini.service.EmailFactory;
import com.stripe.model.Customer;

public class EditCardHandler extends AbstractStoreHandler {

	private static final Logger LOG = Logger.getLogger(EditCardHandler.class);

	@Override
	public void processMessage(Message e, Player p) throws Exception {
		EmailFactory email = getService().getServer().getEmailFactory();

		String card = e.getString("cardtoken");
		if (card == null)
			throw new RequiredFieldException("cardtoken");

		HashMap<String, String> acct = lookupAccount(p);
		if (acct.get("stripecustomerid") != null) {
			this.createCustomer(e, p, acct);
			return;
		}
		Customer customer = retrieveCustomer(acct);
		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("email", acct);
		params.put("card", card);
		customer.update(params);

		Message m = new Message(STORE_EDITCARD);
		m.put("playerid", p.getPlayerID());
		p.pushOutgoingMessage(m);

		try {
			HashMap<String, String> fields = new HashMap<String, String>();
			email.sendEmail(customer.getEmail(), "updatecard", p.getLanguage(), fields);
		} catch (InvalidEmailTemplateException x) {
			LOG.warn("Email template \"updatecard\" missing. No confirmation email sent to user.");
		}
	}

}

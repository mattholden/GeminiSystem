package com.darkenedsky.gemini.store;

import java.util.HashMap;

import com.darkenedsky.gemini.Message;
import com.darkenedsky.gemini.Player;
import com.darkenedsky.gemini.exception.RequiredFieldException;
import com.darkenedsky.gemini.service.JDBCConnection;
import com.stripe.model.Customer;

public class EditCardHandler extends AbstractStoreHandler {

	public EditCardHandler(JDBCConnection j) {
		super(j);	
	}

	@Override
	public void processMessage(Message e, Player p) throws Exception {
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
		
		
	} 
	
	

}

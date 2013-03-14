package com.darkenedsky.gemini.store;

import java.sql.PreparedStatement;
import java.util.HashMap;

import com.darkenedsky.gemini.Message;
import com.darkenedsky.gemini.Player;
import com.darkenedsky.gemini.exception.SQLUpdateFailedException;
import com.darkenedsky.gemini.service.JDBCConnection;
import com.stripe.model.Customer;

public class CancelSubHandler extends AbstractStoreHandler {

	private Message theSettings;
	
	public CancelSubHandler(JDBCConnection j, Message settings) {
		super(j);
		theSettings = settings;
	}

	@Override
	public void processMessage(Message e, Player p) throws Exception {
		
		HashMap<String, String> account = this.lookupAccount(p);
		Customer c = retrieveCustomer(account);
		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("at_period_end", true);		
		com.stripe.model.Subscription s = c.cancelSubscription(params);
				
		PreparedStatement ps = jdbc.prepareStatement("update playeraccounts set subscriptionid = ? where playerid = ?;");
		ps.setInt(1, theSettings.getInt("default_subscription_id"));
		ps.setLong(2, p.getPlayerID());
		if (0 == ps.executeUpdate()) 
			throw new SQLUpdateFailedException();
		
		// TODO: Send an email
		/*
		String textFile = theSettings.getString("cancel_subscription_email_text");
		String htmlFile = theSettings.getString("cancel_subscription_email_html");
		String subject = theSettings.getString("cancel_subscription_email_subject");
		*/
		
		Message m = new Message(STORE_CANCELSUB);
		m.put("playerid", p.getPlayerID());
		m.put("timesubwillend", s.getCurrentPeriodEnd());
		p.pushOutgoingMessage(m);
		
	} 
	
}

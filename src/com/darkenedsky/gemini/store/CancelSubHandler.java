package com.darkenedsky.gemini.store;

import java.sql.PreparedStatement;
import java.util.HashMap;

import org.apache.log4j.Logger;

import com.darkenedsky.gemini.Message;
import com.darkenedsky.gemini.Player;
import com.darkenedsky.gemini.exception.InvalidEmailTemplateException;
import com.darkenedsky.gemini.exception.SQLUpdateFailedException;
import com.darkenedsky.gemini.service.EmailFactory;
import com.darkenedsky.gemini.service.JDBCConnection;
import com.stripe.model.Customer;

public class CancelSubHandler extends AbstractStoreHandler {

	private static final Logger LOG = Logger.getLogger(CancelSubHandler.class);

	@Override
	public void processMessage(Message e, Player p) throws Exception {

		JDBCConnection jdbc = getService().getServer().getJDBC();
		Message theSettings = getService().getServer().getSettings();
		EmailFactory email = getService().getServer().getEmailFactory();

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

		Message m = new Message(STORE_CANCELSUB);
		m.put("playerid", p.getPlayerID());
		m.put("timesubwillend", s.getCurrentPeriodEnd());
		p.pushOutgoingMessage(m);

		try {
			HashMap<String, String> fields = new HashMap<String, String>();
			fields.put("%TIMESUBWILLEND%", new java.sql.Timestamp(s.getCurrentPeriodEnd()).toString());
			email.sendEmail(c.getEmail(), "cancelsub", p.getLanguage(), fields);
		} catch (InvalidEmailTemplateException x) {
			LOG.warn("Email template \"cancelsub\" missing. No confirmation email sent to user.");
		}
	}

}

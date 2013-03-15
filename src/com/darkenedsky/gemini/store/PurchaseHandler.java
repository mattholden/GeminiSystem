package com.darkenedsky.gemini.store;

import java.sql.PreparedStatement;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.darkenedsky.gemini.Message;
import com.darkenedsky.gemini.Player;
import com.darkenedsky.gemini.exception.ChargeProcessingException;
import com.darkenedsky.gemini.exception.InvalidEmailTemplateException;
import com.darkenedsky.gemini.exception.SQLUpdateFailedException;
import com.darkenedsky.gemini.service.EmailFactory;
import com.darkenedsky.gemini.service.JDBCConnection;
import com.stripe.model.Charge;
import com.stripe.model.Customer;
import com.stripe.model.Invoice;

public class PurchaseHandler extends AbstractStoreHandler {

	private static final Logger LOG = Logger.getLogger(PurchaseHandler.class);
	
	private EmailFactory email;
	
	public PurchaseHandler(JDBCConnection j, EmailFactory e) { 
		super(j);
		email = e;
	}
	
	@Override
	public void processMessage(Message e, Player p) throws Exception {
		
		Customer customer;
		HashMap<String, String> account = lookupAccount(p);
		
		if (account.get("stripecustomerid") == null) { 
			customer = createCustomer(e, p, account);
		}
		else { 
			customer = retrieveCustomer(account);
		}
		
		PromoCode promo = null;
		int itemid = -1;
		double discount = 0.00;
		
		if (e.getString("promocode") != null) { 
			promo = lookupPromo(e.getString("promocode"));
			itemid = promo.getStoreItemID();
			discount = promo.getDiscountPercent();
		}
		else 
			itemid = e.getInt("storeitemid");
		
		StoreItem item = lookupItem(itemid);
		Subscription subscription = lookupSubscription(item);
		
		int price = item.getPriceCents();
		price -= (int)(price * (1.00-discount));
		
		Charge charge = chargeCustomer(customer, item, price, subscription);		
		try {
			recordPurchase(e, p, customer, charge, item, price, promo);
		}
		catch (Exception x) { 
			if (charge != null)
				charge.refund();
		}
			
		
	} 
	
	private void recordPurchase(Message m, Player player, Customer customer, Charge charge, StoreItem item, int price, PromoCode promo) throws Exception { 
		
		PreparedStatement ps = jdbc.prepareStatement("select * from record_purchase(?,?,?,?,?,?);");
		ps.setInt(1, item.getStoreItemID());
		ps.setLong(2, player.getPlayerID());
		ps.setString(3, m.getString(Message.SESSION_IPADDRESS));
		ps.setInt(4, promo.getPromoID());
		ps.setInt(5, price);
		ps.setString(6, charge.getInvoice());
		if (ps.executeUpdate() == 0) { 
			throw new SQLUpdateFailedException();
		}
			
		Message msg = new Message(STORE_PURCHASE);
		msg.put("storeitemid", item.getStoreItemID());
		msg.put("pricecents", price);
		msg.put("promocode", promo.getPromoCode());
		msg.put("discountpercent", promo.getDiscountPercent());
		msg.put("stripechargeid", charge.getId());
		player.pushOutgoingMessage(msg);
		
		try {
			HashMap<String, String> fields = new HashMap<String, String>();
			fields.put("%PRICE%", "$" + (price/100) + "." + (price%100));
			fields.put("%CHARGEID%", charge.getId());
			fields.put("%PROMOCODE%", promo.getPromoCode());
			email.sendEmail(customer.getEmail(), "purchase", player.getLanguage(), fields);
		}
		catch (InvalidEmailTemplateException x) { 
			LOG.warn("Email template \"purchase\" missing. No confirmation email sent to user.");
		}
	}
	
	private Charge chargeCustomer(Customer customer, StoreItem item, int price, Subscription sub) throws Exception { 
		
		if (sub != null) {
			Map<String, Object> subscriptionParams = new HashMap<String, Object>();
			subscriptionParams.put("plan", sub.getPlanKey());
			subscriptionParams.put("prorate", "true");					
			customer.updateSubscription(subscriptionParams);
			
			Map<String, Object> invoiceItemParams = new HashMap<String, Object>();
			invoiceItemParams.put("customer", customer.getId());
			Invoice invoice = Invoice.create(invoiceItemParams);
			Charge charge = Charge.retrieve(invoice.getCharge());
			return charge;
		}
		else { 
			Map<String, Object> params = new HashMap<String, Object>();			
			params.put("amount", price);
			params.put("currency", "usd");
			params.put("customer", customer.getId());
			params.put("description", item.getDetailsURL());		
			Charge charge = Charge.create(params);
			
			if (charge.getFailureMessage() != null)			
				throw new ChargeProcessingException(charge.getFailureMessage());
			return charge;
		}
		
	}
	
	
}

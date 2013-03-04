package com.darkenedsky.gemini.service;

import com.darkenedsky.gemini.Handler;
import com.darkenedsky.gemini.Message;
import com.darkenedsky.gemini.Player;

public class AnalyticsService extends Service {

	private JDBCConnection jdbc;
	
	public AnalyticsService(final JDBCConnection jDBC) { 
		
		jdbc = jDBC;
		
		handlers.put(GET_ANALYTICS, new Handler(null) { 
			@Override
			public void processMessage(Message m, Player p) throws Exception { 
				
				doAnalytics(m,p);
			}
		});
	}
	
	private void doAnalytics(Message m, Player p) throws Exception { 
		
		Message reply = new Message(GET_ANALYTICS);
		
		// TODO: Make sure you're authorized to get analytics.
		// TODO: actually get some analytics? ;)
		
		p.pushOutgoingMessage(reply);
	}
	
	
}

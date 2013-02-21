package com.darkenedsky.gemini.stats;
import com.darkenedsky.gemini.MessageSerializable;
import com.darkenedsky.gemini.Message;
import com.darkenedsky.gemini.Player;


public abstract class Modifier implements MessageSerializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 5778294047945625634L;
	protected int amount;
	
	public abstract int modify(int value);

	public Modifier(int amt) { 
		this.amount = amt;
	}

	@Override
	public Message serialize(Player p) { 
		Message m = new Message();
		m.put("type", getClass().getSimpleName());
		m.put("amount", amount);
		return m;
		
	}
	
}





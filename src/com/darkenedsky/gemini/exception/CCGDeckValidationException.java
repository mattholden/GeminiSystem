package com.darkenedsky.gemini.exception;
import com.darkenedsky.gemini.Message;
import com.darkenedsky.gemini.Player;

public abstract class CCGDeckValidationException extends RuleException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7555025601208943568L;
	private boolean error;
	
	public CCGDeckValidationException(int defid, boolean isError, String englishName) {
		super(defid, englishName);
		this.error = isError;
	}

	public boolean isError() { 
		return error;
	}
	
	@Override
	public Message serialize(Player p) { 
		Message m = super.serialize(p);
		m.put("error", error);
		return m;
	}
	
}

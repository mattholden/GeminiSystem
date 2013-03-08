package com.darkenedsky.gemini;

import java.util.HashMap;

public class LocalizedObject implements MessageSerializable, Languages {

	/**
	 * 
	 */
	private static final long serialVersionUID = -128309988640464051L;
	/** Localized names. */
	protected HashMap<String, String> name = new HashMap<String, String>();
	
	public LocalizedObject(String enName) { 
		name.put(Languages.ENGLISH, enName);
	}
	
	@Override
	public Message serialize(Player p) { 
		Message m = new Message();				
		m.put("name", localize(name, p));
		return m;
	}
	
	protected String localize(HashMap<String, String> value, Player p) { 		
		String lang = (p == null || p.getLanguage() == null) ? Languages.ENGLISH : p.getLanguage();
		return value.get(lang);
	}
}

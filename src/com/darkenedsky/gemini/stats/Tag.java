package com.darkenedsky.gemini.stats;
import com.darkenedsky.gemini.GameObject;

public class Tag extends GameObject {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 2048071936243398847L;
	
	public Tag(int defID, String englishName) { 
		super(defID, null, englishName);
	}
	
	public Tag(int defID, Long objID, String englishName) { 
		super(defID, objID, englishName);
	}

	private boolean secret = false;
	
	public boolean isSecret() { 
		return secret;
	}
	
}

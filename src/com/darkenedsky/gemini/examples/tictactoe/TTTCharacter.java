package com.darkenedsky.gemini.examples.tictactoe;

import com.darkenedsky.gemini.GameCharacter;
import com.darkenedsky.gemini.Player;

public class TTTCharacter extends GameCharacter {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4640053496115898651L;

	public TTTCharacter(Player p) {
		super(p);
	}

	private boolean symbol;
	public static final boolean X = true, O = false;
	
	public boolean getSymbol() { 
		return symbol;
	}
	public void setSymbol(boolean isX) { 
		symbol = isX;
	}
	
	
}

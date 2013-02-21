package com.darkenedsky.gemini.stats;


public class SetTo extends Modifier {
	/**
	 * 
	 */
	private static final long serialVersionUID = -7253603754906263801L;
	public SetTo(int amt) { super(amt); 	}
	public int modify(int value) { 	return  amount; 	}
	public String toString() { 	return "->" + amount; }
}
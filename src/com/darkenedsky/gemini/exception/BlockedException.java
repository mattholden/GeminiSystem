package com.darkenedsky.gemini.exception;

public class BlockedException extends RuleException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4454466700224490627L;

	public BlockedException() {
		super(BLOCKED, "This user is blocked waiting on the result of a client callback.");		
	}

}

package com.darkenedsky.gemini.exception;

/** Thrown when an action is executed on a turn it shouldn't be. Usually comes up when you have to do something
 *  on your turn and you tried to do it on somebody else's.
 *  
 * @author Matt Holden
 *
 */
public class NotOnThisTurnException extends RuleException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3866683484391291112L;

	/** Construct the exception. 
	 *  @param not If true, you tried to execute the action when it's not your turn and it needed to be.
	 */
	public NotOnThisTurnException(boolean not) {
		super(NOT_ON_THIS_TURN, "You may not perform this action when it is " + (not ? "not " : "") + "your turn.");
	}
	
	

}

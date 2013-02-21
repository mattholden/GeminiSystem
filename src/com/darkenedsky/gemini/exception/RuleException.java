package com.darkenedsky.gemini.exception;
import com.darkenedsky.gemini.Message;
import com.darkenedsky.gemini.Player;

/** An extension of GeminiException that signifies that nothing broke, but the player tried to do something
 *  that's not allowed. Effectively, it's a user error, not a broken thing, so we probably don't need to log it.
 *  
 * @author Matt Holden
 *
 */
public class RuleException extends GeminiException { 
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -1713296984450617954L;
	
	/** Construct the exception 
	 * 
	 * @param code Exception code
	 * @param enError The English error message
	 */
	public RuleException(int code, String enError) { 
		super(code, enError);
	}
	
	/** Serialize the exception into a message.
	 *  @param p the player we're serializing for. Make sure to support null.
	 *  @return The created message.
	 */
	@Override
	public Message serialize(Player p) { 
		Message m = super.serialize(p);
		m.put("is_rule_exception", true);
		return m;
	}
	
}

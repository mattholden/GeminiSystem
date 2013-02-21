package com.darkenedsky.gemini.exception;

import com.darkenedsky.gemini.Languages;
import com.darkenedsky.gemini.Message;
import com.darkenedsky.gemini.Player;

/** Wrapper to convert a standard Java exception type, like SQLException, etc. into a GeminiException
 * so that it can be serialized and sent back to clients if needed.
 * 
 * @author Matt Holden
 *
 */
public class JavaException extends GeminiException { 
		
	private static final long serialVersionUID = 3968425889083479448L;
	
	/** The wrapped exception. */
	private Throwable t;
	
	/** Construct the exception wrapper.
	 * 
	 * @param thro the wrapped exception
	 */
	public JavaException(Throwable thro) { 
		super(JAVA_EXCEPTION, thro.getMessage());
		t = thro;
	}
	
	/** Serialize the wrapped exception in the same format as a GeminiException. 
	 *  @param player the player we're serializing the message for.
	 *  @return the created message
	 */
	@Override
	public Message serialize(Player player) {
		Message m = new Message();
		
		// Send you the error message in the language your session has selected
		// hooray for one line of code for localization support!
		String lang = (player == null) ? Languages.ENGLISH : player.getLanguage();
		m.put("errormessage", errorMessage.get(lang));
	
		m.put("errorcode", errorCode);
		m.put("class", t.getClass().getName());
		m.put("message", t.getMessage());
		
		StringBuffer traceBuffer = new StringBuffer("\n");
		StackTraceElement[] stack = t.getStackTrace();
		for (int i = 0; i < stack.length; i++)
			traceBuffer.append(stack[i].toString() + "\n");
		m.put("stacktrace", traceBuffer.toString());
		
		Throwable cause = t.getCause();
		if (cause != null)
			m.put("cause", new JavaException(t.getCause()), player);
		
		m.put("details", details, player);
		
		return m;
	}
	
	

}

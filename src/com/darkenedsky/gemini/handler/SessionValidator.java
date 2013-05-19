package com.darkenedsky.gemini.handler;

import com.darkenedsky.gemini.Message;
import com.darkenedsky.gemini.Player;
import com.darkenedsky.gemini.exception.ActionNotAllowedException;

public class SessionValidator extends AbstractHandlerValidator {

	private Boolean requiresSession = REQUIRES_YES;

	public SessionValidator() {
		this(REQUIRES_YES);
	}

	public SessionValidator(Boolean req) {
		requiresSession = req;
	}

	@Override
	public void validate(Message m, Player p) throws Exception {

		// make sure there is a session
		if (requiresSession != null) {
			if ((requiresSession && p == null) || (!requiresSession && p != null)) {
				throw new ActionNotAllowedException(requiresSession);
			}
		}
	}

}

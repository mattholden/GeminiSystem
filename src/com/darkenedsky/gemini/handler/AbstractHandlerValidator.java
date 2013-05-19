package com.darkenedsky.gemini.handler;

public abstract class AbstractHandlerValidator implements HandlerValidator {

	private Handler handler;

	@Override
	public Handler getHandler() {
		return handler;
	}

	@Override
	public void setHandler(Handler h) {
		handler = h;
	}

}

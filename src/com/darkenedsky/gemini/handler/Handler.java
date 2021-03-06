package com.darkenedsky.gemini.handler;

import java.util.Vector;

import com.darkenedsky.gemini.Message;
import com.darkenedsky.gemini.MessageProcessor;
import com.darkenedsky.gemini.Player;
import com.darkenedsky.gemini.service.Service;

public abstract class Handler implements MessageProcessor, HandlerValidationConstants {

	private Service service;

	private Vector<HandlerValidator> validators = new Vector<HandlerValidator>();

	public Handler() { /* Deliberately empty */
	}

	public Handler(HandlerValidator... vals) {

		for (HandlerValidator hv : vals) {
			addValidator(hv);
		}
	}

	public void addValidator(HandlerValidator val) {
		validators.add(val);
		val.setHandler(this);
	}

	public Service getService() {
		return service;
	}

	public void setService(Service s) {
		service = s;
	}

	public void validate(Message msg, Player p) throws Exception {
		for (HandlerValidator val : validators) {
			val.validate(msg, p);
		}
	}

}

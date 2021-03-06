package com.darkenedsky.gemini.handler;

import java.util.Vector;

import com.darkenedsky.gemini.Message;
import com.darkenedsky.gemini.Player;
import com.darkenedsky.gemini.exception.NotOnThisPhaseException;

public class GameStateValidator extends AbstractGameHandlerValidator {

	private Vector<Integer> vPhases = new Vector<Integer>();

	public GameStateValidator(Integer... phases) {
		for (Integer i : phases) {
			vPhases.add(i);
		}
	}

	@Override
	public void validate(Message m, Player p) throws Exception {

		// check the phase
		if (!vPhases.isEmpty()) {
			int phase = getGame().getState();
			if (!vPhases.contains(phase)) {
				throw new NotOnThisPhaseException();
			}
		}
	}

}

package com.darkenedsky.gemini;

import java.util.Vector;

import com.darkenedsky.gemini.exception.ActionNotAllowedException;
import com.darkenedsky.gemini.exception.GamePlayerException;
import com.darkenedsky.gemini.exception.NotOnThisPhaseException;
import com.darkenedsky.gemini.exception.NotOnThisTurnException;
import com.darkenedsky.gemini.exception.PlayerEliminatedException;

public class GeneralHandlerValidator implements HandlerValidator {

	private Vector<Integer> vPhases = new Vector<Integer>();	
	private Integer turnState = ON_ANY_TURN;	
	private Boolean requiresSession = REQUIRES_YES;
	private Boolean requiresInGame = REQUIRES_YES;
	private Boolean requiresEliminated = REQUIRES_NO;
	protected Game<? extends GameCharacter> game;

	GeneralHandlerValidator() { } 
	
	GeneralHandlerValidator(Integer turn, Integer... phases) { 
		turnState = turn;
		for (Integer i : phases) 
			vPhases.add(i);
	}
	

	public Game<? extends GameCharacter> getGame() { return game; }
	
	public void setGame(Game<? extends GameCharacter> g) { 
		game = g;
	}
	

	
	public void setRequiresInGame(Boolean req) { 
		requiresInGame = req;
	}
	
	public void setRequiresSession(Boolean req) { 
		requiresSession = req;
	}
		
	public void setTurnState(int turnState) { 
		this.turnState = turnState; 
	}
	
	public void setPhases(Integer... phases) { 
		for (Integer i : phases) { 
			vPhases.add(i);
		}
	}
	
	public void setRequireEliminated(Boolean status) { 
		requiresEliminated = status;
	}
	
	
	/** Make sure that the game conditions are correct for firing this handler.
	 * Will throw appropriate exceptions if any of the specified conditions are not met.
	 * @param msg the message to validate
	 * @param p the player session that sent the message 
	 */
	public void validate(Message msg, Player p) throws Exception {
		
		// make sure there is a session
		if (requiresSession != null) { 			
			if ((requiresSession && p == null) || (!requiresSession && p != null)) { 
				throw new ActionNotAllowedException(requiresSession);
			}
		}
		
		if (game != null && game instanceof Game<?>) { 
	
			// Make sure you're even in the game
			if (requiresInGame != null) { 
				
				boolean foundPlayer = false;
				for (Player ply : game.getPlayers()) { 
					if (ply.getPlayerID() == p.getPlayerID()) { 
						if (requiresInGame == REQUIRES_NO) { 			
							throw new GamePlayerException(requiresInGame);
						}
						foundPlayer = true;
					}
				}
				if (!foundPlayer && requiresInGame == REQUIRES_YES) { 
					throw new GamePlayerException(requiresInGame);
				}
			}
			
			// not eliminated
			if (requiresEliminated != null && requiresEliminated != game.getCharacter(p.getPlayerID()).isEliminated()) { 
				throw new PlayerEliminatedException();
			}
			
			// check if you're the correct player
			boolean isCurrent = (((Game<?>)game).getCurrentPlayer() == p.getPlayerID());	
			if (isCurrent && turnState == NOT_YOUR_TURN) { 
				throw new NotOnThisTurnException(true);
			}
			else if (!isCurrent && turnState == ON_YOUR_TURN) { 
				throw new NotOnThisTurnException(false);
			}
		
			// check the phase
			if (!vPhases.isEmpty()) { 
				int phase = game.getState();
				if (!vPhases.contains(phase)) { 
					throw new NotOnThisPhaseException();
				}
			}
		}
	}

}

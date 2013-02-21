package com.darkenedsky.gemini;
import java.util.Vector;
import com.darkenedsky.gemini.exception.ActionNotAllowedException;
import com.darkenedsky.gemini.exception.GamePlayerException;
import com.darkenedsky.gemini.exception.NotOnThisPhaseException;
import com.darkenedsky.gemini.exception.NotOnThisTurnException;

public abstract class Handler implements MessageProcessor { 
	
	private Vector<Integer> vPhases = new Vector<Integer>();
	
	private Integer turnState = ON_ANY_TURN;
	
	private Boolean requiresSession = REQUIRES_YES;
	private Boolean requiresInGame = REQUIRES_YES;
	
	public static final Boolean
		REQUIRES_YES = false,
		REQUIRES_NO = true,
		NO_REQUIREMENT = null;
	
	public static final int 
		ON_ANY_TURN = 0,
		ON_YOUR_TURN = 1, 
		NOT_YOUR_TURN = 2,
		ON_TEAM_TURN = 3,
		NOT_TEAM_TURN = 4;
	
	protected Game<? extends GameCharacter, ? extends Player> game;
	
	public Handler() { /* Deliberately empty */ }
	
	public Handler(Game<? extends GameCharacter, ? extends Player> gm) { 
		this(gm, ON_ANY_TURN);
	}
	
	public Handler(Game<? extends GameCharacter, ? extends Player> gm, int theTurnState, Integer... phases) { 
		game = gm;
		turnState = theTurnState;
		
		for (Integer i : phases) { 
			vPhases.add(i);
		}
				
	}
	
	public void setRequiresInGame(Boolean req) { 
		requiresInGame = req;
	}
	
	public void setRequiresSession(Boolean req) { 
		requiresSession = req;
	}
	
	public void setGame(Game<? extends GameCharacter, ? extends Player> g) { 
		game = g;
	}
	
	public void setTurnState(int turnState) { 
		this.turnState = turnState; 
	}
	
	public void setPhases(Integer... phases) { 
		for (Integer i : phases) { 
			vPhases.add(i);
		}
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
		
		if (game != null && game instanceof Game<?,?>) { 
	
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
			
			// check if you're the correct player
			boolean isCurrent = (((Game<?,?>)game).getCurrentPlayer() == p.getPlayerID());	
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

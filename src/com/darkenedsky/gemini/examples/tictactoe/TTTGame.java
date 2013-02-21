package com.darkenedsky.gemini.examples.tictactoe;

import java.util.ArrayList;

import com.darkenedsky.gemini.Game;
import com.darkenedsky.gemini.GameResult;
import com.darkenedsky.gemini.Message;
import com.darkenedsky.gemini.Player;
import com.darkenedsky.gemini.RuleException;
import com.darkenedsky.gemini.service.InvalidActionException;
import com.darkenedsky.gemini.service.JDBCConnection;
import com.darkenedsky.gemini.service.WinLossRecordManager;

public class TTTGame extends Game<TTTCharacter> {
	

	/**
	 * Required for java serialization.
	 */
	private static final long serialVersionUID = 6385284794381057408L;
	
	/** The actual board of where everybody's X and Os go. 
	 *   X is true, O is false, and null is neither.
	 */
	private Boolean[][] board = new Boolean[3][3];
	

	
	public TTTGame(String nm, long id, ArrayList<TTTCharacter> players,
			JDBCConnection jdb, WinLossRecordManager winMgr) {
		super(nm, id, players, jdb, winMgr);	
		
		characters.get(0).setSymbol(TTTCharacter.X);
		characters.get(1).setSymbol(TTTCharacter.O);
		
	}

	/** The serialize() method is where you write any game-specific state out to the players.
	 *  The Message object allows the data to go to JSON or XML just as easily.
	 *  
	 *  @return a Message containing the game's state.
	 */
	@Override	
	public Message serialize() { 
		Message m = super.serialize();
		m.put("board00", board[0][0]);
		m.put("board01", board[0][1]);
		m.put("board02", board[0][2]);
		m.put("board10", board[1][0]);
		m.put("board11", board[1][1]);
		m.put("board12", board[1][2]);
		m.put("board20", board[2][0]);
		m.put("board21", board[2][1]);
		m.put("board22", board[2][2]);
		return m;
	}
	
	@Override
	protected GameResult<TTTCharacter> getGameResult() {
				
		// all the possible wins...
		ArrayList<Boolean> winner = new ArrayList<Boolean>();
		
		// horizontal lines
		winner.add(checkRow(board[0][0], board[1][0], board[2][0]));
		winner.add(checkRow(board[0][1], board[1][1], board[2][1]));
		winner.add(checkRow(board[0][2], board[1][2], board[2][2]));
		
		// vertical lines
		winner.add(checkRow(board[0][0], board[0][1], board[0][2]));
		winner.add(checkRow(board[1][0], board[1][1], board[1][2]));
		winner.add(checkRow(board[2][0], board[2][1], board[2][2]));
		
		// diagonals
		winner.add(checkRow(board[0][0], board[1][1], board[2][2]));
		winner.add(checkRow(board[0][2], board[1][1], board[2][0]));
		
		// Did anyone win?
		Boolean winningSymbol = null; 
		for (Boolean b : winner) { 
			if (b != null) { 
				winningSymbol = b;
				break;
			}
		}
		
		if (winningSymbol != null) {
			GameResult<TTTCharacter> gr = new GameResult<TTTCharacter>(this.getGameID());
			for (TTTCharacter ch : characters) {  
				if (ch.getSymbol() == winningSymbol) { 
					gr.addWinner(ch);
				}
				else { 
					gr.addLoser(ch);
				}
			}
			return gr;
		}
		else if (isBoardFull()) { 
			GameResult<TTTCharacter> gr = new GameResult<TTTCharacter>(this.getGameID());
			for (TTTCharacter ch : characters) {  
				gr.addDrawer(ch);
			}
			return gr;
		}
		else { 
			return null;
		}
		
	}
	
	private boolean isBoardFull() { 
		for (int i = 0; i < 3; i++) { 
			for (int j = 0; j < 3; j++) { 
				if (board[i][j] != null) { 
					return false;
				}
			}
		}
		return true;
	}
	
	private Boolean checkRow(Boolean b1, Boolean b2, Boolean b3) { 
		if (b1 != null && b1 == b2 && b2 == b3) { 
			return b1;
		}
		return null;
	}
	
	public static final int
		NOT_YOUR_TURN = 1000,
		INVALID_SPACE = 1001,
		SPACE_ALREADY_TAKEN = 1002;
	
	public static final int
		ACTION_MOVE = 1000;
	
	private void move(Message m, Player p) throws RuleException { 
		
		if (!isCurrentPlayer(p)) { 
			throw new RuleException(NOT_YOUR_TURN);
		}
		Integer x = m.getInt("x"), y = m.getInt("y");
		if (x == null || x < 0 || x > 2 || y == null || y < 0 || y > 2) { 
			throw new RuleException(INVALID_SPACE);
		}
		if (board[x][y] != null) { 
			throw new RuleException(SPACE_ALREADY_TAKEN);
		}
		
		TTTCharacter c = this.getCharacter(p.getPlayerID());
		board[x][y] = c.getSymbol();
		
		startNewTurn();
	}
	
	@Override
	public void processMessage(Message m, Player p) throws Exception { 
		
		Integer action = m.getInt("action");
		Message response = new Message(action, getGameID());
		
		if (action == ACTION_MOVE) { 
			move(m,p);			
		}
		else { 
			throw new InvalidActionException(action);
		}
	
		GameResult<TTTCharacter> gr = this.getGameResult();
		if (gr != null) { 
			response.put("action", GAME_OVER);
			response.put("result", gr);
		}
		response.put("game", this);
		sendToAllPlayers(response);
	}
}

package com.darkenedsky.gemini.examples.tictactoe;
import java.util.ArrayList;
import com.darkenedsky.gemini.Message;
import com.darkenedsky.gemini.Player;
import com.darkenedsky.gemini.service.GameLauncher;
import com.darkenedsky.gemini.service.JDBCConnection;
import com.darkenedsky.gemini.service.NotEveryoneIsReadyException;
import com.darkenedsky.gemini.service.WinLossRecordManager;


public class TTTLauncher extends GameLauncher<TTTGame> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8469707810077369369L;

	public TTTLauncher(long gid, Message e, JDBCConnection jdbc, WinLossRecordManager wlrm) {
		super(gid, e, jdbc, wlrm);
	}
	
	@Override
	public TTTGame start() throws Exception {
		if (!this.isEveryoneReady())
			throw new NotEveryoneIsReadyException();
		
		ArrayList<TTTCharacter> chrs = new ArrayList<TTTCharacter>();
		for (Player p : this.playersReady.keySet()) { 
			TTTCharacter mc = new TTTCharacter(p);
			chrs.add(mc);
		}
		
		
		TTTGame game = new TTTGame(this.getName(), this.getGameID(), chrs, jdbc, winLossRecordManager);
		return game;		
	}
	

}

package com.darkenedsky.gemini;

import java.util.ArrayList;


public class GameResult<TChar extends GameCharacter> implements MessageSerializable {

	private long gameid;
	
	public GameResult(long gameid) { 
		this.gameid = gameid;		
	}
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -2687076732030185113L;
	private ArrayList<TChar> winners = new ArrayList<TChar>();
	private ArrayList<TChar> losers = new ArrayList<TChar>();
	private ArrayList<TChar> drawers = new ArrayList<TChar>();
	
	public void addWinner(TChar win) { winners.add(win); }
	public void addLoser(TChar lose) { losers.add(lose); } 
	public void addDrawer(TChar draw) { drawers.add(draw); }
	
	public ArrayList<TChar> getWinners() { return winners; }
	public ArrayList<TChar> getLosers() { return losers; }
	public ArrayList<TChar> getDrawers() { return drawers; }
	public long getGameID() { return gameid; }
	
	@Override
	public Message serialize(Player p) { 
		Message m = new Message(ActionList.GAME_END, gameid);
		
		if (!winners.isEmpty()) { 
			m.addList("win");
			for (TChar win : winners) { 
				m.addToList("win", win,p);
			}
		}
		if (!losers.isEmpty()) { 
			m.addList("lose");
			for (TChar win : losers) { 
				m.addToList("lose", win,p);
			}
		}
		if (!drawers.isEmpty()) { 
			m.addList("draw");
			for (TChar win : drawers) { 
				m.addToList("draw", win,p);
			}
		}
		return m;
	}
	
	
}

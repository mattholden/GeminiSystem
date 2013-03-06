package com.darkenedsky.gemini;

public interface HandlerValidator  {

	public static final Boolean REQUIRES_YES = false, REQUIRES_NO = true,
			NO_REQUIREMENT = null;

	public static final int ON_ANY_TURN = 0, ON_YOUR_TURN = 1,
			NOT_YOUR_TURN = 2, ON_TEAM_TURN = 3, NOT_TEAM_TURN = 4;

	public void validate(Message m, Player p) throws Exception;

	public Game<?> getGame();
	
	public void setGame(Game<?> game);
	
}

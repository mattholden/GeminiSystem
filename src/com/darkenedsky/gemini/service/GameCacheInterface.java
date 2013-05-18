package com.darkenedsky.gemini.service;


public interface GameCacheInterface {

	public GeminiServer getServer();

	/**
	 * Get the win/loss record manager
	 * 
	 * @return the win/loss record manager
	 */
	public WinLossRecordManager getWinLossRecordManager();

	/**
	 * Remove the game from the cache when it's over
	 * 
	 * @param id
	 *            the ID to remove
	 */
	public void uncacheGame(long id);

}

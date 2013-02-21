package com.darkenedsky.gemini.service;

import com.darkenedsky.gemini.Library;

public interface GameCacheInterface {

	/** Remove the game from the cache when it's over
	 *  
	 * @param id the ID to remove
	 */
	public void uncacheGame(long id);
	
	/** Get the JDBC Connection
	 * @return the JDBC Connection
	 */
	public JDBCConnection getJDBC();
	
	/** Get the win/loss record manager 
	 * @return the win/loss record manager
	 */
	public WinLossRecordManager getWinLossRecordManager();
	
	/** Get the game definition library 
	 *  @return the library
	 */
	public Library getLibrary();
	
}

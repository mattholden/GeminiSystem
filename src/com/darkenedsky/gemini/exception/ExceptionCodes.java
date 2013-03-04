package com.darkenedsky.gemini.exception;

/** Lookup interface for all exception codes thrown by the core Gemini modules. 
 *  Each game project should extend this to add their own.
 *  Use the constants in the constructors of the Exception classes; in this way,
 *  we can guarantee that the exception codes don't fall out of sync with the constants here.
 *  
 * @author Matt Holden
 *
 */
public interface ExceptionCodes {

	public static final int
	
	// CATCH-ALL FOR WHEN WE HAVE TO PROMOTE JAVA EXCEPTIONS TO GEMINI EXCEPTIONS
	JAVA_EXCEPTION = 0,
	
	// SQL didn't return the number of rows we expected on an insert/update
	SQL_UPDATE_FAILED = 100,
	
	// LOGIN/SESSION
	INVALID_LOGIN = 1001,
	INVALID_SESSION = 1002,
	INVALID_PLAYER = 1003,
	INVALID_GAME = 1004,
	INVALID_ACTION = 1005,
	USER_BANNED = 1006,
	ACTION_NOT_ALLOWED = 1007,

	// GAME LOBBY / INITIALIZATION
	NOT_ON_THIS_TURN = 2001,
	NOT_ON_THIS_PHASE = 2002,
	GAME_FULL = 2003,
	INVALID_GAME_PASSWORD = 2004,
	GAME_PLAYER = 2005,
	NOT_EVERYONE_IS_READY = 2006,
	INVALID_OBJECT = 2007,
	INVALID_LIBRARY_SECTION = 2008,
	
	// GAME MODULES
	CCG_DUPLICATE_DECKNAME = 3001,
	CCG_INVALID_DECK = 3002,

	// deck validation
	CCG_DV_UNPURCHASED_CARD = 3100,
	CCG_DV_TOO_MANY_COPIES = 3101,
	CCG_DV_TOTAL_DECK_SIZE = 3102;
	
	
}

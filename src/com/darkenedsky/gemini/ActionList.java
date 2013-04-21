package com.darkenedsky.gemini;

/** 
 * Interface to list all of the constant Verb IDs.
 * NOTE: When changing this file, make sure to change the client's "actions.js" file to match!
 * 
 * @author Matt Holden
 *
 */
public interface ActionList {

	// DOES NOT REQUIRE A SESSION TO EXECUTE
	public static final int
		HELLO_WORLD = 0,
		LOGIN = 1,
		CREATE_ACCOUNT = 2,
		FORGOTPASS_REQUEST = 3,
		FORGOTPASS_REDEEM = 4;
	
	// GAME LOBBY AND CORE GAME STATES
	public static final int
		CREATE_GAME = 100,
		ADD_PLAYER = 101,
		DROP_PLAYER = 102,
		SET_READY = 103,
		START_GAME = 104,
		GET_OPEN_GAMES = 105,
		GAME_END = 106,
		TURN_START = 107,
		TURN_END = 108,
		MAIN_PHASE = 109,
		FORFEIT = 110,
		GAME_STATE = 111,
		OBJECT_UPDATED = 112,
		CHARACTER_UPDATED = 113,
		CALLBACK = 120,
		CHOOSER = 121,
		CONFIRM_YES_NO = 122,
		CONFIRM_OK_CANCEL = 123;
	
	// POST-LOGIN USER STUFF
	public static final int 
		POLL = 200,
		VERIFY_EMAIL_REQUEST = 201,
		VERIFY_EMAIL_REDEEM = 202,
		LOGOUT = 203,
		GET_ANALYTICS = 204,
		GRANT_BADGE = 205;
	
	// chat system and guild commands
	public static final int
		CHAT = 300,
		WHISPER = 301,
		
		GUILD_CREATE = 350,
		GUILD_CHAT = 351,
		GUILD_GET = 352,
		GUILD_GET_MEMBERS_ONLINE = 353,
		GUILD_LEAVE = 354,
		GUILD_JOIN = 355,
		GUILD_INVITE = 356,
		GUILD_DECLINE = 357,
		GUILD_KICK = 358,
		GUILD_SETRANK = 359,
		GUILD_EDIT = 360;
	
	
	// card games / ccgs
	public static final int 
		CCG_GET_DECKS = 400,
		CCG_CREATE_DECK = 401,
		CCG_DELETE_DECK = 402,
		CCG_EDIT_DECK = 403,
		CCG_CLONE_DECK = 404,
		CCG_GET_STARTER_DECKS = 405,
		CCG_GET_VALID_CARDS = 406,
		
		CCG_SPECIAL0 = 410,
		CCG_SPECIAL1 = 411,
		CCG_SPECIAL2 = 412,
		CCG_SPECIAL3 = 413,
		CCG_SPECIAL4 = 414,
		CCG_SPECIAL5 = 415,
		CCG_SPECIAL6 = 416,
		CCG_SPECIAL7 = 417,
		CCG_SPECIAL8 = 418,
		CCG_SPECIAL9 = 419;
	
	
	// STORE
	public static final int
		STORE_GETCATALOG = 500,
		STORE_EDITCARD = 501,
		STORE_PURCHASE = 502,
		STORE_CANCELSUB = 503;
	
		
	
}

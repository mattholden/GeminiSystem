package com.darkenedsky.gemini.examples.tictactoe;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.darkenedsky.gemini.Player;

public class TTTPlayer extends Player {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1666299609854654313L;

	public TTTPlayer(ResultSet set) throws SQLException {
		super(set);
	}

}

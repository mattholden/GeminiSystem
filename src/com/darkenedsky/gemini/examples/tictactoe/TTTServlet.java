package com.darkenedsky.gemini.examples.tictactoe;
import java.io.File;
import com.darkenedsky.gemini.service.GeminiService;
import com.darkenedsky.gemini.service.GeminiServlet;


public class TTTServlet extends GeminiServlet<TTTCharacter, TTTGame, TTTPlayer> {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -3201080529273719869L;

	@Override
	public void init() { 
		try {
 		 
	     System.out.println("Initializing TicTacToe Servlet...");
	     
	     service = new GeminiService<TTTCharacter, TTTGame, TTTPlayer>(TTTLauncher.class, TTTPlayer.class, new File("tictactoe.xml"));
		 
		 
		}
		catch (Exception x) { 
			x.printStackTrace();
		}
	}
}

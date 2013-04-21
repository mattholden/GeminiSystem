package com.darkenedsky.gemini.card;

import com.darkenedsky.gemini.Message;
import com.darkenedsky.gemini.Player;

public interface CardObserver {

	public void observeDraw(Card drawn, Player p) throws Exception;

	public void observeGainResources(Resources amt, Player p) throws Exception;

	public void observeSpecial(Card card, Message m, Player p) throws Exception;

	public void observeSpendResources(Resources amt, Player p) throws Exception;

	public void observeTap(Card tap) throws Exception;

	public void observeUntap(Card tap) throws Exception;

	public void validateSpecial(Card special, Message m, Player p) throws Exception;

	public void validateTap(Card tap) throws Exception;

	public void validateUntap(Card tap) throws Exception;

}

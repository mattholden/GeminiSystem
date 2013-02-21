package com.darkenedsky.gemini.stats;
import com.darkenedsky.gemini.GameCharacter;
import com.darkenedsky.gemini.Message;
import com.darkenedsky.gemini.Player;

public class StatisticPrerequisite implements Prerequisite { 
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1903081522145546121L;
	protected int value;
	protected String score;
	
	public StatisticPrerequisite(String score, int value) { 
		this.score = score;
		this.value = value;
	}
	
	@Override
	public boolean satisfies(GameCharacter character) { 
		return (character.getStat(score).getBaseValue() >= value);
	}

	
	@Override
	public Message serialize(Player p) { 
		Message m = new Message();
		m.put("type", getClass().getSimpleName());
		m.put("score", score);
		m.put("value", value);
		return m;
	}
	
}

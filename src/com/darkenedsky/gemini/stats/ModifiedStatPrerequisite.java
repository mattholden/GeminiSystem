package com.darkenedsky.gemini.stats;
import com.darkenedsky.gemini.GameCharacter;

public class ModifiedStatPrerequisite extends StatisticPrerequisite {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2027140748991824972L;

	public ModifiedStatPrerequisite(String score, int value) {
		super(score, value);
	} 
	
	@Override
	public boolean satisfies(GameCharacter character) { 
		return (character.getStat(score).getValueWithBonuses() >= value);
	}

	
}

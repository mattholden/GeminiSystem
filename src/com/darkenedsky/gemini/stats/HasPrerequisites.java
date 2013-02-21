package com.darkenedsky.gemini.stats;

import com.darkenedsky.gemini.GameCharacter;

public interface HasPrerequisites<T extends GameCharacter> {

	public boolean hasPrerequisites(T character);
	
}

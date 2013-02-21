package com.darkenedsky.gemini.stats;
import com.darkenedsky.gemini.GameCharacter;
import com.darkenedsky.gemini.MessageSerializable;

/** 
 * Interface for a type of prerequisite
 * @author Matt Holden
 *
 */
public interface Prerequisite extends MessageSerializable {

	public boolean satisfies(GameCharacter character);
	
}

package com.darkenedsky.gemini;

import java.util.HashMap;

import com.darkenedsky.gemini.tools.StringTools;

public abstract class GameObject implements Comparable<GameObject>, MessageSerializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1298599492403350149L;
	
	/** Localized names. */
	private HashMap<String, String> name = new HashMap<String, String>();
	
	/** Object ID. Will be null on definition objects. */
	private Long objectID;
	
	@Override
	public String toString() { 
		return name.get(Languages.ENGLISH) + " [" + getClass().getName() + " DefID:" + getDefinitionID() + ", ObjID:"+ objectID + "]";
	}
	
	@Override
	public boolean equals(Object other) { 
		if (other instanceof GameObject)
			return (((GameObject) other).getObjectID() == getObjectID());
		else return false;
	}
	
	public GameObject(Long objID, String englishName) {	
		name.put("en", englishName);
		objectID = objID;
	}
	
	public Long getObjectID() { 
		return objectID;
	}
	
	public int getDefinitionID() { 
		return hashCode();
	}
	
	@Override
	public int hashCode() { 
		return getIDString().hashCode();
	}
	
	public String getIDString() { 
		return getClass().getName() + "$" + 
			StringTools.stringReplace(StringTools.stringReplace(
				StringTools.replaceSpaces(name.get(Languages.ENGLISH)), "'", ""), ",", "");
	}
	
	@Override
	public int compareTo(GameObject other) { 
		return getIDString().compareToIgnoreCase(other.getIDString());
	}
	
	@Override
	public Message serialize(Player p) {
		Message m = new Message();		
		m.put("name", name.get(p.getLanguage()));
		m.put("definitionid", getDefinitionID());
		m.put("id", getObjectID());
		return m;
	}
}

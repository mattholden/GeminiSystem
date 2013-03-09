package com.darkenedsky.gemini;

public  class GameObject extends LocalizedObject implements Comparable<GameObject>, MessageSerializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1298599492403350149L;
	
	/** Object ID. Will be null on definition objects. */
	private Long objectID;
	
	/** Definition ID */
	private int definitionID;
	
	
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
	
	public GameObject(int defID, Long objID, String englishName) {	
		super(englishName);
		objectID = objID;
		definitionID = defID;
	}
	
	public Long getObjectID() { 
		return objectID;
	}
	
	public int getDefinitionID() { 
		return definitionID;
	}
	
	@Override
	public int hashCode() { 
		return getDefinitionID();
	}
	
	@Override
	public int compareTo(GameObject other) { 
		return name.get(Languages.ENGLISH).compareToIgnoreCase(name.get(Languages.ENGLISH));
	}
	
	@Override
	public Message serialize(Player p) {
		Message m = super.serialize(p);
		m.put("definitionid", getDefinitionID());
		m.put("id", getObjectID());
		return m;
	}
}

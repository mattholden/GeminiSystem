package com.darkenedsky.gemini.stats;
import com.darkenedsky.gemini.Message;
import com.darkenedsky.gemini.Player;
import com.darkenedsky.gemini.GameObject;
import com.darkenedsky.gemini.MessageSerializable;

public class Bonus implements MessageSerializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6915785852300090440L;
	private GameObject source;
	private Modifier modifier;
	private String conditional;
	
	public Bonus(GameObject source, Modifier mod) { this(source, mod, null); }
	
	public Bonus(GameObject source, Modifier modifier, String conditional) {
		super();
		this.source = source;
		this.modifier = modifier;
		this.conditional = conditional;
	}

	public int modify(int value) {
		return modifier.modify(value);
	}

	public GameObject getSource() {
		return source;
	}

 
	public String getConditional() {
		return conditional;
	}


	public boolean isConditional() { 
		return conditional != null;
	}
	
	@Override
	public Message serialize(Player p) { 
		Message m = new Message();
		m.put("type", getClass().getSimpleName());
		m.put("conditional", conditional);
		m.put("source", source.getObjectID());
		m.put("modifier", modifier, p);
		return m;
	}
	
	
}

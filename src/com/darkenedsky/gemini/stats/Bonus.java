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
	private Integer expires = null;
	
	public static final int 
		START_OF_YOUR_NEXT_TURN = 1,
		END_OF_YOUR_NEXT_TURN = 2,
		END_OF_THIS_TURN = 3,
		START_OF_NEXT_TURN = 4;
	

	public Bonus(GameObject source, Modifier mod) { this(source, mod, null, null); }
	public Bonus(GameObject source, Modifier mod, int exp) { this(source, mod, exp, null); }
	
	public Bonus(GameObject source, Modifier modifier, Integer expire, String conditional) {
		super();
		this.expires = expire;
		this.source = source;
		this.modifier = modifier;
		this.conditional = conditional;
	}

	public Integer getExpiration() { return expires; }
	
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
		m.put("expires", expires);
		return m;
	}
	
	
}

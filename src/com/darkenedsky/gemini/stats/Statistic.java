package com.darkenedsky.gemini.stats;
import java.util.Vector;

import com.darkenedsky.gemini.GameObject;
import com.darkenedsky.gemini.Message;
import com.darkenedsky.gemini.MessageSerializable;
import com.darkenedsky.gemini.Player;
import com.darkenedsky.gemini.stats.Bonus;
import com.darkenedsky.gemini.stats.Modifier;
import com.darkenedsky.gemini.stats.Plus;


public class Statistic implements MessageSerializable { 
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1929381674148057599L;

	public Statistic(int i) { baseValue = i; } 
	
	private Integer minCap = null, maxCap = null;
	
	public void setMinCap(Integer i) { 
		minCap = i;
	}
	
	public void setMaxCap(Integer i) { 
		maxCap = i;
	}
	
	private boolean secret = false;
	
	public boolean isSecret() { 
		return secret;
	}
	
	public void clearBonuses() { 
		this.bonuses.clear();
	}
	
	public Statistic() { this(0); }
	
	public String toString() { 
		return Integer.toString(this.getBaseValue());
	}
	
	public int getValueWithBonuses() { 
		int total = baseValue;
		for (Bonus b : getBonuses()) { 
			if (!b.isConditional())
				total = b.modify(total);
		}
		return cap(total);
	}
	
	private int cap(int total) { 
		if (minCap != null && minCap > total)
			return minCap;
		if (maxCap != null && maxCap < total)
			return maxCap;
		return total;
	}
	
	public int getBaseValue() {
		return cap(baseValue);
	}

	public void incrementBase(int val) { 
		baseValue += val;
	}
	
	public void setBaseValue(int base) {
		this.baseValue = base;
	}


	protected int baseValue = 0;
	
	private Vector<Bonus> bonuses = new Vector<Bonus>();
	
	public void addBonus(GameObject src, int plus) { 
		addBonus(src, new Plus(plus));
	}
	
	public void addBonus(GameObject src, Modifier mod) { addBonus(src, mod, null); }
	
	public void addBonus(GameObject source, Modifier mod, String conditional) { 
		bonuses.add(new Bonus(source, mod, conditional));
	}
	public void addBonus(Bonus b) { 
		bonuses.add(b);
	}
	
	public void dropBonuses(GameObject ro) { 
		for (Bonus b : getBonuses()) { 
			if (b.getSource().equals(ro)) { 
				bonuses.remove(b);
			}
		}
	}
	
	public Vector<Bonus> getBonuses() { 
		return bonuses;
	}
	
	@Override
	public Message serialize(Player p) {
		Message m = new Message();
		m.put("basevalue", getBaseValue());
		m.put("currentvalue", this.getValueWithBonuses());
		
		// You don't need this when you're playing but might be nice to print on a "character sheet"
		if (p == null) { 
			m.addList("bonuses");
			for (Bonus b : bonuses) { 
				m.addToList("bonuses", b, p);
			}
		}
		
		return m;		
	}
	 
}

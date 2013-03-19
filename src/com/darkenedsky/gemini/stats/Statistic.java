package com.darkenedsky.gemini.stats;
import java.util.Vector;
import com.darkenedsky.gemini.GameObject;
import com.darkenedsky.gemini.LocalizedObject;
import com.darkenedsky.gemini.Message;
import com.darkenedsky.gemini.Player;
import com.darkenedsky.gemini.stats.Bonus;
import com.darkenedsky.gemini.stats.Modifier;
import com.darkenedsky.gemini.stats.Plus;


public class Statistic extends LocalizedObject { 
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1929381674148057599L;

	private static class StatVisibility { 
		public int visibility;
		private StatVisibility(int vis) { 
			visibility = vis;
		}		
	};
	
	public static final StatVisibility
		ALWAYS_HIDDEN = new StatVisibility(0),
		HIDDEN_IF_ZERO = new StatVisibility(1),
		ALWAYS_VISIBLE = new StatVisibility(2);
	
	private StatVisibility visibility = ALWAYS_VISIBLE;

	public Statistic(String enName) { 
		this(enName, 0, ALWAYS_VISIBLE);
	}
	public Statistic(String enName, int i) { 
		this(enName, i, ALWAYS_VISIBLE);
	}
	
	public Statistic(String enName, int i, StatVisibility viz) { 
		super(enName);
		baseValue = i;
		visibility = viz;
	} 
	
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
		
	public int getValueWithBonuses() { 
		return getConditionalValue(null);
	}
	
	public int getConditionalValue(String conditional) { 
		int total = baseValue;
		for (Bonus b : getBonuses()) { 
			if (!b.isConditional() || b.getConditional().equalsIgnoreCase(conditional))
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
	
	public void incrementBaseValue(int val) { 
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
	public void addBonus(GameObject src, int plus, Integer exp) { 
		addBonus(src, new Plus(plus), exp);
	}
	
	public void addBonus(GameObject src, Modifier mod) { addBonus(src, mod, null, null); }
	public void addBonus(GameObject src, Modifier mod, Integer exp) { addBonus(src, mod, exp, null); }
	
	public void addBonus(GameObject src, Modifier mod, Integer exp, String con) { 
		bonuses.add(new Bonus(src, mod, exp, con));
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
	
	public void removeBonus(Bonus b) { 
		bonuses.remove(b);
	}
	
	public Vector<Bonus> getBonuses() { 
		return bonuses;
	}
	
	@Override
	public Message serialize(Player p) {
		
		if (this.visibility.visibility == ALWAYS_HIDDEN.visibility) { 
			return null;
		}
		if (this.visibility.visibility == HIDDEN_IF_ZERO.visibility && this.getValueWithBonuses() == 0) { 
			return null;
		}
		
		Message m = super.serialize(p);
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

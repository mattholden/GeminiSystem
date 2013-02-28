package com.darkenedsky.gemini;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import com.darkenedsky.gemini.GameObject;
import com.darkenedsky.gemini.Message;
import com.darkenedsky.gemini.Player;
import com.darkenedsky.gemini.stats.Bonus;
import com.darkenedsky.gemini.stats.HasStats;
import com.darkenedsky.gemini.stats.Modifier;
import com.darkenedsky.gemini.stats.Statistic;

public class AdvancedGameObject extends GameObject implements HasStats {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5790615972027062731L;

	protected ConcurrentHashMap<String, Statistic> statistics = new ConcurrentHashMap<String, Statistic>(20);
	

	/* (non-Javadoc)
	 * @see com.darkenedsky.gemini.card.HasStatsAndTags#getStat(java.lang.String)
	 */
	@Override
	public Statistic getStat(String stat) { 
		return statistics.get(stat);
	}

	/* (non-Javadoc)
	 * @see com.darkenedsky.gemini.card.HasStatsAndTags#getStats()
	 */
	@Override
	public Map<String,Statistic> getStats() { 
		return statistics;
	}

	/* (non-Javadoc)
	 * @see com.darkenedsky.gemini.card.HasStatsAndTags#dropEffects(com.darkenedsky.gemini.GameObject)
	 */
	@Override
	public void dropEffects(GameObject source) throws Exception { 
		for (Statistic e : statistics.values()) 
			e.dropBonuses(source);
	}

	/* (non-Javadoc)
	 * @see com.darkenedsky.gemini.card.HasStatsAndTags#addBonus(int, com.darkenedsky.gemini.modifier.Bonus)
	 */
	@Override
	public void addBonus(int stat, Bonus b) { 
			
		Statistic s = statistics.get(stat);
		if (s != null)
			s.addBonus(b);
	}

	/* (non-Javadoc)
	 * @see com.darkenedsky.gemini.card.HasStatsAndTags#addBonus(int, com.darkenedsky.gemini.GameObject, com.darkenedsky.gemini.modifier.Modifier, java.lang.String)
	 */
	@Override
	public void addBonus(int stat, GameObject source, Modifier mod, String conditional) { 
		addBonus(stat, new Bonus(source, mod, conditional));
	}


	public AdvancedGameObject(int defID, Long objID, String englishName) {
		super(defID, objID, englishName);		
	}
	
	@Override
	/** Serialize the object. Note: You will have to override this method in child classes if you wish to 
	 *  reintroduce the enforcement of isSecret() on Statistics and Tags, as the AdvancedGameObject has no 
	 *  "player" object or ID to compare to and know if it belongs to them or not. In this implementation,
	 *  Secret tags and statistics are not rendered no matter who the player is.
	 *  
	 *  @param player the player being serialized for
	 *  @return the Message object.
	 */
	public Message serialize(Player p) { 
		Message m = super.serialize(p);
		
		m.addList("stats");
		for (Map.Entry<String, Statistic> stat : statistics.entrySet()) {
	
			if (stat.getValue().isSecret()) continue;
			Message s = stat.getValue().serialize(p);
			if (s != null) { 
				s.put("stat", stat.getKey());
				m.addToList("stats", s, p);
			}
		}
		
		return m;
	}
	
	@Override
	public boolean hasKeywordOrTag(String field) { 
		return (statistics.get(field).getValueWithBonuses() > 0);
	}
	
	@Override
	public final void addKeyword(String field, String name) { 
		statistics.put(field, new Statistic(name, 0, Statistic.HIDDEN_IF_ZERO));
	}
	
	@Override
	public final void addTag(String field, String name) { 
		statistics.put(field, new Statistic(name, 0, Statistic.ALWAYS_HIDDEN));
	}
	
}

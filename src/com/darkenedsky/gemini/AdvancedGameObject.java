package com.darkenedsky.gemini;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import com.darkenedsky.gemini.GameObject;
import com.darkenedsky.gemini.Message;
import com.darkenedsky.gemini.Player;
import com.darkenedsky.gemini.stats.Bonus;
import com.darkenedsky.gemini.stats.HasStatsAndTags;
import com.darkenedsky.gemini.stats.Modifier;
import com.darkenedsky.gemini.stats.Statistic;
import com.darkenedsky.gemini.stats.Tag;

public class AdvancedGameObject extends GameObject implements HasStatsAndTags {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5790615972027062731L;

	protected ConcurrentHashMap<String, Statistic> statistics = new ConcurrentHashMap<String, Statistic>(20);
	protected ConcurrentHashMap<String, Tag> tags = new ConcurrentHashMap<String, Tag>();
	
	/* (non-Javadoc)
	 * @see com.darkenedsky.gemini.card.HasStatsAndTags#getTags()
	 */
	@Override
	public Vector<String> getTags() { 
		Vector<String> tagz = new Vector<String>();
		tagz.addAll(tags.keySet());
		return tagz;
	}
	
	/* (non-Javadoc)
	 * @see com.darkenedsky.gemini.card.HasStatsAndTags#addTag(java.lang.String, com.darkenedsky.gemini.GameObject)
	 */
	@Override
	public void addTag(String tag, GameObject src) {
		String tg = tag.toLowerCase();
		Tag t = new Tag(tg, src);
		tags.put(tg, t);
	}
	
	/* (non-Javadoc)
	 * @see com.darkenedsky.gemini.card.HasStatsAndTags#hasTag(java.lang.String)
	 */
	@Override
	public boolean hasTag(String tag) { 
		String t = tag.toLowerCase();
		return (tags.get(t) != null);
	}
	

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
		for (Tag t : tags.values())
			if (t.getSource().equals(source))
				tags.remove(t.getTag());
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


	public AdvancedGameObject(Long objID, String englishName) {
		super(objID, englishName);		
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
			s.put("stat", stat.getKey());
			m.addToList("stats", s, p);
		}
		
		m.addList("tags");
		for (Tag t : tags.values()) { 
			if (t.isSecret()) continue;
			m.addToList("tags", t, p);
		}
		return m;
	}
	
}

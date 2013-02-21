package com.darkenedsky.gemini;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import com.darkenedsky.gemini.stats.Bonus;
import com.darkenedsky.gemini.stats.Gender;
import com.darkenedsky.gemini.stats.HasStatsAndTags;
import com.darkenedsky.gemini.stats.Modifier;
import com.darkenedsky.gemini.stats.Statistic;
import com.darkenedsky.gemini.stats.Tag;

public class GameCharacter implements MessageSerializable, HasStatsAndTags, Gender {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8851137714908121840L;
	private Player player;
	protected String name = "New Character";
	protected ConcurrentHashMap<String, Statistic> statistics = new ConcurrentHashMap<String, Statistic>(20);
	protected ConcurrentHashMap<String, Tag> tags = new ConcurrentHashMap<String, Tag>();
	
	@Override
	public Vector<String> getTags() { 
		Vector<String> tagz = new Vector<String>();
		tagz.addAll(tags.keySet());
		return tagz;
	}
	
	@Override
	public void addTag(String tag, GameObject src) {
		String tg = tag.toLowerCase();
		Tag t = new Tag(tg, src);
		tags.put(tg, t);
	}
	
	@Override
	public boolean hasTag(String tag) { 
		String t = tag.toLowerCase();
		return (tags.get(t) != null);
	}
	
	private Game<? extends GameCharacter, ? extends Player> game;
	
	public Game<? extends GameCharacter, ? extends Player> getGame() { return game; }
	
	public void setGame(Game<? extends GameCharacter, ? extends Player> game) { 
		this.game = game;
	}
	
	public GameCharacter(Player p) {
		super();
		player = p;
		name = p.getUsername();
		this.statistics.put(Gender.GENDER_FIELD, new Statistic(p.getGender()));				
	}

	@Override
	public int getGender() { 
		return getStat(Gender.GENDER_FIELD).getValueWithBonuses();
	}
	
	@Override
	public void setGender(int gender) { 
		getStat(Gender.GENDER_FIELD).setBaseValue(gender);
	}
	@Override
	public Statistic getStat(String stat) { 
		return statistics.get(stat);
	}
	@Override
	public Map<String,Statistic> getStats() { 
		return statistics;
	}
	@Override
	public void dropEffects(GameObject source) throws Exception { 
		for (Statistic e : statistics.values()) 
			e.dropBonuses(source);
		for (Tag t : tags.values())
			if (t.getSource().equals(source))
				tags.remove(t.getTag());
	}

	@Override
	public void addBonus(int stat, Bonus b) { 
			
		Statistic s = statistics.get(stat);
		if (s != null)
			s.addBonus(b);
	}

	@Override
	public void addBonus(int stat, GameObject source, Modifier mod, String conditional) { 
		addBonus(stat, new Bonus(source, mod, conditional));
	}

	

	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Player getPlayer() {
		return player;
	}

	public void setPlayer(Player player) {
		this.player = player;
	}

	public void onTurnEnd() throws Exception { } 	
	public void onTurnStart() throws Exception { } 
	
	public boolean isCurrentPlayer() { 
		return game.isCurrentPlayer(this.getPlayer().getPlayerID());
	}
	
	@Override
	public Message serialize(Player p) { 
		Message m = new Message();
		m.put("name", name);
		m.put("player", player, p);
	
		m.addList("stats");
		for (Map.Entry<String, Statistic> stat : statistics.entrySet()) {
	
			// If this statistic is SECRET, only serialize it to the message if I am the one asking for it
			if (stat.getValue().isSecret() && p != null && p.getPlayerID() != player.getPlayerID())
				continue;
			
			Message s = stat.getValue().serialize(p);
			s.put("stat", stat.getKey());
			m.addToList("stats", s, p);
		}
		
		m.addList("tags");
		for (Tag t : tags.values()) { 
			if (t.isSecret() && p != null && p.getPlayerID() != player.getPlayerID()) 
				continue;
			m.addToList("tags", t, p);
		}
		return m;
	}
		
}
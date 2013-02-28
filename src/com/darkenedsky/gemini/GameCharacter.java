package com.darkenedsky.gemini;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import com.darkenedsky.gemini.stats.Bonus;
import com.darkenedsky.gemini.stats.Gender;
import com.darkenedsky.gemini.stats.HasStats;
import com.darkenedsky.gemini.stats.Modifier;
import com.darkenedsky.gemini.stats.Statistic;

public class GameCharacter implements MessageSerializable, HasStats, Gender {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8851137714908121840L;
	private Player player;
	protected String name = "New Character";
	protected ConcurrentHashMap<String, Statistic> statistics = new ConcurrentHashMap<String, Statistic>(20);
	
	
	private Game<? extends GameCharacter, ? extends Player> game;
	
	public Game<? extends GameCharacter, ? extends Player> getGame() { return game; }
	
	public void setGame(Game<? extends GameCharacter, ? extends Player> game) { 
		this.game = game;
	}
	
	public GameCharacter(Player p) {
		super();
		player = p;
		name = p.getUsername();
		this.statistics.put(Gender.GENDER_FIELD, new Statistic("Gender", p.getGender()));				
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
			if (s != null) { 
				s.put("stat", stat.getKey());
				m.addToList("stats", s, p);
			}
		}
		
		
		return m;
	}
		
}
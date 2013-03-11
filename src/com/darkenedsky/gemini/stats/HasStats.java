package com.darkenedsky.gemini.stats;
import java.util.Map;
import com.darkenedsky.gemini.GameObject;
import com.darkenedsky.gemini.exception.InvalidStatisticException;
import com.darkenedsky.gemini.stats.Bonus;
import com.darkenedsky.gemini.stats.Modifier;

public interface HasStats {


	public abstract Statistic getStat(String stat) throws InvalidStatisticException;

	public abstract Map<String, Statistic> getStats();

	public abstract void dropEffects(GameObject source) throws Exception;

	public abstract void addBonus(String stat, Bonus b) throws InvalidStatisticException;

	public boolean hasKeywordOrTag(String field) throws InvalidStatisticException;
	
	public void addKeyword(String field, String name);

	void addTag(String field, String name);

	void addBonus(String stat, GameObject source, Modifier mod, Integer expires) throws InvalidStatisticException;

	void expireBonuses(int expiration);
	
	public int getStatValue(String stat) throws InvalidStatisticException;
}
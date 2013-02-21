package com.darkenedsky.gemini.stats;
import java.util.Map;
import java.util.Vector;
import com.darkenedsky.gemini.GameObject;
import com.darkenedsky.gemini.stats.Bonus;
import com.darkenedsky.gemini.stats.Modifier;

public interface HasStatsAndTags {

	public abstract Vector<String> getTags();

	public abstract void addTag(String tag, GameObject src);

	public abstract boolean hasTag(String tag);

	public abstract Statistic getStat(String stat);

	public abstract Map<String, Statistic> getStats();

	public abstract void dropEffects(GameObject source) throws Exception;

	public abstract void addBonus(int stat, Bonus b);

	public abstract void addBonus(int stat, GameObject source, Modifier mod,
			String conditional);

}
package io.github.wispoffates.minecraft.tktowns.datastore;

import io.github.wispoffates.minecraft.tktowns.Town;

import java.util.Map;
import java.util.concurrent.TimeUnit;

public interface DataStore {
	//town initial cose
	public int townCost();
	public void townCost(int cost);
	//town upkeep time
	public int townUpkeepInterval();
	public void townUpkeepInterval(int interval);
	public TimeUnit townUpkeepUnit();
	public void townUpkeepUnit(TimeUnit unit);
	//town upkeep cost
	public int townUpkeepCost();
	public void townUpkeepCost(int cost);
	
	//town loading and saving
	public Map<String,Town> loadTowns();
	public void saveTowns(Map<String,Town> towns);
	public void saveTown(String name, Town town);
}

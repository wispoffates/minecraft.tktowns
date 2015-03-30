package io.github.wispoffates.minecraft.tktowns.datastore;

import io.github.wispoffates.minecraft.tktowns.api.impl.Town;

import java.util.List;
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
	public void saveTowns(List<Town> towns);
	public void saveTown(Town town);
	public void deleteTown(Town town);
}

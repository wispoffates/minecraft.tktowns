package io.github.wispoffates.minecraft.tktowns.datastore;

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
	public int getTownUpkeepCost();
	public void setTownUpkeepCost(int cost);
}

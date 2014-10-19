package io.github.wispoffates.minecraft.tktowns;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class TownManager {
	
	//singleton instance
	protected static TownManager instance;
	protected static Object lock = new Object();
	
	//variables and stuff
	protected Map<String,Town> towns;
	
	protected TownManager() {
		towns = new HashMap<String,Town>();
	}
	
	public static TownManager getInstance() {
		if(instance == null) {
			synchronized(lock) {
				if(instance == null) {
					instance = new TownManager();
				}
			}
		}
		return instance;
	}
	
	/**
	 * Add a town.
	 * @param town The town to be added.
	 */
	public void addTown(Town town) {
		this.towns.put(town.getName(), town);
	}
	
	public Town getTown(String name) {
		return this.towns.get(name);
	}
	
	public Set<String> listTowns() {
		return this.towns.keySet();
	}
	
}

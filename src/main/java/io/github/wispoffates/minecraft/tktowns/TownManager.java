package io.github.wispoffates.minecraft.tktowns;

public class TownManager {
	
	//singleton instance
	protected static TownManager instance;
	protected static Object lock = new Object();
	
	protected TownManager() {
		
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
}

package io.github.wispoffates.minecraft.tktowns.datastore;

import java.util.concurrent.TimeUnit;

public class GeneralConfig {
	protected int townCost = 1000;
	protected int townUpkeepInterval = 1;
	protected TimeUnit townUpkeepUnit = TimeUnit.DAYS;
	protected int townUpkeepCost = 10;
	
	protected String townMarker = "[Town]";
	protected String realestateMarker = "[Realestate]";

	public int getTownCost() {
		return townCost;
	}

	public void setTownCost(int townCost) {
		this.townCost = townCost;
	}

	public int getTownUpkeepInterval() {
		return townUpkeepInterval;
	}

	public void setTownUpkeepInterval(int townUpkeepInterval) {
		this.townUpkeepInterval = townUpkeepInterval;
	}

	public TimeUnit getTownUpkeepUnit() {
		return townUpkeepUnit;
	}

	public void setTownUpkeepUnit(TimeUnit townUpkeepUnit) {
		this.townUpkeepUnit = townUpkeepUnit;
	}

	public int getTownUpkeepCost() {
		return townUpkeepCost;
	}

	public void setTownUpkeepCost(int townUpkeepCost) {
		this.townUpkeepCost = townUpkeepCost;
	}

	public String getTownMarker() {
		return townMarker;
	}

	public void setTownMarker(String townMarker) {
		this.townMarker = townMarker;
	}

	public String getRealestateMarker() {
		return realestateMarker;
	}

	public void setRealestateMarker(String realestateMarker) {
		this.realestateMarker = realestateMarker;
	}
}

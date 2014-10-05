package io.github.wispoffates.minecraft.tktowns;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class RealEstate {
	/**
	 * Town this piece of real estate belongs too.
	 */
	protected Town parent;
	
	protected String name;
	/**
	 * Time unit to charge for the lease or null if this plot is for sale and not lease.
	 */
	protected TimeUnit leaseTimeUnit = null;
	protected double cost;
	/**
	 * Owner of the plot once bought and null while for sale.
	 */
	protected UUID owner = null;
	
	/**
	 * For Sale 
	 * @param parent
	 * @param name
	 * @param cost
	 */
	RealEstate(Town parent, String name, double cost) {
		
	}
	
	
	
}

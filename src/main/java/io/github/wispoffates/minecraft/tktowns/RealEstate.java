package io.github.wispoffates.minecraft.tktowns;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import me.ryanhamshire.GriefPrevention.Claim;

public class RealEstate {
	/**
	 * Town this piece of real estate belongs too.
	 */
	protected Town parent;

	protected String name;
	/**
	 * Time unit to charge for the lease (1 day min).
	 */
	protected TimeUnit leaseTimeUnit = null;
	protected int leaseTime = -1;
	protected boolean isForSaleOrLease = false;
	protected double cost;
	/**
	 * Owner of the plot. Null if the plot is server owned.
	 */
	protected UUID owner = null;
	
	protected Claim claim = null;
	protected Long gpClaimId = -1L;
	
	/**
	 *  Owned Constructor
	 */
	RealEstate(Claim claim, Town parent, String name) {
		this.claim = claim;
		this.parent = parent;
		this.name = name;
		this.owner = claim.ownerID;
		this.gpClaimId = claim.getID();
	}
	
	/**
	 * For Sale Constructor
	 * 
	 * @param parent
	 *            Parent Town.
	 * @param name
	 *            Name of this piece of real estate.
	 * @param cost
	 *            Cost of this piece of real estate.
	 */
	RealEstate(Claim claim, Town parent, String name, double cost) {
		this(claim, parent, name);
		this.sell(cost);
	}

	/**
	 * For Lease Constructor.
	 * 
	 * @param parent
	 *            Parent Town.
	 * @param name
	 *            Name of this piece of real estate.
	 * @param leaseTimeUnit
	 *            TimeUnit of the lease term. (Day,Week,Month,Year)
	 * @param leaseTime
	 *            Length of the lease term.
	 * @param cost
	 *            Cost of the lease.
	 */
	RealEstate(Claim claim, Town parent, String name, TimeUnit leaseTimeUnit, int leaseTime,
			double cost) {
		this(claim, parent, name);
		this.lease(leaseTimeUnit, leaseTime, cost);
	}
	
	public void sell(double cost) {
		this.cost = cost;
		this.isForSaleOrLease = true;
	}
	
	public void lease(TimeUnit leaseTimeUnit, int leaseTime, double cost) {
		if(leaseTimeUnit.compareTo(TimeUnit.DAYS) < 0) {
			//TODO: Fail intelligently...
		}
		this.leaseTimeUnit = leaseTimeUnit;
		this.leaseTime = leaseTime;
		this.cost = cost;
		this.isForSaleOrLease = true;
	}
	
	public void forclose() {
		this.isForSaleOrLease = true;
		if(this.parent != null) {
			this.setOwner(this.parent.getOwner());
		} else {
			this.owner = null;
		}
	}

	public void buy(UUID newOwner) {
		this.isForSaleOrLease = false;
		this.setOwner(newOwner);
		//TODO: figure out how to charge for times leases probably in the real estate handlers.
	}
	
	//Getters and setters ---------------------------------------------------------------------------
	
	public Town getParent() {
		return parent;
	}

	public void setParent(Town parent) {
		this.parent = parent;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public TimeUnit getLeaseTimeUnit() {
		return leaseTimeUnit;
	}

	public void setLeaseTimeUnit(TimeUnit leaseTimeUnit) {
		this.leaseTimeUnit = leaseTimeUnit;
	}

	public int getLeaseTime() {
		return leaseTime;
	}

	public void setLeaseTime(int leaseTime) {
		this.leaseTime = leaseTime;
	}

	public double getCost() {
		return cost;
	}

	public void setCost(double cost) {
		this.cost = cost;
	}

	public UUID getOwner() {
		return owner;
	}

	public void setOwner(UUID owner) {
		this.owner = owner;
		this.claim.ownerID = owner; //change gp claim owner;
	}

}

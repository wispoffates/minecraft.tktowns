package io.github.wispoffates.minecraft.tktowns;

import io.github.wispoffates.minecraft.tktowns.exceptions.TKTownsException;

import java.util.UUID;

import net.milkbowl.vault.economy.EconomyResponse;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import me.ryanhamshire.GriefPrevention.Claim;

public class RealEstate {
	
	public enum Status {
		OWNED, RENTED, LEASED, FORSALE, FORRENT, FORLEASE, INDEFAULT
	}
	/**
	 * Town this piece of real estate belongs too.
	 */
	protected Town parent;

	protected String name;
	/**
	 * Lease time is specified in number of days
	 */
	protected int leaseTime = -1;			//how long does the lease last
	protected int remainingDays = -1;		//how many days remain
	protected double recurringCost;			//Recurring cost 
	protected double downPayment;			//initial cost of the transaction
	protected Status status = Status.OWNED;
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
	RealEstate(Claim claim, Town parent, String name,  int leaseTime, double downpayment, double recurringCost) {
		this(claim, parent, name);
		this.lease(leaseTime, downpayment, recurringCost);
		this.status = Status.FORLEASE;
	}
	
	public void sell(double cost) {
		this.downPayment = cost;
		this.status = Status.FORSALE;
	}
	
	public void rent(double downpayment, double recurringCost) {
		this.recurringCost = recurringCost;
		this.downPayment = downpayment;
		this.status = Status.FORRENT;
		//rent period set for 24 hours
	}
	
	/**
	 * 
	 * @param leaseTime
	 * @param downpayment
	 * @param recurringCost
	 */
	public void lease(int leaseTime, double downpayment, double recurringCost) {
		this.leaseTime = leaseTime;
		this.recurringCost = recurringCost;
		this.downPayment = downpayment;
		this.status = Status.FORLEASE;
	}
	
	public void forclose() {
		this.status = Status.INDEFAULT;
		if(this.parent != null) {
			this.setOwner(this.parent.getOwner());
		} else {
			this.owner = null;
			//TODO: set as admin claim or is owner null enough?
		}
	}

	public void buy(Player newOwner) throws TKTownsException {
		EconomyResponse er = TKTowns.econ.withdrawPlayer(newOwner, this.downPayment);
		if(!er.transactionSuccess()) {
			throw new TKTownsException(er.errorMessage);
		}
		this.setOwner(newOwner.getUniqueId());
		this.remainingDays = this.leaseTime;
		switch(this.status) {
			case FORRENT: {
				this.setStatus(Status.RENTED);
				break;
			}
			case FORLEASE: {
				this.setStatus(Status.LEASED);
				break;
			}
			case FORSALE: {
				this.setStatus(Status.OWNED);
				break;
			}
			default:  //should come to this
				break;
		}
		
	}
	
	public void collect() {
		switch(this.status) {
			case RENTED: {
				Player player  = Bukkit.getPlayer(this.owner);
				EconomyResponse er = TKTowns.econ.withdrawPlayer(player, this.downPayment);
				if(!er.transactionSuccess()) {
					this.forclose();
				}
				break;
			}
			case LEASED: { //cheat here to decrement lease day
				this.remainingDays--;
				if(this.remainingDays<0) { //foreclose
					this.forclose();
				}
				break;
			}
			default:  //only rent is collected on the 24 hour boundary
				break;
		}
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

	public int getLeaseTime() {
		return leaseTime;
	}

	public void setLeaseTime(int leaseTime) {
		this.leaseTime = leaseTime;
	}

	public double getRecurringCost() {
		return this.recurringCost;
	}

	public void setRecurringCost(double cost) {
		this.recurringCost = cost;
	}

	public double getDownPayment() {
		return this.downPayment;
	}
	
	public void setDownPayment(double downpayment) {
		this.downPayment = downpayment;
	}
	
	public UUID getOwner() {
		return owner;
	}

	public void setOwner(UUID owner) {
		this.owner = owner;
		this.claim.ownerID = owner; //change gp claim owner;
	}
	
	public void setStatus(Status nStatus) {
		this.status = nStatus;
	}
	
	public Status getStatus() {
		return this.status;
	}

}

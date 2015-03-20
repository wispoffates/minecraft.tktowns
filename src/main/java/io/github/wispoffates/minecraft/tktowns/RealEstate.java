package io.github.wispoffates.minecraft.tktowns;

import io.github.wispoffates.minecraft.tktowns.exceptions.TKTownsException;

import java.util.UUID;

import net.milkbowl.vault.economy.EconomyResponse;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.google.common.base.Optional;

import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;

public class RealEstate {
	
	public enum Status {
		OWNED, RENTED, LEASED, FORSALE, FORRENT, FORLEASE, INDEFAULT
	}
	/**
	 * Town this piece of real estate belongs too.
	 */
	transient protected Optional<Town> parent = Optional.absent();
	protected UUID parentId;
	
	/**
	 * Owner of the plot. Null if the plot is server owned.
	 */
	transient protected Optional<Player> owner = Optional.absent();
	protected UUID ownerId = null;
	
	transient protected Optional<Claim> claim = Optional.absent();
	protected Long gpClaimId = -1L;
	

	protected String name;
	protected UUID id;
	/**
	 * Lease time is specified in number of days
	 */
	protected int leaseTime = -1;			//how long does the lease last
	protected int remainingDays = -1;		//how many days remain
	protected double recurringCost;			//Recurring cost 
	protected double downPayment;			//initial cost of the transaction
	protected Status status = Status.OWNED;
	
	
	/** Empty constructor for GSON*/
	RealEstate() {
		
	}

	/**
	 *  Owned Constructor
	 */
	RealEstate(Claim claim, Town parent, String name) {
		this.parent = Optional.of(parent);
		this.parentId = parent.getId();
		this.name = name;
		this.owner = Optional.of(Bukkit.getPlayer(parentId));
		this.ownerId = claim.ownerID;
		this.gpClaimId = claim.getID();
		this.id = UUID.randomUUID();
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
			this.setOwner(this.getParent().getOwner());
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
		this.setOwner(newOwner);
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
				EconomyResponse er = TKTowns.econ.withdrawPlayer(this.getOwner(), this.downPayment);
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
		if(!parent.isPresent())
			parent = Optional.of(TownManager.instance.getTownById(parentId));
		
		return parent.get();
	}

	public void setParent(Town parent) {
		this.parent = Optional.of(parent);
		this.parentId = parent.getId();
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
	
	public Player getOwner() {
		if(!owner.isPresent())
			owner = Optional.of(Bukkit.getPlayer(ownerId));
			
		return owner.get();
	}

	public void setOwner(Player owner) {
		this.owner = Optional.of(owner);
		this.ownerId = owner.getUniqueId();
		this.getClaim().ownerID = owner.getUniqueId(); //change gp claim owner;
	}
	
	public void setStatus(Status nStatus) {
		this.status = nStatus;
	}
	
	public Status getStatus() {
		return this.status;
	}

	public UUID getParentId() {
		return parentId;
	}

	public void setParentnId(UUID parentId) {
		this.parentId = parentId;
	}

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public int getRemainingDays() {
		return remainingDays;
	}

	public void setRemainingDays(int remainingDays) {
		this.remainingDays = remainingDays;
	}

	public UUID getOwnerId() {
		return ownerId;
	}

	public void setOwnerId(UUID ownerId) {
		this.ownerId = ownerId;
	}

	public Long getGpClaimId() {
		return gpClaimId;
	}

	public void setGpClaimId(Long gpClaimId) {
		this.gpClaimId = gpClaimId;
	}

	public Claim getClaim() {
		if(!claim.isPresent())
			claim = Optional.of(GriefPrevention.instance.dataStore.getClaim(gpClaimId));
		
		return claim.get();
	}

	public void setClaim(Claim claim) {
		this.claim = Optional.of(claim);
		this.gpClaimId = claim.getID();
	}

	public void setParentId(UUID parentId) {
		this.parentId = parentId;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		if(this.getParent() != null) {
			sb.append("Real Estate: " + this.name + " in " + getParent().getName() + " ");
		} else {
			sb.append("Town:" + this.name + " ");
		}
		switch(this.status) {
			case FORSALE: {
				sb.append("is for sale! $" + this.downPayment);
				break;
			}
			case FORRENT: {
				sb.append("is for rent! Downpayment:" + this.downPayment + " and " + this.recurringCost + " per day.");
				break;
			}
			case FORLEASE: {
				sb.append("is available to lease! Downpayment:" + this.downPayment + " Period:" + this.leaseTime + " days");
				break;
			}
			case OWNED: {
				sb.append("owned by " + this.getOwner().getName());
				break;
			}
			case RENTED: { 
				sb.append("rented by " + this.getOwner().getName());
				break;			
			}
			case LEASED: {
				sb.append("leased by " + this.getOwner().getName());
				break;
			}
			case INDEFAULT: {
				sb.append("is in default!");
				break;
			}
		}
		return sb.toString();
	}
	
}

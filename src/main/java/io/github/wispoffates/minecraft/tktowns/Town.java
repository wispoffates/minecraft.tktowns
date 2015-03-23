package io.github.wispoffates.minecraft.tktowns;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.google.common.base.Optional;

import net.milkbowl.vault.economy.EconomyResponse;
import me.ryanhamshire.GriefPrevention.Claim;

//TODO Wrap Vault economy so we can throw exceptions here and keep log message and stuff in the Manager
public class Town  extends RealEstate {
	transient private static final Logger log = Logger.getLogger("Minecraft");
	
	transient protected static final String BANK_PREFIX="tktown_5482154"; //extra digits to make the chance of collision with a player name very small

	protected Map<String, RealEstate> children;
	protected Map<String, Outpost> outposts;
	
	transient protected Optional<Set<Player>> residents = Optional.absent();
	protected Set<UUID> residentsIds; //no accessors on purpose we want one coherent API this exists just for gson

	/**
	 * Empty constructor for gson
	 */
	public Town() {
		super();
		this.children = new HashMap<String,RealEstate>();
		this.outposts = new HashMap<String,Outpost>();
		this.residentsIds = new HashSet<UUID>();
		//leave residents empty on purpose... we will initiate in when it is pulled
	}
	
	/**
	 * New Town constructor
	 * @param claim GPClaim that this town.
	 * @param name Name of the town.
	 */
	public Town(Claim claim, String name) {
		super(claim,null,name);
		this.children = new HashMap<String,RealEstate>();
		this.outposts = new HashMap<String,Outpost>();
		this.residentsIds = new HashSet<UUID>();
	}
	
	/**
	 * Withdraw money from the town bank.
	 * @param amount The amount of money to withdraw.
	 * @param recipent The person to receive the money or null if it is the server.
	 * @return Boolean - whether the transaction was successful.
	 */
	public boolean withdraw(double amount, Player recipent) {
		EconomyResponse resp = TKTowns.econ.bankWithdraw(BANK_PREFIX+this.name, amount);
		if(resp.transactionSuccess()) {
			if(recipent == null) {
				return true; //money for the server == money destroyed
			} 
			resp = TKTowns.econ.bankDeposit(recipent.getName(), amount);
			if(resp.transactionSuccess()) {
				return true; //job done
			} else {
				//try to return the money to town bank
				recipent.sendRawMessage("Failed to deposit " + amount + " in your bank! :: " + resp.errorMessage);
				log.severe("Transfering money to player " + recipent.getName() + " from town " + this.name + " failed! :: " + resp.errorMessage);
				TKTowns.econ.bankDeposit(BANK_PREFIX+this.name, amount);
				return false;
			}
		} else {
			recipent.sendRawMessage("Failed to withfraw " + amount + " " + this.name + " bank! :: " + resp.errorMessage);
			log.severe("Withdraw money from town " + this.name + "failed! :: " + resp.errorMessage);
			return false;
		}
	}
	
	/**
	 * Deposit money into the town bank.
	 * @param donor Player depositing money.
	 * @param amount Amount to deposit
	 * @return True on transaction success
	 */
	public boolean deposit(Player donor, double amount) {
		EconomyResponse resp = TKTowns.econ.bankWithdraw(donor.getName(), amount);
		if(resp.transactionSuccess()) {
			resp = TKTowns.econ.bankDeposit(BANK_PREFIX+this.name, amount);
			if(resp.transactionSuccess()) {
				return true; //job done
			} else {
				//try to return the money to the player
				donor.sendRawMessage("Failed to donate " + amount + " " + this.name + " bank! :: " + resp.errorMessage);
				log.severe("Transfering money to player " + donor.getName() + " from town " + this.name + " failed! :: " + resp.errorMessage);
				TKTowns.econ.bankDeposit(donor.getName(), amount);
				return false;
			}
		} else {
			donor.sendRawMessage("Failed to withdraw " + amount + " " + " from your bank! :: " + resp.errorMessage);
			log.severe("Withdraw money from player " + donor.getName() + "failed! :: " + resp.errorMessage);
			return false;
		}
	}
	
	public double getBankBalance(Player requester) {
		EconomyResponse resp = TKTowns.econ.bankBalance(BANK_PREFIX + this.getName());
		if(resp.transactionSuccess()) {
			return resp.balance;
		}
		requester.sendRawMessage("Failed to check blance of " + this.getName() + " :: " + resp.errorMessage);
		log.severe("Checking balance of " + this.getName() + "failed! :: " + resp.errorMessage);
		return -1;
	}

	protected Map<String, RealEstate> getChildren() {
		return children;
	}
	
	public void addChild(RealEstate re) {
		this.children.put(re.getName(), re);
	}
	
	public RealEstate getChild(String name) {
		return this.children.get(name);
	}
	
	public void removeChild(String name) {
		this.children.remove(name);
	}
	
	protected void setChildren(Map<String, RealEstate> children) {
		this.children = children;
	}
	
	public void addResident(Player player) {
		if(this.residents.isPresent()) {
			this.residents.get().add(player);
		}
		this.residentsIds.add(player.getUniqueId());	
	}
	
	public boolean isResident(Player player) {
		return this.residentsIds.contains(player.getUniqueId());
	}

	public void removeResident(Player player) {
		if(this.residents.isPresent()) {
			this.residents.get().remove(player);
		}
		this.residentsIds.remove(player.getUniqueId());
	}
	
	public int countResidents() {
		return this.residentsIds.size();
	}
	
	public Set<Player> getResidents() {
		if(!this.residents.isPresent()) { //residents haven't  been initialized lets do that
			Set<Player> resList = new HashSet<Player>();
			for(UUID id : this.residentsIds) {
				resList.add(Bukkit.getPlayer(id));
			}
			this.residents = Optional.of(resList);
		}
		return this.residents.get();
	}
	
	public boolean isMayor(Player player) {
		return this.owner.equals(player);
	}
	
	//Handle mayor bit (really just owner conviently renamed)
	public void setMayor(Player player) {
		this.setOwner(player);
	}
	
	public Player getMayor() {
		return this.getOwner();
	}
	
	public void addOutpost(Outpost out) {
		this.outposts.put(out.getName(), out);
	}
	
	public void removeOutpost(String name) {
		this.outposts.remove(name);
	}
	
	public Outpost getOutpost(String name) {
		return this.outposts.get(name);
	}
}

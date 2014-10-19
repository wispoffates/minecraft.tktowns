package io.github.wispoffates.minecraft.tktowns;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;

import org.bukkit.entity.Player;

import net.milkbowl.vault.economy.EconomyResponse;
import me.ryanhamshire.GriefPrevention.Claim;

//TODO Wrap Vault economy so we can throw exceptions here and keep log message and stuff in the Manager
public class Town  extends RealEstate {
	private static final Logger log = Logger.getLogger("Minecraft");
	
	protected static final String BANK_PREFIX="tktown_5482154"; //extra digits to make the chance of collision with a player name very small

	protected Map<String, RealEstate> children;
	protected Map<String, Outpost> outposts;
	protected Set<UUID> residents;

	/**
	 * New Town constructor
	 * @param claim GPClaim that this town.
	 * @param name Name of the town.
	 */
	public Town(Claim claim, String name) {
		super(claim,null,name);
		this.children = new HashMap<String,RealEstate>();
		this.outposts = new HashMap<String,Outpost>();
		this.residents = new HashSet<UUID>();
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
		this.residents.add(player.getUniqueId());	
	}
	
	public boolean isResident(Player player) {
		return this.residents.contains(player.getUniqueId());
	}

	public void removeResident(Player player) {
		this.residents.remove(player.getUniqueId());
	}
}

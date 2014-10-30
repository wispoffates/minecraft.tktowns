package io.github.wispoffates.minecraft.tktowns;

import io.github.wispoffates.minecraft.tktowns.exceptions.TKTownsException;
import io.github.wispoffates.minecraft.tktowns.exceptions.TownNotFoundException;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class TownManager {
	
	protected final static String TKTOWNS_HEADER = "--- TKTowns ---";
	
	
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
	 * List all the towns.
	 * @param player
	 */
	public Set<String> listTowns(Player player) {
		return this.towns.keySet();
	}

	public void createTown(Player player, String name) throws TKTownsException {
		Claim claim = GriefPrevention.instance.dataStore.getClaimAt(player.getLocation(), true, null);
		if(claim == null) {
			throw new TKTownsException("You are not standing in a GriefPrevention claim.");
		}
		if(this.towns.containsKey(name)) {
			throw new TKTownsException("A town with that name all ready exists.");
		}
		if(!claim.ownerID.equals(player.getUniqueId())) {
			throw new TKTownsException("You do not own this claim.");
		}
		
		Town town = new Town(claim,name);
		town.setMayor(player.getUniqueId());
		town.addResident(player);
		towns.put(name, town);
	}

	public void deleteTown(Player player, String name) throws TKTownsException {
		Town town = null;
		if(name != null) {
			town = this.towns.get(town);
		} else {
			town = this.getTownAtPlayerLocation(player);
		}
		if(town == null) {
			throw new TownNotFoundException("No town with that name exists.");
		}
		if(!town.isMayor(player)) {
			throw new TKTownsException("Only the mayor of a town can delete it.");
		}
	}

	public void depositTown(Player player, String name, String amount) throws TownNotFoundException, IllegalArgumentException {
		if(amount == null) {
			throw new IllegalArgumentException("You did not specify an amount to deposit.");
		}
		
		Town town = null;
		if(name == null) {
			town = this.getTownMayorOf(player);
		} else {
			town = this.towns.get(name);
		}
		if(town == null) {
			throw new TownNotFoundException("No town with that name exists.");
		}
		try {
			town.deposit(player, Double.parseDouble(amount));
		} catch(NumberFormatException e) {
			throw new IllegalArgumentException("The amount must be a double ex: 20.5");
		}
	}

	public Town listTownInfo(Player player, String name) throws TownNotFoundException {
		Town town = null;
		if(name == null) {
			town = this.getTownAtPlayerLocation(player);
		} else {
			town = this.towns.get(name);
		}
		if(town == null) {
			throw new TownNotFoundException("No town with that name exists.");
		}
		return town;
	}

	public void withdrawTown(Player player, String name, String amount) throws IllegalArgumentException, TKTownsException {
		if(amount == null) {
			throw new IllegalArgumentException("You did not specify an amount to deposit.");
		}
		
		Town town = null;
		if(name == null) {
			town = this.getTownMayorOf(player);
		} else {
			town = this.towns.get(name);
		}
		if(town == null) {
			throw new TownNotFoundException("No town with that name exists.");
		}
		if(!town.isMayor(player)) {
			throw new TKTownsException("Only the mayor of a town can withdrawl from the bank.");
		}
		try {
			town.withdraw(Double.parseDouble(amount), player);
		} catch(NumberFormatException e) {
			throw new IllegalArgumentException("The amount must be a double ex: 20.5");
		}
		
	}

	public Set<RealEstate> listRealestate(Player player, String name) throws TownNotFoundException {
		if(name != null) {
			Town town = this.towns.get(name);
			if(town == null) {
				throw new TownNotFoundException("No town with that name exists.");
			}
			return this.getForSale(town);
		}
		Set<RealEstate> ret = new HashSet<RealEstate>();
		for(Town town : this.towns.values()) {
			ret.addAll(this.getForSale(town));
		}
		return ret;
	}

	public void createRealestate(Player player, String name) throws TKTownsException {
		// TODO Check to make sure they want to put their Town up for sale (protection against standing in the wrong claim)
		// TODO Make sure it is not all ready a RealEstate.
		Claim claim = GriefPrevention.instance.dataStore.getClaimAt(player.getLocation(), true, null);
		Town town = this.getTownMayorOf(player);
		if(claim == null) {
			throw new TKTownsException("You are not standing in a GriefPrevention claim.");
		}
		if(town == null) {
			throw new TKTownsException("This claim is not part of a town.");
		}
		if(town.getChildren().containsKey(name)) {
			throw new TKTownsException("A claim with that name all ready exists.");
		}
		if(!claim.ownerID.equals(player.getUniqueId())) {
			throw new TKTownsException("You do not own this claim.");
		}
		
		RealEstate re = new RealEstate(claim, town, name);
		town.addChild(re);
	}

	public void sellRealestate(Player player, String amount) throws TKTownsException, IllegalArgumentException {
		if(amount == null) {
			throw new IllegalArgumentException("Amount must be specified.");
		}
		RealEstate re = this.getRealEstateAtPlayerLocation(player);
		if(re == null) {
			throw new TKTownsException("Your are not standing in a piece of RealEstate.");
		}
		if(!re.getOwner().equals(player.getUniqueId())) {
			throw new TKTownsException("You do not own this RealEstate.");
		}
		try {
			re.sell(Double.parseDouble(amount));
		} catch(NumberFormatException e) {
			throw new IllegalArgumentException("The amount must be a double ex: 20.5");
		}
	}

	public void leaseRealestate(Player player, String amount, String downpayment, String period) throws TKTownsException, IllegalArgumentException {
		if(amount == null || period == null || downpayment == null) {
			throw new IllegalArgumentException("Amount must be specified.");
		}
		RealEstate re = this.getRealEstateAtPlayerLocation(player);
		if(re == null) {
			throw new TKTownsException("Your are not standing in a piece of RealEstate.");
		}
		if(!re.getOwner().equals(player.getUniqueId())) {
			throw new TKTownsException("You do not own this RealEstate.");
		}
		try {
			//TODO: don't hardcode this?
			re.lease(Integer.parseInt(period), Double.parseDouble(amount));
		} catch(NumberFormatException e) {
			throw new IllegalArgumentException("The amount must be a double ex: 20.5");
		}
		//TODO: Do that scheduling thing
	}

	public void buyRealestate(Player player) throws TKTownsException {
		RealEstate re = this.getRealEstateAtPlayerLocation(player);
		if(re == null) {
			throw new TKTownsException("Your are not standing in a piece of RealEstate.");
		}
		re.buy(player);
	}

	public Set<Outpost> listOutposts(Player player) {
		Set<Outpost> outposts = new HashSet<Outpost>();
		Set<Town> towns = this.getTownsAPlayerIsResident(player);
		for(Town town : towns) {
			outposts.addAll(town.outposts.values());
		}
		return outposts;
	}

	public void createOutpost(Player player, String name) throws TKTownsException {
		Town town = this.getTownMayorOf(player);
		if(town == null || !town.isMayor(player)) {
			throw new TKTownsException("Only the mayor of a town can create outposts.");
		}
		Claim claim = GriefPrevention.instance.dataStore.getClaimAt(player.getLocation(), true, null);
		if(claim == null) {
			throw new TKTownsException("You are not standing in a GriefPrevention claim.");
		}
		if(town.outposts.containsKey(name)) {
			throw new TKTownsException("An outpost with that name all ready exists.");
		}
		Outpost out = new Outpost(claim, town, name);
		town.addOutpost(out);
	}

	public void deleteOutpost(Player player, String name) {
		// TODO Auto-generated method stub
		
	}

	public Set<Player> listResidents(Player player, String name) throws TownNotFoundException {
		Town town = null;
		if(name == null) {
			town = this.getTownMayorOf(player);
		} else {
			town = this.towns.get(name);
		}
		if(town == null) {
			throw new TownNotFoundException("No town with that name exists.");
		}
		
		return town.residents;
	}

	public void addResident(Player player, String name) throws TKTownsException {
		Town town = this.getTownMayorOf(player);
		Player resident = Bukkit.getPlayer(name);
		if(town == null || !town.isMayor(player)) {
			throw new TKTownsException("Only the mayor of a town can add residents.");
		}
		if(resident == null) {
			throw new TKTownsException("Player by that name does not exist.");
		}
		town.addResident(resident);
	}

	public void deleteResident(Player player, String name) throws TKTownsException {
		Town town = this.getTownMayorOf(player);
		Player resident = Bukkit.getPlayer(name);
		if(town == null || !town.isMayor(player)) {
			throw new TKTownsException("Only the mayor of a town can remove residents.");
		}
		if(resident == null) {
			throw new TKTownsException("Player by that name does not exist.");
		}
		town.removeResident(resident);	
	}
	
	protected Town getTownMayorOf(Player player) {
		Town ret = null;
		for(Town town: this.towns.values()) {
			if(town.isMayor(player)) {
				ret = town;
			}
		}
		return ret;
	}
	
	protected Town getTownAtPlayerLocation(Player player) {
		Town ret = null;
		Claim claim = GriefPrevention.instance.dataStore.getClaimAt(player.getLocation(), true, null);
		for(Town town : this.towns.values()) {
			if(town.claim.getID() == claim.getID() || town.claim.getID() == claim.parent.getID()) {
				ret = town;
			}
		}
		return ret;
	}
	
	protected RealEstate getRealEstateAtPlayerLocation(Player player) {
		//n^2
		RealEstate ret = null;
		Claim claim = GriefPrevention.instance.dataStore.getClaimAt(player.getLocation(), true, null);
		for(Town town : this.towns.values()) {
			for(RealEstate re : town.getChildren().values()) {
				if(re.claim.getID() == claim.getID() || re.claim.getID() == claim.parent.getID()) {
					ret = re;
				}
			}
		}
		return ret;
	}
	
	protected Set<Town> getTownsAPlayerIsResident( Player player ) {
		Set<Town> towns = new HashSet<Town>();
		for(Town town: this.towns.values()) {
			if(town.isResident(player)) {
				towns.add(town);
			}
		}
		return towns;
	}
	
	protected Set<RealEstate> getForSale(Town town) {
		Set<RealEstate> ret = new HashSet<RealEstate>();
		for(RealEstate re : town.getChildren().values()) {
			if(re.getStatus() == RealEstate.Status.FORLEASE || re.getStatus() == RealEstate.Status.FORRENT || re.getStatus() == RealEstate.Status.FORSALE)
				ret.add(re);
		}
		return ret;
	}

	public double getBalance(Player player, String string, String string2) {
		// TODO Auto-generated method stub
		return 0.0;
	}

	public void rentRealestate(Player player, String string, String string2,
			String string3) {
		// TODO Auto-generated method stub
		
	}
}

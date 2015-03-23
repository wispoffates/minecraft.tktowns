package io.github.wispoffates.minecraft.tktowns;

import io.github.wispoffates.minecraft.tktowns.datastore.DataStore;
import io.github.wispoffates.minecraft.tktowns.datastore.YamlStore;
import io.github.wispoffates.minecraft.tktowns.exceptions.TKTownsException;
import io.github.wispoffates.minecraft.tktowns.exceptions.TownNotFoundException;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class TownManager {
	
	protected final static String TKTOWNS_HEADER = "--- TKTowns ---";
	
	//singleton instance
	protected static TownManager instance;
	protected static Object lock = new Object();
	
	//variables and stuff
	protected Map<String,Town> towns;
	protected Map<UUID,Town> townsById; //secondary index to get towns by id
	protected DataStore config;
	
	protected TownManager(File fileConfiguration) {
		towns = new HashMap<String,Town>();
		townsById = new HashMap<UUID, Town>();
		instance = this;
		//load general configuration
		this.config = new YamlStore(fileConfiguration);
		//load towns
		this.towns = this.config.loadTowns();
		//populate ID index
		for(Town t : this.towns.values()) {
			this.townsById.put(t.getId(), t);
		}
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
		town.setMayor(player);
		town.addResident(player);
		towns.put(name, town);
		townsById.put(town.getId(), town);
		//save the town
		this.config.saveTown(town);
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
		//delete town
		this.towns.remove(name);
		this.townsById.remove(town.getId());
		this.config.deleteTown(town);
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
		// TODO Check to make sure they want to put their Town up for sale (protection against standing in the wrong claim
		if(this.getRealEstateAtPlayerLocation(player) != null) {
			throw new TKTownsException("This is all ready a piece of Real Estate.");
		}
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
		this.config.saveTown(town); //save the town now that the realestate is created.
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
			this.config.saveTown(re.getParent()); //save town so we keep the realestate saved.
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
			re.lease(Integer.parseInt(period), Double.parseDouble(downpayment), Double.parseDouble(amount));
			this.config.saveTown(re.getParent()); //save town so we keep the realestate saved.
		} catch(NumberFormatException e) {
			throw new IllegalArgumentException("The amount must be a double ex: 20.5");
		}
		//TODO: Do that scheduling thing
	}
	
	public void rentRealestate(Player player, String downpayment, String reaccuring) throws TKTownsException, IllegalArgumentException {
		if(reaccuring == null ||  downpayment == null) {
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
			re.rent(Double.parseDouble(downpayment), Double.parseDouble(reaccuring));
			this.config.saveTown(re.getParent()); //save town so we keep the realestate saved.
		} catch(NumberFormatException e) {
			throw new IllegalArgumentException("The amount must be a double ex: 20.5");
		}
		
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
		this.config.saveTown(town); //save town so we keep the outpost saved.
	}

	public void deleteOutpost(Player player, String name) throws TKTownsException {
		RealEstate re = this.getRealEstateAtPlayerLocation(player);
		if(re instanceof Outpost) {
			Outpost out = (Outpost) re;
			if(!out.getParent().isMayor(player)) {
				throw new TKTownsException("Only the mayor can delete an outpost.");
			}
			out.getParent().removeOutpost(out.getName());
			this.config.saveTown(re.getParent()); //save town so we keep the realestate saved.
		} else {
			throw new TKTownsException("You are not standing in an outpost.");
		}
		
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
		this.config.saveTown(town); //save town with the new residents.
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
		this.config.saveTown(town); //save town with the new residents.
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
			if(town.getClaim().getID() == claim.getID() || town.getClaim().getID() == claim.parent.getID()) {
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
				if(re.getClaim().getID() == claim.getID() || re.getClaim().getID() == claim.getID()) {
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
	
	public double getBalance(Player player) throws TKTownsException {
		Town town = this.getTownMayorOf(player);
		if(town == null) {
			throw new TKTownsException("Only the mayor of a town can check the bank balance.");
		}
		return town.getBankBalance(player);
	}
	
	public Town getTownById(UUID id) {
		return townsById.get(id);
	}

	
}

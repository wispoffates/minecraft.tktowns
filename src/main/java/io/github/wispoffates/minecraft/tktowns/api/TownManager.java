package io.github.wispoffates.minecraft.tktowns.api;

import io.github.wispoffates.minecraft.tktowns.TKTowns;
import io.github.wispoffates.minecraft.tktowns.api.impl.Outpost;
import io.github.wispoffates.minecraft.tktowns.api.impl.RealEstate;
import io.github.wispoffates.minecraft.tktowns.api.impl.RealEstate.SignLocation;
import io.github.wispoffates.minecraft.tktowns.api.impl.Town;
import io.github.wispoffates.minecraft.tktowns.datastore.DataStore;
import io.github.wispoffates.minecraft.tktowns.datastore.YamlStore;
import io.github.wispoffates.minecraft.tktowns.exceptions.TKTownsException;
import io.github.wispoffates.minecraft.tktowns.exceptions.TownNotFoundException;
import io.github.wispoffates.minecraft.tktowns.responses.GenericModificationResponse;
import io.github.wispoffates.minecraft.tktowns.responses.OutpostModificationResponse;
import io.github.wispoffates.minecraft.tktowns.responses.RealestateModificationResponse;
import io.github.wispoffates.minecraft.tktowns.responses.TownModificationResponse;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.metadata.FixedMetadataValue;

import com.google.common.base.Optional;

public class TownManager {
	public final static String TKTOWNS_METADATA_TAG 	= "TKTowns";
	public static String TKTOWNS_TOWN_SIGN_HEADER       = "[Town]"; //not final so I can make it configurable
	public static String TKTOWNS_REALESTATE_SIGN_HEADER = "[Realestate]";
	
	//singleton instance
	protected static TownManager instance;
	protected static Object lock = new Object();
	
	//variables and stuff
	protected Map<String,Town> towns;
	protected Map<UUID,Town> townsById; //secondary index to get towns by id
	protected DataStore config;
	
	public TownManager(File fileConfiguration) {
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
	
	public static TownManager get() {
		return TownManager.instance;
	}
	
	/**
	 * List all the towns.
	 * @param player
	 */
	public Set<String> listTowns(Player player) {
		return this.towns.keySet();
	}

	public TownModificationResponse createTown(Player player, Location signLoc, String name) throws TKTownsException {
		Claim claim = GriefPrevention.instance.dataStore.getClaimAt(signLoc, true, null);
		if(claim == null) {
			throw new TKTownsException("Your sign is not in a GriefPrevention claim.");
		}
		if(this.towns.containsKey(name)) {
			throw new TKTownsException("A town with that name all ready exists.");
		}
		if(claim.parent != null || claim.ownerID == null) {
			throw new TKTownsException("Towns can only be created from top level claims.");
		}
		if(!claim.ownerID.equals(player.getUniqueId())) {
			throw new TKTownsException("You do not own this claim.");
		}
		
		Town town = new Town(claim,SignLocation.fromLocation(signLoc),name);
		town.setMayor(player);
		town.addResident(player);
		towns.put(name, town);
		townsById.put(town.getId(), town);
		//save the town
		this.config.saveTown(town);
		return new TownModificationResponse(town.getName() + " established. Welcome Mayor.",town,true);
	}

	public TownModificationResponse deleteTown(Player player, String name) throws TKTownsException {
		Town town = null;
		if(name != null) {
			town = this.towns.get(town);
		} else {
			town = this.getTownAtLocation(player.getLocation()).get();
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
		return new TownModificationResponse(town.getName() + " deleted.",town,true);
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
			town = this.getTownAtLocation(player.getLocation()).get();
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

	public RealestateModificationResponse createRealestate(Player player, Location loc, String name) throws TKTownsException {
		if(this.getRealEstateAtLocation(loc).isPresent()) {
			throw new TKTownsException("This is all ready a piece of Real Estate.");
		}
		Claim claim = GriefPrevention.instance.dataStore.getClaimAt(player.getLocation(), true, null);
		Town town = this.getTownMayorOf(player);
		if(claim == null) {
			throw new TKTownsException("You are not standing in a GriefPrevention claim.");
		}
		if(town == null) {
			throw new TKTownsException("You are not the mayor of a town.");
		}
		if(claim.parent == null) {
			throw new TKTownsException("This claim is not part of a town.");
		}
		if(claim.parent.getID().equals(town.getId())) {
			throw new TKTownsException("You are not the mayor of this claim's town.");
		}
		if(town.getChildren().containsKey(name)) {
			throw new TKTownsException("A claim with that name all ready exists.");
		}
		/*
		if(!claim.ownerID.equals(player.getUniqueId())) {  //TODO: Do we care we have all ready checked that they are town mayor...
			throw new TKTownsException("You do not own this claim.");
		}*/
		
		RealEstate re = new RealEstate(claim, SignLocation.fromLocation(loc), town, name);
		town.addChild(re);
		this.config.saveTown(town); //save the town now that the realestate is created.
		return new RealestateModificationResponse(re.getName() + " ceated.",re,true);
	}

	public RealestateModificationResponse sellRealestate(Player player, Location signLoc, String amount) throws TKTownsException, IllegalArgumentException {
		// TODO Check to make sure they want to put their Town up for sale (protection against standing in the wrong claim
		if(amount == null) {
			throw new IllegalArgumentException("Amount must be specified.");
		}
		Optional<RealEstate> re = this.getRealEstateAtLocation(signLoc);
		if(!re.isPresent()) {
			throw new TKTownsException("Your are not standing in a piece of RealEstate.");
		}
		if(!re.get().getOwner().getUniqueId().equals(player.getUniqueId())) {
			throw new TKTownsException("You do not own this RealEstate.");
		}
		if(re.get().getStatus() != RealEstate.Status.OWNED) {
			throw new TKTownsException("RealEstate is all ready for sale, lease or rent.");
		}
		try {
			re.get().sell(SignLocation.fromLocation(signLoc), Double.parseDouble(amount));
			this.config.saveTown(re.get().getParent()); //save town so we keep the realestate saved.
		} catch(NumberFormatException e) {
			throw new IllegalArgumentException("The amount must be a double ex: 20.5");
		}
		return new RealestateModificationResponse(re.get().getName() + " put up for sell.",re.get(),true);
	}

	public RealestateModificationResponse leaseRealestate(Player player, Location signLoc, String amount, String downpayment, String period) throws TKTownsException, IllegalArgumentException {
		if(amount == null || period == null || downpayment == null) {
			throw new IllegalArgumentException("Amount must be specified.");
		}
		Optional<RealEstate> reOp = this.getRealEstateAtLocation(signLoc);
		if(!reOp.isPresent()) {
			throw new TKTownsException("Your are not standing in a piece of RealEstate.");
		}
		RealEstate re = reOp.get();
		if(re.getParent() == null) {
			throw new TKTownsException("You cannot sell your town!");
		}
		if(!re.getOwner().getUniqueId().equals(player.getUniqueId())) {
			throw new TKTownsException("You do not own this RealEstate.");
		}
		if(re.getStatus() != RealEstate.Status.OWNED) {
			throw new TKTownsException("RealEstate is all ready for sale, lease or rent.");
		}
		try {
			re.lease(SignLocation.fromLocation(signLoc),Integer.parseInt(period), Double.parseDouble(downpayment), Double.parseDouble(amount));
			this.config.saveTown(re.getParent()); //save town so we keep the realestate saved.
		} catch(NumberFormatException e) {
			throw new IllegalArgumentException("The amount must be a double ex: 20.5");
		}
		//TODO: Do that scheduling thing
		return new RealestateModificationResponse(re.getName() + " now available for lease.",re,true);
	}
	
	public RealestateModificationResponse rentRealestate(Player player, Location signLoc, String downpayment, String reaccuring) throws TKTownsException, IllegalArgumentException {
		if(reaccuring == null ||  downpayment == null) {
			throw new IllegalArgumentException("Amount must be specified.");
		}
		Optional<RealEstate> reOp = this.getRealEstateAtLocation(signLoc);
		if(!reOp.isPresent()) {
			throw new TKTownsException("Your are not standing in a piece of RealEstate.");
		}
		RealEstate re = reOp.get();
		if(!re.getOwner().getUniqueId().equals(player.getUniqueId())) {
			throw new TKTownsException("You do not own this RealEstate.");
		}
		if(re.getStatus() != RealEstate.Status.OWNED) {
			throw new TKTownsException("RealEstate is all ready for sale, lease or rent.");
		}
		try {
			re.rent(SignLocation.fromLocation(signLoc),Double.parseDouble(downpayment), Double.parseDouble(reaccuring));
			this.config.saveTown(re.getParent()); //save town so we keep the realestate saved.
		} catch(NumberFormatException e) {
			throw new IllegalArgumentException("The amount must be a double ex: 20.5");
		}
		return new RealestateModificationResponse(re.getName() + " now available for rent.",re,true);
	}

	public RealestateModificationResponse buyRealestate(Player player, Location signLoc) throws TKTownsException {
		Optional<RealEstate> reOp = this.getRealEstateAtLocation(signLoc);
		if(!reOp.isPresent()) {
			throw new TKTownsException("Your are not standing in a piece of RealEstate.");
		}
		RealEstate re = reOp.get();
		re.buy(player);
		return new RealestateModificationResponse(re.getName() + " Welcome to your new home.",re,true);
	}

	public Set<Outpost> listOutposts(Player player) {
		Set<Outpost> outposts = new HashSet<Outpost>();
		Set<Town> towns = this.getTownsAPlayerIsResident(player);
		for(Town town : towns) {
			outposts.addAll(town.getOutposts().values());
		}
		return outposts;
	}

	public OutpostModificationResponse createOutpost(Player player, Location loc, String name) throws TKTownsException {
		Town town = this.getTownMayorOf(player);
		if(town == null || !town.isMayor(player)) {
			throw new TKTownsException("Only the mayor of a town can create outposts.");
		}
		Claim claim = GriefPrevention.instance.dataStore.getClaimAt(player.getLocation(), true, null);
		if(claim == null) {
			throw new TKTownsException("You are not standing in a GriefPrevention claim.");
		}
		if(town.getOutposts().containsKey(name)) {
			throw new TKTownsException("An outpost with that name all ready exists.");
		}
		Outpost out = new Outpost(claim, SignLocation.fromLocation(loc), town, name);
		town.addOutpost(out);
		this.config.saveTown(town); //save town so we keep the outpost saved.
		return new OutpostModificationResponse(out.getName() + " established.",out,true);
	}

	public OutpostModificationResponse deleteOutpost(Player player, String name) throws TKTownsException {
		RealEstate re = this.getRealEstateAtLocation(player.getLocation()).get();
		Outpost out;
		if(re instanceof Outpost) {
			out = (Outpost) re;
			if(!out.getParent().isMayor(player)) {
				throw new TKTownsException("Only the mayor can delete an outpost.");
			}
			out.getParent().removeOutpost(out.getName());
			this.config.saveTown(re.getParent()); //save town so we keep the realestate saved.
		} else {
			throw new TKTownsException("You are not standing in an outpost.");
		}
		return new OutpostModificationResponse(out.getName() + " established.",out,true);
	}

	public Set<OfflinePlayer> listResidents(Player player, String name) throws TownNotFoundException {
		Town town = null;
		if(name == null) {
			town = this.getTownMayorOf(player);
		} else {
			town = this.towns.get(name);
		}
		if(town == null) {
			throw new TownNotFoundException("No town with that name exists.");
		}
		
		return town.getResidents();
	}

	public TownModificationResponse addResident(Player player, String name) throws TKTownsException {
		Town town = this.getTownMayorOf(player);
		Player resident = getOnlinePlayer(name);
		if(town == null || !town.isMayor(player)) {
			throw new TKTownsException("Only the mayor of a town can add residents.");
		}
		if(resident == null) {
			throw new TKTownsException("Player by that name is not online or does not exist.");
		}
		town.addResident(resident);
		this.config.saveTown(town); //save town with the new residents.
		return new TownModificationResponse(resident.getName() + " is now a resident of " + town.getName(),town,true);
	}

	public TownModificationResponse deleteResident(Player player, String name) throws TKTownsException {
		Town town = this.getTownMayorOf(player);
		Player resident = getOnlinePlayer(name);
		if(town == null || !town.isMayor(player)) {
			throw new TKTownsException("Only the mayor of a town can remove residents.");
		}
		if(resident == null) {
			throw new TKTownsException("Player by that name does not exist.");
		}
		town.removeResident(resident);
		this.config.saveTown(town); //save town with the new residents.
		return new TownModificationResponse(resident.getName() + " is no longer a resident of " + town.getName(),town,true);
	}
	
	/**
	 * Because I hate warnings...
	 * @param name Name of player to fetch
	 * @return
	 */
	protected Player getOnlinePlayer(String name) {
		for(Player player : Bukkit.getOnlinePlayers()) {
			if(player.getName().equals(name))
				return player;
		}
		return null;
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
	
	protected Optional<Town> getTownAtLocation(Location loc) {
		Town ret = null;
		Claim claim = GriefPrevention.instance.dataStore.getClaimAt(loc, true, null);
		for(Town town : this.towns.values()) {
			if(town.getClaim().getID() == claim.getID() || town.getClaim().getID() == claim.parent.getID()) {
				ret = town;
				break;
			}
		}
		return Optional.fromNullable(ret) ;
	}
	
	protected Optional<RealEstate> getRealEstateAtLocation(Location loc) {
		RealEstate ret = null;
		//get the town
		Optional<Town> townOp = this.getTownAtLocation(loc);
		if(townOp.isPresent()) {
			Town town = townOp.get();
			for(RealEstate re : town.getChildren().values()) {
				if(re.getClaim().contains(loc, false, false)) {
					ret = re;
					break;
				}
			}
		}
		return Optional.fromNullable(ret);
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

	//TODO: Maybe this should be up in the plugin in layer in not down here in the api?
	public GenericModificationResponse handleSignEdit(Player player,SignChangeEvent signEvent) throws IndexOutOfBoundsException, TKTownsException {
		if(TownManager.TKTOWNS_TOWN_SIGN_HEADER.equalsIgnoreCase(signEvent.getLine(0))) {
			//Handle town related signs
			return this.handleTownSignEdit(player, signEvent);
		} else if(TownManager.TKTOWNS_REALESTATE_SIGN_HEADER.equalsIgnoreCase(signEvent.getLine(0))) {
			//Handle realestate related signs
			return this.handleRealestateSignEdit(player, signEvent);
		} else {
			//what?
			return new GenericModificationResponse("Unknown sign command! :: " + signEvent.getLine(1),false);
		}
	}
	
	public GenericModificationResponse handleTownSignEdit(Player player,SignChangeEvent signEvent) throws IndexOutOfBoundsException, TKTownsException {
		if("[Create]".equalsIgnoreCase(signEvent.getLine(1))) {
			if(signEvent.getLine(2) != null) {
				GenericModificationResponse tmr = this.createTown(player, signEvent.getBlock().getLocation(), signEvent.getLine(2));
				signEvent.setLine(0, "Welcome to");
				signEvent.setLine(1, signEvent.getLine(2));
				signEvent.setLine(2, null);
				signEvent.setLine(3, null);
				//TODO: Real meta data just setting this so that the break code can be tested.
				signEvent.getBlock().setMetadata(TownManager.TKTOWNS_METADATA_TAG, new FixedMetadataValue(TKTowns.plugin,new String("Town sign!")));
				return tmr;
			} else {
				throw new TKTownsException("The second line must be the name of the new town.");
			}
		}
		
		//TODO: Show some real help here...
		return new TownModificationResponse("Unknown sign command! :: " + signEvent.getLine(1),null,false);
	}
	
	public GenericModificationResponse handleRealestateSignEdit(Player player,SignChangeEvent signEvent) throws IndexOutOfBoundsException, TKTownsException {
		if("[Create]".equalsIgnoreCase(signEvent.getLine(1))) {
			if(signEvent.getLine(2) != null) {
				GenericModificationResponse tmr = this.createRealestate(player, signEvent.getBlock().getLocation(), signEvent.getLine(2));
				signEvent.setLine(0, "Welcome to");
				signEvent.setLine(1, signEvent.getLine(2));
				signEvent.setLine(2, null);
				signEvent.setLine(3, null);
				//TODO: Real meta data just setting this so that the break code can be tested.
				signEvent.getBlock().setMetadata(TownManager.TKTOWNS_METADATA_TAG, new FixedMetadataValue(TKTowns.plugin,new String("Town sign!")));
				return tmr;
			} else {
				throw new TKTownsException("The second line must be the name of the realestate.");
			}
		}
		
		if("[Sell]".equalsIgnoreCase(signEvent.getLine(1))) {
			if(signEvent.getLine(2) != null) {
				GenericModificationResponse tmr = this.sellRealestate(player, signEvent.getBlock().getLocation(), signEvent.getLine(2));
				signEvent.setLine(0, "For sale");
				signEvent.setLine(1, signEvent.getLine(2));
				signEvent.setLine(2, null);
				signEvent.setLine(3, null);
				//TODO: Real meta data just setting this so that the break code can be tested.
				signEvent.getBlock().setMetadata(TownManager.TKTOWNS_METADATA_TAG, new FixedMetadataValue(TKTowns.plugin,new String("Sale sign!")));
				return tmr;
			} else {
				throw new TKTownsException("The second line must be the amount to sell the realestate for.");
			}
		}
		
		if("[Lease]".equalsIgnoreCase(signEvent.getLine(1))) {
			if(signEvent.getLine(2) != null  && signEvent.getLine(3) != null && signEvent.getLine(4) != null) {
				GenericModificationResponse tmr = this.leaseRealestate(player, signEvent.getBlock().getLocation(), signEvent.getLine(2), signEvent.getLine(3), signEvent.getLine(4));
				signEvent.setLine(0, "For sale");
				signEvent.setLine(1, signEvent.getLine(2));
				signEvent.setLine(2, null);
				signEvent.setLine(3, null);
				//TODO: Real meta data just setting this so that the break code can be tested.
				signEvent.getBlock().setMetadata(TownManager.TKTOWNS_METADATA_TAG, new FixedMetadataValue(TKTowns.plugin,new String("Sale sign!")));
				return tmr;
			} else {
				throw new TKTownsException("The second line must be the lease amount.  The third line must be the down payment.  The fourth line must be the length of the lease in days");
			}
		}
		
		if("[Rent]".equalsIgnoreCase(signEvent.getLine(1))) {
			if(signEvent.getLine(2) != null  && signEvent.getLine(3) != null && signEvent.getLine(4) != null) {
				GenericModificationResponse tmr = this.rentRealestate(player, signEvent.getBlock().getLocation(), signEvent.getLine(2), signEvent.getLine(3));
				signEvent.setLine(0, "For sale");
				signEvent.setLine(1, signEvent.getLine(2));
				signEvent.setLine(2, null);
				signEvent.setLine(3, null);
				//TODO: Real meta data just setting this so that the break code can be tested.
				signEvent.getBlock().setMetadata(TownManager.TKTOWNS_METADATA_TAG, new FixedMetadataValue(TKTowns.plugin,new String("Sale sign!")));
				return tmr;
			} else {
				throw new TKTownsException("The second line must be the lease amount.  The third line must be the down payment.");
			}
		}
		//TODO: Show some real help here...
		return new TownModificationResponse("Unknown sign command! :: " + signEvent.getLine(1),null,false);
	}
	
	public boolean handleSignBreak(Optional<Player> p, Block block) {
		if(p.isPresent()) {
			p.get().sendMessage("TKTowns: You can not break town signs");
		}
		return true;
	}
	
}

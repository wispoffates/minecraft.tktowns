package io.github.wispoffates.minecraft.tktowns;

import io.github.wispoffates.minecraft.tktowns.RealEstate.Status;
import io.github.wispoffates.minecraft.tktowns.datastore.DataStore;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class TKTowns extends JavaPlugin {
	
	private static final Logger log = Logger.getLogger("Minecraft");
    public static Economy econ = null;
    public static Permission perms = null;
    public static Chat chat = null;
    public static TownManager townManager = null;
    public static DataStore config = null;
    
	@Override
	public void onEnable() {
		if (!setupEconomy() ) {
            log.severe(String.format("[%s] - Disabled due to no Vault dependency found!", getDescription().getName()));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        setupPermissions();
        setupChat();
        
        TKTowns.townManager = new TownManager();
        
        log.info(String.format("[%s] Eanbled Version %s", getDescription().getName(), getDescription().getVersion()));
	}
	
	@Override
	public void onDisable() {
		 log.info(String.format("[%s] Disabled Version %s", getDescription().getName(), getDescription().getVersion()));
	}
	
	private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }

    private boolean setupPermissions() {
        RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
        if(rsp != null) {
        	perms = rsp.getProvider();
        }
        return perms != null;
    }
    
    private boolean setupChat() {
        RegisteredServiceProvider<Chat> rsp = getServer().getServicesManager().getRegistration(Chat.class);
        if(rsp != null) {
        	chat = rsp.getProvider();
        }
        return chat != null;
    }
    
    public boolean onCommand( CommandSender sender, Command cmd, String label, String[] args) {
		//Lets make a frame breaking mod since they are weird 
		try {
	    	List<String> argsList = Arrays.asList(args);
			Player player = (Player) sender;
			String cmdStr = cmd.getName();
			if(cmdStr.equalsIgnoreCase("tkt")) {
				//TODO: List help.
				player.sendMessage("No help here yet... but soom (tm)");
				return true;
			} else if(cmdStr.equalsIgnoreCase("tkt_town")) {
				//[list/create/delete/deposit/withdraw]
				if(args.length == 0) {
					Town town = TKTowns.townManager.listTownInfo(player, null);
					player.sendMessage(TKTowns.formatTown(town));
				} else if(args[0].equalsIgnoreCase("list")) {
					//List all the towns
					Set<String> towns = TKTowns.townManager.listTowns(player);
					player.sendMessage(TownManager.TKTOWNS_HEADER);
					player.sendMessage("Towns: " + TKTowns.collectionToString(towns));
				} else if(args[0].equalsIgnoreCase("create")) {
					TKTowns.townManager.createTown(player,argsList.get(1));
					player.sendMessage("Town created.");
				} else if(args[0].equalsIgnoreCase("delete")) {
					TKTowns.townManager.deleteTown(player,argsList.get(1));
					player.sendMessage("Town deleted.");
				} else if(args[0].equalsIgnoreCase("deposit")) {
					if(argsList.size()>2) {
						TKTowns.townManager.depositTown(player,argsList.get(1),argsList.get(2));
					} else {
						TKTowns.townManager.depositTown(player,null,argsList.get(1));
					}
					player.sendMessage("Deposit made.");
				} else if(args[0].equalsIgnoreCase("withdraw")) {
					if(argsList.size()>2) {
						TKTowns.townManager.withdrawTown(player,argsList.get(1),argsList.get(2));
					} else {
						TKTowns.townManager.withdrawTown(player,null,argsList.get(2));
					}
					player.sendMessage("Withdraw made.");
				} else if(args[0].equalsIgnoreCase("balance")) {
					double bal = -1;
					if(argsList.size()>2) {
						bal = TKTowns.townManager.getBalance(player);
					} else {
						bal = TKTowns.townManager.getBalance(player);
					}
					player.sendMessage("Balance: +" + bal); 
				} else {
					TKTowns.townManager.listTownInfo(player,argsList.get(1));
				}
				return true;
			} else if(cmdStr.equalsIgnoreCase("tkt_realestate")) {
				//[List/Create/Sell/Lease/Buy]
				if(args.length == 0) {
					TKTowns.townManager.listRealestate(player, null);
				} else if(args[0].equalsIgnoreCase("list")) {
					TKTowns.townManager.listRealestate(player, argsList.get(1));
				} else if(args[0].equalsIgnoreCase("create")) {
					TKTowns.townManager.createRealestate(player, argsList.get(1));
					player.sendMessage("Real Estate created.");
				} else if(args[0].equalsIgnoreCase("sell")) {
					TKTowns.townManager.sellRealestate(player, argsList.get(1));
					player.sendMessage("Real Estate put up for sale.");
				} else if(args[0].equalsIgnoreCase("lease")) {
					TKTowns.townManager.leaseRealestate(player, argsList.get(1),argsList.get(2), argsList.get(3));
					player.sendMessage("Real Estate put up for lease.");
				} else if(args[0].equalsIgnoreCase("rent")) {
					TKTowns.townManager.rentRealestate(player, argsList.get(1),argsList.get(2));
					player.sendMessage("Real Estate put up for rent.");
				} else if(args[0].equalsIgnoreCase("buy")) {
					TKTowns.townManager.buyRealestate(player);
					player.sendMessage("Congratulations you now own this Real Estate.");
				} else {
					TKTowns.townManager.listRealestate(player, argsList.get(1));
				}
				return true;
			} else if(cmdStr.equalsIgnoreCase("tkt_outpost")) {
				//[Create/Delete]
				if(args.length == 0) {
					Set<Outpost> outposts = TKTowns.townManager.listOutposts(player);
					StringBuilder sb = new StringBuilder();
					sb.append("Outposts: ");
					for(Outpost out : outposts) {
						sb.append(out.getName() + " ");
					}
					player.sendMessage(sb.toString());
				} else if(args[0].equalsIgnoreCase("create")) {
					TKTowns.townManager.createOutpost(player,argsList.get(1));
					player.sendMessage("Outpost created.");
				} else if(args[0].equalsIgnoreCase("delete")) {
					TKTowns.townManager.deleteOutpost(player,argsList.get(1));
					player.sendMessage("Outpost deleted.");
				}
				return true;
			} else if(cmdStr.equalsIgnoreCase("tkt_resident")) {
				//[List/add/delete]
				if(args.length == 0) {
					Set<Player> residents = TKTowns.townManager.listResidents(player, null);
					StringBuilder sb = new StringBuilder();
					sb.append("Residents: ");
					for(Player res : residents) {
						sb.append(res.getName() + " ");
					}
					player.sendMessage(sb.toString());
				} else if(args[0].equalsIgnoreCase("list")) {
					TKTowns.townManager.listResidents(player,argsList.get(1));
				} else if(args[0].equalsIgnoreCase("add")) {
					TKTowns.townManager.addResident(player,argsList.get(1));
					player.sendMessage("Resident added.");
				} else if(args[0].equalsIgnoreCase("delete")) {
					TKTowns.townManager.deleteResident(player,argsList.get(1));
					player.sendMessage("Resident delted.");
				}
				return true;
			}
		} catch (Exception e) {
			Player player = (Player) sender;
			player.sendMessage(TownManager.TKTOWNS_HEADER);
			player.sendMessage(e.getMessage());
			return true;
		}
		return false;
    }
    
    protected static String collectionToString(Collection<String> collect) {
		StringBuilder sb = new StringBuilder();
		for(String str : collect) {
			sb.append(str + ", ");
		}
		
		return sb.toString().substring(0, sb.length()-2);
	}
    
    protected static String formatRealestate(Set<RealEstate> realestate, boolean onlyAvailable) {
    	StringBuilder sb = new StringBuilder();
    	for(RealEstate re : realestate) {
    		if(!onlyAvailable || (re.getStatus() == Status.FORLEASE || re.getStatus() == Status.FORSALE || re.getStatus() == Status.FORRENT)) {
    			sb.append(re.toString()).append("\n"); 
    		}
    	}
    	return sb.toString();
    }
    
    protected static String formatTown(Town town) {
    	StringBuilder sb = new StringBuilder();
    	sb.append("------------" + town.getName() + "--------------").append("\n");
    	sb.append("Mayor: " + Bukkit.getPlayer(town.getOwner())).append("\n");
    	sb.append("Residents: " + town.countResidents()).append("\n");
    	
    	return sb.toString();
    }
}

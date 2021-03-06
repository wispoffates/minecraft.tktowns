package io.github.wispoffates.minecraft.tktowns;

import io.github.wispoffates.minecraft.tktowns.api.TownManager;
import io.github.wispoffates.minecraft.tktowns.api.impl.Outpost;
import io.github.wispoffates.minecraft.tktowns.api.impl.RealEstate;
import io.github.wispoffates.minecraft.tktowns.api.impl.Town;
import io.github.wispoffates.minecraft.tktowns.api.impl.RealEstate.Status;
import io.github.wispoffates.minecraft.tktowns.datastore.DataStore;
import io.github.wispoffates.minecraft.tktowns.exceptions.TKTownsException;
import io.github.wispoffates.minecraft.tktowns.responses.GenericModificationResponse;
import io.github.wispoffates.minecraft.tktowns.responses.TownModificationResponse;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;

import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import com.google.common.base.Optional;

public class TKTowns extends JavaPlugin implements Listener {
	
	protected final static String TKTOWNS_HEADER 	   	= "--- TKTowns ---";
	protected final static String TKTOWNS_ERROR_HEADER 	= "--- TKTowns Error! ---";
	
	private static final Logger log = Logger.getLogger("Minecraft");
    public static Economy econ = null;
    public static Permission perms = null;
    public static Chat chat = null;
    public static TownManager townManager = null;
    public static DataStore config = null;
    
    public static TKTowns plugin = null;
    
	@Override
	public void onEnable() {
		this.getConfig().options().copyDefaults(true);
		this.saveConfig();
		if (!setupEconomy() ) {
            log.severe(String.format("[%s] - Disabled due to no Vault dependency found!", getDescription().getName()));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        setupPermissions();
        setupChat();
        
        TKTowns.townManager = new TownManager(this.getDataFolder());
        this.getServer().getPluginManager().registerEvents(this, this);
        
        TKTowns.plugin = this;
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
    
    //Handle sign changes
    @EventHandler(priority=EventPriority.HIGHEST)
    public void onSignChangeEvent(SignChangeEvent signEvent) {
    	//See if it is a modification of a sign we care about
    	if(TownManager.TKTOWNS_TOWN_SIGN_HEADER.equalsIgnoreCase(signEvent.getLine(0)) || TownManager.TKTOWNS_REALESTATE_SIGN_HEADER.equalsIgnoreCase(signEvent.getLine(0))) {
    		try {
				GenericModificationResponse gmr = TownManager.get().handleSignEdit(signEvent.getPlayer(), signEvent);
				signEvent.getPlayer().sendMessage(gmr.getMessage());
			} catch (IndexOutOfBoundsException e) {
				Player player = signEvent.getPlayer();
				player.sendMessage(TKTOWNS_ERROR_HEADER);
				player.sendMessage(e.getClass().getName() + " :: " + e.getMessage());
				e.printStackTrace();
			} catch (TKTownsException e) {
				Player player = signEvent.getPlayer();
				player.sendMessage(TKTOWNS_ERROR_HEADER);
				player.sendMessage(e.getClass().getName() + " :: " + e.getMessage());
				e.printStackTrace();
			}
    	}
    }
    
    //stop breaking of our signs 
    @EventHandler(priority=EventPriority.HIGHEST)
    public void onBlockBreakEvent(BlockBreakEvent breakEvent) {
    	if(this.handleTKTownsSign(Optional.of(breakEvent.getPlayer()),breakEvent.getBlock())) {
    		breakEvent.setCancelled(true);
    	}
    }
    
    @EventHandler(priority=EventPriority.HIGHEST)
    public void onBlockPistonExtendEvent(BlockPistonExtendEvent breakEvent) {
    	if(this.handleTKTownsSign(Optional.<Player>absent(),breakEvent.getBlock())) {
    		breakEvent.setCancelled(true);
    	}
    }
    
    @EventHandler(priority=EventPriority.HIGHEST)
    public void onBlockPistonetractEvent(BlockPistonRetractEvent breakEvent) {
    	if(this.handleTKTownsSign(Optional.<Player>absent(),breakEvent.getBlock())) {
    		breakEvent.setCancelled(true);
    	}
    }
    
    @EventHandler(priority=EventPriority.HIGHEST)
    public void onLeavesDecayEvent(LeavesDecayEvent breakEvent) {
    	if(this.handleTKTownsSign(Optional.<Player>absent(),breakEvent.getBlock())) {
    		breakEvent.setCancelled(true);
    	}
    }
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent pie) {
    	if( TownManager.get().isForSaleSign(pie.getClickedBlock())) {
    		pie.getPlayer().sendMessage("You clicked the sale sign! wooo!");
    	}
    }
    /**
     * 
     * @param block
     * @return true if we the event should be cancelled
     */
    protected boolean handleTKTownsSign(Optional<Player> player, Block block) {
    	try {
	    	//is the block a sign?
	    	 if (block.getType() == Material.SIGN) {
	    		 return TownManager.get().handleSignBreak(player, block);
	         }
	    	 
	    	 if (block.getType() == Material.SIGN_POST) {
	    		 return TownManager.get().handleSignBreak(player, block);
	         }
	    	 
	    	 //is the block a sign?
	    	 if (block.getType() == Material.WALL_SIGN) {
	    		 return TownManager.get().handleSignBreak(player, block);
	         }
	    	 
	    	 //Is a sign attached to it?
	    	for (BlockFace f : BlockFace.values()) {
	    		Block relative = block.getRelative(f);
	            if (relative.getType() == Material.WALL_SIGN) {
	            	return TownManager.get().handleSignBreak(player, block);
	            }
	        }
	        return false;
    	} catch (Exception e) {
    		if(player.isPresent()) {
    			player.get().sendMessage(TKTOWNS_ERROR_HEADER);
				player.get().sendMessage(e.getClass().getName() + " :: " + e.getMessage());
    		}
			e.printStackTrace();
			return true;
		}
    }
    
    public boolean onCommand( CommandSender sender, Command cmd, String label, String[] args) {
		try {
	    	List<String> argsList = Arrays.asList(args);
			Player player = (Player) sender;
			String cmdStr = cmd.getName();
			if(cmdStr.equalsIgnoreCase("tkt")) {
				//TODO: List help.
				player.sendMessage("No help here yet... but soom (tm)");
				return true;
			} else if(cmdStr.equalsIgnoreCase("tkt_town") || cmdStr.equalsIgnoreCase("tktown")) {
				//[list/create/delete/deposit/withdraw]
				if(args.length == 0) {
					Town town = TKTowns.townManager.listTownInfo(player, null);
					player.sendMessage(TKTowns.formatTown(town));
				} else if(args[0].equalsIgnoreCase("list")) {
					//List all the towns
					Set<String> towns = TKTowns.townManager.listTowns(player);
					player.sendMessage(TKTOWNS_HEADER);
					player.sendMessage("Towns: " + TKTowns.collectionToString(towns));
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
			} else if(cmdStr.equalsIgnoreCase("tkt_realestate") || cmdStr.equalsIgnoreCase("tkreal")) {
				//[List/Create/Sell/Lease/Buy]
				if(args.length == 0) {
					Set<RealEstate> re = TKTowns.townManager.listRealestate(player, null);
					player.sendMessage(TKTowns.formatRealestate(re, false));
				} else if(args[0].equalsIgnoreCase("list")) {
					Set<RealEstate> reals = TKTowns.townManager.listRealestate(player, null);
					player.sendMessage(TKTowns.formatRealestate(reals, false));
				}  else {
					Set<RealEstate> re = TKTowns.townManager.listRealestate(player, argsList.get(1));
					player.sendMessage(TKTowns.formatRealestate(re, false));
				}
				return true;
			} else if(cmdStr.equalsIgnoreCase("tkt_outpost") || cmdStr.equalsIgnoreCase("tkout")) {
				//[Create/Delete]
				if(args.length == 0) {
					Set<Outpost> outposts = TKTowns.townManager.listOutposts(player);
					StringBuilder sb = new StringBuilder();
					sb.append("Outposts: ");
					for(Outpost out : outposts) {
						sb.append(out.getName() + " ");
					}
					player.sendMessage(sb.toString());
				} else if(args[0].equalsIgnoreCase("delete")) {
					GenericModificationResponse gmr = TKTowns.townManager.deleteOutpost(player,argsList.get(1));
					player.sendMessage(gmr.getMessage());
				}
				return true;
			} else if(cmdStr.equalsIgnoreCase("tkt_resident") || cmdStr.equalsIgnoreCase("tkres")) {
				//[List/add/delete]
				if(args.length == 0) {
					Set<OfflinePlayer> residents = TKTowns.townManager.listResidents(player, null);
					StringBuilder sb = new StringBuilder();
					sb.append("Residents: ");
					for(OfflinePlayer res : residents) {
						sb.append(res.getName() + " ");
					}
					player.sendMessage(sb.toString());
				} else if(args[0].equalsIgnoreCase("list")) {
					Set<OfflinePlayer> residents = null;
					if(argsList.size() > 1) {
						residents = TKTowns.townManager.listResidents(player,argsList.get(1));
					} else {
						residents = TKTowns.townManager.listResidents(player, null);
					}
					StringBuilder sb = new StringBuilder();
					sb.append("Residents: ");
					for(OfflinePlayer res : residents) {
						sb.append(res.getName() + " ");
					}
					player.sendMessage(sb.toString());
				} else if(args[0].equalsIgnoreCase("add")) {
					TownModificationResponse tmr = TKTowns.townManager.addResident(player,argsList.get(1));
					player.sendMessage(tmr.getMessage());
				} else if(args[0].equalsIgnoreCase("delete")) {
					TownModificationResponse tmr =  TKTowns.townManager.deleteResident(player,argsList.get(1));
					player.sendMessage(tmr.getMessage());
				}
				return true;
			}
		} catch (Exception e) {
			Player player = (Player) sender;
			player.sendMessage(TKTOWNS_ERROR_HEADER);
			player.sendMessage(e.getClass().getName() + " :: " + e.getMessage());
			e.printStackTrace();
			return true;
		}
		return false;
    }
    
    protected static String collectionToString(Collection<String> collect) {
		StringBuilder sb = new StringBuilder();
		for(String str : collect) {
			sb.append(str + ", ");
		}
		if(sb.toString().contains(",")) {
			return sb.toString().substring(0, sb.length()-2);
		} else {
			return sb.toString();
		}
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
    	sb.append("Mayor: " + town.getOwner().getName()).append("\n");
    	sb.append("Residents: " + town.countResidents()).append("\n");
    	
    	return sb.toString();
    }
}

package io.github.wispoffates.minecraft.tktowns;

import io.github.wispoffates.minecraft.tktowns.datastore.DataStore;

import java.util.logging.Logger;

import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
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
        perms = rsp.getProvider();
        return perms != null;
    }
    
    private boolean setupChat() {
        RegisteredServiceProvider<Chat> rsp = getServer().getServicesManager().getRegistration(Chat.class);
        chat = rsp.getProvider();
        return chat != null;
    }
    
    public boolean onCommand( CommandSender sender, Command cmd, String label, String[] args) {
		//Lets make a frame breaking mod since they are weird 
		if(cmd.getName().equalsIgnoreCase("tktowns") || cmd.getName().equalsIgnoreCase("tkt")) {
			//TODO: Actually call the command
			return true;
		}
		String cmdStr = cmd.getName().split("_")[1];
		if(cmdStr.equalsIgnoreCase("realestate")) {
			
		} else if(cmdStr.equalsIgnoreCase("sell")) {
			
		} else if(cmdStr.equalsIgnoreCase("lease")) {
			
		} else if(cmdStr.equalsIgnoreCase("buy")) {
			
		} else if(cmdStr.equalsIgnoreCase("outpost")) {
			
		} else if(cmdStr.equalsIgnoreCase("market")) {
			
		}
		return false;
    }
}

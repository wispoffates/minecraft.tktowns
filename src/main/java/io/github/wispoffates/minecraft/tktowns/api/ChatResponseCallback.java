package io.github.wispoffates.minecraft.tktowns.api;

import org.bukkit.entity.Player;

public interface ChatResponseCallback {
	public void onAbort();
	public void onConfirm();
	public void onDeny(); 
	
	public Player getPlayer();
}

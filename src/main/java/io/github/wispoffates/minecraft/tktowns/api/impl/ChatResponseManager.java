package io.github.wispoffates.minecraft.tktowns.api.impl;

import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatResponseManager {
	
	protected Map<Player,Object> monitors;

	public void handleChatEvent(AsyncPlayerChatEvent e) {
		if(monitors.isEmpty()) {
			return; //no monitors active bail out.
		}
		Object o = monitors.get(e.getPlayer());
		if(o == null) {
			return; //no monitor for this player
		}
	}
	
}

package me.goodandevil.skyblock.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import me.goodandevil.skyblock.island.Island;

public class IslandDeleteEvent extends Event {
	
	private Island island;
	
	public IslandDeleteEvent(Island island) {
		this.island = island;
	}
	
	public Island getIsland() {
		return island;
	}
	
    private static final HandlerList handlers = new HandlerList();
    
	public HandlerList getHandlers() {
		return handlers;
	}
}

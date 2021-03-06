package me.goodandevil.skyblock.listeners;

import java.io.File;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import me.goodandevil.skyblock.Main;
import me.goodandevil.skyblock.config.FileManager.Config;
import me.goodandevil.skyblock.island.Island;
import me.goodandevil.skyblock.island.IslandLocation;
import me.goodandevil.skyblock.island.IslandManager;
import me.goodandevil.skyblock.island.IslandSettings;
import me.goodandevil.skyblock.playerdata.PlayerData;
import me.goodandevil.skyblock.playerdata.PlayerDataManager;
import me.goodandevil.skyblock.utils.version.Sounds;
import me.goodandevil.skyblock.utils.world.LocationUtil;

public class Move implements Listener {

	private final Main plugin;
	
 	public Move(Main plugin) {
		this.plugin = plugin;
	}
	
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		Player player = event.getPlayer();
		
		Location from = event.getFrom();
		Location to = event.getTo();

		if (from.getX() != to.getX() || from.getY() != to.getY() || from.getZ() != to.getZ()) {
			if (player.getWorld().getName().equals(plugin.getWorldManager().getWorld(IslandLocation.World.Normal).getName()) || player.getWorld().getName().equals(plugin.getWorldManager().getWorld(IslandLocation.World.Nether).getName())) {
				PlayerDataManager playerDataManager = plugin.getPlayerDataManager();
				
				if (playerDataManager.hasPlayerData(player)) {
					PlayerData playerData = playerDataManager.getPlayerData(player);
					UUID islandOwnerUUID = playerData.getIsland();
					
					if (islandOwnerUUID != null) {
						IslandManager islandManager = plugin.getIslandManager();
						Island island = islandManager.getIsland(islandOwnerUUID);
						
						if (island != null) {
							IslandLocation.World world = null;
							
							if (LocationUtil.isLocationAtLocationRadius(to, island.getLocation(IslandLocation.World.Normal, IslandLocation.Environment.Island), 85)) {
								world = IslandLocation.World.Normal;
							} else if (LocationUtil.isLocationAtLocationRadius(to, island.getLocation(IslandLocation.World.Nether, IslandLocation.Environment.Island), 85)) {
								world = IslandLocation.World.Nether;
							}
							
							if (world != null) {
								Config config = plugin.getFileManager().getConfig(new File(plugin.getDataFolder(), "config.yml"));
								FileConfiguration configLoad = config.getFileConfiguration();
								
								if (configLoad.getBoolean("Island.Void.Teleport.Enable")) {
									if (to.getY() <= configLoad.getInt("Island.Void.Teleport.Offset")) {
										if (island.getSetting(IslandSettings.Role.Owner, "KeepItemsOnDeath").getStatus()) {
											player.setFallDistance(0.0F);
											player.teleport(island.getLocation(world, IslandLocation.Environment.Main));
											player.playSound(player.getLocation(), Sounds.ENDERMAN_TELEPORT.bukkitSound(), 1.0F, 1.0F);
										}
									}	
								}
							} else {
								if (LocationUtil.isLocationAtLocationRadius(to, island.getLocation(IslandLocation.World.Normal, IslandLocation.Environment.Island), 87) || LocationUtil.isLocationAtLocationRadius(to, island.getLocation(IslandLocation.World.Nether, IslandLocation.Environment.Island), 87)) {
									player.teleport(player.getLocation().add(from.toVector().subtract(to.toVector()).normalize().multiply(2.0D)));
									player.playSound(player.getLocation(), Sounds.ENDERMAN_TELEPORT.bukkitSound(), 1.0F, 1.0F);
								} else {
									if (player.getWorld().getName().equals(plugin.getWorldManager().getWorld(IslandLocation.World.Normal).getName())) {
										player.teleport(island.getLocation(IslandLocation.World.Normal, IslandLocation.Environment.Main));
									} else {
										player.teleport(island.getLocation(IslandLocation.World.Nether, IslandLocation.Environment.Main));
									}
									
									player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getFileManager().getConfig(new File(plugin.getDataFolder(), "language.yml")).getFileConfiguration().getString("Island.WorldBorder.Outside.Message")));
									player.playSound(player.getLocation(), Sounds.ENDERMAN_TELEPORT.bukkitSound(), 1.0F, 1.0F);
								}
							}
							
							return;
						}
					}
					
					LocationUtil.teleportPlayerToSpawn(player);
					
					player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getFileManager().getConfig(new File(plugin.getDataFolder(), "language.yml")).getFileConfiguration().getString("Island.WorldBorder.Disappeared.Message")));
					player.playSound(player.getLocation(), Sounds.ENDERMAN_TELEPORT.bukkitSound(), 1.0F, 1.0F);
				}
			}	
		}
	}
}

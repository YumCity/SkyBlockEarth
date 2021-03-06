package me.goodandevil.skyblock.utils.world;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import me.goodandevil.skyblock.Main;
import me.goodandevil.skyblock.config.FileManager;
import me.goodandevil.skyblock.config.FileManager.Config;
import me.goodandevil.skyblock.utils.math.VectorUtil;
import me.goodandevil.skyblock.utils.version.Materials;
import me.goodandevil.skyblock.utils.world.block.BlockDegreesType;

public final class LocationUtil {

    public static boolean isLocationCentreOfBlock(Location location) {
    	double x = location.getX() - location.getBlockX() - 0.5D, z = location.getZ() - location.getBlockZ() - 0.5D;
    	
    	if (Math.abs(x) < 0.2D && Math.abs(z) < 0.2D) {
    		return true;
    	}
    	
    	return false;
    }
    
    public static boolean isLocationLocation(Location location1, Location location2) {
    	if (location1.getBlockX() == location2.getBlockX() && location1.getBlockY() == location2.getBlockY() && location1.getBlockZ() == location2.getBlockZ()) {
    		return true;
    	}
    	
    	return false;
    }
    
	public static boolean isLocationAtLocationRadius(Location location1, Location location2, int radius) {
		if (location1 == null || location2 == null || !location1.getWorld().getName().equals(location2.getWorld().getName())) {
			return false;
		}
		
		int x = Math.abs(location1.getBlockX() - location2.getBlockX());
		int z = Math.abs(location1.getBlockZ() - location2.getBlockZ());
		
		return x < radius && z < radius;
	}
    
    public static List<Location> getLocations(Location minLocation, Location maxLocation) {
    	List<Location> locations = new ArrayList<>();
    	
	    int MinX = Math.min(maxLocation.getBlockX(), minLocation.getBlockX());
	    int MinY = Math.min(maxLocation.getBlockY(), minLocation.getBlockY());
	    int MinZ = Math.min(maxLocation.getBlockZ(), minLocation.getBlockZ());
	    
	    int MaxX = Math.max(maxLocation.getBlockX(), minLocation.getBlockX());
	    int MaxY = Math.max(maxLocation.getBlockY(), minLocation.getBlockY());
	    int MaxZ = Math.max(maxLocation.getBlockZ(), minLocation.getBlockZ());
	    
	    for (int x = MinX; x <= MaxX; x++) {
	        for (int y = MinY; y <= MaxY; y++) {
	            for (int z = MinZ; z <= MaxZ; z++) {
	            	locations.add(new Location(minLocation.getWorld(), x, y, z));
	            }
	        }
	    }
	    
	    return locations;
    }
    
    public static Location getHighestBlock(Location location) {
        for(int y = 256; y > 0; y--){
        	location.setY(y);
        	
            Block block = location.getBlock();
            
            if(!(block.getType() == Material.AIR)){
                return location;
            }
        }
        
        return location;
    }
    
    public static int getYSurface(Location location, boolean isNether) {
        int maxY = 0;
        boolean followY = false;
        
        for(int y = 0; y < 256; y++){
            Location loc = new Location(location.getWorld(), location.getBlockX(), y, location.getBlockZ());
            Block block = loc.getBlock().getRelative(BlockFace.UP);
            
            if(isNether) {
                if(y < 127 && (block.getType() == Material.LAVA || block.getType() == Materials.LEGACY_STATIONARY_LAVA.getPostMaterial() || block.getType() == Material.AIR)){
                    maxY = y;
                    break;
                }
            } else {
                if(block.getType() == Materials.OAK_LEAVES.parseMaterial() || block.getType() == Materials.ACACIA_LEAVES.parseMaterial()) {
                    break;
                }
                
                if(block.getType() == Material.AIR || block.getType() == Materials.LEGACY_STATIONARY_WATER.getPostMaterial() || block.getType() == Material.WATER || block.getType() == Materials.LEGACY_STATIONARY_LAVA.getPostMaterial() || block.getType() == Material.LAVA) {
                    if(!followY) {
                        maxY = y;
                        followY = true;
                    }
                } else {
                    followY = false;
                    maxY = 0;
                }
            }
        }
        
        return maxY;
    }
    
    public static double rotateYaw(double a, double b) throws Exception {
        if (a < -180 || a > 180) {
            throw new Exception();
        }
        
        double c = a + b;
        return (c > 180) ? -(c - 180) : 180 - c;
    }

    public static double rotatePitch(double a, double b) throws Exception {
        if(a < -90 || a > 90){
            throw new Exception();
        }
        
        double c = a + b;
        return (c > 90) ? -(c - 90) : 90 - c;
    }
    
    public static Location rotateLocation(Location location, BlockDegreesType blockTypeDegrees) {
        if (blockTypeDegrees == BlockDegreesType.ROTATE_90) {
        	return VectorUtil.rotateAroundAxisY(location.toVector(), 90).toLocation(location.getWorld());
        } else if (blockTypeDegrees == BlockDegreesType.ROTATE_180) {
            return VectorUtil.rotateAroundAxisY(location.toVector(), 180).toLocation(location.getWorld());
        } else if (blockTypeDegrees == BlockDegreesType.ROTATE_270) {
            return VectorUtil.rotateAroundAxisY(location.toVector(), 270).toLocation(location.getWorld());
        } else {
            return location;
        }
    }
    
    public static void teleportPlayerToSpawn(Player player) {
    	Main plugin = Main.getInstance();
    	
    	FileManager fileManager = plugin.getFileManager();
    	
		Config config = fileManager.getConfig(new File(plugin.getDataFolder(), "locations.yml"));
		
		if (config.getFileConfiguration().getString("Location.Spawn") == null) {
			Bukkit.getServer().getLogger().log(Level.WARNING, "SkyBlock | Error: A spawn point hasn't been set.");
		} else {
			new BukkitRunnable() {
				@Override
				public void run() {
					player.teleport(fileManager.getLocation(config, "Location.Spawn", true));
				}
			}.runTask(plugin);
		}
    }
}

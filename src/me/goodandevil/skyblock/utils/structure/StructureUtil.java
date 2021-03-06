package me.goodandevil.skyblock.utils.structure;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import me.goodandevil.skyblock.Main;
import me.goodandevil.skyblock.config.FileManager;
import me.goodandevil.skyblock.config.FileManager.Config;
import me.goodandevil.skyblock.utils.GZipUtil;
import me.goodandevil.skyblock.utils.version.NMSUtil;
import me.goodandevil.skyblock.utils.world.LocationUtil;
import me.goodandevil.skyblock.utils.world.block.BlockData;
import me.goodandevil.skyblock.utils.world.block.BlockDegreesType;
import me.goodandevil.skyblock.utils.world.block.BlockUtil;
import me.goodandevil.skyblock.utils.world.entity.EntityData;
import me.goodandevil.skyblock.utils.world.entity.EntityUtil;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public final class StructureUtil {
	
    public static void saveStructure(File configFile, Location originLocation, Location[] positions) throws Exception {
        if (!configFile.exists()) {
        	configFile.createNewFile();
        }
        
        LinkedHashMap<Block, StructureLocation> blocks = SelectionLocation.getBlocks(originLocation, positions[0], positions[1]);
        LinkedHashMap<Entity, StructureLocation> entities = SelectionLocation.getEntities(originLocation, positions[0], positions[1]);
        
        List<BlockData> blockData = new ArrayList<>();
        List<EntityData> entityData = new ArrayList<>();
        
        String originBlockLocation = "";
        
        for (Block blockList : blocks.keySet()) {
        	StructureLocation structureLocation = blocks.get(blockList);
        	
        	if (structureLocation.isOriginLocation()) {
        		originBlockLocation = structureLocation.getX() + ":" + structureLocation.getY() + ":" + structureLocation.getZ() + ":" + positions[0].getWorld().getName();
        	
        		if (blockList.getType() == Material.AIR) {
                    blockData.add(BlockUtil.convertBlockToBlockData(blockList, structureLocation.getX(), structureLocation.getY(), structureLocation.getZ()));
        		}
        	}
        	
            if (blockList.getType() == Material.AIR) {
                continue;
            }
            
            blockData.add(BlockUtil.convertBlockToBlockData(blockList, structureLocation.getX(), structureLocation.getY(), structureLocation.getZ()));
        }
        
        for (Entity entityList : entities.keySet()) {
            if (entityList.getType() == EntityType.PLAYER) {
                continue;
            }
            
            StructureLocation structureLocation = entities.get(entityList);
            entityData.add(EntityUtil.convertEntityToEntityData(entityList, structureLocation.getX(), structureLocation.getY(), structureLocation.getZ()));
        }
    	
    	String JSONString = new Gson().toJson(new StructureStorage(new Gson().toJson(blockData), new Gson().toJson(entityData), originBlockLocation, System.currentTimeMillis(), NMSUtil.getVersionNumber()), new TypeToken<StructureStorage>(){}.getType());
        
        FileOutputStream fileOutputStream = new FileOutputStream(configFile, false);
        fileOutputStream.write(GZipUtil.compress(JSONString.getBytes(StandardCharsets.UTF_8)));
        fileOutputStream.flush();
        fileOutputStream.close();
    }

    public static Structure loadStructure(File configFile) throws IOException {
        if(!configFile.exists()) {
            return null;
        }
        
        byte[] content = new byte[(int) configFile.length()];
        
        FileInputStream fileInputStream = new FileInputStream(configFile);
        fileInputStream.read(content);
        fileInputStream.close();
        
        String JSONString = new String(GZipUtil.decompress(content));
        StructureStorage structureStorage = new Gson().fromJson(JSONString, new TypeToken<StructureStorage>(){}.getType());
        
        return new Structure(structureStorage, configFile.getName());
    }
    
    @SuppressWarnings("unchecked")
	public static StructureIsland pasteStructure(Structure s, Location location, BlockDegreesType type) throws Exception {
        StructureStorage bs = s.getStructureStorage();
        
        String[] originLocationPositions = null;
        
        if (!bs.getOriginLocation().isEmpty()) {
        	originLocationPositions = bs.getOriginLocation().split(":");
        }
        
        Location originLocation = location;
        
        List<BlockData> blockData = (List<BlockData>) new Gson().fromJson(bs.getBlocks(), new TypeToken<List<BlockData>>(){}.getType());
        
        for (BlockData blockDataList : blockData) {
        	try {
                Location blockRotationLocation = LocationUtil.rotateLocation(new Location(location.getWorld(), blockDataList.getX(), blockDataList.getY(), blockDataList.getZ()), type);
                Location blockLocation = new Location(location.getWorld(),  location.getX() - Math.abs(Integer.valueOf(originLocationPositions[0])), location.getY() - Integer.valueOf(originLocationPositions[1]), location.getZ() + Math.abs(Integer.valueOf(originLocationPositions[2])));
                blockLocation.add(blockRotationLocation);
                
                if (originLocationPositions != null) {
                	if (blockDataList.getX() == Integer.valueOf(originLocationPositions[0]) && blockDataList.getY() == Integer.valueOf(originLocationPositions[1]) && blockDataList.getZ() == Integer.valueOf(originLocationPositions[2])) {
                		originLocation = blockLocation;
                	}
                }
                
                BlockUtil.convertBlockDataToBlock(blockLocation.getBlock(), blockDataList);	
        	} catch (Exception e) {
        		// TODO Prevent unnecessary spamming in the console until todo comments are fixed
        	}
        }
        
        for (EntityData entityDataList : (List<EntityData>) new Gson().fromJson(bs.getEntities(), new TypeToken<List<EntityData>>(){}.getType())) {
	    	try {
	        	entityDataList.setY(entityDataList.getY() - Integer.valueOf(originLocationPositions[1]));
	            EntityUtil.convertEntityDataToEntity(entityDataList, location, type);
        	} catch (Exception e) {
        		// TODO Prevent unnecessary spamming in the console until todo comments are fixed
	    	}
        }
        
        return new StructureIsland(location, originLocation);
    }

    public static ItemStack getTool() throws Exception {
    	Main plugin = Main.getInstance();
    	
    	FileManager fileManager = plugin.getFileManager();
    	
    	Config config = fileManager.getConfig(new File(plugin.getDataFolder(), "language.yml"));
    	FileConfiguration configLoad = config.getFileConfiguration();
    	
    	ItemStack is = new ItemStack(Material.valueOf(fileManager.getConfig(new File(plugin.getDataFolder(), "config.yml")).getFileConfiguration().getString("Island.Admin.Structure.Selector")));
    	ItemMeta im = is.getItemMeta();
    	im.setDisplayName(ChatColor.translateAlternateColorCodes('&', configLoad.getString("Island.Structure.Tool.Item.Displayname")));
    	
    	List<String> itemLore = new ArrayList<>();
    	
    	for (String itemLoreList : configLoad.getStringList("Island.Structure.Tool.Item.Lore")) {
    		itemLore.add(ChatColor.translateAlternateColorCodes('&', itemLoreList));
    	}
    	
    	im.setLore(itemLore);
    	is.setItemMeta(im);
    	
    	return is;
    }
    
    public static Location[] getFixedLocations(Location location1, Location location2) {
    	Location location1Fixed = location1.clone();
    	Location location2Fixed = location2.clone();
    	
    	if (location1.getX() > location2.getX()) {
    		location1Fixed.setX(location2.getX());
    		location2Fixed.setX(location1.getX());
    	}
    	
    	if (location1.getZ() < location2.getZ()) {
    		location1Fixed.setZ(location2.getZ());
    		location2Fixed.setZ(location1.getZ());
    	}
    	
    	return new Location[] { location1Fixed, location2Fixed };
    }
}

package com.fembuncollective.recreatorlib.hopper;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages hopper handlers and custom hopper interactions.
 * Register your custom handlers here to control how hoppers interact with your blocks.
 * 
 * @author MissAnilka
 */
public class HopperManager {

    private static HopperManager instance;

    // Handlers registered by material type
    private final Map<Material, List<RegisteredHandler>> materialHandlers = new ConcurrentHashMap<>();
    
    // Handlers registered by specific location
    private final Map<Location, RegisteredHandler> locationHandlers = new ConcurrentHashMap<>();
    
    // Global handlers that check every block
    private final List<RegisteredHandler> globalHandlers = Collections.synchronizedList(new ArrayList<>());

    // Hopper settings per location
    private final Map<Location, HopperSettings> hopperSettings = new ConcurrentHashMap<>();

    private HopperManager() {}

    /**
     * Gets the HopperManager instance.
     * 
     * @return The singleton instance
     */
    public static HopperManager getInstance() {
        if (instance == null) {
            instance = new HopperManager();
        }
        return instance;
    }

    /**
     * Registers a handler for a specific material type.
     * 
     * @param plugin The plugin registering the handler
     * @param material The material to handle
     * @param handler The handler implementation
     */
    public void registerHandler(@NotNull Plugin plugin, @NotNull Material material, @NotNull HopperHandler handler) {
        materialHandlers.computeIfAbsent(material, k -> Collections.synchronizedList(new ArrayList<>()))
                .add(new RegisteredHandler(plugin, handler));
    }

    /**
     * Registers a handler for a specific block location.
     * 
     * @param plugin The plugin registering the handler
     * @param location The exact block location
     * @param handler The handler implementation
     */
    public void registerHandler(@NotNull Plugin plugin, @NotNull Location location, @NotNull HopperHandler handler) {
        locationHandlers.put(location.toBlockLocation(), new RegisteredHandler(plugin, handler));
    }

    /**
     * Registers a global handler that can handle any block.
     * 
     * @param plugin The plugin registering the handler
     * @param handler The handler implementation
     */
    public void registerGlobalHandler(@NotNull Plugin plugin, @NotNull HopperHandler handler) {
        globalHandlers.add(new RegisteredHandler(plugin, handler));
    }

    /**
     * Unregisters all handlers from a specific plugin.
     * 
     * @param plugin The plugin to unregister
     */
    public void unregisterAll(@NotNull Plugin plugin) {
        // Remove from material handlers
        materialHandlers.values().forEach(list -> 
            list.removeIf(rh -> rh.plugin().equals(plugin)));
        
        // Remove from location handlers
        locationHandlers.entrySet().removeIf(entry -> 
            entry.getValue().plugin().equals(plugin));
        
        // Remove from global handlers
        globalHandlers.removeIf(rh -> rh.plugin().equals(plugin));
    }

    /**
     * Unregisters a handler for a specific location.
     * 
     * @param location The location to unregister
     */
    public void unregisterLocation(@NotNull Location location) {
        locationHandlers.remove(location.toBlockLocation());
    }

    /**
     * Gets the appropriate handler for a block.
     * Priority: Location > Material > Global
     * 
     * @param block The block to get a handler for
     * @return The handler, or null if no custom handler exists
     */
    @Nullable
    public HopperHandler getHandler(@NotNull Block block) {
        Location loc = block.getLocation().toBlockLocation();
        
        // Check location-specific handlers first
        RegisteredHandler locationHandler = locationHandlers.get(loc);
        if (locationHandler != null && locationHandler.handler().canHandle(block)) {
            return locationHandler.handler();
        }
        
        // Check material-specific handlers
        List<RegisteredHandler> matHandlers = materialHandlers.get(block.getType());
        if (matHandlers != null) {
            for (RegisteredHandler rh : matHandlers) {
                if (rh.handler().canHandle(block)) {
                    return rh.handler();
                }
            }
        }
        
        // Check global handlers
        for (RegisteredHandler rh : globalHandlers) {
            if (rh.handler().canHandle(block)) {
                return rh.handler();
            }
        }
        
        return null;
    }

    /**
     * Checks if a block has a custom handler.
     * 
     * @param block The block to check
     * @return true if a custom handler exists
     */
    public boolean hasHandler(@NotNull Block block) {
        return getHandler(block) != null;
    }

    /**
     * Sets custom settings for a hopper at a location.
     * 
     * @param location The hopper location
     * @param settings The hopper settings
     */
    public void setHopperSettings(@NotNull Location location, @NotNull HopperSettings settings) {
        hopperSettings.put(location.toBlockLocation(), settings);
    }

    /**
     * Gets the settings for a hopper at a location.
     * 
     * @param location The hopper location
     * @return The settings, or default settings if none set
     */
    @NotNull
    public HopperSettings getHopperSettings(@NotNull Location location) {
        return hopperSettings.getOrDefault(location.toBlockLocation(), HopperSettings.DEFAULT);
    }

    /**
     * Removes hopper settings for a location.
     * 
     * @param location The location to remove settings for
     */
    public void removeHopperSettings(@NotNull Location location) {
        hopperSettings.remove(location.toBlockLocation());
    }

    /**
     * Clears all handlers and settings. Used for cleanup.
     */
    public void clear() {
        materialHandlers.clear();
        locationHandlers.clear();
        globalHandlers.clear();
        hopperSettings.clear();
    }

    /**
     * Record for tracking which plugin registered a handler.
     */
    private record RegisteredHandler(Plugin plugin, HopperHandler handler) {}
}

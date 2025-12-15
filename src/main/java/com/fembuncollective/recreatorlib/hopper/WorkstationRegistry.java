package com.fembuncollective.recreatorlib.hopper;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry for custom workstations and their inventory providers.
 * This allows plugins to register their custom blocks and define how hoppers interact with them.
 * 
 * @author MissAnilka
 */
public class WorkstationRegistry {

    private static WorkstationRegistry instance;

    // Workstation configurations by ID
    private final Map<String, WorkstationConfig> configsById = new ConcurrentHashMap<>();
    
    // Workstation configurations by material (for quick lookup)
    private final Map<Material, List<WorkstationConfig>> configsByMaterial = new ConcurrentHashMap<>();
    
    // Inventory providers for custom workstations (by location)
    private final Map<Location, InventoryProvider> locationProviders = new ConcurrentHashMap<>();
    
    // Inventory providers by workstation ID
    private final Map<String, InventoryProvider> idProviders = new ConcurrentHashMap<>();
    
    // Plugin tracking for cleanup
    private final Map<String, Plugin> configOwners = new ConcurrentHashMap<>();

    private WorkstationRegistry() {}

    public static WorkstationRegistry getInstance() {
        if (instance == null) {
            instance = new WorkstationRegistry();
        }
        return instance;
    }

    /**
     * Registers a workstation configuration.
     * 
     * @param plugin The plugin registering the config
     * @param config The workstation configuration
     */
    public void registerWorkstation(@NotNull Plugin plugin, @NotNull WorkstationConfig config) {
        configsById.put(config.getId(), config);
        configOwners.put(config.getId(), plugin);
        
        for (Material material : config.getMaterials()) {
            configsByMaterial.computeIfAbsent(material, k -> Collections.synchronizedList(new ArrayList<>()))
                    .add(config);
        }
        
        plugin.getLogger().info("Registered workstation: " + config.getId());
    }

    /**
     * Registers an inventory provider for a workstation type.
     * This is called when your plugin needs to provide custom inventories.
     * 
     * @param workstationId The workstation ID
     * @param provider Function that returns the inventory for a block
     */
    public void registerInventoryProvider(@NotNull String workstationId, @NotNull InventoryProvider provider) {
        idProviders.put(workstationId, provider);
    }

    /**
     * Registers an inventory provider for a specific location.
     * Use this for blocks that have unique inventories.
     * 
     * @param location The block location
     * @param provider The inventory provider
     */
    public void registerLocationProvider(@NotNull Location location, @NotNull InventoryProvider provider) {
        locationProviders.put(location.toBlockLocation(), provider);
    }

    /**
     * Unregisters a location provider.
     * Call this when the custom block is removed.
     * 
     * @param location The block location
     */
    public void unregisterLocation(@NotNull Location location) {
        locationProviders.remove(location.toBlockLocation());
    }

    /**
     * Gets the workstation config for a block.
     * 
     * @param block The block to check
     * @return The config, or null if not a registered workstation
     */
    @Nullable
    public WorkstationConfig getConfig(@NotNull Block block) {
        List<WorkstationConfig> configs = configsByMaterial.get(block.getType());
        if (configs == null || configs.isEmpty()) return null;
        
        // Return first enabled config
        return configs.stream()
                .filter(WorkstationConfig::isEnabled)
                .findFirst()
                .orElse(null);
    }

    /**
     * Gets a workstation config by ID.
     */
    @Nullable
    public WorkstationConfig getConfig(@NotNull String id) {
        return configsById.get(id);
    }

    /**
     * Gets the inventory for a block.
     * 
     * @param block The block
     * @param config The workstation config (optional, for type-based lookup)
     * @return The inventory, or null if not found
     */
    @Nullable
    public Inventory getInventory(@NotNull Block block, @Nullable WorkstationConfig config) {
        Location loc = block.getLocation().toBlockLocation();
        
        // Check location-specific provider first
        InventoryProvider locationProvider = locationProviders.get(loc);
        if (locationProvider != null) {
            return locationProvider.getInventory(block);
        }
        
        // Check ID-based provider
        if (config != null) {
            InventoryProvider idProvider = idProviders.get(config.getId());
            if (idProvider != null) {
                return idProvider.getInventory(block);
            }
        }
        
        // Fall back to vanilla block inventory
        if (block.getState() instanceof org.bukkit.inventory.InventoryHolder holder) {
            return holder.getInventory();
        }
        
        return null;
    }

    /**
     * Check if a block is a registered workstation.
     */
    public boolean isWorkstation(@NotNull Block block) {
        return getConfig(block) != null;
    }

    /**
     * Check if a block has a custom inventory provider.
     */
    public boolean hasCustomProvider(@NotNull Block block) {
        Location loc = block.getLocation().toBlockLocation();
        if (locationProviders.containsKey(loc)) return true;
        
        WorkstationConfig config = getConfig(block);
        return config != null && idProviders.containsKey(config.getId());
    }

    /**
     * Unregisters all configs from a plugin.
     */
    public void unregisterAll(@NotNull Plugin plugin) {
        List<String> toRemove = new ArrayList<>();
        configOwners.forEach((id, owner) -> {
            if (owner.equals(plugin)) {
                toRemove.add(id);
            }
        });
        
        for (String id : toRemove) {
            WorkstationConfig config = configsById.remove(id);
            configOwners.remove(id);
            idProviders.remove(id);
            
            if (config != null) {
                for (Material material : config.getMaterials()) {
                    List<WorkstationConfig> list = configsByMaterial.get(material);
                    if (list != null) {
                        list.remove(config);
                    }
                }
            }
        }
    }

    /**
     * Clears all registrations.
     */
    public void clear() {
        configsById.clear();
        configsByMaterial.clear();
        locationProviders.clear();
        idProviders.clear();
        configOwners.clear();
    }

    /**
     * Gets all registered workstation IDs.
     */
    public Set<String> getRegisteredIds() {
        return Collections.unmodifiableSet(configsById.keySet());
    }

    /**
     * Functional interface for providing inventories.
     */
    @FunctionalInterface
    public interface InventoryProvider {
        /**
         * Gets the inventory for a block.
         * 
         * @param block The block
         * @return The inventory, or null if not available
         */
        @Nullable
        Inventory getInventory(@NotNull Block block);
    }
}

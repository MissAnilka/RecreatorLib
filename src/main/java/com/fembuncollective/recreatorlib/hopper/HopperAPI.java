package com.fembuncollective.recreatorlib.hopper;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Main API for hopper integration with custom workstations.
 * Use this class to register your custom blocks and inventories.
 * 
 * <p>Supports two types of workstations:</p>
 * <ul>
 *   <li><b>Simple workstations</b> - Basic slot configuration</li>
 *   <li><b>Custom workstations</b> - Recipe groups with fuel restrictions</li>
 * </ul>
 * 
 * <p>Example for custom furnace with recipe groups:</p>
 * <pre>{@code
 * HopperAPI api = RecreatorLib.getInstance().getHopperAPI();
 * 
 * // Register custom furnace with recipe groups
 * api.registerCustomWorkstation(this, CustomWorkstationConfig.builder("my_furnace")
 *     .material(Material.FURNACE)
 *     .inventorySize(27)
 *     .inputSlot(10)   // Configurable slot
 *     .fuelSlot(12)    // Configurable slot  
 *     .outputSlot(16)  // Configurable slot
 *     .enableVanillaRecipes(true)   // Allow normal smelting
 *     .enableVanillaFuel(true)      // Allow coal, wood, etc.
 *     .addGeneralFuel(Material.BLAZE_ROD)  // Custom fuel
 *     .addRecipeGroup(RecipeGroup.builder("soul_conversion")
 *         .input(Material.SOUL_SAND, Material.SOUL_SOIL)
 *         .fuel(Material.MAGMA_BLOCK)  // Only magma works for this recipe
 *         .build())
 *     .build());
 * }</pre>
 * 
 * @author MissAnilka
 */
public class HopperAPI {

    private final HopperManager hopperManager;
    private final WorkstationRegistry workstationRegistry;
    private final CustomWorkstationRegistry customWorkstationRegistry;

    public HopperAPI() {
        this.hopperManager = HopperManager.getInstance();
        this.workstationRegistry = WorkstationRegistry.getInstance();
        this.customWorkstationRegistry = CustomWorkstationRegistry.getInstance();
    }

    // ==================== CUSTOM WORKSTATIONS (with recipe groups) ====================

    /**
     * Registers a custom workstation with recipe group support.
     * Use this for workstations with complex recipe validation (like your custom furnaces).
     * 
     * <p>The hopper is PERMISSIVE - it allows items that COULD be valid for any recipe.
     * Your workstation plugin handles showing invalid indicators.</p>
     * 
     * @param plugin Your plugin instance
     * @param config The custom workstation configuration
     */
    public void registerCustomWorkstation(@NotNull Plugin plugin, @NotNull CustomWorkstationConfig config) {
        customWorkstationRegistry.register(plugin, config);
    }

    /**
     * Registers an inventory provider for a custom workstation.
     * 
     * @param workstationId The ID used when creating the config
     * @param provider Function that returns the inventory for a block
     */
    public void registerCustomInventoryProvider(@NotNull String workstationId,
                                                 @NotNull CustomWorkstationRegistry.InventoryProvider provider) {
        customWorkstationRegistry.registerProvider(workstationId, provider);
    }

    /**
     * Registers an inventory provider for a specific location (custom workstations).
     * 
     * @param location The block location
     * @param provider The inventory provider
     */
    public void registerCustomLocationInventory(@NotNull Location location,
                                                 @NotNull CustomWorkstationRegistry.InventoryProvider provider) {
        customWorkstationRegistry.registerProvider(location, provider);
    }

    /**
     * Unregisters a custom workstation location provider.
     * Call this when your custom block is broken.
     * 
     * @param location The block location
     */
    public void unregisterCustomLocation(@NotNull Location location) {
        customWorkstationRegistry.unregisterLocation(location);
    }

    /**
     * Gets the custom workstation config for a block.
     * 
     * @param block The block
     * @return The config, or null if not a custom workstation
     */
    @Nullable
    public CustomWorkstationConfig getCustomWorkstationConfig(@NotNull Block block) {
        return customWorkstationRegistry.getConfig(block);
    }

    /**
     * Gets the custom workstation config by ID.
     * 
     * @param id The workstation ID
     * @return The config, or null if not found
     */
    @Nullable
    public CustomWorkstationConfig getCustomWorkstationConfig(@NotNull String id) {
        return customWorkstationRegistry.getConfig(id);
    }

    // ==================== SIMPLE WORKSTATIONS ====================

    /**
     * Registers a simple workstation (basic slot configuration).
     * Use this for workstations without complex recipe validation.
     * 
     * @param plugin Your plugin instance
     * @param config The workstation configuration
     */
    public void registerWorkstation(@NotNull Plugin plugin, @NotNull WorkstationConfig config) {
        workstationRegistry.registerWorkstation(plugin, config);
    }

    /**
     * Registers an inventory provider for a simple workstation type.
     * 
     * @param workstationId The ID used when creating the WorkstationConfig
     * @param provider Function that returns the inventory for a block
     */
    public void registerInventoryProvider(@NotNull String workstationId, 
                                          @NotNull WorkstationRegistry.InventoryProvider provider) {
        workstationRegistry.registerInventoryProvider(workstationId, provider);
    }

    /**
     * Registers an inventory provider for a specific block location.
     * 
     * @param location The block location
     * @param provider The inventory provider
     */
    public void registerLocationInventory(@NotNull Location location, 
                                          @NotNull WorkstationRegistry.InventoryProvider provider) {
        workstationRegistry.registerLocationProvider(location, provider);
    }

    /**
     * Unregisters a location-specific inventory provider.
     * Call this when your custom block is broken/removed.
     * 
     * @param location The block location
     */
    public void unregisterLocation(@NotNull Location location) {
        workstationRegistry.unregisterLocation(location);
        customWorkstationRegistry.unregisterLocation(location);
    }

    // ==================== CUSTOM HANDLERS ====================

    /**
     * Registers a fully custom hopper handler for a material.
     * Use this for complete control over hopper behavior.
     * 
     * @param plugin Your plugin
     * @param material The material to handle
     * @param handler Your custom handler
     */
    public void registerHandler(@NotNull Plugin plugin, @NotNull Material material, 
                                @NotNull HopperHandler handler) {
        hopperManager.registerHandler(plugin, material, handler);
    }

    /**
     * Registers a custom hopper handler for a specific location.
     * 
     * @param plugin Your plugin
     * @param location The block location
     * @param handler Your custom handler
     */
    public void registerHandler(@NotNull Plugin plugin, @NotNull Location location, 
                                @NotNull HopperHandler handler) {
        hopperManager.registerHandler(plugin, location, handler);
    }

    // ==================== HOPPER SETTINGS ====================

    /**
     * Sets custom settings for a specific hopper.
     * 
     * @param hopperLocation The hopper's location
     * @param settings The hopper settings
     */
    public void setHopperSettings(@NotNull Location hopperLocation, @NotNull HopperSettings settings) {
        hopperManager.setHopperSettings(hopperLocation, settings);
    }

    /**
     * Gets the settings for a hopper.
     * 
     * @param hopperLocation The hopper's location
     * @return The settings (default if none set)
     */
    @NotNull
    public HopperSettings getHopperSettings(@NotNull Location hopperLocation) {
        return hopperManager.getHopperSettings(hopperLocation);
    }

    /**
     * Removes custom settings for a hopper.
     * 
     * @param hopperLocation The hopper's location
     */
    public void removeHopperSettings(@NotNull Location hopperLocation) {
        hopperManager.removeHopperSettings(hopperLocation);
    }

    // ==================== QUERIES ====================

    /**
     * Checks if a block is a registered workstation (simple or custom).
     * 
     * @param block The block to check
     * @return true if it's a registered workstation
     */
    public boolean isWorkstation(@NotNull Block block) {
        return workstationRegistry.isWorkstation(block) || 
               customWorkstationRegistry.isCustomWorkstation(block);
    }

    /**
     * Checks if a block is a custom workstation (with recipe groups).
     * 
     * @param block The block
     * @return true if custom workstation
     */
    public boolean isCustomWorkstation(@NotNull Block block) {
        return customWorkstationRegistry.isCustomWorkstation(block);
    }

    /**
     * Gets the simple workstation config for a block.
     * 
     * @param block The block
     * @return The config, or null if not a workstation
     */
    @Nullable
    public WorkstationConfig getWorkstationConfig(@NotNull Block block) {
        return workstationRegistry.getConfig(block);
    }

    /**
     * Gets the simple workstation config by ID.
     * 
     * @param id The workstation ID
     * @return The config, or null if not found
     */
    @Nullable
    public WorkstationConfig getWorkstationConfig(@NotNull String id) {
        return workstationRegistry.getConfig(id);
    }

    /**
     * Gets the inventory for a workstation block.
     * 
     * @param block The block
     * @return The inventory, or null if not found
     */
    @Nullable
    public Inventory getWorkstationInventory(@NotNull Block block) {
        // Check custom workstation first
        CustomWorkstationConfig customConfig = customWorkstationRegistry.getConfig(block);
        if (customConfig != null) {
            return customWorkstationRegistry.getInventory(block, customConfig);
        }
        
        // Check simple workstation
        WorkstationConfig config = workstationRegistry.getConfig(block);
        return workstationRegistry.getInventory(block, config);
    }

    // ==================== CLEANUP ====================

    /**
     * Unregisters all handlers and workstations from a plugin.
     * Call this in your plugin's onDisable.
     * 
     * @param plugin The plugin to unregister
     */
    public void unregisterAll(@NotNull Plugin plugin) {
        hopperManager.unregisterAll(plugin);
        workstationRegistry.unregisterAll(plugin);
        customWorkstationRegistry.unregisterAll(plugin);
    }

    // ==================== INTERNAL ====================

    /**
     * Gets the underlying HopperManager.
     */
    public HopperManager getHopperManager() {
        return hopperManager;
    }

    /**
     * Gets the underlying WorkstationRegistry.
     */
    public WorkstationRegistry getWorkstationRegistry() {
        return workstationRegistry;
    }

    /**
     * Gets the underlying CustomWorkstationRegistry.
     */
    public CustomWorkstationRegistry getCustomWorkstationRegistry() {
        return customWorkstationRegistry;
    }
}

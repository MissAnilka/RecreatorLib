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
 * Registry for custom workstations with complex recipe support.
 * 
 * @author MissAnilka
 */
public class CustomWorkstationRegistry {

    private static CustomWorkstationRegistry instance;

    private final Map<String, CustomWorkstationConfig> configsById = new ConcurrentHashMap<>();
    private final Map<Material, List<CustomWorkstationConfig>> configsByMaterial = new ConcurrentHashMap<>();
    private final Map<Location, InventoryProvider> locationProviders = new ConcurrentHashMap<>();
    private final Map<String, InventoryProvider> idProviders = new ConcurrentHashMap<>();
    private final Map<String, Plugin> configOwners = new ConcurrentHashMap<>();

    private CustomWorkstationRegistry() {}

    public static CustomWorkstationRegistry getInstance() {
        if (instance == null) instance = new CustomWorkstationRegistry();
        return instance;
    }

    public void register(@NotNull Plugin plugin, @NotNull CustomWorkstationConfig config) {
        configsById.put(config.getId(), config);
        configOwners.put(config.getId(), plugin);
        for (Material material : config.getMaterials()) {
            configsByMaterial.computeIfAbsent(material, k -> Collections.synchronizedList(new ArrayList<>())).add(config);
        }
        plugin.getLogger().info("Registered custom workstation: " + config.getId());
    }

    public void registerProvider(@NotNull String workstationId, @NotNull InventoryProvider provider) {
        idProviders.put(workstationId, provider);
    }

    public void registerProvider(@NotNull Location location, @NotNull InventoryProvider provider) {
        locationProviders.put(location.toBlockLocation(), provider);
    }

    public void unregisterLocation(@NotNull Location location) {
        locationProviders.remove(location.toBlockLocation());
    }

    @Nullable
    public CustomWorkstationConfig getConfig(@NotNull Block block) {
        List<CustomWorkstationConfig> configs = configsByMaterial.get(block.getType());
        if (configs == null || configs.isEmpty()) return null;
        return configs.stream().filter(CustomWorkstationConfig::isEnabled).findFirst().orElse(null);
    }

    @Nullable
    public CustomWorkstationConfig getConfig(@NotNull String id) {
        return configsById.get(id);
    }

    @Nullable
    public Inventory getInventory(@NotNull Block block, @Nullable CustomWorkstationConfig config) {
        Location loc = block.getLocation().toBlockLocation();
        InventoryProvider locationProvider = locationProviders.get(loc);
        if (locationProvider != null) return locationProvider.getInventory(block);
        if (config != null) {
            InventoryProvider idProvider = idProviders.get(config.getId());
            if (idProvider != null) return idProvider.getInventory(block);
        }
        if (block.getState() instanceof org.bukkit.inventory.InventoryHolder holder) return holder.getInventory();
        return null;
    }

    public boolean isCustomWorkstation(@NotNull Block block) {
        return getConfig(block) != null;
    }

    public void unregisterAll(@NotNull Plugin plugin) {
        List<String> toRemove = new ArrayList<>();
        configOwners.forEach((id, owner) -> { if (owner.equals(plugin)) toRemove.add(id); });
        for (String id : toRemove) {
            CustomWorkstationConfig config = configsById.remove(id);
            configOwners.remove(id);
            idProviders.remove(id);
            if (config != null) {
                for (Material material : config.getMaterials()) {
                    List<CustomWorkstationConfig> list = configsByMaterial.get(material);
                    if (list != null) list.remove(config);
                }
            }
        }
    }

    public void clear() {
        configsById.clear();
        configsByMaterial.clear();
        locationProviders.clear();
        idProviders.clear();
        configOwners.clear();
    }

    @FunctionalInterface
    public interface InventoryProvider {
        @Nullable Inventory getInventory(@NotNull Block block);
    }
}

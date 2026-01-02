package com.fembuncollective.recreatorlib.integration;

import com.fembuncollective.recreatorlib.RecreatorLib;
import com.fembuncollective.recreatorlib.integration.floodgate.FloodgateIntegration;
import com.fembuncollective.recreatorlib.integration.luckperms.LuckPermsIntegration;
import com.fembuncollective.recreatorlib.integration.placeholderapi.PlaceholderAPIIntegration;
import org.bukkit.Bukkit;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Manages all plugin integrations for RecreatorLib.
 * Automatically detects and loads integrations for supported plugins.
 * 
 * @author MissAnilka
 */
public class IntegrationManager {

    private static IntegrationManager instance;
    private final Map<String, PluginIntegration> integrations = new HashMap<>();
    private final RecreatorLib plugin;

    private IntegrationManager(RecreatorLib plugin) {
        this.plugin = plugin;
    }

    /**
     * Initializes the IntegrationManager singleton.
     * 
     * @param plugin The RecreatorLib plugin instance
     * @return The IntegrationManager instance
     */
    public static IntegrationManager init(RecreatorLib plugin) {
        if (instance == null) {
            instance = new IntegrationManager(plugin);
        }
        return instance;
    }

    /**
     * Gets the IntegrationManager instance.
     * 
     * @return The IntegrationManager instance, or null if not initialized
     */
    public static IntegrationManager getInstance() {
        return instance;
    }

    /**
     * Loads all available integrations.
     * Checks for plugin presence before attempting to load.
     */
    public void loadIntegrations() {
        // Floodgate Integration (load first so PAPI can use it)
        if (isPluginPresent("floodgate")) {
            loadIntegration("floodgate", new FloodgateIntegration());
        }

        // PlaceholderAPI Integration
        if (isPluginPresent("PlaceholderAPI")) {
            loadIntegration("placeholderapi", new PlaceholderAPIIntegration());
        }

        // LuckPerms Integration
        if (isPluginPresent("LuckPerms")) {
            loadIntegration("luckperms", new LuckPermsIntegration());
        }

        // Log integration status
        if (integrations.isEmpty()) {
            plugin.getLogger().info("No optional integrations loaded.");
        } else {
            plugin.getLogger().info("Loaded " + integrations.size() + " integration(s): " + 
                    String.join(", ", integrations.keySet()));
        }
    }

    /**
     * Loads a specific integration.
     * 
     * @param name The integration name
     * @param integration The integration instance
     */
    private void loadIntegration(String name, PluginIntegration integration) {
        try {
            if (integration.load(plugin)) {
                integrations.put(name.toLowerCase(), integration);
                plugin.getLogger().info("Successfully loaded " + name + " integration!");
            } else {
                plugin.getLogger().warning("Failed to load " + name + " integration.");
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Error loading " + name + " integration: " + e.getMessage());
        }
    }

    /**
     * Checks if a plugin is present on the server.
     * 
     * @param pluginName The plugin name to check
     * @return true if the plugin is present and enabled
     */
    public boolean isPluginPresent(String pluginName) {
        return Bukkit.getPluginManager().getPlugin(pluginName) != null;
    }

    /**
     * Gets an integration by name.
     * 
     * @param name The integration name
     * @return Optional containing the integration if found
     */
    public Optional<PluginIntegration> getIntegration(String name) {
        return Optional.ofNullable(integrations.get(name.toLowerCase()));
    }

    /**
     * Gets the Floodgate integration if available.
     * 
     * @return Optional containing the FloodgateIntegration if loaded
     */
    public Optional<FloodgateIntegration> getFloodgate() {
        return getIntegration("floodgate")
                .filter(i -> i instanceof FloodgateIntegration)
                .map(i -> (FloodgateIntegration) i);
    }

    /**
     * Gets the PlaceholderAPI integration if available.
     * 
     * @return Optional containing the PlaceholderAPIIntegration if loaded
     */
    public Optional<PlaceholderAPIIntegration> getPlaceholderAPI() {
        return getIntegration("placeholderapi")
                .filter(i -> i instanceof PlaceholderAPIIntegration)
                .map(i -> (PlaceholderAPIIntegration) i);
    }

    /**
     * Gets the LuckPerms integration if available.
     * 
     * @return Optional containing the LuckPermsIntegration if loaded
     */
    public Optional<LuckPermsIntegration> getLuckPerms() {
        return getIntegration("luckperms")
                .filter(i -> i instanceof LuckPermsIntegration)
                .map(i -> (LuckPermsIntegration) i);
    }

    /**
     * Checks if a specific integration is loaded.
     * 
     * @param name The integration name
     * @return true if the integration is loaded
     */
    public boolean hasIntegration(String name) {
        return integrations.containsKey(name.toLowerCase());
    }

    /**
     * Unloads all integrations.
     */
    public void unloadAll() {
        for (Map.Entry<String, PluginIntegration> entry : integrations.entrySet()) {
            try {
                entry.getValue().unload();
            } catch (Exception e) {
                plugin.getLogger().warning("Error unloading " + entry.getKey() + " integration: " + e.getMessage());
            }
        }
        integrations.clear();
        instance = null;
    }
}

package com.fembuncollective.recreatorlib.integration.placeholderapi;

import com.fembuncollective.recreatorlib.RecreatorLib;
import com.fembuncollective.recreatorlib.integration.PluginIntegration;
import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Integration with PlaceholderAPI.
 * Provides custom placeholder transformations and the %recreator_*% placeholder namespace.
 * 
 * @author MissAnilka
 * @see <a href="https://placeholderapi.com/">PlaceholderAPI</a>
 */
public class PlaceholderAPIIntegration implements PluginIntegration {

    private RecreatorLib plugin;
    private RecreatorExpansion expansion;
    private CustomPlaceholderManager placeholderManager;
    private boolean active = false;

    @Override
    public String getPluginName() {
        return "PlaceholderAPI";
    }

    @Override
    public boolean load(RecreatorLib plugin) {
        this.plugin = plugin;
        try {
            // Initialize the custom placeholder manager
            placeholderManager = new CustomPlaceholderManager(plugin);
            placeholderManager.loadPlaceholders();
            
            // Register our expansion
            expansion = new RecreatorExpansion(plugin, placeholderManager);
            if (expansion.register()) {
                active = true;
                plugin.getLogger().info("PlaceholderAPI expansion registered: %recreator_<placeholder>%");
                plugin.getLogger().info("Loaded " + placeholderManager.getPlaceholderCount() + " custom placeholder(s)");
                return true;
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to hook into PlaceholderAPI: " + e.getMessage());
        }
        return false;
    }

    @Override
    public void unload() {
        if (expansion != null) {
            expansion.unregister();
        }
        active = false;
    }

    @Override
    public boolean isActive() {
        return active;
    }

    /**
     * Parses placeholders in a string for a player.
     * 
     * @param player The player to parse for
     * @param text The text containing placeholders
     * @return The parsed text
     */
    public String parse(Player player, String text) {
        if (!isActive()) return text;
        return PlaceholderAPI.setPlaceholders(player, text);
    }

    /**
     * Parses placeholders in a string for an offline player.
     * 
     * @param player The offline player to parse for
     * @param text The text containing placeholders
     * @return The parsed text
     */
    public String parse(OfflinePlayer player, String text) {
        if (!isActive()) return text;
        return PlaceholderAPI.setPlaceholders(player, text);
    }

    /**
     * Gets the custom placeholder manager.
     * 
     * @return The CustomPlaceholderManager instance
     */
    public CustomPlaceholderManager getPlaceholderManager() {
        return placeholderManager;
    }

    /**
     * Reloads custom placeholders from config.
     */
    public void reload() {
        if (placeholderManager != null) {
            placeholderManager.loadPlaceholders();
            plugin.getLogger().info("Reloaded " + placeholderManager.getPlaceholderCount() + " custom placeholder(s)");
        }
    }

    /**
     * The PlaceholderAPI expansion for RecreatorLib.
     */
    private static class RecreatorExpansion extends PlaceholderExpansion {

        private final RecreatorLib plugin;
        private final CustomPlaceholderManager manager;

        public RecreatorExpansion(RecreatorLib plugin, CustomPlaceholderManager manager) {
            this.plugin = plugin;
            this.manager = manager;
        }

        @Override
        public @NotNull String getIdentifier() {
            return "recreator";
        }

        @Override
        public @NotNull String getAuthor() {
            return "MissAnilka";
        }

        @Override
        public @NotNull String getVersion() {
            return plugin.getPluginMeta().getVersion();
        }

        @Override
        public boolean persist() {
            return true; // Don't unregister on PAPI reload
        }

        @Override
        public @Nullable String onRequest(OfflinePlayer player, @NotNull String params) {
            // Check if it's a custom placeholder
            String result = manager.getPlaceholder(player, params);
            if (result != null) {
                return result;
            }

            // Built-in placeholders
            return switch (params.toLowerCase()) {
                case "version" -> plugin.getPluginMeta().getVersion();
                case "is_bedrock" -> {
                    if (player instanceof Player p) {
                        yield String.valueOf(com.fembuncollective.recreatorlib.integration.floodgate.FloodgateUtil.isBedrock(p));
                    }
                    yield "false";
                }
                case "platform" -> {
                    if (player instanceof Player p) {
                        yield com.fembuncollective.recreatorlib.integration.floodgate.FloodgateUtil.isBedrock(p) ? "Bedrock" : "Java";
                    }
                    yield "unknown";
                }
                case "device" -> {
                    if (player instanceof Player p) {
                        yield com.fembuncollective.recreatorlib.integration.floodgate.FloodgateUtil.getDeviceOs(p).orElse("unknown");
                    }
                    yield "unknown";
                }
                case "input" -> {
                    if (player instanceof Player p) {
                        yield com.fembuncollective.recreatorlib.integration.floodgate.FloodgateUtil.getInputMode(p).orElse("unknown");
                    }
                    yield "unknown";
                }
                default -> null;
            };
        }
    }
}

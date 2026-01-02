package com.fembuncollective.recreatorlib.integration.floodgate;

import com.fembuncollective.recreatorlib.RecreatorLib;
import com.fembuncollective.recreatorlib.integration.PluginIntegration;
import org.geysermc.floodgate.api.FloodgateApi;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * Integration with GeyserMC's Floodgate plugin.
 * Provides utilities for detecting and handling Bedrock players.
 * 
 * @author MissAnilka
 * @see <a href="https://geysermc.org/">GeyserMC</a>
 */
public class FloodgateIntegration implements PluginIntegration {

    private FloodgateApi floodgateApi;
    private boolean active = false;
    private RecreatorLib plugin;

    @Override
    public String getPluginName() {
        return "floodgate";
    }

    @Override
    public boolean load(RecreatorLib plugin) {
        this.plugin = plugin;
        try {
            floodgateApi = FloodgateApi.getInstance();
            if (floodgateApi != null) {
                active = true;
                plugin.getLogger().info("Floodgate API v" + getFloodgateVersion() + " hooked successfully!");
                return true;
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to hook into Floodgate API: " + e.getMessage());
        }
        return false;
    }

    @Override
    public void unload() {
        floodgateApi = null;
        active = false;
    }

    @Override
    public boolean isActive() {
        return active && floodgateApi != null;
    }

    /**
     * Checks if a player is a Bedrock player (connected via Geyser/Floodgate).
     * 
     * @param player The player to check
     * @return true if the player is on Bedrock Edition
     */
    public boolean isBedrockPlayer(Player player) {
        if (!isActive()) return false;
        return floodgateApi.isFloodgatePlayer(player.getUniqueId());
    }

    /**
     * Checks if a UUID belongs to a Bedrock player.
     * 
     * @param uuid The UUID to check
     * @return true if the UUID belongs to a Bedrock player
     */
    public boolean isBedrockPlayer(UUID uuid) {
        if (!isActive()) return false;
        return floodgateApi.isFloodgatePlayer(uuid);
    }

    /**
     * Gets the Bedrock username for a player.
     * Returns null if the player is not a Bedrock player.
     * 
     * @param player The player to get the username for
     * @return The Bedrock username, or null if not a Bedrock player
     */
    public String getBedrockUsername(Player player) {
        if (!isActive() || !isBedrockPlayer(player)) return null;
        var floodgatePlayer = floodgateApi.getPlayer(player.getUniqueId());
        return floodgatePlayer != null ? floodgatePlayer.getUsername() : null;
    }

    /**
     * Gets the Xbox UID (XUID) for a Bedrock player.
     * 
     * @param player The player to get the XUID for
     * @return The XUID as a string, or null if not a Bedrock player
     */
    public String getXuid(Player player) {
        if (!isActive() || !isBedrockPlayer(player)) return null;
        var floodgatePlayer = floodgateApi.getPlayer(player.getUniqueId());
        return floodgatePlayer != null ? floodgatePlayer.getXuid() : null;
    }

    /**
     * Gets the linked Java UUID for a Bedrock player, if they have linked accounts.
     * 
     * @param player The player to check
     * @return The linked Java UUID, or null if not linked or not a Bedrock player
     */
    public UUID getLinkedJavaUuid(Player player) {
        if (!isActive() || !isBedrockPlayer(player)) return null;
        var floodgatePlayer = floodgateApi.getPlayer(player.getUniqueId());
        return floodgatePlayer != null ? floodgatePlayer.getLinkedPlayer().getJavaUniqueId() : null;
    }

    /**
     * Checks if a Bedrock player has a linked Java account.
     * 
     * @param player The player to check
     * @return true if the player has a linked Java account
     */
    public boolean hasLinkedAccount(Player player) {
        if (!isActive() || !isBedrockPlayer(player)) return false;
        var floodgatePlayer = floodgateApi.getPlayer(player.getUniqueId());
        return floodgatePlayer != null && floodgatePlayer.getLinkedPlayer() != null;
    }

    /**
     * Gets the device OS of a Bedrock player.
     * 
     * @param player The player to check
     * @return The device OS string (e.g., "Android", "iOS", "Windows"), or null
     */
    public String getDeviceOs(Player player) {
        if (!isActive() || !isBedrockPlayer(player)) return null;
        var floodgatePlayer = floodgateApi.getPlayer(player.getUniqueId());
        return floodgatePlayer != null ? floodgatePlayer.getDeviceOs().toString() : null;
    }

    /**
     * Gets the input mode of a Bedrock player (controller, touch, keyboard).
     * 
     * @param player The player to check
     * @return The input mode string, or null if not a Bedrock player
     */
    public String getInputMode(Player player) {
        if (!isActive() || !isBedrockPlayer(player)) return null;
        var floodgatePlayer = floodgateApi.getPlayer(player.getUniqueId());
        return floodgatePlayer != null ? floodgatePlayer.getInputMode().toString() : null;
    }

    /**
     * Gets the UI profile of a Bedrock player (classic or pocket).
     * 
     * @param player The player to check
     * @return The UI profile string, or null if not a Bedrock player
     */
    public String getUiProfile(Player player) {
        if (!isActive() || !isBedrockPlayer(player)) return null;
        var floodgatePlayer = floodgateApi.getPlayer(player.getUniqueId());
        return floodgatePlayer != null ? floodgatePlayer.getUiProfile().toString() : null;
    }

    /**
     * Gets the Floodgate prefix used for Bedrock usernames.
     * 
     * @return The username prefix (typically ".")
     */
    public String getUsernamePrefix() {
        if (!isActive()) return "";
        return floodgateApi.getPlayerPrefix();
    }

    /**
     * Gets the Floodgate API instance for advanced usage.
     * 
     * @return The FloodgateApi instance, or null if not active
     */
    public FloodgateApi getApi() {
        return isActive() ? floodgateApi : null;
    }

    /**
     * Gets the Floodgate plugin version.
     * 
     * @return The version string
     */
    private String getFloodgateVersion() {
        try {
            var floodgatePlugin = plugin.getServer().getPluginManager().getPlugin("floodgate");
            return floodgatePlugin != null ? floodgatePlugin.getDescription().getVersion() : "unknown";
        } catch (Exception e) {
            return "unknown";
        }
    }
}

package com.fembuncollective.recreatorlib.integration.luckperms;

import com.fembuncollective.recreatorlib.integration.IntegrationManager;
import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Static utility class for easy access to LuckPerms functionality.
 * All methods gracefully handle cases where LuckPerms is not available.
 * 
 * @author MissAnilka
 */
public final class LuckPermsUtil {

    private LuckPermsUtil() {
        // Utility class, no instantiation
    }

    /**
     * Gets the LuckPerms integration if available.
     * 
     * @return Optional containing the integration
     */
    public static Optional<LuckPermsIntegration> getIntegration() {
        IntegrationManager manager = IntegrationManager.getInstance();
        if (manager == null) return Optional.empty();
        return manager.getLuckPerms();
    }

    /**
     * Checks if LuckPerms integration is active.
     * 
     * @return true if LuckPerms is available and hooked
     */
    public static boolean isAvailable() {
        return getIntegration().map(LuckPermsIntegration::isActive).orElse(false);
    }

    // ==================== Permission Methods ====================

    /**
     * Checks if a player has a permission using LuckPerms.
     * Falls back to Bukkit permission check if LuckPerms is not available.
     * 
     * @param player The player to check
     * @param permission The permission to check
     * @return true if the player has the permission
     */
    public static boolean hasPermission(Player player, String permission) {
        return getIntegration()
                .map(lp -> lp.hasPermission(player, permission))
                .orElse(player.hasPermission(permission));
    }

    /**
     * Adds a permission to a player.
     * 
     * @param player The player
     * @param permission The permission to add
     * @return CompletableFuture that completes when done
     */
    public static CompletableFuture<Void> addPermission(Player player, String permission) {
        return getIntegration()
                .map(lp -> lp.addPermission(player, permission))
                .orElse(CompletableFuture.completedFuture(null));
    }

    /**
     * Removes a permission from a player.
     * 
     * @param player The player
     * @param permission The permission to remove
     * @return CompletableFuture that completes when done
     */
    public static CompletableFuture<Void> removePermission(Player player, String permission) {
        return getIntegration()
                .map(lp -> lp.removePermission(player, permission))
                .orElse(CompletableFuture.completedFuture(null));
    }

    // ==================== Group Methods ====================

    /**
     * Gets the primary group for a player.
     * 
     * @param player The player
     * @return The primary group name, or "default" if unavailable
     */
    public static String getPrimaryGroup(Player player) {
        return getIntegration()
                .map(lp -> lp.getPrimaryGroup(player))
                .orElse("default");
    }

    /**
     * Gets all groups a player is in.
     * 
     * @param player The player
     * @return Set of group names, empty if unavailable
     */
    public static Set<String> getGroups(Player player) {
        return getIntegration()
                .map(lp -> lp.getGroups(player))
                .orElse(Set.of());
    }

    /**
     * Checks if a player is in a specific group.
     * 
     * @param player The player
     * @param groupName The group name
     * @return true if the player is in the group
     */
    public static boolean isInGroup(Player player, String groupName) {
        return getIntegration()
                .map(lp -> lp.isInGroup(player, groupName))
                .orElse(false);
    }

    /**
     * Adds a player to a group.
     * 
     * @param player The player
     * @param groupName The group name
     * @return CompletableFuture that completes when done
     */
    public static CompletableFuture<Void> addToGroup(Player player, String groupName) {
        return getIntegration()
                .map(lp -> lp.addToGroup(player, groupName))
                .orElse(CompletableFuture.completedFuture(null));
    }

    /**
     * Removes a player from a group.
     * 
     * @param player The player
     * @param groupName The group name
     * @return CompletableFuture that completes when done
     */
    public static CompletableFuture<Void> removeFromGroup(Player player, String groupName) {
        return getIntegration()
                .map(lp -> lp.removeFromGroup(player, groupName))
                .orElse(CompletableFuture.completedFuture(null));
    }

    // ==================== Prefix/Suffix Methods ====================

    /**
     * Gets the prefix for a player.
     * 
     * @param player The player
     * @return The prefix, or empty string if unavailable
     */
    public static String getPrefix(Player player) {
        return getIntegration()
                .map(lp -> lp.getPrefix(player))
                .orElse("");
    }

    /**
     * Gets the suffix for a player.
     * 
     * @param player The player
     * @return The suffix, or empty string if unavailable
     */
    public static String getSuffix(Player player) {
        return getIntegration()
                .map(lp -> lp.getSuffix(player))
                .orElse("");
    }

    // ==================== Meta Methods ====================

    /**
     * Gets a meta value for a player.
     * 
     * @param player The player
     * @param key The meta key
     * @return The meta value, or null if not found
     */
    public static String getMeta(Player player, String key) {
        return getIntegration()
                .map(lp -> lp.getMeta(player, key))
                .orElse(null);
    }

    /**
     * Gets a meta value for a player with a default value.
     * 
     * @param player The player
     * @param key The meta key
     * @param defaultValue The default value if not found
     * @return The meta value, or the default value
     */
    public static String getMeta(Player player, String key, String defaultValue) {
        String value = getMeta(player, key);
        return value != null ? value : defaultValue;
    }

    /**
     * Gets the display name for a group.
     * 
     * @param groupName The group name
     * @return The display name, or the group name if unavailable
     */
    public static String getGroupDisplayName(String groupName) {
        return getIntegration()
                .map(lp -> lp.getGroupDisplayName(groupName))
                .orElse(groupName);
    }

    /**
     * Gets the weight of a group.
     * 
     * @param groupName The group name
     * @return The group weight, or 0 if unavailable
     */
    public static int getGroupWeight(String groupName) {
        return getIntegration()
                .map(lp -> lp.getGroupWeight(groupName))
                .orElse(0);
    }
}

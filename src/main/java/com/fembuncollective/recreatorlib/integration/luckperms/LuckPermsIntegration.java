package com.fembuncollective.recreatorlib.integration.luckperms;

import com.fembuncollective.recreatorlib.RecreatorLib;
import com.fembuncollective.recreatorlib.integration.PluginIntegration;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import net.luckperms.api.node.types.InheritanceNode;
import net.luckperms.api.node.types.PermissionNode;
import net.luckperms.api.node.types.PrefixNode;
import net.luckperms.api.node.types.SuffixNode;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Integration with LuckPerms permission plugin.
 * Provides utilities for permission management, groups, and metadata.
 * 
 * @author MissAnilka
 * @see <a href="https://luckperms.net/">LuckPerms</a>
 */
public class LuckPermsIntegration implements PluginIntegration {

    private LuckPerms luckPermsApi;
    private boolean active = false;
    private RecreatorLib plugin;

    @Override
    public String getPluginName() {
        return "LuckPerms";
    }

    @Override
    public boolean load(RecreatorLib plugin) {
        this.plugin = plugin;
        try {
            luckPermsApi = LuckPermsProvider.get();
            if (luckPermsApi != null) {
                active = true;
                plugin.getLogger().info("LuckPerms API hooked successfully!");
                return true;
            }
        } catch (IllegalStateException e) {
            plugin.getLogger().warning("LuckPerms API is not loaded yet: " + e.getMessage());
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to hook into LuckPerms API: " + e.getMessage());
        }
        return false;
    }

    @Override
    public void unload() {
        luckPermsApi = null;
        active = false;
    }

    @Override
    public boolean isActive() {
        return active && luckPermsApi != null;
    }

    /**
     * Gets the LuckPerms API instance.
     * 
     * @return The LuckPerms API, or null if not active
     */
    public LuckPerms getApi() {
        return isActive() ? luckPermsApi : null;
    }

    // ==================== User Methods ====================

    /**
     * Gets the LuckPerms User for a player.
     * 
     * @param player The player to get the user for
     * @return The User object, or null if not found
     */
    public User getUser(Player player) {
        if (!isActive()) return null;
        return luckPermsApi.getUserManager().getUser(player.getUniqueId());
    }

    /**
     * Gets the LuckPerms User for a UUID.
     * 
     * @param uuid The UUID to get the user for
     * @return The User object, or null if not found
     */
    public User getUser(UUID uuid) {
        if (!isActive()) return null;
        return luckPermsApi.getUserManager().getUser(uuid);
    }

    /**
     * Loads a user from the database asynchronously.
     * 
     * @param uuid The UUID of the user to load
     * @return CompletableFuture containing the User
     */
    public CompletableFuture<User> loadUser(UUID uuid) {
        if (!isActive()) return CompletableFuture.completedFuture(null);
        return luckPermsApi.getUserManager().loadUser(uuid);
    }

    // ==================== Permission Methods ====================

    /**
     * Checks if a player has a specific permission.
     * Uses LuckPerms' permission checking for accurate results.
     * 
     * @param player The player to check
     * @param permission The permission node to check
     * @return true if the player has the permission
     */
    public boolean hasPermission(Player player, String permission) {
        if (!isActive()) return player.hasPermission(permission);
        User user = getUser(player);
        if (user == null) return player.hasPermission(permission);
        return user.getCachedData().getPermissionData().checkPermission(permission).asBoolean();
    }

    /**
     * Adds a permission to a player.
     * 
     * @param player The player to add the permission to
     * @param permission The permission node to add
     * @return CompletableFuture that completes when the operation is done
     */
    public CompletableFuture<Void> addPermission(Player player, String permission) {
        if (!isActive()) return CompletableFuture.completedFuture(null);
        User user = getUser(player);
        if (user == null) return CompletableFuture.completedFuture(null);
        
        user.data().add(PermissionNode.builder(permission).build());
        return luckPermsApi.getUserManager().saveUser(user);
    }

    /**
     * Removes a permission from a player.
     * 
     * @param player The player to remove the permission from
     * @param permission The permission node to remove
     * @return CompletableFuture that completes when the operation is done
     */
    public CompletableFuture<Void> removePermission(Player player, String permission) {
        if (!isActive()) return CompletableFuture.completedFuture(null);
        User user = getUser(player);
        if (user == null) return CompletableFuture.completedFuture(null);
        
        user.data().remove(PermissionNode.builder(permission).build());
        return luckPermsApi.getUserManager().saveUser(user);
    }

    // ==================== Group Methods ====================

    /**
     * Gets the primary group name for a player.
     * 
     * @param player The player to get the group for
     * @return The primary group name, or "default" if not found
     */
    public String getPrimaryGroup(Player player) {
        if (!isActive()) return "default";
        User user = getUser(player);
        return user != null ? user.getPrimaryGroup() : "default";
    }

    /**
     * Gets all groups a player is a member of.
     * 
     * @param player The player to get groups for
     * @return Set of group names the player is in
     */
    public Set<String> getGroups(Player player) {
        if (!isActive()) return Set.of();
        User user = getUser(player);
        if (user == null) return Set.of();
        
        return user.getNodes().stream()
                .filter(node -> node instanceof InheritanceNode)
                .map(node -> ((InheritanceNode) node).getGroupName())
                .collect(Collectors.toSet());
    }

    /**
     * Checks if a player is in a specific group.
     * 
     * @param player The player to check
     * @param groupName The group name to check
     * @return true if the player is in the group
     */
    public boolean isInGroup(Player player, String groupName) {
        if (!isActive()) return false;
        return getGroups(player).contains(groupName.toLowerCase());
    }

    /**
     * Adds a player to a group.
     * 
     * @param player The player to add
     * @param groupName The group to add the player to
     * @return CompletableFuture that completes when the operation is done
     */
    public CompletableFuture<Void> addToGroup(Player player, String groupName) {
        if (!isActive()) return CompletableFuture.completedFuture(null);
        User user = getUser(player);
        if (user == null) return CompletableFuture.completedFuture(null);
        
        InheritanceNode node = InheritanceNode.builder(groupName).build();
        user.data().add(node);
        return luckPermsApi.getUserManager().saveUser(user);
    }

    /**
     * Removes a player from a group.
     * 
     * @param player The player to remove
     * @param groupName The group to remove the player from
     * @return CompletableFuture that completes when the operation is done
     */
    public CompletableFuture<Void> removeFromGroup(Player player, String groupName) {
        if (!isActive()) return CompletableFuture.completedFuture(null);
        User user = getUser(player);
        if (user == null) return CompletableFuture.completedFuture(null);
        
        InheritanceNode node = InheritanceNode.builder(groupName).build();
        user.data().remove(node);
        return luckPermsApi.getUserManager().saveUser(user);
    }

    /**
     * Gets a group by name.
     * 
     * @param groupName The name of the group
     * @return Optional containing the Group if found
     */
    public Optional<Group> getGroup(String groupName) {
        if (!isActive()) return Optional.empty();
        return Optional.ofNullable(luckPermsApi.getGroupManager().getGroup(groupName));
    }

    /**
     * Gets all loaded groups.
     * 
     * @return Collection of all loaded groups
     */
    public Collection<Group> getLoadedGroups() {
        if (!isActive()) return Set.of();
        return luckPermsApi.getGroupManager().getLoadedGroups();
    }

    // ==================== Prefix/Suffix Methods ====================

    /**
     * Gets the prefix for a player.
     * 
     * @param player The player to get the prefix for
     * @return The player's prefix, or empty string if none
     */
    public String getPrefix(Player player) {
        if (!isActive()) return "";
        User user = getUser(player);
        if (user == null) return "";
        String prefix = user.getCachedData().getMetaData().getPrefix();
        return prefix != null ? prefix : "";
    }

    /**
     * Gets the suffix for a player.
     * 
     * @param player The player to get the suffix for
     * @return The player's suffix, or empty string if none
     */
    public String getSuffix(Player player) {
        if (!isActive()) return "";
        User user = getUser(player);
        if (user == null) return "";
        String suffix = user.getCachedData().getMetaData().getSuffix();
        return suffix != null ? suffix : "";
    }

    /**
     * Sets a prefix for a player.
     * 
     * @param player The player to set the prefix for
     * @param prefix The prefix to set
     * @param priority The priority of the prefix
     * @return CompletableFuture that completes when the operation is done
     */
    public CompletableFuture<Void> setPrefix(Player player, String prefix, int priority) {
        if (!isActive()) return CompletableFuture.completedFuture(null);
        User user = getUser(player);
        if (user == null) return CompletableFuture.completedFuture(null);
        
        PrefixNode node = PrefixNode.builder(prefix, priority).build();
        user.data().add(node);
        return luckPermsApi.getUserManager().saveUser(user);
    }

    /**
     * Sets a suffix for a player.
     * 
     * @param player The player to set the suffix for
     * @param suffix The suffix to set
     * @param priority The priority of the suffix
     * @return CompletableFuture that completes when the operation is done
     */
    public CompletableFuture<Void> setSuffix(Player player, String suffix, int priority) {
        if (!isActive()) return CompletableFuture.completedFuture(null);
        User user = getUser(player);
        if (user == null) return CompletableFuture.completedFuture(null);
        
        SuffixNode node = SuffixNode.builder(suffix, priority).build();
        user.data().add(node);
        return luckPermsApi.getUserManager().saveUser(user);
    }

    // ==================== Meta Methods ====================

    /**
     * Gets a meta value for a player.
     * 
     * @param player The player to get meta for
     * @param key The meta key
     * @return The meta value, or null if not found
     */
    public String getMeta(Player player, String key) {
        if (!isActive()) return null;
        User user = getUser(player);
        if (user == null) return null;
        return user.getCachedData().getMetaData().getMetaValue(key);
    }

    /**
     * Gets the display name for a group.
     * 
     * @param groupName The group name
     * @return The display name, or the group name if no display name is set
     */
    public String getGroupDisplayName(String groupName) {
        if (!isActive()) return groupName;
        Group group = luckPermsApi.getGroupManager().getGroup(groupName);
        if (group == null) return groupName;
        String displayName = group.getDisplayName();
        return displayName != null ? displayName : groupName;
    }

    /**
     * Gets the weight of a group.
     * 
     * @param groupName The group name
     * @return The group weight, or 0 if not found
     */
    public int getGroupWeight(String groupName) {
        if (!isActive()) return 0;
        Group group = luckPermsApi.getGroupManager().getGroup(groupName);
        if (group == null) return 0;
        return group.getWeight().orElse(0);
    }
}

# LuckPerms Integration

RecreatorLib provides seamless integration with [LuckPerms](https://luckperms.net/), the most popular permission plugin for Minecraft servers.

## Features

- Permission checking and management
- Group membership operations
- Prefix and suffix retrieval
- Meta data access
- Asynchronous operations support

## Requirements

- LuckPerms 5.4 or higher installed on the server
- RecreatorLib automatically detects and hooks into LuckPerms

## Quick Start

### Using the Utility Class (Recommended)

The `LuckPermsUtil` class provides static methods for easy access:

```java
import com.fembuncollective.recreatorlib.integration.luckperms.LuckPermsUtil;

// Check if LuckPerms is available
if (LuckPermsUtil.isAvailable()) {
    // Get player's primary group
    String group = LuckPermsUtil.getPrimaryGroup(player);
    
    // Get prefix/suffix
    String prefix = LuckPermsUtil.getPrefix(player);
    String suffix = LuckPermsUtil.getSuffix(player);
    
    // Check group membership
    if (LuckPermsUtil.isInGroup(player, "vip")) {
        // Player is a VIP
    }
}
```

### Using the Integration Directly

For more advanced usage, access the integration directly:

```java
import com.fembuncollective.recreatorlib.integration.IntegrationManager;
import com.fembuncollective.recreatorlib.integration.luckperms.LuckPermsIntegration;

IntegrationManager.getInstance().getLuckPerms().ifPresent(luckperms -> {
    // Access the LuckPerms API directly
    LuckPerms api = luckperms.getApi();
    
    // Get user object
    User user = luckperms.getUser(player);
});
```

## API Reference

### Permission Methods

| Method | Description |
|--------|-------------|
| `hasPermission(Player, String)` | Check if player has a permission |
| `addPermission(Player, String)` | Add a permission to a player (async) |
| `removePermission(Player, String)` | Remove a permission from a player (async) |

### Group Methods

| Method | Description |
|--------|-------------|
| `getPrimaryGroup(Player)` | Get the player's primary group name |
| `getGroups(Player)` | Get all groups the player is in |
| `isInGroup(Player, String)` | Check if player is in a specific group |
| `addToGroup(Player, String)` | Add player to a group (async) |
| `removeFromGroup(Player, String)` | Remove player from a group (async) |
| `getGroup(String)` | Get a Group object by name |
| `getLoadedGroups()` | Get all loaded groups |

### Prefix/Suffix Methods

| Method | Description |
|--------|-------------|
| `getPrefix(Player)` | Get the player's current prefix |
| `getSuffix(Player)` | Get the player's current suffix |
| `setPrefix(Player, String, int)` | Set a prefix with priority (async) |
| `setSuffix(Player, String, int)` | Set a suffix with priority (async) |

### Meta Methods

| Method | Description |
|--------|-------------|
| `getMeta(Player, String)` | Get a meta value for a player |
| `getGroupDisplayName(String)` | Get the display name of a group |
| `getGroupWeight(String)` | Get the weight of a group |

## Examples

### Permission-Based Features

```java
// Grant temporary VIP access
LuckPermsUtil.addToGroup(player, "vip").thenRun(() -> {
    player.sendMessage("You are now a VIP!");
});

// Check custom permission
if (LuckPermsUtil.hasPermission(player, "myplugin.special")) {
    // Enable special feature
}
```

### Display Player Info

```java
String prefix = LuckPermsUtil.getPrefix(player);
String suffix = LuckPermsUtil.getSuffix(player);
String group = LuckPermsUtil.getPrimaryGroup(player);
String displayName = LuckPermsUtil.getGroupDisplayName(group);

player.sendMessage("Your rank: " + prefix + displayName + suffix);
```

### Async Operations

All modification operations return `CompletableFuture<Void>` for async handling:

```java
LuckPermsUtil.addPermission(player, "myplugin.feature")
    .thenRun(() -> {
        player.sendMessage("Permission granted!");
    })
    .exceptionally(ex -> {
        player.sendMessage("Failed to grant permission!");
        return null;
    });
```

### Group Weight Comparison

```java
// Get player's highest weighted group
Set<String> groups = LuckPermsUtil.getGroups(player);
String highestGroup = groups.stream()
    .max(Comparator.comparingInt(LuckPermsUtil::getGroupWeight))
    .orElse("default");
```

## Graceful Degradation

All utility methods handle the case where LuckPerms is not available:

- Permission checks fall back to Bukkit's `Player.hasPermission()`
- Group methods return empty sets or "default"
- Prefix/suffix methods return empty strings
- Async operations return completed futures with null

This allows your code to work regardless of whether LuckPerms is installed.

## See Also

- [LuckPerms Wiki](https://luckperms.net/wiki)
- [LuckPerms API Javadocs](https://javadoc.io/doc/net.luckperms/api)
- [Integration Guide](INTEGRATION_GUIDE.md)

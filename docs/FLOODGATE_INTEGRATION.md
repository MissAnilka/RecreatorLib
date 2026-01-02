# Floodgate Integration

RecreatorLib provides seamless integration with [GeyserMC's Floodgate](https://wiki.geysermc.org/floodgate/) plugin, allowing you to detect and handle Bedrock Edition players on your server.

## Requirements

- Floodgate plugin installed on your server
- RecreatorLib will automatically detect Floodgate and enable the integration

## Quick Usage

The easiest way to use the Floodgate integration is through the `FloodgateUtil` utility class:

```java
import com.fembuncollective.recreatorlib.integration.floodgate.FloodgateUtil;
import org.bukkit.entity.Player;

// Check if Floodgate is available
if (FloodgateUtil.isAvailable()) {
    // Floodgate integration is active
}

// Check if a player is on Bedrock Edition
if (FloodgateUtil.isBedrock(player)) {
    // Handle Bedrock player
}

// Check if a player is on Java Edition
if (FloodgateUtil.isJava(player)) {
    // Handle Java player
}

// Get the Bedrock username (without prefix)
String bedrockName = FloodgateUtil.getBedrockName(player);

// Get the player's device
Optional<String> device = FloodgateUtil.getDeviceOs(player);
// Returns: "Android", "iOS", "Windows", "Xbox", "PlayStation", etc.

// Check input method
if (FloodgateUtil.isUsingTouch(player)) {
    // Player is using touch controls
}
if (FloodgateUtil.isUsingController(player)) {
    // Player is using a game controller
}
```

## Available Methods

### FloodgateUtil (Static Utility Class)

| Method | Description |
|--------|-------------|
| `isAvailable()` | Returns `true` if Floodgate integration is active |
| `isBedrock(Player)` | Check if a player is on Bedrock Edition |
| `isBedrock(UUID)` | Check if a UUID belongs to a Bedrock player |
| `isJava(Player)` | Check if a player is on Java Edition |
| `getBedrockName(Player)` | Get the original Bedrock username |
| `getXuid(Player)` | Get the Xbox UID (Optional) |
| `getDeviceOs(Player)` | Get the device OS (Optional) |
| `getInputMode(Player)` | Get input mode - Touch/Controller/Keyboard (Optional) |
| `isUsingTouch(Player)` | Check if using touch controls |
| `isUsingController(Player)` | Check if using a controller |
| `isUsingKeyboard(Player)` | Check if using keyboard/mouse |
| `hasLinkedAccount(Player)` | Check if player has linked Java account |
| `getLinkedJavaUuid(Player)` | Get linked Java UUID (Optional) |
| `getPrefix()` | Get the Bedrock username prefix (usually ".") |

### FloodgateIntegration (Direct Access)

For more advanced usage, you can access the `FloodgateIntegration` directly:

```java
import com.fembuncollective.recreatorlib.RecreatorLib;
import com.fembuncollective.recreatorlib.integration.floodgate.FloodgateIntegration;

// Get the integration
Optional<FloodgateIntegration> floodgate = RecreatorLib.getInstance()
    .getIntegrationManager()
    .getFloodgate();

// Use the integration
floodgate.ifPresent(f -> {
    if (f.isBedrockPlayer(player)) {
        String xuid = f.getXuid(player);
        String device = f.getDeviceOs(player);
        String input = f.getInputMode(player);
        String uiProfile = f.getUiProfile(player);
    }
});

// Access the raw Floodgate API
FloodgateApi api = floodgate.map(FloodgateIntegration::getApi).orElse(null);
```

## Practical Examples

### Send Different Messages Based on Platform

```java
@EventHandler
public void onJoin(PlayerJoinEvent event) {
    Player player = event.getPlayer();
    
    if (FloodgateUtil.isBedrock(player)) {
        player.sendMessage("Welcome, Bedrock player!");
        
        FloodgateUtil.getDeviceOs(player).ifPresent(os -> {
            player.sendMessage("You're playing on: " + os);
        });
    } else {
        player.sendMessage("Welcome, Java player!");
    }
}
```

### Adapt UI Based on Input Method

```java
public void openCustomInventory(Player player) {
    // Use larger buttons/spacing for touch/controller players
    if (FloodgateUtil.isUsingTouch(player) || FloodgateUtil.isUsingController(player)) {
        openSimplifiedUI(player);
    } else {
        openFullUI(player);
    }
}
```

### Track Linked Accounts

```java
public void handleLinkedAccount(Player player) {
    if (FloodgateUtil.hasLinkedAccount(player)) {
        UUID javaUuid = FloodgateUtil.getLinkedJavaUuid(player).orElse(null);
        if (javaUuid != null) {
            // Transfer data from Java account
            transferPlayerData(javaUuid, player.getUniqueId());
        }
    }
}
```

## Device OS Values

When using `getDeviceOs()`, you may receive:
- `UNKNOWN`
- `ANDROID`
- `IOS`
- `OSX` (macOS)
- `AMAZON` (Fire OS)
- `GEARVR`
- `HOLOLENS`
- `WINDOWS_10`
- `WIN32` (Windows x86)
- `DEDICATED` (Bedrock Dedicated Server)
- `TVOS` (Apple TV)
- `PLAYSTATION`
- `NINTENDO`
- `XBOX`
- `WINDOWS_PHONE`

## Input Mode Values

When using `getInputMode()`:
- `UNKNOWN`
- `KEYBOARD_MOUSE`
- `TOUCH`
- `CONTROLLER`
- `VR`

## Null Safety

All methods in `FloodgateUtil` are designed to be null-safe:
- Boolean methods return `false` if Floodgate isn't available
- `getBedrockName()` returns the player's regular name if not Bedrock
- Other getters return `Optional` to handle missing data gracefully

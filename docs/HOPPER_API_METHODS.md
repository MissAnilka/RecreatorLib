# HopperAPI Methods Reference

The `HopperAPI` class is the main facade for hopper integration with custom workstations.

## Getting the API

```java
HopperAPI api = RecreatorLib.getInstance().getHopperAPI();
```

---

## Custom Workstations (Recipe Groups)

Use these methods for workstations with complex recipe validation (like custom furnaces with recipe groups).

### registerCustomWorkstation

Registers a custom workstation with recipe group support.

```java
void registerCustomWorkstation(@NotNull Plugin plugin, @NotNull CustomWorkstationConfig config)
```

**Parameters:**
- `plugin` - Your plugin instance
- `config` - The custom workstation configuration

**Example:**
```java
api.registerCustomWorkstation(this, CustomWorkstationConfig.builder("my_furnace")
    .material(Material.FURNACE)
    .inventorySize(27)
    .inputSlot(10)
    .fuelSlot(12)
    .outputSlot(16)
    .enableVanillaRecipes(true)
    .build());
```

---

### registerCustomInventoryProvider

Registers an inventory provider for a custom workstation by ID.

```java
void registerCustomInventoryProvider(@NotNull String workstationId,
                                     @NotNull CustomWorkstationRegistry.InventoryProvider provider)
```

**Parameters:**
- `workstationId` - The ID used when creating the config
- `provider` - Function that returns the inventory for a block

**Example:**
```java
api.registerCustomInventoryProvider("my_furnace", block -> {
    // Return your custom inventory for this block
    return myCustomInventoryMap.get(block.getLocation());
});
```

---

### registerCustomLocationInventory

Registers an inventory provider for a specific location.

```java
void registerCustomLocationInventory(@NotNull Location location,
                                     @NotNull CustomWorkstationRegistry.InventoryProvider provider)
```

**Parameters:**
- `location` - The block location
- `provider` - The inventory provider

---

### unregisterCustomLocation

Unregisters a custom workstation location provider. Call when block is broken.

```java
void unregisterCustomLocation(@NotNull Location location)
```

---

### getCustomWorkstationConfig (by Block)

Gets the custom workstation config for a block.

```java
@Nullable
CustomWorkstationConfig getCustomWorkstationConfig(@NotNull Block block)
```

**Returns:** The config, or `null` if not a custom workstation

---

### getCustomWorkstationConfig (by ID)

Gets the custom workstation config by ID.

```java
@Nullable
CustomWorkstationConfig getCustomWorkstationConfig(@NotNull String id)
```

**Returns:** The config, or `null` if not found

---

## Simple Workstations

Use these methods for workstations without complex recipe validation.

### registerWorkstation

Registers a simple workstation (basic slot configuration).

```java
void registerWorkstation(@NotNull Plugin plugin, @NotNull WorkstationConfig config)
```

---

### registerInventoryProvider

Registers an inventory provider for a simple workstation type.

```java
void registerInventoryProvider(@NotNull String workstationId, 
                               @NotNull WorkstationRegistry.InventoryProvider provider)
```

---

### registerLocationInventory

Registers an inventory provider for a specific block location.

```java
void registerLocationInventory(@NotNull Location location, 
                               @NotNull WorkstationRegistry.InventoryProvider provider)
```

---

### unregisterLocation

Unregisters a location-specific inventory provider (both simple and custom).

```java
void unregisterLocation(@NotNull Location location)
```

---

## Custom Handlers (Full Control)

Use these methods for complete control over hopper behavior.

### registerHandler (by Material)

Registers a fully custom hopper handler for a material type.

```java
void registerHandler(@NotNull Plugin plugin, @NotNull Material material, 
                     @NotNull HopperHandler handler)
```

---

### registerHandler (by Location)

Registers a custom hopper handler for a specific location.

```java
void registerHandler(@NotNull Plugin plugin, @NotNull Location location, 
                     @NotNull HopperHandler handler)
```

---

## Hopper Settings

### setHopperSettings

Sets custom settings for a specific hopper.

```java
void setHopperSettings(@NotNull Location hopperLocation, @NotNull HopperSettings settings)
```

---

### getHopperSettings

Gets the settings for a hopper.

```java
@NotNull
HopperSettings getHopperSettings(@NotNull Location hopperLocation)
```

**Returns:** The settings (default if none set)

---

### removeHopperSettings

Removes custom settings for a hopper.

```java
void removeHopperSettings(@NotNull Location hopperLocation)
```

---

## Query Methods

### isWorkstation

Checks if a block is a registered workstation (simple or custom).

```java
boolean isWorkstation(@NotNull Block block)
```

---

### isCustomWorkstation

Checks if a block is a custom workstation (with recipe groups).

```java
boolean isCustomWorkstation(@NotNull Block block)
```

---

### getWorkstationConfig (by Block)

Gets the simple workstation config for a block.

```java
@Nullable
WorkstationConfig getWorkstationConfig(@NotNull Block block)
```

---

### getWorkstationConfig (by ID)

Gets the simple workstation config by ID.

```java
@Nullable
WorkstationConfig getWorkstationConfig(@NotNull String id)
```

---

### getWorkstationInventory

Gets the inventory for a workstation block (works for both simple and custom).

```java
@Nullable
Inventory getWorkstationInventory(@NotNull Block block)
```

---

## Cleanup

### unregisterAll

Unregisters all handlers and workstations from a plugin. **Call this in your plugin's `onDisable()`!**

```java
void unregisterAll(@NotNull Plugin plugin)
```

**Example:**
```java
@Override
public void onDisable() {
    RecreatorLib.getInstance().getHopperAPI().unregisterAll(this);
}
```

---

## Internal Access

### getHopperManager

Gets the underlying HopperManager.

```java
HopperManager getHopperManager()
```

---

### getWorkstationRegistry

Gets the underlying WorkstationRegistry.

```java
WorkstationRegistry getWorkstationRegistry()
```

---

### getCustomWorkstationRegistry

Gets the underlying CustomWorkstationRegistry.

```java
CustomWorkstationRegistry getCustomWorkstationRegistry()
```

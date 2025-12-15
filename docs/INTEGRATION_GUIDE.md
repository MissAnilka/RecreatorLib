# Integration Guide

How to integrate RecreatorLib into your Minecraft plugin.

---

## Maven Dependency

Add RecreatorLib as a dependency in your `pom.xml`:

```xml
<dependencies>
    <!-- RecreatorLib -->
    <dependency>
        <groupId>com.fembuncollective</groupId>
        <artifactId>RecreatorLib</artifactId>
        <version>1.0.0</version>
        <scope>provided</scope>
    </dependency>
    
    <!-- Paper API (required) -->
    <dependency>
        <groupId>io.papermc.paper</groupId>
        <artifactId>paper-api</artifactId>
        <version>1.21-R0.1-SNAPSHOT</version>
        <scope>provided</scope>
    </dependency>
</dependencies>

<repositories>
    <!-- Paper repository -->
    <repository>
        <id>papermc</id>
        <url>https://repo.papermc.io/repository/maven-public/</url>
    </repository>
    
    <!-- Your repository for RecreatorLib (if hosted) -->
    <!-- Or use local installation with: mvn install -->
</repositories>
```

### Local Installation

If RecreatorLib is not hosted in a remote repository, install it locally:

```bash
cd RecreatorLib
mvn clean install
```

This installs the JAR to your local Maven repository (`~/.m2/repository/`).

---

## Plugin Configuration

### plugin.yml

Add RecreatorLib as a dependency in your `plugin.yml`:

```yaml
name: YourPlugin
version: '${project.version}'
main: com.yourpackage.YourPlugin
api-version: '1.21'

# Required dependency - plugin won't load without RecreatorLib
depend:
  - RecreatorLib

# Or use soft dependency if you want optional support
# softdepend:
#   - RecreatorLib
```

---

## Basic Setup

### Main Plugin Class

```java
package com.yourpackage;

import com.fembuncollective.recreatorlib.RecreatorLib;
import com.fembuncollective.recreatorlib.hopper.HopperAPI;
import com.fembuncollective.recreatorlib.hopper.CustomWorkstationConfig;
import com.fembuncollective.recreatorlib.hopper.CustomWorkstationConfig.RecipeGroup;
import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;

public class YourPlugin extends JavaPlugin {

    private HopperAPI hopperAPI;

    @Override
    public void onEnable() {
        // Get RecreatorLib API
        hopperAPI = RecreatorLib.getInstance().getHopperAPI();
        
        // Register your custom workstations
        registerWorkstations();
        
        getLogger().info("YourPlugin enabled with RecreatorLib integration!");
    }

    @Override
    public void onDisable() {
        // IMPORTANT: Unregister all your handlers when plugin disables
        if (hopperAPI != null) {
            hopperAPI.unregisterAll(this);
        }
    }

    private void registerWorkstations() {
        // See examples below
    }
}
```

---

## Hopper Direction Behavior

RecreatorLib automatically routes items based on hopper position:

| Hopper Position | Target Slot Type |
|-----------------|------------------|
| **Above** workstation (facing down) | Input slots |
| **Side** of workstation | Fuel slots |
| **Below** workstation | Pulls from output slots |

This matches vanilla furnace behavior - no configuration needed!

---

## Example: Custom Furnace with Recipe Groups

```java
private void registerWorkstations() {
    // Define a recipe group for soul conversion
    // Soul Sand + Soul Soil can only be smelted with Magma Block as fuel
    RecipeGroup soulConversion = RecipeGroup.builder("soul_conversion")
        .input(Material.SOUL_SAND, Material.SOUL_SOIL)
        .fuel(Material.MAGMA_BLOCK)
        .build();
    
    // Define another recipe group
    RecipeGroup enderRecipes = RecipeGroup.builder("ender_recipes")
        .input(Material.ENDER_PEARL, Material.ENDER_EYE)
        .fuel(Material.END_STONE, Material.END_ROD)
        .build();

    // Register the custom furnace
    CustomWorkstationConfig furnaceConfig = CustomWorkstationConfig.builder("custom_furnace")
        .material(Material.FURNACE)           // Block type
        .inventorySize(27)                    // Custom inventory size
        .inputSlot(10)                        // Custom slot for input
        .fuelSlot(12)                         // Custom slot for fuel
        .outputSlot(16)                       // Custom slot for output
        .enableVanillaRecipes(true)           // Allow normal smelting recipes
        .enableVanillaFuel(true)              // Allow coal, wood, etc.
        .addGeneralFuel(Material.BLAZE_ROD)   // Custom fuel for all recipes
        .addGeneralInput(Material.RAW_COPPER) // Custom input for all fuels
        .addRecipeGroup(soulConversion)       // Add recipe group
        .addRecipeGroup(enderRecipes)         // Add another recipe group
        .build();

    hopperAPI.registerCustomWorkstation(this, furnaceConfig);

    // Register inventory provider - tells RecreatorLib how to get the inventory
    hopperAPI.registerCustomInventoryProvider("custom_furnace", block -> {
        // Return your custom inventory for this block location
        return getCustomFurnaceInventory(block.getLocation());
    });
}
```

---

## Example: Register Location-Specific Inventory

When a player places your custom block:

```java
// When block is placed
public void onBlockPlace(Block block, Inventory customInventory) {
    hopperAPI.registerCustomLocationInventory(block.getLocation(), b -> customInventory);
}

// When block is broken
public void onBlockBreak(Block block) {
    hopperAPI.unregisterCustomLocation(block.getLocation());
}
```

---

## Example: Custom Hopper Handler (Full Control)

For complete control over hopper behavior:

```java
import com.fembuncollective.recreatorlib.hopper.HopperHandler;
import com.fembuncollective.recreatorlib.hopper.TransferResult;

public class MyCustomHandler implements HopperHandler {

    @Override
    public @NotNull TransferResult onHopperPush(@NotNull Block targetBlock, 
                                                 @NotNull ItemStack item, 
                                                 @NotNull Block hopperBlock) {
        // Custom logic for items being pushed INTO your block
        if (canAcceptItem(item)) {
            return TransferResult.success(item, 0); // slot 0
        }
        return TransferResult.denied("Item not allowed");
    }

    @Override
    public @Nullable TransferResult onHopperPull(@NotNull Block sourceBlock, 
                                                  @NotNull Block hopperBlock) {
        // Custom logic for items being pulled FROM your block
        ItemStack output = getOutputItem(sourceBlock);
        if (output != null) {
            return TransferResult.success(output, 2); // from slot 2
        }
        return null; // Nothing to pull
    }

    @Override
    public boolean canHandle(@NotNull Block block) {
        return block.getType() == Material.BARREL; // Example
    }

    @Override
    public int[] getInputSlots(@NotNull Block block) {
        return new int[]{0, 1}; // Slots that accept input
    }

    @Override
    public int[] getOutputSlots(@NotNull Block block) {
        return new int[]{2}; // Slots that allow output
    }

    @Override
    public boolean canInsert(@NotNull Block block, int slot, @NotNull ItemStack item) {
        return slot == 0 || slot == 1;
    }

    @Override
    public boolean canExtract(@NotNull Block block, int slot) {
        return slot == 2;
    }
}
```

Register the handler:

```java
hopperAPI.registerHandler(this, Material.BARREL, new MyCustomHandler());
```

---

## Checking for RecreatorLib (Soft Dependency)

If using `softdepend`:

```java
@Override
public void onEnable() {
    if (getServer().getPluginManager().getPlugin("RecreatorLib") != null) {
        // RecreatorLib is available
        hopperAPI = RecreatorLib.getInstance().getHopperAPI();
        registerWorkstations();
        getLogger().info("RecreatorLib integration enabled!");
    } else {
        getLogger().warning("RecreatorLib not found - hopper integration disabled");
    }
}
```

---

## Important Notes

1. **Always call `unregisterAll(this)` in `onDisable()`** to clean up your registrations
2. **RecreatorLib loads first** (load: STARTUP) so it's available when your plugin enables
3. **Hopper behavior is permissive** - it allows items that COULD be valid; your plugin handles invalid indicators
4. **Use location-based registration** when you need different behavior per block instance
5. **Use material-based registration** when all blocks of that type behave the same

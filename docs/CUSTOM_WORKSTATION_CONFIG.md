# CustomWorkstationConfig Class Structure

Configuration for workstations with complex recipe support (recipe groups, fuel restrictions).

---

## Class Overview

```java
package com.fembuncollective.recreatorlib.hopper;

public class CustomWorkstationConfig {
    // Fields (all immutable after construction)
    private final String id;
    private final Set<Material> materials;
    private final int inventorySize;
    private final Map<Integer, SlotDefinition> slots;
    private final boolean vanillaRecipesEnabled;
    private final boolean vanillaFuelEnabled;
    private final Set<Material> generalFuelSources;
    private final Set<Material> generalInputItems;
    private final List<RecipeGroup> recipeGroups;
    private boolean enabled;
}
```

---

## Creating a Config

Use the builder pattern:

```java
CustomWorkstationConfig config = CustomWorkstationConfig.builder("workstation_id")
    .material(Material.FURNACE)
    .inventorySize(27)
    .inputSlot(10)
    .fuelSlot(12)
    .outputSlot(16)
    .enableVanillaRecipes(true)
    .enableVanillaFuel(true)
    .addGeneralFuel(Material.BLAZE_ROD)
    .addGeneralInput(Material.RAW_COPPER)
    .addRecipeGroup(myRecipeGroup)
    .build();
```

---

## Builder Methods

| Method | Description |
|--------|-------------|
| `builder(String id)` | Create a new builder with the given workstation ID |
| `material(Material)` | Add a block material type for this workstation |
| `materials(Material...)` | Add multiple block material types |
| `inventorySize(int)` | Set the inventory size (default: 27) |
| `inputSlot(int)` | Define an input slot |
| `fuelSlot(int)` | Define a fuel slot |
| `outputSlot(int)` | Define an output slot |
| `enableVanillaRecipes(boolean)` | Allow vanilla smelting recipes |
| `enableVanillaFuel(boolean)` | Allow vanilla fuel items (coal, wood, etc.) |
| `addGeneralFuel(Material...)` | Add custom fuel valid for ALL recipes |
| `addGeneralInput(Material...)` | Add custom input valid for ALL fuels |
| `addRecipeGroup(RecipeGroup)` | Add a recipe group with specific fuel requirements |
| `build()` | Build the config (validates required fields) |

---

## Getter Methods

| Method | Return Type | Description |
|--------|-------------|-------------|
| `getId()` | `String` | Unique workstation identifier |
| `getMaterials()` | `Set<Material>` | Block types for this workstation |
| `getInventorySize()` | `int` | Size of the custom inventory |
| `isEnabled()` | `boolean` | Whether hopper integration is enabled |
| `isVanillaRecipesEnabled()` | `boolean` | Whether vanilla recipes are allowed |
| `isVanillaFuelEnabled()` | `boolean` | Whether vanilla fuels are allowed |
| `getGeneralFuelSources()` | `Set<Material>` | Custom fuels for all recipes |
| `getGeneralInputItems()` | `Set<Material>` | Custom inputs for all fuels |
| `getRecipeGroups()` | `List<RecipeGroup>` | Recipe groups with fuel restrictions |
| `getSlot(int)` | `SlotDefinition` | Get slot definition (nullable) |
| `getInputSlots()` | `int[]` | All input slot indices |
| `getFuelSlots()` | `int[]` | All fuel slot indices |
| `getOutputSlots()` | `int[]` | All output slot indices |

---

## Validation Methods

### canAcceptInput(ItemStack)

Checks if an item can be placed in an input slot.

```java
boolean canAcceptInput(@NotNull ItemStack item)
```

**Returns true if:**
- Vanilla recipes are enabled (accepts anything)
- Item is in `generalInputItems`
- Item is in any `RecipeGroup`'s input items
- No inputs are defined (accepts anything)

---

### canAcceptFuel(ItemStack)

Checks if an item can be placed in a fuel slot.

```java
boolean canAcceptFuel(@NotNull ItemStack item)
```

**Returns true if:**
- Vanilla fuel enabled AND item is a vanilla fuel
- Item is in `generalFuelSources`
- Item is in any `RecipeGroup`'s fuel sources

---

## SlotDefinition Inner Class

Defines a single slot's behavior.

```java
public static class SlotDefinition {
    private final int slot;
    private final SlotType type;
    private final SlotItemFilter filter;
    
    // Methods
    public int getSlot();
    public SlotType getType();
    public SlotItemFilter getFilter();  // nullable
    public boolean acceptsHopperInput();  // true for INPUT, FUEL, STORAGE
    public boolean allowsHopperOutput();  // true for OUTPUT, STORAGE
}
```

---

## SlotType Enum

```java
public enum SlotType {
    INPUT,    // Accepts input items
    FUEL,     // Accepts fuel items
    OUTPUT,   // Only allows extraction
    STORAGE,  // Both input and output
    LOCKED    // No hopper interaction
}
```

---

## RecipeGroup Inner Class

Defines a group of recipes with specific input/fuel requirements.

```java
public static class RecipeGroup {
    private final String name;
    private final Set<Material> inputItems;
    private final Set<Material> fuelSources;
    
    // Methods
    public String getName();
    public Set<Material> getInputItems();
    public Set<Material> getFuelSources();
    
    // Builder
    public static RecipeGroupBuilder builder(String name);
}
```

### RecipeGroup Builder

```java
RecipeGroup group = RecipeGroup.builder("group_name")
    .input(Material.SOUL_SAND, Material.SOUL_SOIL)
    .fuel(Material.MAGMA_BLOCK)
    .build();
```

| Method | Description |
|--------|-------------|
| `builder(String name)` | Create builder with group name |
| `input(Material...)` | Add input items for this group |
| `fuel(Material...)` | Add fuel items that work with these inputs |
| `build()` | Build the recipe group |

---

## Complete Example

```java
// Recipe group: Soul items need Magma Block as fuel
RecipeGroup soulGroup = RecipeGroup.builder("soul_conversion")
    .input(Material.SOUL_SAND, Material.SOUL_SOIL)
    .fuel(Material.MAGMA_BLOCK)
    .build();

// Recipe group: Ender items need End-related fuel
RecipeGroup enderGroup = RecipeGroup.builder("ender_processing")
    .input(Material.ENDER_PEARL, Material.ENDER_EYE, Material.CHORUS_FRUIT)
    .fuel(Material.END_STONE, Material.END_ROD, Material.PURPUR_BLOCK)
    .build();

// Full config
CustomWorkstationConfig config = CustomWorkstationConfig.builder("advanced_furnace")
    // Block types
    .material(Material.FURNACE)
    .material(Material.BLAST_FURNACE)
    
    // Inventory
    .inventorySize(27)
    
    // Slots (custom positions)
    .inputSlot(10)
    .inputSlot(11)    // Multiple input slots
    .fuelSlot(12)
    .outputSlot(14)
    .outputSlot(15)   // Multiple output slots
    
    // Vanilla support
    .enableVanillaRecipes(true)
    .enableVanillaFuel(true)
    
    // Custom items that work with ANY recipe/fuel
    .addGeneralFuel(Material.BLAZE_ROD, Material.LAVA_BUCKET)
    .addGeneralInput(Material.RAW_COPPER, Material.RAW_IRON)
    
    // Recipe groups with specific requirements
    .addRecipeGroup(soulGroup)
    .addRecipeGroup(enderGroup)
    
    .build();
```

---

## How Hopper Validation Works

The hopper system is **permissive**. It allows items that COULD be valid:

### Hopper Direction Determines Slot Type

| Hopper Position | Direction | Target Slot |
|-----------------|-----------|-------------|
| Above workstation | Facing DOWN | **Input slots** |
| Side of workstation | Facing SIDE | **Fuel slots** |
| Below workstation | Pulling UP | **Output slots** |

### Item Validation

1. **Input slot validation** (`canAcceptInput`):
   - If vanilla recipes enabled → allow anything
   - If item in general inputs → allow
   - If item in ANY recipe group inputs → allow

2. **Fuel slot validation** (`canAcceptFuel`):
   - If vanilla fuel enabled AND item.isFuel() → allow
   - If item in general fuels → allow
   - If item in ANY recipe group fuels → allow

3. **Output slot extraction**:
   - Hopper below pulls from output slots only

**Your workstation plugin** handles:
- Showing "invalid combination" indicators
- Actual recipe processing validation
- Preventing smelting when fuel doesn't match input

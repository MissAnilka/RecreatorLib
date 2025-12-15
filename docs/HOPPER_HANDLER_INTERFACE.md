# HopperHandler Interface

Interface for full control over hopper interactions with custom blocks.

---

## Interface Definition

```java
package com.fembuncollective.recreatorlib.hopper;

public interface HopperHandler {

    @NotNull
    TransferResult onHopperPush(@NotNull Block targetBlock, 
                                 @NotNull ItemStack item, 
                                 @NotNull Block hopperBlock);

    @Nullable
    TransferResult onHopperPull(@NotNull Block sourceBlock, 
                                 @NotNull Block hopperBlock);

    boolean canHandle(@NotNull Block block);

    int[] getInputSlots(@NotNull Block block);

    int[] getOutputSlots(@NotNull Block block);

    boolean canInsert(@NotNull Block block, int slot, @NotNull ItemStack item);

    boolean canExtract(@NotNull Block block, int slot);
}
```

---

## Methods

### onHopperPush

Called when a hopper tries to push items **INTO** your block.

```java
@NotNull
TransferResult onHopperPush(@NotNull Block targetBlock, 
                            @NotNull ItemStack item, 
                            @NotNull Block hopperBlock);
```

**Parameters:**
- `targetBlock` - Your block receiving items
- `item` - The item being pushed (1 item per transfer)
- `hopperBlock` - The hopper block pushing the item

**Returns:** `TransferResult` indicating success/failure

**Example:**
```java
@Override
public TransferResult onHopperPush(Block targetBlock, ItemStack item, Block hopperBlock) {
    Inventory inv = getMyInventory(targetBlock);
    
    // Check if we can accept this item
    if (!isValidInput(item)) {
        return TransferResult.denied("Item not accepted");
    }
    
    // Find available slot
    int slot = findAvailableInputSlot(inv, item);
    if (slot == -1) {
        return TransferResult.denied("No space available");
    }
    
    // Add item to inventory
    ItemStack existing = inv.getItem(slot);
    if (existing == null) {
        inv.setItem(slot, item.clone());
    } else {
        existing.setAmount(existing.getAmount() + 1);
    }
    
    return TransferResult.success(item, slot);
}
```

---

### onHopperPull

Called when a hopper tries to pull items **FROM** your block.

```java
@Nullable
TransferResult onHopperPull(@NotNull Block sourceBlock, 
                            @NotNull Block hopperBlock);
```

**Parameters:**
- `sourceBlock` - Your block items are being pulled from
- `hopperBlock` - The hopper block pulling items

**Returns:** `TransferResult` with the item to transfer, or `null` if nothing to pull

**Example:**
```java
@Override
public TransferResult onHopperPull(Block sourceBlock, Block hopperBlock) {
    Inventory inv = getMyInventory(sourceBlock);
    
    // Check output slots for items
    for (int slot : getOutputSlots(sourceBlock)) {
        ItemStack item = inv.getItem(slot);
        if (item != null && !item.getType().isAir()) {
            // Create a copy with amount 1
            ItemStack toTransfer = item.clone();
            toTransfer.setAmount(1);
            
            // Reduce the source
            item.setAmount(item.getAmount() - 1);
            if (item.getAmount() <= 0) {
                inv.setItem(slot, null);
            }
            
            return TransferResult.success(toTransfer, slot);
        }
    }
    
    return null; // Nothing to pull
}
```

---

### canHandle

Check if this handler can handle the given block.

```java
boolean canHandle(@NotNull Block block);
```

**Example:**
```java
@Override
public boolean canHandle(Block block) {
    // Handle custom barrel blocks at specific locations
    return block.getType() == Material.BARREL && 
           isMyCustomBarrel(block.getLocation());
}
```

---

### getInputSlots

Gets the slot indices that accept hopper input.

```java
int[] getInputSlots(@NotNull Block block);
```

**Example:**
```java
@Override
public int[] getInputSlots(Block block) {
    return new int[]{0, 1, 2}; // Slots 0-2 accept input
}
```

---

### getOutputSlots

Gets the slot indices that allow hopper extraction.

```java
int[] getOutputSlots(@NotNull Block block);
```

**Example:**
```java
@Override
public int[] getOutputSlots(Block block) {
    return new int[]{8}; // Only slot 8 allows output
}
```

---

### canInsert

Check if a specific item can be inserted into a specific slot.

```java
boolean canInsert(@NotNull Block block, int slot, @NotNull ItemStack item);
```

**Example:**
```java
@Override
public boolean canInsert(Block block, int slot, ItemStack item) {
    // Slot 0-1: Only accept ores
    if (slot == 0 || slot == 1) {
        return isOre(item.getType());
    }
    // Slot 2: Only accept fuel
    if (slot == 2) {
        return item.getType().isFuel();
    }
    return false;
}
```

---

### canExtract

Check if items can be extracted from a specific slot.

```java
boolean canExtract(@NotNull Block block, int slot);
```

**Example:**
```java
@Override
public boolean canExtract(Block block, int slot) {
    // Only allow extraction from output slot (8)
    return slot == 8;
}
```

---

## TransferResult

Result object for transfer operations.

```java
public class TransferResult {
    // Factory methods
    public static TransferResult success(ItemStack item, int slot);
    public static TransferResult denied(String reason);
    public static TransferResult partial(ItemStack item, int slot, int amountTransferred);
    
    // Getters
    public boolean isSuccess();
    public ItemStack getItem();
    public int getSlot();
    public String getDenialReason();
    public int getAmountTransferred();
}
```

---

## Complete Implementation Example

```java
public class CustomCrusherHandler implements HopperHandler {

    private final Map<Location, Inventory> crusherInventories;
    
    // Slots: 0-2 = input, 3 = fuel, 4-6 = output
    private static final int[] INPUT_SLOTS = {0, 1, 2};
    private static final int[] OUTPUT_SLOTS = {4, 5, 6};
    private static final int FUEL_SLOT = 3;

    @Override
    public @NotNull TransferResult onHopperPush(@NotNull Block targetBlock, 
                                                 @NotNull ItemStack item, 
                                                 @NotNull Block hopperBlock) {
        Inventory inv = crusherInventories.get(targetBlock.getLocation());
        if (inv == null) {
            return TransferResult.denied("Not a crusher");
        }

        // Determine which slots to try based on item type
        int[] slots = item.getType().isFuel() ? new int[]{FUEL_SLOT} : INPUT_SLOTS;
        
        for (int slot : slots) {
            if (!canInsert(targetBlock, slot, item)) continue;
            
            ItemStack existing = inv.getItem(slot);
            if (existing == null || existing.getType().isAir()) {
                inv.setItem(slot, item.clone());
                return TransferResult.success(item, slot);
            } else if (existing.isSimilar(item) && 
                       existing.getAmount() < existing.getMaxStackSize()) {
                existing.setAmount(existing.getAmount() + 1);
                return TransferResult.success(item, slot);
            }
        }
        
        return TransferResult.denied("No available slot");
    }

    @Override
    public @Nullable TransferResult onHopperPull(@NotNull Block sourceBlock, 
                                                  @NotNull Block hopperBlock) {
        Inventory inv = crusherInventories.get(sourceBlock.getLocation());
        if (inv == null) return null;

        for (int slot : OUTPUT_SLOTS) {
            ItemStack item = inv.getItem(slot);
            if (item != null && !item.getType().isAir()) {
                ItemStack toTransfer = item.clone();
                toTransfer.setAmount(1);
                
                item.setAmount(item.getAmount() - 1);
                if (item.getAmount() <= 0) {
                    inv.setItem(slot, null);
                }
                
                return TransferResult.success(toTransfer, slot);
            }
        }
        
        return null;
    }

    @Override
    public boolean canHandle(@NotNull Block block) {
        return crusherInventories.containsKey(block.getLocation());
    }

    @Override
    public int[] getInputSlots(@NotNull Block block) {
        return INPUT_SLOTS;
    }

    @Override
    public int[] getOutputSlots(@NotNull Block block) {
        return OUTPUT_SLOTS;
    }

    @Override
    public boolean canInsert(@NotNull Block block, int slot, @NotNull ItemStack item) {
        if (slot == FUEL_SLOT) {
            return item.getType().isFuel();
        }
        for (int inputSlot : INPUT_SLOTS) {
            if (slot == inputSlot) {
                return isCrushableOre(item.getType());
            }
        }
        return false;
    }

    @Override
    public boolean canExtract(@NotNull Block block, int slot) {
        for (int outputSlot : OUTPUT_SLOTS) {
            if (slot == outputSlot) return true;
        }
        return false;
    }
    
    private boolean isCrushableOre(Material mat) {
        return mat.name().contains("ORE") || mat.name().startsWith("RAW_");
    }
}
```

---

## Registration

```java
// Register by material (all blocks of this type)
hopperAPI.registerHandler(this, Material.DROPPER, new CustomCrusherHandler());

// Register by location (specific block only)
hopperAPI.registerHandler(this, crusherLocation, new CustomCrusherHandler());
```

package com.fembuncollective.recreatorlib.hopper;

import org.bukkit.block.Block;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Hopper handler for custom workstations with complex recipe support.
 * PERMISSIVE - allows items that COULD be valid for any recipe.
 * 
 * @author MissAnilka
 */
public class CustomWorkstationHandler implements HopperHandler {

    private final CustomWorkstationRegistry registry;

    public CustomWorkstationHandler() {
        this.registry = CustomWorkstationRegistry.getInstance();
    }

    @Override
    @NotNull
    public TransferResult onHopperPush(@NotNull Block targetBlock, @NotNull ItemStack item, @NotNull Block hopperBlock) {
        CustomWorkstationConfig config = registry.getConfig(targetBlock);
        if (config == null || !config.isEnabled()) return TransferResult.passThrough();

        Inventory inv = registry.getInventory(targetBlock, config);
        if (inv == null) return TransferResult.passThrough();

        int[] inputSlots = config.getInputSlots();
        if (inputSlots.length > 0 && config.canAcceptInput(item)) {
            TransferResult result = tryPushToSlots(inv, item, inputSlots, config);
            if (result.isSuccessful()) return result;
        }

        int[] fuelSlots = config.getFuelSlots();
        if (fuelSlots.length > 0 && config.canAcceptFuel(item)) {
            TransferResult result = tryPushToSlots(inv, item, fuelSlots, config);
            if (result.isSuccessful()) return result;
        }

        return TransferResult.noSpace();
    }

    @Override
    @Nullable
    public TransferResult onHopperPull(@NotNull Block sourceBlock, @NotNull Block hopperBlock) {
        CustomWorkstationConfig config = registry.getConfig(sourceBlock);
        if (config == null || !config.isEnabled()) return TransferResult.passThrough();

        Inventory inv = registry.getInventory(sourceBlock, config);
        if (inv == null) return TransferResult.passThrough();

        int[] outputSlots = config.getOutputSlots();
        if (outputSlots.length == 0) return TransferResult.denied();

        for (int slot : outputSlots) {
            if (slot >= inv.getSize()) continue;
            ItemStack existing = inv.getItem(slot);
            if (existing != null && !existing.getType().isAir() && existing.getAmount() > 0) {
                ItemStack pulled = existing.clone();
                pulled.setAmount(1);
                if (existing.getAmount() <= 1) inv.setItem(slot, null);
                else existing.setAmount(existing.getAmount() - 1);
                return TransferResult.success(pulled, slot);
            }
        }
        return TransferResult.empty();
    }

    private TransferResult tryPushToSlots(Inventory inv, ItemStack item, int[] slots, CustomWorkstationConfig config) {
        for (int slot : slots) {
            if (slot >= inv.getSize()) continue;
            CustomWorkstationConfig.SlotDefinition slotDef = config.getSlot(slot);
            if (slotDef != null && slotDef.getFilter() != null && !slotDef.getFilter().test(item)) continue;

            ItemStack existing = inv.getItem(slot);
            if (existing == null || existing.getType().isAir()) {
                ItemStack toPlace = item.clone();
                toPlace.setAmount(1);
                inv.setItem(slot, toPlace);
                return TransferResult.success(toPlace, slot);
            } else if (canStack(existing, item)) {
                if (existing.getAmount() < existing.getMaxStackSize()) {
                    existing.setAmount(existing.getAmount() + 1);
                    return TransferResult.success(item, slot);
                }
            }
        }
        return TransferResult.noSpace();
    }

    private boolean canStack(@NotNull ItemStack existing, @NotNull ItemStack toAdd) {
        return existing.getType() == toAdd.getType() && existing.getAmount() < existing.getMaxStackSize() && existing.isSimilar(toAdd);
    }

    @Override public boolean canHandle(@NotNull Block block) { return registry.isCustomWorkstation(block); }

    @Override
    public int[] getInputSlots(@NotNull Block block) {
        CustomWorkstationConfig config = registry.getConfig(block);
        if (config == null) return new int[0];
        int[] inputSlots = config.getInputSlots();
        int[] fuelSlots = config.getFuelSlots();
        int[] combined = new int[inputSlots.length + fuelSlots.length];
        System.arraycopy(inputSlots, 0, combined, 0, inputSlots.length);
        System.arraycopy(fuelSlots, 0, combined, inputSlots.length, fuelSlots.length);
        return combined;
    }

    @Override
    public int[] getOutputSlots(@NotNull Block block) {
        CustomWorkstationConfig config = registry.getConfig(block);
        return config != null ? config.getOutputSlots() : new int[0];
    }

    @Override
    public boolean canInsert(@NotNull Block block, int slot, @NotNull ItemStack item) {
        CustomWorkstationConfig config = registry.getConfig(block);
        if (config == null) return false;
        CustomWorkstationConfig.SlotDefinition slotDef = config.getSlot(slot);
        if (slotDef == null || !slotDef.acceptsHopperInput()) return false;
        return switch (slotDef.getType()) {
            case INPUT -> config.canAcceptInput(item);
            case FUEL -> config.canAcceptFuel(item);
            case STORAGE -> true;
            default -> false;
        };
    }

    @Override
    public boolean canExtract(@NotNull Block block, int slot) {
        CustomWorkstationConfig config = registry.getConfig(block);
        if (config == null) return false;
        CustomWorkstationConfig.SlotDefinition slotDef = config.getSlot(slot);
        return slotDef != null && slotDef.allowsHopperOutput();
    }
}

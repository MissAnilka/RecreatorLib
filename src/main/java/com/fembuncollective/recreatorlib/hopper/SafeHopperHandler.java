package com.fembuncollective.recreatorlib.hopper;

import org.bukkit.block.Block;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Safe hopper handler that prevents duplication and item loss.
 * 
 * @author MissAnilka
 */
public class SafeHopperHandler implements HopperHandler {

    private final WorkstationRegistry registry;

    public SafeHopperHandler() {
        this.registry = WorkstationRegistry.getInstance();
    }

    @Override
    @NotNull
    public TransferResult onHopperPush(@NotNull Block targetBlock, @NotNull ItemStack item, @NotNull Block hopperBlock) {
        WorkstationConfig config = registry.getConfig(targetBlock);
        if (config == null || !config.isEnabled()) return TransferResult.passThrough();

        Inventory targetInv = registry.getInventory(targetBlock, config);
        if (targetInv == null) return TransferResult.passThrough();

        int[] inputSlots = config.getInputSlots();
        if (inputSlots.length == 0) return TransferResult.denied();

        for (int slot : inputSlots) {
            if (slot >= targetInv.getSize()) continue;
            WorkstationConfig.SlotConfig slotConfig = config.getSlotConfig(slot);
            if (slotConfig != null) {
                if (slotConfig.getFunction() == WorkstationConfig.SlotFunction.FUEL && !item.getType().isFuel()) continue;
                if (!slotConfig.canAcceptMaterial(item.getType())) continue;
            }

            ItemStack existing = targetInv.getItem(slot);
            if (existing == null || existing.getType().isAir()) {
                ItemStack toPlace = item.clone();
                toPlace.setAmount(1);
                targetInv.setItem(slot, toPlace);
                return TransferResult.success(toPlace, slot);
            } else if (canStack(existing, item)) {
                int maxStack = existing.getMaxStackSize();
                if (existing.getAmount() < maxStack) {
                    existing.setAmount(existing.getAmount() + 1);
                    return TransferResult.success(item, slot);
                }
            }
        }
        return TransferResult.noSpace();
    }

    @Override
    @Nullable
    public TransferResult onHopperPull(@NotNull Block sourceBlock, @NotNull Block hopperBlock) {
        WorkstationConfig config = registry.getConfig(sourceBlock);
        if (config == null || !config.isEnabled()) return TransferResult.passThrough();

        Inventory sourceInv = registry.getInventory(sourceBlock, config);
        if (sourceInv == null) return TransferResult.passThrough();

        int[] outputSlots = config.getOutputSlots();
        if (outputSlots.length == 0) return TransferResult.denied();

        for (int slot : outputSlots) {
            if (slot >= sourceInv.getSize()) continue;
            WorkstationConfig.SlotConfig slotConfig = config.getSlotConfig(slot);
            if (slotConfig != null && !slotConfig.allowsHopperOutput()) continue;

            ItemStack existing = sourceInv.getItem(slot);
            if (existing != null && !existing.getType().isAir() && existing.getAmount() > 0) {
                ItemStack pulled = existing.clone();
                pulled.setAmount(1);
                if (existing.getAmount() <= 1) sourceInv.setItem(slot, null);
                else existing.setAmount(existing.getAmount() - 1);
                return TransferResult.success(pulled, slot);
            }
        }
        return TransferResult.empty();
    }

    @Override
    public boolean canHandle(@NotNull Block block) { return registry.isWorkstation(block); }

    @Override
    public int[] getInputSlots(@NotNull Block block) {
        WorkstationConfig config = registry.getConfig(block);
        return config != null ? config.getInputSlots() : new int[0];
    }

    @Override
    public int[] getOutputSlots(@NotNull Block block) {
        WorkstationConfig config = registry.getConfig(block);
        return config != null ? config.getOutputSlots() : new int[0];
    }

    @Override
    public boolean canInsert(@NotNull Block block, int slot, @NotNull ItemStack item) {
        WorkstationConfig config = registry.getConfig(block);
        if (config == null) return false;
        WorkstationConfig.SlotConfig slotConfig = config.getSlotConfig(slot);
        if (slotConfig == null || !slotConfig.acceptsHopperInput()) return false;
        if (slotConfig.getFunction() == WorkstationConfig.SlotFunction.FUEL) return item.getType().isFuel();
        return slotConfig.canAcceptMaterial(item.getType());
    }

    @Override
    public boolean canExtract(@NotNull Block block, int slot) {
        WorkstationConfig config = registry.getConfig(block);
        if (config == null) return false;
        WorkstationConfig.SlotConfig slotConfig = config.getSlotConfig(slot);
        return slotConfig != null && slotConfig.allowsHopperOutput();
    }

    private boolean canStack(@NotNull ItemStack existing, @NotNull ItemStack toAdd) {
        return existing.getType() == toAdd.getType() && existing.getAmount() < existing.getMaxStackSize() && existing.isSimilar(toAdd);
    }
}
